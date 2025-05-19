# Wellniary 📘 – Your Personal Wellness Diary App
![Platform](https://img.shields.io/badge/platform-Android-blue)
![Language](https://img.shields.io/badge/language-Kotlin-orange)
![License](https://img.shields.io/badge/license-Academic-informational)

**GitHub Repository:** [https://github.com/jinyu1227/wellniary-project](https://github.com/jinyu1227/wellniary-project)

Wellniary is a wellness-tracking Android application built with **Kotlin** and **Jetpack Compose**, designed to help users monitor their diet, hydration, body weight, and overall lifestyle habits. With features like nutritional logging, reminder notifications, health goal setting, and visual reporting, Wellniary empowers users to build healthier routines.

---

## 🌟 Key Features

### 👤 User Authentication
- Email/password and Google Sign-In via **Firebase Authentication**
- First-time login requires completing a personal profile (username, birthday, gender, height, weight)

### 🥗 Diet Logging
- Daily intake logging with meal type (Breakfast, Lunch, Dinner) and categories: Staple, Meat, Vegetable, Other
- Nutritional data fetched from the **Nutritionix API**
- Data is synced both **locally (Room DB)** and **remotely (Firebase Firestore)**
- Background sync is handled automatically via **WorkManager**

### 💧 Health Tracking
- Daily water intake and body weight tracking
- Personalized health goals (water cups & target weight)
- Motivational daily quotes powered by external API

### ⏰ Smart Reminders
- Customizable health reminders (e.g., hydration, meal time)
- System-level push notifications via `BroadcastReceiver`

### 📊 Weekly Reports
- Weight and water consumption over the week
- Pie chart analysis of dietary categories
- Historical diet log with delete and sync functionality

---

## 🛠️ Tech Stack

| Layer            | Technology                                                    |
|------------------|---------------------------------------------------------------|
| Language         | Kotlin                                                        |
| UI Framework     | Jetpack Compose, Material 3                                   |
| Architecture     | MVVM, ViewModel, StateFlow, CoroutineScope, WorkManager       |
| Database         | Room (Local), Firebase Realtime DB & Firestore                |
| Networking       | Retrofit + Gson + Nutritionix Food API                        |
| Auth             | Firebase Auth (Email & Google)                                |
| Notification     | AlarmManager + BroadcastReceiver + Notification               |
| Build Tools      | Gradle (KTS), Android Studio                                  |

---

## 📁 Project Structure
```bash
📁 app/
├── MainActivity.kt // Entry point
├── SplashScreen.kt // Welcome screen with delayed transition
├── Login.kt / SignUp.kt // Firebase user authentication
├── InitialProfile.kt // First-time profile setup
├── Me.kt // Central logic for authentication + routing
├── Profile.kt // View and edit profile
├── Intake.kt // Daily food intake input
├── DietRecordsScreen.kt // Diet log history
├── ReminderSettings.kt // Health reminders
├── Home.kt // Water & weight tracking
├── Report.kt / ReportCharts.kt// Weekly health summary & charts
├── SyncWorker.kt // Daily sync to Firestore
└── viewmodels, daos, entities, repositories
```

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/jinyu1227/wellniary-project.git
cd wellniary-project
```
### 2. Setup Firebase & API
Add your google-services.json into the /app directory
Enable Firebase Authentication (Email & Google Sign-In)
Replace API keys in NutritionixService.kt with your own:
@Headers(
  "x-app-id: YOUR_APP_ID",
  "x-app-key: YOUR_API_KEY",
  ...
)

### 3. Open in Android Studio
Use Android Studio Arctic Fox or above
Sync Gradle files and build the project

### 4. Run the App
Run on emulator or physical Android device (API level 26+)

---

## 👨‍💻 Author

Jinyu Yan
Monash University - Master of Information Technology
https://github.com/jinyu1227

Tong Gao
Monash University - Master of Information Technology
https://github.com/Gaotong27

Hao Xiao
Monash University - Master of Information Technology
https://github.com/Morerer

Chiyu Chen
Monash University - Master of Information Technology
https://github.com/kelvin422318

---

📜 License

This project is licensed for educational use under Monash University FIT5046 Project Guidelines.
