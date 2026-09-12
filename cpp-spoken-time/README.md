# cpp-spoken-time

A portable C++20 library for formatting clock times as spoken language. This initial scaffold
defines only the build, installation, and package-consumption boundaries; the public API and
language implementations will be designed separately.

The library is exposed to CMake consumers as `spoken_time::spoken_time`. It is position-independent
and uses hidden symbol visibility so it can later be linked into Android JNI and Qt/QML adapters
without exporting implementation details accidentally. CMake generates the cross-platform symbol
export header; no public formatting API has been committed prematurely.

## Build

From the repository root:

```sh
mise run build:cpp
mise run check:cpp
```

Release builds use `cmake --preset release` followed by `cmake --build --preset release` from
this directory.
