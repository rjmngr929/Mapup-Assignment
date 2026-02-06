# MapUp Assessment – Android Tracking App

## 📱 Project Overview

This project is an Android application developed as part of the **MapUp assessment**. The app focuses on **location tracking**, **route visualization**, and **session management**, while following **clean architecture principles**.

The main goal of the assignment was to demonstrate:

* Proper use of **MVVM architecture**
* Dependency management using **Dagger-Hilt**
* Realtime state updates between **Foreground Service** and **UI**
* Clean and maintainable Android code

---

## 🧠 Architecture Used – MVVM

The project is structured using the **MVVM (Model–View–ViewModel)** architecture to maintain clear separation of concerns.

### 🔹 Model

* Handles data and business logic
* Includes:

  * `SharedPrefManager` (tracking state & preferences)
  * Session-related logic

### 🔹 ViewModel

* Acts as a bridge between UI and data layer
* Exposes observable state using **StateFlow**
* Does not contain any Android UI references

### 🔹 View (Activity / Fragment)

* Responsible only for rendering UI
* Observes ViewModel state and reacts to updates
* No direct data or business logic

This structure improves:

* Readability
* Testability
* Scalability

---

## 🔗 Dependency Injection – Dagger-Hilt

**Dagger-Hilt** is used to manage dependencies across the app.

### Why Hilt?

* Avoids manual object creation
* Ensures single instances where required (Singleton)
* Simplifies lifecycle management

### Usage Examples:

* Injecting `SharedPrefManager` into ViewModel and Service
* Providing an `ApplicationScope` CoroutineScope

```kotlin
@Inject lateinit var sharedPrefManager: SharedPrefManager
```

---

## 📍 Location Tracking & Foreground Service

* Location tracking is handled using a **Foreground Service**
* Ensures tracking continues even when the app goes to background
* Service updates tracking state via SharedPreferences

### Tracking State Handling

* `SharedPreferences` acts as the **single source of truth**
* Converted into a **StateFlow** using `callbackFlow`
* UI updates in realtime whenever tracking state changes

---

## 🔄 Realtime State Management

To keep UI and background components in sync:

* Tracking state is stored in SharedPreferences
* Observed using a `StateFlow`
* StateFlow is scoped to **Application lifecycle**, not UI lifecycle

This ensures:

* Realtime updates from Service → UI
* No stale state issues
* Works even when updates come from notifications

---

## 🗺️ Map & Route Visualization

* Map is implemented using **osmdroid MapView**
* Features:

  * Route polyline rendering
  * Start & End markers for each session
  * Custom label overlays

### Session Switching Handling

* Old session markers & overlays are removed before adding new ones
* Prevents overlay duplication when switching between sessions

---

## 🛑 Permissions & Battery Optimization Handling

* Runtime permissions for location and notifications
* Graceful handling of:

  * Background location permission
  * Battery optimization restrictions
* User-friendly dialogs guide users to system settings when required

---

## 🧪 Key Technical Highlights

* MVVM architecture
* Dagger-Hilt for DI
* StateFlow for reactive UI updates
* Foreground Service for background tracking
* Clean separation of concerns
* Lifecycle-aware components

---

## ✅ Conclusion

This assignment demonstrates a clean and scalable Android implementation with a strong focus on:

* Architecture
* State management
* Background processing
* User experience

The project is designed to be easily extensible and maintainable, following modern Android development best practices.

---

## 👨‍💻 Developed By

**[Your Name]**
Android Developer
