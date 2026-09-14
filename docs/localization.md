# Localization and Google Play publishing

The native C++ catalog remains the source of truth for the 91 written-time options.
`localization/locales.json` maps every option to an Android UI locale and, where
supported, a Google Play listing locale. App language follows Android's language
preference; Android 13+ also exposes the supported app languages in system settings.
The language selected inside a widget controls that widget's written time only.

## Source and generated files

- `localization/en.json`: English copy matching the default Android resources;
  the [ASO rationale](store-aso.md) explains the store positioning.
- `localization/<locale>.json`: editable translations, with translation provenance.
- `android/app/src/main/res/values-*/strings.xml`: generated Android resources.
- `android/fastlane/metadata/android/<Play locale>/`: generated title, short/full
  descriptions and initial-release notes, in supply's upload format.
- `android/app/src/main/res/xml/locales_config.xml`: generated app-language list.

All non-English UI and store catalogs were rewritten directly by AI agents on
14 September 2026 using English and corrected Russian context, without external
translation APIs or services. The previous translations were replaced. Each
catalog records its authorship and contextual reread. This is not independent
native-speaker review or market-specific keyword research. Builds use checked-in
translations and never call a translation service.

Coverage: 65 localized UI strings and 51 English color/font labels across 91 UI
locale configurations, mapped
to all 91 time options; 67 Google Play listings. A base Portuguese resource also
covers unlisted Portuguese regions on older Android versions.

The 91 time options are not 91 distinct Play locales. Arabic dialects share the
standard Arabic UI/listing; generic Portuguese shares European Portuguese. The
shared Serbo-Croatian time option has Croatian, Bosnian and Serbian UI resources.
Script-specific Punjabi and Chinese use BCP 47 Android qualifiers. Unsupported
Play locales are omitted from the upload tree, with an explicit explanation in
the mapping. Cantonese is not mislabeled as the regional Chinese `zh-HK` locale.
Google's [accepted listing languages](https://support.google.com/googleplay/android-developer/answer/9844778)
are separate from Android's resource locale support.

Language names use Android's locale data and retain native names in the picker;
English catalog names remain searchable. All 40 color names and 11 font names
stay in English and are marked `translatable="false"`; stored style IDs stay stable. Legal source documents (privacy policy and upstream licenses/notices)
remain in English; their navigation labels are localized. The privacy policy
still needs the owner's contact details before publication.

To edit translations, change the JSON files, then run from the repository root:

```sh
python3 scripts/localization.py
python3 scripts/localization.py --check
mise exec -- ./android/gradlew -p android testDebugUnitTest lintRelease bundleRelease
```

`review.json` records source and replacement content hashes. Publishing lanes
check these hashes so subsequent text changes require an updated contextual review.
The checker rejects missing keys, malformed format placeholders, changed branding,
unsupported generated files and Play field overflows. CI also checks generated
files for drift. Add or change native time options by updating the mapping and
translation catalogs together. Review long labels with large text and small
screens; check RTL controls separately from the widget's own text direction.

## Why fastlane supply

Use Gradle to build and [fastlane supply](https://docs.fastlane.tools/actions/upload_to_play_store/)
to publish. Fastlane is pinned in `android/Gemfile` and its dependency lockfile.
This keeps store publishing independent of AGP 9.4. The alternative
[Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher)
currently declares maintenance mode. A custom Play API client would duplicate
metadata, authentication and edit-transaction handling already provided by supply.
This is a choice for this repository, not a claim of an official Android mandate.

From `android/` with Ruby 3.3+ and Bundler:

```sh
bundle install
bundle exec fastlane android check
bundle exec fastlane android validate_metadata
bundle exec fastlane android upload_metadata
bundle exec fastlane android upload_internal aab:/absolute/path/to/signed.aab
```

`check` is fully local. The other lanes require `GOOGLE_PLAY_JSON_KEY` pointing to
an absolute credentials JSON path outside the repository and app-specific Play
Console permissions. `validate_metadata` validates an API edit without committing
it. `upload_metadata` uploads text only and leaves changes unsent for review;
it does not replace images or screenshots. `upload_internal` verifies an existing
AAB signature, uploads it to the internal track as a draft and includes localized
release notes; it does not roll out to production. API errors are not retried with
settings that could automatically submit changes for review.

The manual **Google Play metadata** GitHub workflow defaults to validation.
Create a `google-play` GitHub environment and store the credential JSON in its
`GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` secret. Restrict deployment branches and use
reviewers if desired. Temporary credentials are deleted in an always-run cleanup
step. Pull-request validation does not receive Play credentials.

## Signing and first upload

Create the app in Play Console and complete the initial manual AAB upload before
using API publishing. Enable the Android Publisher API and grant the service
account only the required permissions on this app (store presence and testing
releases; no production-release permission is needed for these lanes).

Release signing uses an existing upload key via environment variables:
`ANDROID_UPLOAD_KEYSTORE` (absolute path), `ANDROID_UPLOAD_STORE_PASSWORD`,
`ANDROID_UPLOAD_KEY_ALIAS`, and `ANDROID_UPLOAD_KEY_PASSWORD`. Supply these from a
local secret manager or CI secrets; do not put passwords in shell history. Build:

```sh
mise exec -- ./android/gradlew -p android bundleRelease
```

Without signing variables the normal local release build remains unsigned; the
upload lane rejects it. Increase `versionCode` before reusing the pipeline after
any successful Play upload. A first app release still needs Console setup,
account access, support email, public privacy URL, Data safety and other app
content declarations, a feature graphic and real screenshots. The existing Play
icon is `assets/branding/play-store-icon.png`. See the [release checklist](release.md).
The text upload package is not a claim that these separate account and visual
asset requirements have been completed. No store changes have been submitted.

## Verification recorded for this change

- Local generator checks and `fastlane android check`: pass for 91 UI locale
  configurations (65 localized strings and 51 shared English labels) and 67 Play listings.
- `testDebugUnitTest lintRelease bundleRelease`: pass; lint has no errors and
  27 warnings. Three warnings concern legacy `in`/`iw`/`ji` resource names versus
  canonical `id`/`he`/`yi` in the locale configuration. Runtime tests confirm those
  languages resolve correctly. Other warnings are listed in the lint report.
- Three `LocalizationTest` instrumentation tests pass on Pixel 10 Pro XL /
  Android 17: all configured locales, format arguments, script variants,
  Portuguese regional fallback, context-localized language names and long
  button labels at 200% text. Buttons now grow vertically for translated text.
- Workflow syntax and Ruby lane syntax pass. The full device-regression suite,
  older-device visual QA and native-speaker review have not been run.
- Live Play validation/upload has not run because Console credentials and
  initial app setup have not been supplied. No signed upload artifact was created.
