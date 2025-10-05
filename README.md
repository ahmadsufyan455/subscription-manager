# Subscription Manager

A modern Android application built with Jetpack Compose for managing and tracking your recurring subscriptions. Stay on top of your subscription payments with smart notifications and comprehensive spending analytics.

## Features

### Core Features
- **Subscription Management**: Add, edit, and delete subscriptions with ease
- **Smart Notifications**: Get reminders 7, 3, and 1 day before payment due dates
- **Spending Overview**: Track monthly spending and active subscriptions count
- **Upcoming Payments**: Quick view of subscriptions due within the next 7 days
- **Multiple Billing Cycles**: Support for Monthly, Quarterly, and Yearly billing periods
- **Subscription Status**: Track active, cancelled, and expired subscriptions
- **Undo Delete**: Accidentally deleted? Undo with a single tap

### UI/UX Features
- **Material Design 3**: Modern, clean UI following Material Design guidelines
- **Bottom Sheet Forms**: Intuitive bottom sheet interface for adding/editing subscriptions
- **Swipe to Delete**: Quick deletion with swipe gestures
- **Pre-defined Services**: Quick selection from popular services (Netflix, Spotify, YouTube, etc.)
- **Custom Icons**: Support for custom subscription icons
- **Loading States**: Smooth loading experiences with proper state management

## Tech Stack

### Architecture
- **Clean Architecture**: Separation of concerns with data, domain, and presentation layers
- **MVVM Pattern**: Model-View-ViewModel architecture pattern
- **Repository Pattern**: Abstraction layer for data sources

### Core Technologies
- **Jetpack Compose**: Modern declarative UI toolkit
- **Kotlin**: 100% Kotlin codebase with Coroutines for async operations
- **Room Database**: Local data persistence
- **Koin**: Dependency injection
- **Navigation Compose**: Type-safe navigation

### Jetpack Components
- **ViewModel**: UI state management and lifecycle awareness
- **StateFlow**: Reactive state management
- **WorkManager**: Background task scheduling for notifications
- **LiveData**: Observable data holder

### UI Libraries
- **Material3**: Material Design 3 components
- **Material Icons Extended**: Extended icon set
- **Haze**: Blur effects for modern UI
- **Swipe**: Swipe gesture library

## Project Structure

```
com.zerodev.subscriptionmanager/
├── core/
│   ├── di/                          # Dependency Injection modules
│   │   └── injection.kt            # Koin modules setup
│   ├── helper/                      # Helper classes
│   │   ├── NotificationHelper.kt   # Notification creation and management
│   │   ├── NotificationScheduler.kt # Scheduling notifications
│   │   ├── NotificationTracker.kt  # Track sent notifications
│   │   ├── RenewalHelper.kt        # Subscription renewal logic
│   │   └── RenewalScheduler.kt     # Schedule renewal tasks
│   ├── utils/                       # Utility classes
│   │   ├── DateFormatter.kt        # Date formatting utilities
│   │   ├── IconMapper.kt           # Map services to icons
│   │   └── InputValidation.kt      # Form validation
│   └── workers/                     # Background workers
│       ├── NotificationWorker.kt   # Daily notification check
│       └── SubscriptionRenewalWorker.kt # Renewal processing
├── data/
│   ├── local/
│   │   ├── converters/              # Room type converters
│   │   │   └── BillingCycleConverter.kt
│   │   ├── dao/                     # Data Access Objects
│   │   │   └── SubscriptionDao.kt
│   │   ├── database/                # Room database
│   │   │   └── SubscriptionDatabase.kt
│   │   └── entities/                # Database entities
│   │       └── Subscription.kt     # Subscription data model
│   └── repository/                  # Repository implementations
│       └── SubscriptionRepository.kt
├── presentation/
│   ├── navigation/                  # Navigation setup
│   │   ├── BottomNavItem.kt        # Bottom nav items
│   │   └── MainNavigation.kt       # Navigation graph
│   ├── screens/                     # UI screens
│   │   ├── HomeScreen.kt           # Main subscription list
│   │   ├── SettingsScreen.kt       # App settings
│   │   └── AddSubscriptionBottomSheet.kt # Add/Edit form
│   └── viewmodel/                   # ViewModels
│       └── HomeViewModel.kt        # Home screen state management
├── ui/
│   ├── components/                  # Reusable components
│   │   ├── SubscriptionCard.kt     # Subscription list item
│   │   ├── UpcomingCard.kt         # Upcoming payment card
│   │   ├── GlobalTextField.kt      # Custom text field
│   │   └── DatePickerField.kt      # Date picker component
│   └── theme/                       # Theme configuration
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── MainActivity.kt                  # App entry point
└── SubscriptionManagerApp.kt       # Application class
```

## Data Model

### Subscription Entity
```kotlin
@Entity(tableName = "subscriptions")
data class Subscription(
    val id: Int,
    val name: String,
    val price: Double,
    val billingCycle: BillingCycle,  // MONTHLY, QUARTERLY, YEARLY
    val startDate: Long,
    val status: SubscriptionStatus,   // ACTIVE, CANCELLED, EXPIRED
    val cancelledAt: Long?,
    val createdAt: Long
)
```

### Key Methods
- `getNextBillingDate()`: Calculate next payment date
- `getRemainingDays()`: Days until next payment
- `getCurrentBillingPeriodStart()`: Current billing period start date
- `needsRenewal()`: Check if subscription needs renewal

## Background Tasks

### NotificationWorker
- Runs daily to check upcoming payments
- Sends notifications at 7, 3, and 1 days before due date
- Respects user notification preferences
- Tracks sent notifications to avoid duplicates

### SubscriptionRenewalWorker
- Processes subscription renewals
- Updates billing dates for active subscriptions
- Handles expired subscriptions

## Dependency Injection

Using Koin for dependency injection with three modules:

1. **databaseModule**: Room database and DAOs
2. **repositoryModule**: Repository implementations
3. **viewModelModule**: ViewModels with dependencies

## Build Configuration

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Version**: 21
- **ProGuard**: Enabled for release builds with R8 optimization

## Setup & Installation

### Prerequisites
- Android Studio Ladybug or newer
- JDK 21
- Android SDK 36

### Steps
1. Clone the repository
```bash
git clone https://github.com/yourusername/SubscriptionManager.git
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Create `gradle.properties` in the root directory with signing credentials (for release builds):
```properties
MYAPP_RELEASE_STORE_FILE=path/to/keystore.jks
MYAPP_RELEASE_STORE_PASSWORD=your_store_password
MYAPP_RELEASE_KEY_ALIAS=your_key_alias
MYAPP_RELEASE_KEY_PASSWORD=your_key_password
```

5. Build and run the app

## Permissions

- `POST_NOTIFICATIONS`: Required for sending payment reminder notifications (Android 13+)

## Supported Services

Pre-configured icons for popular services:
- Netflix
- Spotify
- YouTube
- ChatGPT
- Claude
- Perplexity
- JetBrains
- Shopee
- And more...

## Future Enhancements

Potential features for future releases:
- Export subscription data (CSV/JSON)
- Data backup and restore
- Multi-currency support
- Subscription categories
- Advanced analytics and charts
- Widget support
- Dark mode customization
- Cloud sync

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- Material Design 3 guidelines
- Jetpack Compose community
- Koin documentation
- Room persistence library

---

**Built with ❤️ using Jetpack Compose**
