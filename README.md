# GymPro Mobile App

GymPro is a local-first Android application for small gyms and fitness centers. It is designed to manage members, membership plans, attendance, renewals, and portable local backups without requiring a backend service.

## Features

- Member management
- Membership plans and expiry tracking
- Daily attendance
- Renewal history
- Local Room database storage
- JSON export/import for explicit backups
- Material 3 Compose UI
- Offline-first operation

## Architecture

GymPro uses Jetpack Compose for the UI, ViewModels for presentation state, repositories for data access, and Room for local persistence.

```text
Compose UI
   ↓
ViewModels
   ↓
Repositories
   ↓
Room / SQLite
```

The current implementation is intentionally local-first. No cloud database is required for normal operation.

## Build

Open the project in Android Studio and use the project's Gradle tooling to build the `app` module.

Requirements:

- Android Studio with Android Gradle Plugin 9.x support
- JDK 11
- Android SDK 36

The debug build uses Android's standard debug signing unless you provide a release keystore through environment variables.

## Release signing

Do not commit signing keys or passwords.

Set these environment variables when producing a signed release build:

```text
KEYSTORE_PATH
STORE_PASSWORD
KEY_ALIAS
KEY_PASSWORD
```

The release configuration is intentionally unsigned when `KEYSTORE_PATH` is not provided so a public clone does not depend on a private keystore.

## Data and privacy

GymPro stores member and gym-management records locally on the device. Automatic Android backup and device-transfer extraction are disabled for application data. Use GymPro's explicit export/import workflow when you need to move or back up records.

The current export format is portable JSON. It is not encrypted, so exported backup files should be handled as sensitive data and stored securely.

## Database migrations

The current database configuration uses destructive fallback migration while the schema is still evolving. Before a production release with established user data, explicit Room migrations should replace destructive fallback migration.

## Testing

The repository contains unit-test and Robolectric test infrastructure. Generated Android Studio sample tests have been removed from the public source tree so tests can focus on GymPro behavior rather than template assertions.

## License

GymPro is released under the MIT License. See [LICENSE](LICENSE).
