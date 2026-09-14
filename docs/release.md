# Tijd first-release checklist

Reviewed 14 September 2026 against the repository and current Google documentation.
Developer account verification is complete. The free app is created in Play
Console as `4973691103378440619` (Tijd: Word Clock Widget), package
`com.podkovyrin.tijd`. Version 1.0 (7) was published to internal testing on
14 September 2026. No testers are configured yet; production requires 12 closed
testers opted in continuously for 14 days.

Upload signing was generated outside the repository and the owner confirmed a
backup. Source repository: https://github.com/podkovyrin/tijd.
Privacy policy: https://podkovyrin.github.io/tijd/privacy-policy.html.

Console declarations saved: no restricted app access, no ads, non-government,
no financial features, no health features; category Personalization. The privacy URL is saved in Console. Data safety
is saved as no collection/sharing. Target audience is 13–15, 16–17 and 18+. English listing
text, icon, feature graphic and two phone screenshots were uploaded with Fastlane;
Console shows Ready to send for review. The 1024 × 500 graphic source is
`assets/store/feature-graphic.png`. App-scoped publishing credentials are configured
outside Git (see [localization publishing setup](localization.md)).
Console setup is 9 of 11 complete. Support email, content rating and tester access
remain incomplete. Build 7
incorporates the finalized bundled privacy policy;
version codes 6 and 7 have already been uploaded and must not be reused.

Build 7 English release notes saved in Console: “Updated the in-app privacy policy
with the published policy and contact details.”

## Identity

| Field | Value |
| --- | --- |
| Google Play title | Tijd: Word Clock Widget |
| Installed app name | Tijd |
| Single-row widget label | Tijd — Single row |
| Application ID | `com.podkovyrin.tijd` |
| First release version | `1.0` |
| Current build number | `7` |
| Minimum Android | Android 6.0 / API 23 |
| Compile / target SDK | 37 / 37 |

The package ID is the app's technical identity, separate from its visible name.
Keep the existing ID unless a different identity is deliberately chosen before
the first Play upload; renaming it later creates a different app. Build number 7
has been uploaded; increase it for subsequent uploads and do not reuse an
uploaded code. See [Google's app setup guidance](https://support.google.com/googleplay/android-developer/answer/9859152).

## Release preparation

- [x] Apply the Tijd display name, update manual widget instructions, and rename the
  Android/project notice heading. The shared C++ library remains `spoken_time`.
- [x] Prepare a [store listing draft](store-listing.md) and [icon prompt](icon-prompt.md).
- [x] **Integrate the supplied icon.** Original artwork and a 512 px Play export
  are in `assets/branding`. Android now uses legacy density icons, an adaptive
  foreground/background, and a themed monochrome layer. Check launcher rendering
  and the system splash screen on the final release candidate.
- [x] **Prepare store graphics.** Google requires a 512 × 512 PNG icon, a
  1024 × 500 feature graphic, and at least two screenshots. Aim for four real phone
  screenshots covering both widgets, styling, and language selection. Prepare
  device-specific screenshots if expanding the listing to other form factors.
  Follow the [asset specifications](https://support.google.com/googleplay/android-developer/answer/9866151).
- [x] **Draft the privacy policy and expose it in the app.** The offline entry reads
  `res/raw/privacy_policy.txt`; the GitHub Pages build renders that same text.
  It covers local data, permissions, retention/deletion, and external services.
- [x] **Publish the privacy page.** GitHub Pages serves the finalized policy with
  GitHub Issues as the privacy contact. The URL and no-collection/no-sharing Data
  safety answers are saved in Console. A public store support email is still needed.
- [x] **Set up release signing.** Upload key and passwords are stored outside Git,
  backed up by the owner, and used to produce signed bundles. Build 7 is already
  on the internal testing track.
- [ ] **Run release-candidate QA.** Use the matrix below, fix findings, and record
  device/OS/build and results. A successful build does not establish clock
  reliability, battery impact, or language accuracy.
- [ ] **Prepare tester recruitment.** If this is a personal developer account created
  after 13 November 2023, arrange at least 12 testers who will remain enrolled in
  the closed test continuously for 14 days. Keep feedback and fixes documented for
  the production-access application; elapsed enrollment alone does not guarantee
  approval. Internal testing does not satisfy this requirement. See
  [Google's testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465).
- [ ] **Complete the third-party attribution review.** Existing MIT, article, and
  Material acknowledgments are bundled. Compare the final runtime dependency list
  with the bundled notices, including Kotlin/coroutines and transitive AndroidX
  components, and add any missing required notices.

## After account access is ready

- [ ] Complete any remaining identity, contact, and device verification steps shown
  by Play Console. Confirm whether the account is personal or organizational.
- [ ] Create the app entry with the chosen title, default listing language, and
  package ID. Select category, pricing, and launch countries deliberately.
- [ ] Complete App content declarations: Data safety, privacy URL, ads (none),
  app access (no login/restrictions), content-rating questionnaire, and intended
  audience. Answer any additional Console declarations for the actual app.
  The reviewed code supports “no data collected/shared”; reassess this against the
  final artifact and any added SDKs before submitting.
- [ ] Upload the signed AAB to internal testing; confirm installation from Play,
  widget creation, native-library loading, and upgrades. Retain native symbols for
  crash diagnosis and inspect the pre-launch report when available.
- [ ] Start the closed test and, if required for the account, satisfy the enrollment
  requirement and apply for production access with real testing feedback.
- [ ] Resolve test and pre-launch-report findings, upload the final candidate with
  a fresh version code, finish the listing, and submit for production review.
  Allow time for both account/production-access and app-review decisions.

## Technical audit and evidence

- `mise exec -- ./android/gradlew -p android testDebugUnitTest lintRelease bundleRelease`
  succeeds. The unit suite contains **one scheduler test**; it passes. Release lint
  reports **0 errors and 27 warnings**. Warnings cover newer-API widget attributes,
  dependency updates, compatibility/style suggestions, and a splash-screen
  heuristic on the clock-launch forwarding activity. Review older-device rendering
  and clock launch during QA; dependency upgrades are not automatically release blockers.
- The release merged manifest contains no `INTERNET`, advertising-ID, location,
  or storage permission. App permissions are `SCHEDULE_EXACT_ALARM` and
  `RECEIVE_BOOT_COMPLETED`, plus AndroidX's signature-protected internal receiver
  permission. Backup and device transfer are excluded by the current configuration.
- Environment-based upload signing is now configured in `android/app/build.gradle.kts`.
  Builds remain unsigned without signing secrets; see [publishing setup](localization.md).
- The bundle includes `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64` native builds.
  `llvm-objdump -p` confirms all LOAD segments of the arm64 and x86_64 libraries
  have `2**14` (16 KB) alignment. AGP 9.4 and NDK r28 meet Android's recommended
  baseline. APK packaging alignment and execution on a 16 KB device still need
  verification using the final artifact; ELF alignment alone is not full runtime
  certification. See [16 KB page-size guidance](https://developer.android.com/guide/practices/page-sizes).
- The configured target SDK 37 exceeds Google's current new-phone-app minimum
  of API 36 effective 31 August 2026. Confirm Console acceptance of the final
  artifact. See [target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en).
- Three localization instrumentation tests pass on a Pixel 10 Pro XL / Android 17,
  covering all 91 configured UI locales, formatting, script variants, a Portuguese
  regional fallback, localized language names and expanding buttons at 200% text.
  The broader rendering, editing, persistence, recovery and lifecycle suites
  were **not run in this localization review**. The C++ project has no
  standalone automated language-correctness test suite. Native-speaker validation
  remains necessary before treating all 91 variants as linguistically verified.
- Both widget providers have `previewLayout` but no legacy `previewImage`.
  Add a fallback preview for Android 6–11 as a polish task; otherwise older widget
  pickers may show a generic preview. The single-row provider also reuses the
  compact preview layout, so review its appearance in actual launcher pickers.

## Release-candidate QA matrix

Record results against the release build, including at least one physical device.

| Area | Acceptance checks |
| --- | --- |
| Android coverage | API 23 minimum, Android 11, Android 12/14 permission behavior, current Android, and a 16 KB environment |
| Installation | Fresh install and same-signature upgrade; existing widgets and saved styles survive the upgrade |
| Widget lifecycle | Add both layouts from the app and launcher; cancel setup, resize, edit, delete, add multiple independent instances |
| Clock correctness | Minute/hour/day rollover, timezone/manual-time changes, sleep/wake, reboot, app update, process death |
| Permission flow | Grant, deny, revoke, and regrant Alarms & reminders; clear explanation when minute updates are unavailable |
| Appearance | Light/dark and live wallpapers, manual ink colors, transparent/translucent panels, smallest supported widget bounds |
| Accessibility | TalkBack, large font/display scaling, touch targets, contrast, RTL text, long phrases, missing-glyph checks |
| Language quality | Native-speaker checks of launch languages, midnight/noon and relative-hour wording, locale fallback and persisted language |
| Battery/performance | Overnight idle and wake recovery, multiple widgets, cold-cache rendering, no persistent background work after last widget is deleted |
| Device integration | Pixel and Samsung launchers where available; tap opens clock or gives a useful fallback; launcher branding and previews |

Instrumentation command for a dedicated test device/emulator:

```sh
mise exec -- ./android/gradlew -p android connectedDebugAndroidTest
```

This runs the existing debug instrumentation suite. Separately smoke-test the
signed release delivered by Play, since a debug test pass does not validate that
distribution artifact.

## Localized screenshots

See [the screenshot workflow](screenshots.md) to generate and verify the main and
edit-widget screens for every language with `mise run build:screenshots`. All
captured PNGs use Git LFS, with copies in the matching fastlane listing folders.
