# Core Components

## MainActivity
The primary activity that manages:
- Playlist and EPG downloading
- UI fragment management (playback, playlist, program info)
- Remote control input handling
- Settings management
- System UI visibility control

## PlaybackFragment
Handles media playback using ExoPlayer:
- Initializes ExoPlayer with FFmpeg extension support
- Manages buffer duration settings
- Handles playback errors
- Persists last played channel
- Supports RTP/UDP multicast streams

## PlaylistViewModel
Manages application state:
- Downloads and parses M3U playlists
- Downloads and parses XMLTV EPG data
- Handles channel grouping and switching
- Provides LiveData for UI updates

## M3UParser
Parses M3U playlist files:
- Extracts channel information (name, group, URL, etc.)
- Parses TVG (TV Guide) URL from playlist headers
- Maps channel attributes (tvg-id, tvg-logo, etc.)

## XMLTV Package
Handles EPG data:
- Parses XMLTV format for program information
- Maps programs to channels using tvg-id or channel name
- Provides program scheduling information

## RTP Package
Handles RTP/UDP multicast streaming:
- `RtpPacket.kt`: RTP packet parsing and header stripping
- `RtpTransport.kt`: UDP multicast socket management
- `RtpDataSource.kt`: DataSource for ExoPlayer integration
- `RtpDataSourceFactory.kt`: Factory for creating RTP data sources
- `RtpMediaSource.kt`: MediaSource for ExoPlayer integration
- `NetworkInterfaceUtils.kt`: Network interface detection

## Settings Activity
Configuration interface for:
- Custom M3U playlist URL
- Multicast interface selection
- EPG source URL
- Other playback preferences
