# cpp-spoken-time

Portable C++20 spoken-time formatting with 91 language, script, and regional entries.
The library uses the C++ standard library and has no other runtime dependencies. It can be built as a static library or
as a shared library with exported public functions.

## License

[MIT License](LICENSE). Third-party components retain their own licenses.

The library was developed with reference to Anastasiia I. Puchkova's
[“Clock time expression in languages of Europe” (2025)](https://journals.rcsi.science/2306-5737/article/view/416861),
which is licensed under CC BY-NC-SA 4.0. See [NOTICE](NOTICE) for the full acknowledgment
and description of how the article informed the implementation.
The license and notice are included in source packages and installed under
`share/doc/spoken_time` in binary distributions.

## API

~~~cpp
#include <spoken_time/spoken_time.hpp>

auto dutch = spoken_time::format_dutch(14, 25); // vijf voor half drie
auto german = spoken_time::format(spoken_time::Language::German, 14, 35);
// fünf nach halb drei

for (const auto& language : spoken_time::supported_languages()) {
    // language.language, language.code, language.name
}
~~~

Each language has an exported format_<language>(hour_of_day, minute) function.
The dispatcher uses the same functions. Inputs are a local wall-clock hour in
0..23 and a minute in 0..59; invalid inputs and unknown enum values throw
std::invalid_argument.

Output is an exact-minute spoken-time phrase in UTF-8. There is no rounding,
sentence prefix ("it is"), optional day-part label, or terminal punctuation.
Most languages use a colloquial 12-hour clock: midnight and noon both use
twelve, and the next hour wraps from twelve to one. Language-required articles and clock/hour
nouns are retained. Output is lowercase in scripts that have letter case.

Amharic, Oromo, and Swahili convert the civil hour to the local six-hour-offset
clock: 07:00 becomes one o'clock. Somali uses the conventional Western 12-hour
clock. Thai uses its everyday system of hour names. Pass the device's local
civil hour directly, without adjusting it for these conventions.

Mandarin has Simplified and Traditional entries; Punjabi has Gurmukhi and
Shahmukhi entries. Kurmanji uses Latin script and Sorani uses Arabic script.
Arabic includes separate Standard, Egyptian, and Levantine entries. Javanese
uses Ngoko vocabulary. The enum, registry, and public functions are alphabetized;
enum ordinals are not persistent language identifiers.

The library does not read the system clock, select time zones, resolve locale
tags, or perform text-to-speech. No Android, JNI, Qt, or application integration
is included.

## Build and format-check

From the repository root:

~~~sh
mise run build:cpp
mise run check:cpp
~~~

Or from this directory with the pinned tools on PATH:

~~~sh
cmake --preset dev
cmake --build --preset dev
~~~

Release builds use the release configure/build presets. Set BUILD_SHARED_LIBS=ON
at configuration time to build a shared library.

## CMake package

CMake install exports the spoken_time::spoken_time target:

~~~cmake
find_package(spoken_time CONFIG REQUIRED)
target_link_libraries(your_target PRIVATE spoken_time::spoken_time)
~~~
