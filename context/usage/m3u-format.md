# M3U Playlist Format

Configure an M3U URL as your streaming source. The playlist should follow this format:

```m3u
#EXTM3U
#EXTM3U x-tvg-url="http://192.168.1.1/epg.xml"
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
http://192.168.1.1:4022/udp/239.9.9.9:9999
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtp://239.9.9.9:9999
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
udp://239.9.9.9:9999
```

**Default URL**: `http://openwrt.lan/iptv.m3u`

## Supported Protocols

- `http://` - HTTP streaming
- `rtp://` / `udp://` - RTP/UDP multicast (experimental)
  - Configure multicast interface in settings
- `rtsp://` - RTSP streaming (SMIL file support)

## SMIL File Support

Channels can reference SMIL files for dynamic stream URLs:

```m3u
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
rtsp://server.example.com/stream.smil
```

When a channel URL ends with `.smil` or `.smi`, the player automatically downloads and parses the SMIL file to extract the actual video URL.
