# FlowFi

[![CI](https://github.com/saadkhalidkhan/FlowFi/actions/workflows/ci.yml/badge.svg)](https://github.com/saadkhalidkhan/FlowFi/actions/workflows/ci.yml)
[![Docs](https://github.com/saadkhalidkhan/FlowFi/actions/workflows/docs.yml/badge.svg)](https://github.com/saadkhalidkhan/FlowFi/actions/workflows/docs.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.saadkhalidkhan/flowfi-core?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.saadkhalidkhan/flowfi-core)
[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/saadkhalidkhan/FlowFi/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![minSdk](https://img.shields.io/badge/minSdk-26-green)](app/build.gradle.kts)
[![targetSdk](https://img.shields.io/badge/targetSdk-36-green)](app/build.gradle.kts)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)

A fast, Material 3 personal finance app for tracking income and expenses. Built with Kotlin, Jetpack Compose, Room, and MVVM.

## Demo

Watch the full walkthrough: **[flowfidemo.webm](docs/media/flowfidemo.webm)** (WebM, ~6 MB)

> **Tip:** Open the video in a browser or Android Studio device preview. To create a GIF locally:  
> `ffmpeg -i docs/media/flowfidemo.webm -vf "fps=8,scale=400:-1" -t 15 docs/media/flowfidemo.gif`

## Screenshots

| Dashboard | Add / Edit | View All |
|:---:|:---:|:---:|
| ![Dashboard](docs/images/dashboard.png) | ![Add transaction](docs/images/add_edit.png) | ![All transactions](docs/images/view_all.png) |

## Features

- **Transaction tracking** — Record income and expenses with amount, category, date, and notes
- **Financial dashboard** — Balance, income, and expense totals with smart insights
- **Transaction history** — Chronological list with dates
- **Material 3 UI** — Dynamic color, light/dark themes, edge-to-edge layout

## Installation

### Run the app from source

**Requirements:** Android Studio Ladybug or newer, JDK 17+, Android SDK 36

```bash
git clone https://github.com/saadkhalidkhan/FlowFi.git
cd FlowFi
./gradlew :app:assembleDebug
```

Install on a device or emulator:

```bash
./gradlew :app:installDebug
adb shell am start -n com.example.flowfi/.MainActivity
```

### Use the published library (`flowfi-core`)

Add Maven Central and the dependency:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("io.github.saadkhalidkhan:flowfi-core:1.0.0")
}
```

> Publishing requires [Sonatype Central](https://central.sonatype.com/) credentials and GPG signing. See [Publishing](#publishing) and `gradle/publishing.properties.example`.

## Usage examples

### Insert a transaction

```kotlin
import com.example.flowfi.data.database.AppDatabase
import com.example.flowfi.data.entity.TransactionEntity
import com.example.flowfi.data.entity.TransactionType
import com.example.flowfi.data.repository.TransactionRepositoryImpl

val repository = TransactionRepositoryImpl(
    AppDatabase.getDatabase(context).transactionDao()
)

repository.insertTransaction(
    TransactionEntity(
        amount = 42.50,
        category = "Food",
        date = System.currentTimeMillis(),
        type = TransactionType.EXPENSE,
        note = "Lunch"
    )
)
```

### Observe totals in a ViewModel

```kotlin
import com.example.flowfi.data.entity.TransactionType
import kotlinx.coroutines.flow.map

repository.getAllTransactions()
    .map { list ->
        val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        income - expenses // balance
    }
```

### Wire the repository in `Application`

```kotlin
class FlowFiApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepositoryImpl(database.transactionDao()) }
}
```

## Documentation

API reference is generated with [Dokka](https://kotlinlang.org/docs/dokka-introduction.html):

```bash
./gradlew :flowfi-core:dokkaGeneratePublicationHtml
```

Output: `flowfi-core/build/dokka/html/`

Published docs (GitHub Pages): **https://saadkhalidkhan.github.io/FlowFi/** (after the Docs workflow runs on `main`).

## Project structure

```
FlowFi/
├── app/                 # Compose UI, navigation, ViewModel
├── flowfi-core/         # Room entities, DAO, database, repository (Maven artifact)
├── docs/
│   ├── images/          # Screenshots
│   └── media/           # Demo video
└── .github/workflows/   # CI, docs, release publishing
```

## Tech stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Repository pattern |
| Persistence | Room (KSP) |
| Async | Kotlin Coroutines, StateFlow |
| Navigation | Navigation Compose |

## Publishing

1. Register namespace `io.github.saadkhalidkhan` on [Maven Central](https://central.sonatype.com/).
2. Add GitHub repository secrets (see `gradle/publishing.properties.example`):
   - `MAVEN_CENTRAL_USERNAME`
   - `MAVEN_CENTRAL_PASSWORD`
   - `SIGNING_IN_MEMORY_KEY`
   - `SIGNING_IN_MEMORY_KEY_PASSWORD`
3. Create a release tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The [Release](.github/workflows/release.yml) workflow publishes `io.github.saadkhalidkhan:flowfi-core` to Maven Central.

## Development

```bash
./gradlew testDebugUnitTest          # Unit tests
./gradlew :app:assembleDebug          # Debug APK
./gradlew :flowfi-core:assembleRelease
```

## License

This project is licensed under the [MIT License](LICENSE).

Copyright (c) 2026 Saad Khalid Khan
