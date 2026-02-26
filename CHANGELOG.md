# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Channel number input support - Press number keys (0-9) followed by Enter to quickly switch to a channel by its position
- Loading indicator with status messages when loading playlist or EPG
- Improved error dialogs with user-friendly messages and optional technical details
- Better error categorization for network timeouts, format errors, and access issues
- ProGuard/R8 optimization for release builds to reduce APK size

### Changed
- Enabled R8 code shrinking and resource shrinking for release builds
- Enhanced error messages with specific guidance for common issues
- Updated README with improved structure and visual elements
- Added Chinese version of README (README.zh.md)

### Fixed
- Menu button representation in documentation using ☰ character

## [1.2.1] - 2026-02-26

### Added
- Text size settings for channel list and EPG display (Normal, Large, Extra Large)
- Automatic whitespace trimming with .editorconfig
- Consistent line endings with .gitattributes
- Pre-commit hook to fix trailing whitespaces

### Changed
- Use c_no as channel id for HNDX EPG parsing

## [1.2.0] - Previous Release

### Features
- M3U playlist support with EPG integration
- Support for MP2/MP3 audio streams via FFmpeg
- RTP/UDP multicast streaming support
- SMIL file parsing for dynamic stream URLs
- Android TV remote control optimization
- Settings menu for configuration
- EPG (Electronic Program Guide) support with XMLTV format

### Technical
- Built with Android Media3 ExoPlayer
- FFmpeg extension for audio codec support
- OkHttp for network requests with caching
- Jetpack Compose UI components
- Kotlin coroutines for async operations

---

## Version History

- **1.2.1** - Text size customization and EPG improvements
- **1.2.0** - Initial public release with core IPTV functionality
