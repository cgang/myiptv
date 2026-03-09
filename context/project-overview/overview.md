# Project Overview: MyIPTV

MyIPTV is a simple IPTV player for Android devices that utilizes Android Media3 ExoPlayer and FFmpeg library for media playback. The application allows users to stream IPTV channels using M3U playlists and supports EPG (Electronic Program Guide) functionality through XMLTV format.

## Key Features

- IPTV streaming using M3U playlists
- EPG support through XMLTV format
- Remote control navigation optimized for Android TV
- Channel grouping and browsing
- Configurable buffer duration for smoother playback
- Support for channels with MP2/MP3 audio codecs through FFmpeg extension
- RTP over UDP multicast support for direct streaming

## Technology Stack

- **Language**: Kotlin
- **Framework**: Android SDK with Jetpack libraries
- **Media Engine**: Android Media3 ExoPlayer with FFmpeg extension
- **UI Framework**: Android Views (with some Compose elements)
- **Networking**: OkHttp
- **Architecture**: MVVM with LiveData
- **Build System**: Gradle with Kotlin DSL

## Build Information

- **Compile SDK**: 36
- **Min SDK**: 23
- **Target SDK**: 36
- **Version**: 1.1.0 (code 10)

## Prerequisites

- **Required**: RTP/UDP multicast streams provider
- **Optional**: HTTP streaming server (e.g., [udpxy](https://github.com/pch/udpxy)) for HTTP-based IPTV
- **Optional**: EPG provider for program guide data

## Known Limitations

- ❌ No channel selection by number input
- ⚠️ Multicast support is experimental
- ❌ No support for SMIL playlists (by ExoPlayer)
