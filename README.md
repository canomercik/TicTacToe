# Tic Tac Toe Android

![TicTacToe](https://github.com/user-attachments/assets/317cca57-d9e6-41f1-af2b-c694b8465c15)

A simple yet feature-rich Tic Tac Toe game built for Android. It demonstrates animations, AI opponents, scoring, and localization.

---

## 🚀 Features

* **Welcome Screen** with floating "Tap to Start" animation and falling X/O background
* **Game Modes**: Player vs Player (PvP) and Player vs Bot (PvBot)
* **Difficulty Levels**: Easy (random), Medium (win/block heuristics), Hard (Minimax algorithm)
* **Scoring System**: Based on number of moves, time elapsed, and difficulty multiplier with high-score persistence
* **Localization**: English and Turkish support with runtime language switching
* **Full-Screen Immersive** experience without status or action bars

---

## 🛠 Installation

1. Clone this repository:

   ```bash
   git clone https://github.com/<your-username>/tic-tac-toe-android.git
   ```
2. Open in Android Studio (Arctic Fox or later).
3. Let Gradle sync and build the project.
4. Run on an emulator or Android device (API 21+).

---

## ▶️ Usage

1. **Launch** the app to see the Welcome Screen.
2. **Tap** anywhere to open the Mode selection dialog.
3. Choose **Player vs Player** or **Player vs Bot**.
4. If PvBot, select **Easy**, **Medium**, or **Hard** difficulty.
5. **Play** the game. Your score updates after each win and high scores are stored.
6. Use the **Settings** icon to toggle between English and Turkish.

---

## 🌐 Localization

* Default language: English
* Turkish translations stored in `res/values-tr/`
* Switch languages at runtime from the Settings button.

---

## 🤖 Bot AI Logic

* **Easy**: Chooses a random empty cell.
* **Medium**: Attempts to win in one move; otherwise blocks opponent; else random.
* **Hard**: Uses the Minimax algorithm for optimal play.

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
│   └── FallingXOView.java
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
└── docs/    ← screenshots & GIFs
```

---

## ✍️ Contributing

Contributions welcome! Feel free to open issues or submit pull requests.

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
