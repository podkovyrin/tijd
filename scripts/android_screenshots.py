#!/usr/bin/env python3
"""Capture real Android release screens for every language variant and Play listing."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import struct
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
import zlib

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'android/screenshots'
META = ROOT / 'android/fastlane/metadata/android'
PACKAGE = 'nl.nederlandstijd.widget'
SCREENS = ('01-main.png', '02-edit-widget.png')


def run(*args, timeout=120):
    result = subprocess.run(args, cwd=ROOT, capture_output=True, text=True, timeout=timeout)
    if result.returncode:
        raise RuntimeError(f'{" ".join(args)} failed:\n{result.stdout}{result.stderr}')
    return result.stdout.strip()


def catalog():
    rows = json.loads((ROOT / 'localization/locales.json').read_text())
    locales = {}
    for row in rows:
        locales.setdefault(row['app_locale'], row['time_code'])
    locales.update({'bs': 'sh', 'sr': 'sh', 'sr-Latn': 'sh'})
    declared = {node.attrib['{http://schemas.android.com/apk/res/android}name']
                for node in ET.parse(ROOT / 'android/app/src/main/res/xml/locales_config.xml').getroot()}
    if set(locales) != declared:
        raise RuntimeError('Screenshot catalog differs from locales_config.xml; update the locale mapping.')
    play = {row['play_locale']: row['app_locale'] for row in rows if row['play_locale']}
    play.update({'sr': 'sr', 'en-GB': 'en'})
    captures = {tag: (tag, code) for tag, code in locales.items()}
    for row in rows:
        if locales[row['app_locale']] != row['time_code']:
            captures[row['time_code']] = (row['app_locale'], row['time_code'])
    return captures, play


def png_info(path):
    data = path.read_bytes()
    if (len(data) < 33 or data[:8] != b'\x89PNG\r\n\x1a\n'
            or struct.unpack('>II', data[16:24]) != (1080, 2400)
            or data[24:26] != bytes((8, 2))):
        raise RuntimeError(f'Expected a 1080 x 2400 RGB PNG without alpha: {path}')
    offset = 8
    complete = False
    while offset + 12 <= len(data):
        length = struct.unpack('>I', data[offset:offset + 4])[0]
        end = offset + 8 + length
        if end + 4 > len(data) or zlib.crc32(data[offset + 4:end]) != struct.unpack('>I', data[end:end + 4])[0]:
            raise RuntimeError(f'Corrupt PNG chunk: {path}')
        if data[offset + 4:offset + 8] == b'IEND':
            complete = end + 4 == len(data)
            break
        offset = end + 4
    if not complete:
        raise RuntimeError(f'Incomplete PNG: {path}')
    return hashlib.sha256(data).hexdigest()


def verify(locales, play):
    paths = []
    for locale in locales:
        for screen in SCREENS:
            path = OUTPUT / locale / screen
            png_info(path)
            paths.append(path)
    for listing, locale in play.items():
        if locale not in locales:
            continue
        for screen in SCREENS:
            path = META / listing / 'images/phoneScreenshots' / screen
            if png_info(path) != png_info(OUTPUT / locale / screen):
                raise RuntimeError(f'Play screenshot is stale: {path}')
            paths.append(path)
    for path in paths:
        if run('git', 'check-attr', 'filter', '--', str(path)).split(': ')[-1] != 'lfs':
            raise RuntimeError(f'Screenshot is not covered by Git LFS: {path}')
    return len(paths)


def main():
    parser = argparse.ArgumentParser(description=__doc__, epilog=(
        'Examples: mise exec -- python3 scripts/android_screenshots.py; '
        'mise exec -- python3 scripts/android_screenshots.py --locales en,nl,ar --serial emulator-5580. '
        'Uses an emulator only, resets its Tijd app data, and writes screenshots locally. Never uploads.'))
    parser.add_argument('--locales', help='Comma-separated language variants (default: every declared locale)')
    parser.add_argument('--serial', help='Reuse this running emulator; physical devices are rejected')
    parser.add_argument('--avd', default='NederlandsTime_API37', help='AVD to start (default: %(default)s)')
    parser.add_argument('--skip-build', action='store_true', help='Reuse already built debug and test APKs')
    parser.add_argument('--check', action='store_true', help='Check existing PNGs, Play copies, and LFS rules without a device')
    args = parser.parse_args()
    if args.serial and not args.serial.startswith('emulator-'):
        parser.error('--serial must name an emulator; this command resets the capture app and display settings')
    locales, play = catalog()
    if args.locales:
        selected = args.locales.split(',')
        unknown = set(selected) - locales.keys()
        if unknown:
            parser.error('Unknown language variants: ' + ', '.join(sorted(unknown)))
        locales = {tag: locales[tag] for tag in selected}
    if args.check:
        print(f'Checked {verify(locales, play)} screenshots for {len(locales)} language variants.')
        return
    run('git', 'lfs', 'version')
    run('git', 'lfs', 'install', '--local')
    logs = ROOT / 'build/screenshots'
    logs.mkdir(parents=True, exist_ok=True)
    if not args.skip_build:
        print('Building app and screenshot instrumentation…', file=sys.stderr, flush=True)
        with (logs / 'build.log').open('w') as log:
            build = subprocess.run([str(ROOT / 'android/gradlew'), '-p', str(ROOT / 'android'),
                                    'assembleDebug', 'assembleDebugAndroidTest'], cwd=ROOT,
                                   stdout=log, stderr=subprocess.STDOUT)
        if build.returncode:
            raise RuntimeError(f'Build failed. See {logs / "build.log"}')
    serial = args.serial or 'emulator-5580'
    def adb(*command, timeout=120):
        return run('adb', '-s', serial, *command, timeout=timeout)
    process = None
    configured = False
    try:
        if not args.serial:
            devices = run('adb', 'devices')
            if serial in devices:
                raise RuntimeError(f'{serial} is already running. Pass --serial {serial} to reuse it explicitly.')
            with (logs / 'emulator.log').open('w') as log:
                process = subprocess.Popen(['emulator', '-avd', args.avd, '-port', '5580', '-no-window',
                                            '-no-audio', '-no-snapshot', '-no-boot-anim', '-read-only'],
                                           stdout=log, stderr=subprocess.STDOUT)
        deadline = time.monotonic() + 180
        while time.monotonic() < deadline:
            if process and process.poll() is not None:
                raise RuntimeError(f'Emulator exited. See {logs / "emulator.log"}')
            try:
                if adb('shell', 'getprop', 'sys.boot_completed', timeout=10) == '1':
                    break
            except (RuntimeError, subprocess.TimeoutExpired):
                pass
            time.sleep(1)
        else:
            raise RuntimeError('Emulator did not finish booting within 180 seconds.')
        if adb('shell', 'getprop', 'ro.kernel.qemu') != '1':
            raise RuntimeError('The selected device is not an emulator.')
        if int(adb('shell', 'getprop', 'ro.build.version.sdk')) < 33:
            raise RuntimeError('Use an Android 13 / API 33 or newer emulator for app locales.')
        configured = True
        adb('shell', 'wm', 'size', '1080x2400')
        adb('shell', 'wm', 'density', '420')
        adb('shell', 'settings', 'put', 'system', 'font_scale', '1.0')
        adb('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
        adb('shell', 'settings', 'put', 'system', 'user_rotation', '0')
        for key in ('window_animation_scale', 'transition_animation_scale', 'animator_duration_scale'):
            adb('shell', 'settings', 'put', 'global', key, '0')
        adb('shell', 'cmd', 'uimode', 'night', 'no')
        adb('shell', 'input', 'keyevent', 'KEYCODE_WAKEUP')
        adb('shell', 'wm', 'dismiss-keyguard')
        adb('install', '-r', str(ROOT / 'android/app/build/outputs/apk/debug/app-debug.apk'))
        adb('install', '-r', '-t', str(ROOT / 'android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'))
        adb('shell', 'pm', 'clear', PACKAGE)
        adb('shell', 'appops', 'set', PACKAGE, 'SCHEDULE_EXACT_ALARM', 'allow')
        adb('shell', 'settings', 'put', 'global', 'sysui_demo_allowed', '1')
        def demo(*extra):
            adb('shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', *extra)
        demo('--es', 'command', 'enter')
        demo('--es', 'command', 'clock', '--es', 'hhmm', '1010')
        demo('--es', 'command', 'battery', '--es', 'level', '100', '--es', 'plugged', 'false')
        demo('--es', 'command', 'notifications', '--es', 'visible', 'false')
        demo('--es', 'command', 'network', '--es', 'wifi', 'show', '--es', 'level', '4', '--es', 'mobile', 'hide')
        for index, (locale, (app_locale, time_code)) in enumerate(sorted(locales.items()), 1):
            print(f'[{index}/{len(locales)}] {locale}: main + edit widget', file=sys.stderr, flush=True)
            adb('shell', 'am', 'force-stop', PACKAGE)
            adb('shell', 'cmd', 'locale', 'set-app-locales', PACKAGE, '--locales', app_locale)
            remote = f'/sdcard/Android/data/{PACKAGE}/files/screenshots/{locale}'
            adb('shell', 'rm', '-rf', remote)
            result = adb('shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
                         f'{PACKAGE}.StoreScreenshotTest', '-e', 'screenshots', 'true',
                         '-e', 'locale', app_locale, '-e', 'captureKey', locale, '-e', 'timeCode', time_code,
                         f'{PACKAGE}.test/androidx.test.runner.AndroidJUnitRunner', timeout=180)
            (logs / f'{locale}.log').write_text(result + '\n')
            if 'OK (1 test)' not in result:
                raise RuntimeError(f'{locale}: capture failed. See {logs / (locale + ".log")}\n{result}')
            destination = OUTPUT / locale
            destination.mkdir(parents=True, exist_ok=True)
            for screen in SCREENS:
                temporary = destination / (screen + '.tmp')
                adb('pull', remote + '/' + screen, str(temporary))
                png_info(temporary)
                temporary.replace(destination / screen)
            for listing, source in play.items():
                if source == locale:
                    listing_dir = META / listing / 'images/phoneScreenshots'
                    listing_dir.mkdir(parents=True, exist_ok=True)
                    for screen in SCREENS:
                        shutil.copyfile(destination / screen, listing_dir / screen)
        count = verify(locales, play)
        print(f'Captured and checked {count} PNGs for {len(locales)} language variants, including Play listing copies.')
    finally:
        if process:
            process.terminate()
            try:
                process.wait(timeout=15)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait(timeout=15)
        elif args.serial and configured:
            # Explicitly reused emulators keep the installed APKs and display setup.
            try:
                adb('shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '--es', 'command', 'exit')
                adb('shell', 'cmd', 'locale', 'set-app-locales', PACKAGE)
            except (RuntimeError, subprocess.TimeoutExpired) as error:
                print(f'Could not reset emulator demo mode / locale: {error}', file=sys.stderr)


if __name__ == '__main__':
    try:
        main()
    except (RuntimeError, OSError, subprocess.TimeoutExpired) as error:
        print(f'error: {error}', file=sys.stderr)
        sys.exit(1)
    except KeyboardInterrupt:
        print('Screenshot capture interrupted; completed locale files are retained.', file=sys.stderr)
        sys.exit(130)
