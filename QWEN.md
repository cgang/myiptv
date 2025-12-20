# Project Overview: MyIPTV

MyIPTV is a simple IPTV player for Android devices that utilizes Android Media3 ExoPlayer and FFmpeg library for media playback. The application allows users to stream IPTV channels using M3U playlists and supports EPG (Electronic Program Guide) functionality through XMLTV format.

## Key Features

- IPTV streaming using M3U playlists
- EPG support through XMLTV format
- Remote control navigation optimized for Android TV
- Channel grouping and browsing
- Configurable buffer duration for smoother playback
- Support for channels with MP2/MP3 audio codecs through FFmpeg extension

## Technology Stack

- **Language**: Kotlin
- **Framework**: Android SDK with Jetpack libraries
- **Media Engine**: Android Media3 ExoPlayer with FFmpeg extension
- **UI Framework**: Android Views (with some Compose elements)
- **Networking**: OkHttp
- **Architecture**: MVVM with LiveData
- **Build System**: Gradle with Kotlin DSL

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/github/cgang/myiptv/
│   │   │   ├── xmltv/           # XMLTV parsing for EPG data
│   │   │   ├── Channel.kt       # Data class for channel information
│   │   │   ├── Downloader.kt    # Network operations for downloading playlists/EPG
│   │   │   ├── M3UParser.kt     # Parser for M3U playlist files
│   │   │   ├── MainActivity.kt  # Main entry point and primary activity
│   │   │   ├── PlaybackFragment.kt # Media playback using ExoPlayer
│   │   │   ├── Playlist.kt      # Data class for playlist information
│   │   │   ├── PlaylistAdapter.kt # RecyclerView adapter for playlist display
│   │   │   ├── PlaylistFragment.kt # Fragment for displaying channel list
│   │   │   ├── PlaylistViewModel.kt # ViewModel for managing playlist data
│   │   │   ├── ProgramAdapter.kt # Adapter for program information display
│   │   │   ├── ProgramInfoFragment.kt # Fragment for showing EPG program info
│   │   │   ├── SettingsActivity.kt # Activity for app settings
│   │   │   └── SettingsFragment.kt # Fragment for settings UI
│   │   └── res/                 # Resources (layouts, strings, drawables)
│   └── libs/                   # FFmpeg extension AAR files
└── build.gradle.kts            # Module-level build configuration
```

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

## Configuration Options

Users can configure the application through the settings menu:
1. **Playlist URL**: M3U playlist URL for IPTV channels (defaults to `http://openwrt.lan/iptv.m3u`)
2. **Prefer TVG URL**: Whether to use the EPG URL specified in the playlist
3. **EPG URL**: Alternative XMLTV EPG URL
4. **Enable "All Channels" group**: Shows all channels in a single group
5. **Buffer duration**: Adjusts buffering time in milliseconds (default: 500ms)

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

## Common Use Cases

1. **Streaming Setup**: Configure M3U playlist URL pointing to IPTV service
2. **EPG Integration**: Enable EPG through playlist TVG URL or separate XMLTV URL
3. **Channel Browsing**: Navigate through channel groups using remote control
4. **Performance Tuning**: Adjust buffer duration based on network conditions

## Known Limitations

As documented in the README:
- No channel selection by number
- No support for SMIL playlists (by ExoPlayer)

## Entry Points

- **Main Activity**: `MainActivity.kt` - Launch point for the application
- **Settings Activity**: `SettingsActivity.kt` - Configuration interface
- **Default Playlist URL**: `http://openwrt.lan/iptv.m3u` (can be changed in settings)
