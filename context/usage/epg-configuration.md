# EPG (Electronic Program Guide) Configuration

## Overview

EPG provides program guide data for IPTV channels using XMLTV format.

## Configuration Options

### M3U TVG URL

Use `x-tvg-url` in the M3U file to specify the EPG source:

```m3u
#EXTM3U x-tvg-url="http://192.168.1.1/epg.xml"
```

### Channel Matching

Use `tvg-id` in EXTINF tags to match channels with EPG entries:

```m3u
#EXTINF:-1 tvg-id="1" group-title="Group",Channel Name
```

**Note**: Currently only tvg-id matching is supported (no name matching).

### Settings Configuration

Users can configure EPG through the settings menu:

1. **Prefer TVG URL**: Whether to use the EPG URL specified in the playlist
2. **EPG URL**: Alternative XMLTV EPG URL (if not using TVG URL from playlist)

## XMLTV Format

The application parses XMLTV format for program information and maps programs to channels using tvg-id.
