# Configuration Options

Access the settings page via the **☰ MENU** button to configure:

## Available Settings

| Setting | Description | Default Value |
|---------|-------------|---------------|
| **Playlist URL** | M3U playlist URL for IPTV channels | `http://openwrt.lan/iptv.m3u` |
| **Prefer TVG URL** | Whether to use the EPG URL specified in the playlist | - |
| **EPG URL** | Alternative XMLTV EPG URL | - |
| **Enable "All Channels" group** | Shows all channels in a single group | - |
| **Buffer duration** | Adjusts buffering time in milliseconds | 500ms |
| **Multicast Interface** | Select network interface for RTP/UDP multicast | auto-detect |

## Data Persistence

The application remembers:
- Last played channel (using SharedPreferences)
- All user-configured settings
