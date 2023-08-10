# Changelog for com.github.tno.gltsdiff

All notable changes to this project will be documented in this file.

## [1.1.0] - 2023-08-10
* `Array2DRowRealMatrix` is now used to represent scoring matrices, rather than `OpenMapRealMatrix` and `BlockRealMatrix`.
  This prevents `NumberIsTooLargeException` for larger models, and for most algorithms also improves performance.
* DOT files are now written using UTF-8 encoding.
* In DOT files, the hidden initial nodes now have size zero, to prevent unwanted margins in rendered SVG images.
* Extended and improved the documentation.
* The Maven group ID has been changed from `com.github.tno.gltsdiff` to `io.github.tno`.
* This is the first version of gLTSdiff that is deployed to Maven Central.

## [1.0.1] - 2023-04-08
* Fixed a bug where builders did not use the configured scorer if a dynamic matcher is used. (#115)

## [1.0.0] - 2023-04-06
* First release of com.github.tno.gltsdiff.
