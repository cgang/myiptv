# Project Structure and Layout Conventions

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

## Directory Conventions

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

## Important Notes for Development

1. **FFmpeg Extension**: Required for MP2/MP3 audio codec support. Pre-compiled AAR files must be placed in `app/libs/` directory.
2. **Network Permissions**: Requires INTERNET permission for streaming.
3. **Cleartext Traffic**: Enabled to support HTTP streams (common in IPTV setups).
4. **TV Optimization**: Supports both touch and Leanback interfaces, though Leanback launcher is commented out.
5. **Data Persistence**: Remembers the last played channel using SharedPreferences.
6. **Test Files**: Unit tests should be placed in `app/src/test/` directory, not in the main source tree.
