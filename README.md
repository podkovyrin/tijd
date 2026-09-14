# Tijd: Word Clock Widget

Tijd is an Android widget for showing the time in spoken words. It uses
the shared C++20 library through JNI, with 91 language, script, and regional choices.

- Each widget saves its own language, initially selected from the current device locale. Unsupported locales fall back to English. Saved defaults can override the initial selection for future widgets.
- Independent styles for every widget: 40 curated colors, available system fonts, text size and alignment, and optional translucent panels.
- Transparent background and wallpaper-aware text color by default. Rounded panels require Android 12 or newer.
- Compact and single-row layouts, with automatic sizing and whole-word wrapping.
- Material 3 screens with Android toolbars, outlined controls, and a swipeable layout picker.
- Live style previews and a **My widgets** list for editing installed widgets. Save a style as the default for future widgets without changing existing ones.
- Tap the widget to open your clock app.
- Works offline, with no ads, accounts, or data collection.

Open the app, allow **Alarms & reminders**, then add a widget to your home screen. Requires Android 6.0 or newer. Updates may pause while your phone sleeps to save battery.

Edit a widget from **My widgets**, or use your launcher’s widget settings where available. Tap a preview card to edit it. Changes save automatically, and the searchable language picker includes native names and flag hints. Lock Screen widgets can have their own styles on devices that provide a Lock Screen widget picker.

## Implementation

- `cpp-spoken-time` is the platform-neutral C++20 library with 91 language, script, and regional entries.
- `android` contains the Android widget and its CMake/JNI bridge to `cpp-spoken-time`.
- `WidgetStyle`, `StyleCatalog`, and `WidgetStyleStore` define validated settings, the curated palette, and atomic per-widget storage. Defaults are copied when a widget is created.
- `ClockFont`, `StableClockText`, and `ClockViews` render native text and fit every possible time phrase. Font-specific XML layouts keep host rendering and measurement identical without rasterizing text.
- `WidgetConfigurationActivity` saves each edit immediately; `WidgetPreview` uses the same renderer as the host. `MainActivity` lists installed widgets.
- `ClockUpdates` serializes publications and includes the style in its layout identity, so editing triggers a complete update and later minute ticks preserve the style.

## Development

The Google Play title is **Tijd: Word Clock Widget**; the installed app name is **Tijd**.
See the [first-release checklist](docs/release.md), [store listing draft](docs/store-listing.md),
and [icon generation prompt](docs/icon-prompt.md).

Source: https://github.com/podkovyrin/tijd

The [privacy policy](https://podkovyrin.github.io/tijd/privacy-policy.html) is also
bundled in the app. See the [GitHub Pages setup guide](docs/github-pages.md) for
publication and updates.

[Mise](https://mise.jdx.dev/) pins the JDK, CMake, Ninja, and C++ formatting tools used by the
repository. After installing Mise, run:

```sh
mise install
mise run bootstrap
mise run check
```

Android native builds use NDK `28.1.13356709` and SDK CMake `4.1.2`, installed automatically by Gradle when SDK licenses are accepted.

Use `mise run build` to build both host C++ and Android debug artifacts, or run the scoped
`build:cpp`, `build:android`, `check:cpp`, and `check:android` tasks. Run
`./android/gradlew -p android connectedDebugAndroidTest` with an emulator for native rendering,
configuration, and widget lifecycle checks.

## License and acknowledgments

The original project code and documentation, including the C++ library and Android app,
are licensed under the [MIT License](LICENSE). Third-party components retain their own
licenses.

Anastasiia I. Puchkova's [“Clock time expression in languages of Europe” (2025)](https://journals.rcsi.science/2306-5737/article/view/416861)
was consulted while developing the C++ formatters. See [NOTICE](NOTICE) for the full
acknowledgment, source license, and description of the project's use of the article.
Android includes these notices under **License and acknowledgments**; C++ installations
include them under `share/doc/spoken_time`.

## Localization and store publishing

See [localization and publishing](docs/localization.md) for all supported app
locales, generated Google Play metadata, validation and fastlane upload lanes.
The [English ASO rationale](docs/store-aso.md) records the keyword choices and
shorter description before translation.
