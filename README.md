
# Tic Tac Toe Android

![TicTacToe Banner](https://github.com/canomercik/TicTacToe/blob/main/Banner.gif)

A simple yet feature-rich Tic Tac Toe game built for Android. It demonstrates animations, AI opponents (including a ChatGPT-powered “Hard” mode with automatic Minimax fallback), scoring, and localization.

---

## 🚀 Features

- **Welcome Screen** with floating “Tap to Start” animation and falling X/O background  
- **Game Modes**: Player vs Player (PvP) and Player vs Bot (PvBot)  
- **Difficulty Levels**:  
  - **Easy** (random)  
  - **Medium** (win/block heuristics)  
  - **Hard** (“O” moves via OpenAI API; on error or offline, falls back to Minimax)  
- **Scoring System**: Based on number of moves, time elapsed, and difficulty multiplier with high-score persistence  
- **Localization**: English and Turkish support with runtime language switching  
- **Full-Screen Immersive** experience without status or action bars  

---

## 🛠 Installation

1. Clone this repository  
   ```bash
   git clone https://github.com/canomercik/TicTacToe.git


2. **Set your OpenAI API key**
   In the **project root**, open (or create) `gradle.properties` and add:

   ```properties
   OPENAI_API_KEY=sk-…your_api_key…
   ```
3. Open in Android Studio (Arctic Fox or later).
4. Let Gradle sync and build the project.
5. Run on an emulator or Android device (API 21+).

---

## ▶️ Usage

1. **Launch** the app to see the Welcome Screen.
2. **Tap** anywhere to open the Mode selection dialog.
3. Choose **Player vs Player** or **Player vs Bot**.
4. If PvBot, select **Easy**, **Medium**, or **Hard** difficulty.
5. **Play** the game. Your score updates after each win and high scores are stored.
6. Use the **Settings** icon to toggle between English and Turkish.

---

## 📦 Dependencies & Permissions

```groovy
// app/build.gradle
dependencies {
    implementation "androidx.appcompat:appcompat:1.6.1"
    implementation "com.google.android.material:material:1.9.0"

    // HTTP client for OpenAI API
    implementation "com.squareup.okhttp3:okhttp:4.11.0"
    // JSON parsing (Android has org.json built-in, but you can declare explicitly)
    implementation "org.json:json:20230227"
}

android {
    defaultConfig {
        // Inject your key from gradle.properties
        buildConfigField "String", "OPENAI_API_KEY", "\"${project.property('OPENAI_API_KEY')}\""
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

---

## 🤖 Bot AI Logic

* **Easy**: Chooses a random empty cell.
* **Medium**: Attempts to win in one move; otherwise blocks opponent; else random.
* **Hard**:

  1. If network available, sends current board JSON to OpenAI’s Chat API (`gpt-3.5-turbo`).
  2. On success, parses `{ "row": r, "col": c }` from the response and plays that move.
  3. On any error (HTTP failure, timeout, offline), automatically falls back to the local Minimax algorithm for an optimal move.

```java
// sample fallback logic in MainActivity
if (difficultyLevel == 2 && isNetworkAvailable()) {
    requestBotMoveFromAPI();           // async OpenAI call
} else {
    choice = (difficultyLevel == 1)
        ? mediumMove(empties)
        : (difficultyLevel == 2)
            ? minimaxMove()          // offline Hard fallback
            : easyMove(empties);
    applyBotMove(choice);
}
```

---

## 📊 Scoring System

Score formula:

```text
rawMoveScore = (9 - moveCount + 1) * 100
timeBonus    = max(0, 60 - elapsedSeconds)
multiplier   = {1.0, 1.5, 2.0} for {Easy, Medium, Hard}
finalScore   = (rawMoveScore + timeBonus) * multiplier
```

High scores persist via `SharedPreferences`.

---

## 📂 Project Structure

```
app/
├── src/main/java/com/example/tictactoe/
│   ├── MainActivity.java
│   ├── WelcomeActivity.java
│   ├── FallingXOView.java
│   └── OpenAIBot.java
├── src/main/res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   └── activity_welcome.xml
│   ├── values/
│   │   ├── strings.xml
│   │   ├── colors.xml
│   │   ├── dimens.xml
│   │   └── styles.xml
│   └── values-tr/strings.xml
└── gradle.properties   ← OPENAI_API_KEY
```

---

## ✍️ Contributing

Contributions welcome! Feel free to open issues or submit pull requests.

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
