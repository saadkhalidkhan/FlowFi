# FlowFi

[![CI](https://github.com/saadkhalidkhan/FlowFi/actions/workflows/ci.yml/badge.svg)](https://github.com/saadkhalidkhan/FlowFi/actions/workflows/ci.yml)
[![Docs](https://github.com/saadkhalidkhan/FlowFi/actions/workflows/docs.yml/badge.svg)](https://github.com/saadkhalidkhan/FlowFi/actions/workflows/docs.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/saadkhalidkhan/FlowFi/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![minSdk](https://img.shields.io/badge/minSdk-26-green)](app/build.gradle.kts)
[![targetSdk](https://img.shields.io/badge/targetSdk-36-green)](app/build.gradle.kts)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)

A fast, Material 3 personal finance app for tracking income and expenses. Built with Kotlin, Jetpack Compose, Room, and MVVM.

## Demo

<p align="center">
  <img src="docs/media/flowfidemo.gif" alt="FlowFi app demo" width="400" />
</p>

<p align="center">
  <sub>Also available as <a href="docs/media/flowfidemo.webm">flowfidemo.webm</a> (higher quality)</sub>
</p>

## Screenshots

<p align="center">
  <img src="docs/images/dashboard.png" alt="Dashboard" width="260" />
  <img src="docs/images/add_edit.png" alt="Add transaction" width="260" />
  <img src="docs/images/view_all.png" alt="All transactions" width="260" />
</p>

<p align="center">
  <sub>Dashboard · Add / Edit · View All</sub>
</p>

## Features

- **Transaction tracking** — Record income and expenses with amount, category, date, and notes
- **Edit transactions** — Tap any item on the dashboard or list to update it
- **Delete with undo** — Swipe to delete on the list; tap Undo on the snackbar to restore
- **List filters** — Narrow the full history by month and category
- **Financial dashboard** — Balance, income, and expense totals with smart insights
- **Transaction history** — Chronological list with dates
- **Material 3 UI** — Dynamic color, light/dark themes, edge-to-edge layout

## Installation

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
./gradlew :app:dokkaGeneratePublicationHtml
```

Output: `app/build/dokka/html/`

Published docs (GitHub Pages): **https://saadkhalidkhan.github.io/FlowFi/**

> First-time setup: enable Pages under **Settings → Pages → Build and deployment → Source: GitHub Actions**, or run  
> `gh api -X POST repos/saadkhalidkhan/FlowFi/pages -f build_type=workflow`

## Project structure

```
FlowFi/
├── app/                 # UI, ViewModel, Room data layer
├── docs/
│   ├── images/          # Screenshots
│   └── media/           # Demo video
└── .github/workflows/   # CI and docs
```

## Tech stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Repository pattern |
| Persistence | Room (KSP) |
| Async | Kotlin Coroutines, StateFlow |
| Navigation | Navigation Compose |

## Development

```bash
./gradlew testDebugUnitTest   # Unit tests
./gradlew :app:assembleDebug   # Debug APK
```

## License

This project is licensed under the [MIT License](LICENSE).

Copyright (c) 2026 Saad Khalid Khan
