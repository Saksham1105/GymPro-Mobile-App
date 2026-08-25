# GymPro

A local-first, privacy-focused Android application for tracking gym members, attendances, and membership plans.

## Features

- Member management
- Attendance tracking
- Membership plans
- Renewals
- Dashboard
- Local/offline data
- Backup/restore

## Architecture

Compose
↓
ViewModels
↓
Repositories
↓
Room
↓
SQLite

## Technical Details

- **Offline/local-first behavior:** All data is stored locally on the device using Room/SQLite. The application does not require an internet connection and there is no cloud synchronization.
- **Data ownership & Privacy:** You own your data. Personal member information stays entirely on your device.
- **Backup format:** Backups are exported as plaintext JSON files, which can be stored securely or transferred manually. **Note: These backups are not encrypted by default.**
- **Build requirements:** JDK 17, Android SDK 36.
- **Development setup:** Open in Android Studio or use `./gradlew` to build.
- **Testing:** Standard Android testing frameworks. No instrumentation tests are provided out of the box yet.
- **Release signing:** Release APKs must be signed using environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).

## Limitations

- The database migrations currently use a destructive fallback mechanism in development. Production usage should ensure proper Room migrations are written to avoid accidental data loss.
- There is no cloud backend, authentication, or automated offsite synchronization.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
