# Spoken Time

A cross-platform project for showing the time in spoken words. The current Android widget
renders Dutch phrases such as **vijf voor half negen**; a shared C++ library is being prepared
for additional platforms and languages.

- Independent styles for every widget: 40 curated colors, available system fonts, text size and alignment, and optional translucent panels.
- Transparent background and wallpaper-aware text color by default. Rounded panels require Android 12 or newer.
- Compact and single-row layouts, with automatic sizing and whole-word wrapping.
- Live style previews and a **My widgets** list for editing installed widgets. Save a style as the default for future widgets without changing existing ones.
- Tap the widget to open your clock app.
- Works offline, with no ads, accounts, or data collection.

Open the app, allow **Alarms & reminders**, then add a widget to your home screen. Requires Android 6.0 or newer. Updates may pause while your phone sleeps to save battery.

Edit a widget from **My widgets**, or use your launcher’s widget settings where available. Changes take effect on **Save style**; Cancel leaves the widget unchanged. Lock Screen widgets can have their own styles on devices that provide a Lock Screen widget picker.

## Implementation

- `cpp-spoken-time` is the platform-neutral C++20 library. Its build and package boundaries are
  bootstrapped, but its formatting API is intentionally not defined yet.
- `android` contains the existing Android widget.
- `WidgetStyle`, `StyleCatalog`, and `WidgetStyleStore` define validated settings, the curated palette, and atomic per-widget storage. Defaults are copied when a widget is created.
- `ClockFont`, `StableClockText`, and `ClockViews` render native text and fit every possible time phrase. Font-specific XML layouts keep host rendering and measurement identical without rasterizing text.
- `WidgetConfigurationActivity` owns the unsaved draft; `WidgetPreview` uses the same renderer as the host. `MainActivity` lists installed widgets.
- `ClockUpdates` serializes publications and includes the style in its layout identity, so editing triggers a complete update and later minute ticks preserve the style.

## Development

[Mise](https://mise.jdx.dev/) pins the JDK, CMake, Ninja, and C++ formatting tools used by the
repository. After installing Mise, run:

```sh
mise install
mise run bootstrap
mise run check
```

Use `mise run build` to build both host C++ and Android debug artifacts, or run the scoped
`build:cpp`, `build:android`, `check:cpp`, and `check:android` tasks. Run
`./android/gradlew -p android connectedDebugAndroidTest` with an emulator for native rendering,
configuration, and widget lifecycle checks.

[MIT license](LICENSE).
