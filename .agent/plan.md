# Project Plan

FlowFi: A simple personal finance app to track income and expenses. Focus on speed, simplicity, and clarity. Using Kotlin, Compose, MVVM, Room, and Material 3.

## Project Brief

# FlowFi Project Brief

FlowFi is a streamlined personal finance application designed for users who prioritize speed and clarity. It enables quick tracking of income and expenses while providing a clear, real-time view of monthly financial health through a vibrant and modern Material 3 interface.

## Features

- **Transaction Tracking**: Quickly record income and expenses with fields for amount, category, and date to ensure minimal friction during entry.
- **Financial Dashboard**: A high-level overview of the total balance, total income, and total expenses for the current month.
- **Categorized History**: A chronological list of transactions that can be filtered or grouped by category to help users understand their spending habits.
- **Dynamic Theming**: Support for light and dark modes with a vibrant, energetic color scheme that adapts to the system's Material 3 settings.

## High-Level Technical Stack

- **Kotlin**: The primary language for modern Android development.
- **Jetpack Compose**: A modern toolkit for building native UI using a declarative approach.
- **MVVM Architecture**: Ensures a clean separation of logic and UI for better testability and maintenance.
- **Room Database**: Local persistence for storing transaction data, utilizing **KSP (Kotlin Symbol Processing)** for efficient code generation.
- **Kotlin Coroutines**: For managing background tasks and ensuring a smooth, responsive UI.
- **Jetpack Navigation**: To handle transitions between the dashboard, transaction entry, and history screens.
- **Material 3**: Implementation of the latest Material Design components and full edge-to-edge display support.

## Implementation Steps
**Total Duration:** 1h 47m 23s

### Task_1_DataLayer_and_MVVM: Set up the data persistence layer using Room and the MVVM architecture core components.
- **Status:** COMPLETED
- **Updates:** Task_1_DataLayer_and_MVVM completed.
Key Actions Taken:
1. Data Persistence Layer:
- Transaction Entity: Created TransactionEntity with fields for amount, category, date (timestamp), type (Income/Expense), and notes.
- DAO: Implemented TransactionDao with CRUD operations and specialized queries for total income and expenses.
- Database: Set up AppDatabase using Room. Included a Converters class for the TransactionType enum and implemented a singleton pattern.
- Debug Feature: Added logic to AppDatabase to programmatically delete the database in debug builds to prevent persistent data issues during development.
2. MVVM Architecture:
- Repository: Established the TransactionRepository interface and its implementation TransactionRepositoryImpl to abstract data access.
- ViewModel: Created TransactionViewModel which exposes a TransactionUiState via a StateFlow. It automatically calculates total income, expenses, and balance from the transaction list.
- ViewModel Factory: Implemented TransactionViewModelFactory for proper dependency injection.
3. Application Setup:
- Created FlowFiApplication to provide global access to the database and repository.
- Updated AndroidManifest.xml to include the custom application class.
4. Build Configuration:
- Updated build.gradle.kts to include Room (with KSP), Lifecycle, and Coroutines dependencies.
- Set compileSdk and targetSdk to 36 as required by the latest androidx.core dependencies.
- Enabled buildConfig to support the debug database cleanup logic.
Outcomes:
- The data persistence layer is fully functional and integrated with the MVVM structure.
- The application successfully compiles and builds.
- The architecture is ready for the UI implementation (Dashboard and Transaction Entry).
- **Acceptance Criteria:**
  - Room Database and Transaction Entity (amount, category, date, type) implemented
  - DAO and Repository pattern established
  - ViewModel handles transaction state and logic
  - Data persistence layer is functional
- **Duration:** 1h 47m 23s

### Task_2_Navigation_and_UI_Screens: Implement the application navigation and core screens (Dashboard and Transaction Entry).
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Jetpack Navigation configured for screen transitions
  - Dashboard displays financial summary (Balance, Income, Expenses) and history
  - Transaction Entry screen allows adding/editing transactions
  - UI components built with Jetpack Compose
- **StartTime:** 2026-04-13 20:26:52 PKT

### Task_3_Theming_and_Assets: Apply Material 3 vibrant theming, edge-to-edge support, and create the app icon.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Material 3 vibrant color scheme (light/dark) implemented
  - Full edge-to-edge display support active
  - Adaptive app icon matching FlowFi branding created
  - UI reflects an energetic and modern aesthetic

### Task_4_Run_and_Verify: Final verification of the application functionality and stability.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Project builds successfully
  - App does not crash during navigation or data entry
  - All existing tests pass
  - App meets all requirements in the project brief

