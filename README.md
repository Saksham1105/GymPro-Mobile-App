# GymPro

A local-first Android app for gym member management, daily attendance, membership plans, renewals, and portable backup/restore.

[![Android CI](https://github.com/Saksham1105/GymPro-Mobile-App/actions/workflows/android.yml/badge.svg)](https://github.com/Saksham1105/GymPro-Mobile-App/actions/workflows/android.yml)

## Overview

GymPro is designed for gyms that want a simple member-management workflow without requiring accounts, cloud infrastructure, or a continuous internet connection.

All operational data is stored locally on the device using Room/SQLite. The app does not depend on Supabase, Firebase, or another remote backend for its core functionality.

## Features

- **Member management** — create, edit, view, and remove member records.
- **Attendance tracking** — record daily attendance and review attendance history.
- **Membership plans** — manage available membership plans and pricing.
- **Renewals** — record membership renewals and payment information.
- **Dashboard** — review key gym activity and membership information.
- **Offline-first storage** — continue using the app without an internet connection.
- **Backup & restore** — export gym data to portable JSON and import it back when needed.

## Architecture

```text
Jetpack Compose UI
        ↓
ViewModels
        ↓
Repositories
        ↓
Room Database
        ↓
SQLite
```

The codebase follows a practical MVVM + Repository structure with Room as the local persistence layer.

## Technology Stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX ViewModel
- Room / SQLite
- Kotlin Coroutines
- KSP
- Gradle
- GitHub Actions

## Privacy & Data

GymPro is local-first. Core member, attendance, plan, renewal, and payment data is stored locally on the device.

Automatic Android backup/device transfer is disabled for GymPro's operational data so that gym records are not silently copied through the normal Android backup pipeline.

The repository does not require a cloud database or authentication service to run the application.

## Backup & Restore

GymPro supports manual export/import using a portable JSON backup.

**Important:** exported backups are plaintext JSON and are **not encrypted by default**. Treat backup files as sensitive gym records and store or transfer them accordingly.

Backups are intended for manual portability and recovery; they are not a substitute for an encrypted off-site backup system.

## Requirements

- Android Studio with a compatible Android Gradle Plugin setup
- JDK 17
- Android SDK 36
- Gradle Wrapper included in the repository

## Build

Clone the repository and use the Gradle Wrapper from the project root.

### Linux / macOS

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

### Windows

```powershell
.\gradlew.bat clean
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

The generated debug APK is written under `app/build/outputs/apk/debug/`.

## Testing

The project includes JVM/unit-test coverage for the application build. GitHub Actions runs the test task and builds a debug APK on pushes to the main branch.

The CI workflow is intended to catch compilation, dependency, and test regressions before changes are considered complete.

## Release Signing

Release signing credentials should never be committed to the repository.

Configure the following environment variables when producing a signed release build:

```text
KEYSTORE_PATH
STORE_PASSWORD
KEY_ALIAS
KEY_PASSWORD
```

Use GitHub Actions Secrets or a secure local environment for real release credentials.

## Development Notes

The project intentionally avoids a backend dependency for its core workflow. This keeps the app usable offline and makes the local database the primary source of truth.

Before treating the app as a production-grade data-preservation system, maintain explicit Room migrations for every schema change and test backup/restore procedures regularly.

## CI

GitHub Actions is configured in `.github/workflows/android.yml`.

The workflow uses:

- JDK 17
- Android SDK 36
- the repository Gradle Wrapper
- unit tests
- debug APK assembly

[View workflow runs](https://github.com/Saksham1105/GymPro-Mobile-App/actions)

## License

GymPro is licensed under the MIT License. See [LICENSE](LICENSE) for the full license text.
