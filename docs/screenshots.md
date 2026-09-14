# Android release screenshots

Run from the repository root after `mise run bootstrap`, with Git LFS installed
and an Android API 33+ AVD available:

```sh
mise run build:screenshots
```

The default AVD is `NederlandsTime_API37`. The command builds the debug app and
instrumentation APK, starts that AVD headlessly in read-only mode on port 5580,
captures every language, and stops the emulator. Override the AVD name with
`--avd NAME`. Find installed AVDs with `mise exec -- emulator -list-avds`.
The selected emulator's Tijd app data is reset; never use this on an emulator
containing app data you want to keep. Physical devices are rejected.

For an explicitly selected, already running emulator:

```sh
mise run build:screenshots --serial emulator-5580
mise run build:screenshots --serial emulator-5580 --locales en,nl,ar
```

A reused emulator retains the capture APKs and display configuration. The command
exits status-bar demo mode and resets the app locale after capturing.
Use `--skip-build` only when both APKs are current. Logs are in
`build/screenshots/`; failures return a nonzero exit code. Successful language
captures are retained if a later language fails; `--locales` can retry a subset.

## Outputs

- `android/screenshots/<language>/01-main.png`: main screen with one compact widget.
- `android/screenshots/<language>/02-edit-widget.png`: that widget's editor at the top.
- `android/fastlane/metadata/android/<listing>/images/phoneScreenshots/`: matching
  copies in upload order for all supported Play listing locales.

Coverage comes from `localization/locales.json` and `locales_config.xml`: all 91
app locales, plus three additional time-language variants sharing an interface
(`arz`, `apc`, and `pt`), for 94 pairs. Play listings use the canonical variant
(e.g. Arabic for `ar`); both English listings receive the English pair.
Languages that Play does not support still have their own captures.

The capture uses the real activities and a genuinely bound widget through the
existing instrumentation test APIs. Only the test APK supplies the fixture. It
uses the default style, a fixed 10:10 phrase through the existing preview API,
light mode, 1080 × 2400 pixels, 420 dpi, normal font scale, and the granted minute
update permission. The system status clock also shows 10:10. Each screen checks
its localized heading, active locale, rendered time phrase, and PNG dimensions.
The editor is naturally scrollable; a screenshot shows the initial viewport.

All screenshot PNG paths are covered by `.gitattributes` Git LFS rules. Normal
`git add` stores LFS pointers in the index and image content in LFS. Contributors
need Git LFS installed and should run `git lfs pull` after cloning.

```sh
mise run build:screenshots --check
```

This checks complete language coverage, image dimensions, matching Play copies,
and LFS attributes without an emulator. This workflow writes local files only;
the existing fastlane upload lanes still skip screenshots.
