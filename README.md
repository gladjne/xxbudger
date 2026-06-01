# Budget Joy

Budget Joy is an AI-powered student budget tracker utilizing Google's Gemini models to help students take control of their finances. It combines modern Android design with intelligent financial insights and strict privacy controls.

## Key Features

*   **Smart Tracking & Categorization**: Log and visualize income, savings, and expense transactions through a highly clean Material 3 interface.
*   **AI Financial Diagnostics**: Leverages Gemini to deliver tailored financial advice, automatic weekly summaries, and predictive budgeting strategies.
*   **MASVS-Aligned Security**: Includes an on-device Android KeyStore AES-GCM cryptography engine, robust salted password hashing, real-time system integrity/root check diagnostics, and a toggleable anti-screenshot/screen-record shield.
*   **Two-Factor Authentication (2FA)**: Safe, local multi-factor authentication setup for securing account credentials.
*   **Multilingual Design**: Supports dynamic localizations including English, French, Spanish, German, Italian, Portuguese, Arabic, and more.

## Prerequisites

*   **Android Studio** Koala (or newer)
*   **JDK** 17
*   **Android SDK 26** (Android 8.0 Oreo) as `minSdk`, running up to **SDK 34** (Android 14)
*   **Google Gemini API Key** (configured in your environment)

## Setup & Installation

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/BudgetJoy.git
    cd BudgetJoy
    ```

2.  **Configure Environment Secrets**:
    *   Duplicate `.env.example` to create a `.env` file in the project's root folder:
        ```bash
        cp .env.example .env
        ```
    *   Open your newly created `.env` file and insert your personal Google Gemini API credential:
        ```env
        GEMINI_API_KEY=your_actual_gemini_api_key_here
        ```

3.  **Build & Run**:
    *   Open the project folder inside **Android Studio**.
    *   Allow the Gradle build daemon to automatically pull and sync external dependencies.
    *   Click **Run** in the toolbar or build via the terminal command line interface:
        ```bash
        gradle assembleDebug
        ```

## Project Structure Overview

```text
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/example
│   │   │   │   ├── MainActivity.kt        # Entry-point handling core windows & security shields
│   │   │   │   ├── data                   # Local repositories, Room Databases, and CryptoUtils
│   │   │   │   ├── domain                 # Models, repository contracts, and AI services
│   │   │   │   ├── presentation           # Composable Tab UI, screens (Privacy, Auth, etc.), and ViewModels
│   │   │   │   └── ui                     # M3 themes, custom palettes, and localizations (FR, EN...)
│   │   │   └── AndroidManifest.xml        # Application declarations, permissions, and build rules
│   │   └── test                           # Local Robolectric test suites
│   └── build.gradle.kts                   # Module level Android Gradle build configuration
├── gradle/libs.versions.toml              # Centralized Version Catalog file
├── settings.gradle.kts                    # Root settings pointing project namespace to BudgetJoy
└── README.md                              # This documentation file
```

## Contributing

Contributions make the open-source community an amazing place to learn, inspire, and create.

1.  Fork the Project.
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the Branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request targeting the Master/Main branch.

## License

This project is licensed under the MIT License - see the placeholder fields or standard terms for details.
