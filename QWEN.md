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

## Project Structure and Layout Conventions

The project follows standard Android project structure with specific conventions for organizing code:

```
app/
├── src/
│   ├── main/                    # Main application source code
│   │   ├── java/com/github/cgang/myiptv/  # Application source code
│   │   │   ├── rtp/             # RTP multicast implementation
│   │   │   ├── xmltv/           # XMLTV parsing for EPG data
│   │   │   └── ...              # Other application components
│   │   └── res/                 # UI resources (layouts, strings, drawables, etc.)
│   ├── test/                    # Unit tests
│   │   └── java/com/github/cgang/myiptv/  # Test source code
│   │       └── rtp/             # RTP multicast tests
│   └── libs/                    # External libraries (FFmpeg extension AAR files)
└── build.gradle.kts             # Module-level build configuration
```

### Directory Conventions

When contributing to this project, please follow these conventions:

1. **`app/src/main/java/com/github/cgang/myiptv/`**: Main application source code
   - Organize related functionality into sub-packages (e.g., `rtp/`, `xmltv/`)
   - Place new features in appropriate sub-packages

2. **`app/src/test/java/com/github/cgang/myiptv/`**: Unit tests
   - All test files should be placed here, not in the main source tree
   - Mirror the package structure of main source code

3. **`app/src/main/res/`**: UI resources
   - Layout files, drawables, values (strings, colors, themes), etc.
   - Organized by resource type in standard Android directories

4. **`app/src/main/res/xml/`**: Configuration files
   - Preferences configuration (`preferences.xml`)
   - Other XML configuration files

5. **`app/libs/`**: External library files
   - FFmpeg extension AAR files and other third-party libraries

## Core Components

### MainActivity
The primary activity that manages:
- Playlist and EPG downloading
- UI fragment management (playback, playlist, program info)
- Remote control input handling
- Settings management
- System UI visibility control

### PlaybackFragment
Handles media playback using ExoPlayer:
- Initializes ExoPlayer with FFmpeg extension support
- Manages buffer duration settings
- Handles playback errors
- Persists last played channel
- Supports RTP/UDP multicast streams

### PlaylistViewModel
Manages application state:
- Downloads and parses M3U playlists
- Downloads and parses XMLTV EPG data
- Handles channel grouping and switching
- Provides LiveData for UI updates

### M3UParser
Parses M3U playlist files:
- Extracts channel information (name, group, URL, etc.)
- Parses TVG (TV Guide) URL from playlist headers
- Maps channel attributes (tvg-id, tvg-logo, etc.)

### XMLTV Package
Handles EPG data:
- Parses XMLTV format for program information
- Maps programs to channels using tvg-id or channel name
- Provides program scheduling information

### RTP Package
Handles RTP/UDP multicast streaming:
- `RtpPacket.kt`: RTP packet parsing and header stripping
- `RtpTransport.kt`: UDP multicast socket management
- `RtpDataSource.kt`: DataSource for ExoPlayer integration
- `RtpDataSourceFactory.kt`: Factory for creating RTP data sources
- `RtpMediaSource.kt`: MediaSource for ExoPlayer integration
- `NetworkInterfaceUtils.kt`: Network interface detection

## Configuration Options

Users can configure the application through the settings menu:
1. **Playlist URL**: M3U playlist URL for IPTV channels (defaults to `http://openwrt.lan/iptv.m3u`)
2. **Prefer TVG URL**: Whether to use the EPG URL specified in the playlist
3. **EPG URL**: Alternative XMLTV EPG URL
4. **Enable "All Channels" group**: Shows all channels in a single group
5. **Buffer duration**: Adjusts buffering time in milliseconds (default: 500ms)
6. **Multicast Interface**: Select network interface for RTP/UDP multicast (default: auto-detect)

## Remote Control Navigation

Designed for Android TV remote controllers:
- **OK/D-Pad Center**: Show/hide channel list
- **Left/Right**: Switch between channel groups
- **Up/Down**: Select previous/next channel
- **Menu**: Open settings
- **Back**: Hide channel list when visible

## Dependencies

Key libraries used in the project:
- `androidx.media3:media3-exoplayer`: Core playback functionality
- `androidx.media3:media3-ui`: Player UI components
- `com.squareup.okhttp3:okhttp`: Network operations
- `androidx.lifecycle:lifecycle-runtime-ktx`: Lifecycle-aware components
- `androidx.preference:preference-ktx`: Settings management
- FFmpeg extension AAR files (custom compiled for MP2/MP3 support)

## Build Information

- **Compile SDK**: 36
- **Min SDK**: 23
- **Target SDK**: 36
- **Version**: 1.1.0 (code 10)

## Important Notes for Development

1. **FFmpeg Extension**: Required for MP2/MP3 audio codec support. Pre-compiled AAR files must be placed in `app/libs/` directory.
2. **Network Permissions**: Requires INTERNET permission for streaming.
3. **Cleartext Traffic**: Enabled to support HTTP streams (common in IPTV setups).
4. **TV Optimization**: Supports both touch and Leanback interfaces, though Leanback launcher is commented out.
5. **Data Persistence**: Remembers the last played channel using SharedPreferences.
6. **Test Files**: Unit tests should be placed in `app/src/test/` directory, not in the main source tree.

## Known Limitations

As documented in the README:
- No channel selection by number
- No support for SMIL playlists (by ExoPlayer)
- RTP multicast support is experimental

## Entry Points

- **Main Activity**: `MainActivity.kt` - Launch point for the application
- **Settings Activity**: `SettingsActivity.kt` - Configuration interface
- **Default Playlist URL**: `http://openwrt.lan/iptv.m3u` (can be changed in settings)
