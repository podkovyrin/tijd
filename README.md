# nederlandse tijd

A small, resizable Android widget that shows the time in Dutch words, such as **vijf voor half negen**.

- Independent styles for every widget: 40 curated colors, available system fonts, text size and alignment, and optional translucent panels.
- Transparent background and wallpaper-aware text color by default. Rounded panels require Android 12 or newer.
- Compact and single-row layouts, with automatic sizing and whole-word wrapping.
- Live style previews and a **My widgets** list for editing installed widgets. Save a style as the default for future widgets without changing existing ones.
- Tap the widget to open your clock app.
- Works offline, with no ads, accounts, or data collection.

Open the app, allow **Alarms & reminders**, then add a widget to your home screen. Requires Android 6.0 or newer. Updates may pause while your phone sleeps to save battery.

Edit a widget from **My widgets**, or use your launcher’s widget settings where available. Changes take effect on **Save style**; Cancel leaves the widget unchanged. Lock Screen widgets can have their own styles on devices that provide a Lock Screen widget picker.

## Implementation

- `WidgetStyle`, `StyleCatalog`, and `WidgetStyleStore` define validated settings, the curated palette, and atomic per-widget storage. Defaults are copied when a widget is created.
- `ClockFont`, `StableClockText`, and `ClockViews` render native text and fit every possible time phrase. Font-specific XML layouts keep host rendering and measurement identical without rasterizing text.
- `WidgetConfigurationActivity` owns the unsaved draft; `WidgetPreview` uses the same renderer as the host. `MainActivity` lists installed widgets.
- `ClockUpdates` serializes publications and includes the style in its layout identity, so editing triggers a complete update and later minute ticks preserve the style.

Run `./gradlew testDebugUnitTest lintDebug` for local checks and `./gradlew connectedDebugAndroidTest` with an emulator for native rendering, configuration, and widget lifecycle checks.

[MIT license](LICENSE).
