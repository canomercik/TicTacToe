package com.example.tictactoe;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button[][] buttons = new Button[3][3];
    private boolean playerXTurn = true;
    private TextView textViewStatus;
    private int moveCount = 0;
    // BOT
    private boolean vsBot;
    private String difficulty;
    private int difficultyLevel;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean gameOver = false;
    // High Score
    private long   gameStartTime;
    private int    currentScore;
    private int    highScore;
    private static final String PREFS = "ticTacToePrefs";
    private static final String KEY_HIGH_SCORE = "highScore";
    private TextView tvScore, tvHighScore;  // onCreate’da bind et
    private static final String KEY_LANGUAGE   = "language";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textViewStatus = findViewById(R.id.textViewStatus);
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        FloatingActionButton btnReset = findViewById(R.id.btnReset);
        FloatingActionButton btnSettings = findViewById(R.id.btnSettings);
        tvScore = findViewById(R.id.textViewScore);
        tvHighScore = findViewById(R.id.textViewHighScore);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);




        // 1) Mod ve zorluk bilgilerini oku
        String mode = getIntent().getStringExtra("mode");
        vsBot       = "BOT".equals(mode);
        difficulty = getIntent().getStringExtra("difficulty"); // PVP modunda null döner
        difficultyLevel = getIntent().getIntExtra("difficulty_level", 0);


        // 2) 3×3 düğmeleri bağla ve tıklama mantığını ekle
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button = (Button) gridLayout.getChildAt(index++);
                buttons[i][j] = button;
                button.setOnClickListener(v -> {
                    if (!button.getText().toString().isEmpty() || gameOver) return;
                    moveCount++;
                    // Only set the symbol for current player
                    if (playerXTurn) {
                        button.setText("X");
                        textViewStatus.setText(getString(R.string.status_o_turn));
                    } else {
                        button.setText("O");
                        textViewStatus.setText(getString(R.string.status_x_turn));
                    }
                    playerXTurn = !playerXTurn;

                    checkWinner();

                    if (vsBot && !playerXTurn && moveCount < 9) {
                        handler.postDelayed(this::botMove, 500);
                    }
                    if (moveCount == 9 && !gameOver) {
                        textViewStatus.setText(getString(R.string.msg_draw));
                        gameOver = true;
                        disableButtons();
                    }
                });
            }
        }
        // 3) Reset butonu
        btnReset.setOnClickListener(v -> resetGame());

        // 4) Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        btnSettings.setOnClickListener(v -> {
            // Mevcut dili oku
            String current = getResources().getConfiguration()
                    .getLocales().get(0).getLanguage();
            // Geçilecek dil
            String next = current.equals("tr") ? "en" : "tr";

            // Locale’i değiştir
            setLocale(next);
        });

        updateScoreDisplay();
    }

    /** BOT HAMLESİ **/
    private void botMove() {
        if (gameOver) return;

        // 1) Boş hücreleri topla
        List<int[]> empties = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (buttons[i][j].getText().toString().isEmpty())
                    empties.add(new int[]{i, j});
        if (empties.isEmpty()) return;

        // 2) Hard + internet var mı?
        if (difficultyLevel == 2 && isNetworkAvailable()) {
            requestBotMoveFromAPI();
            return;  // asenkron, callback’te applyBotMove çağrılır
        }

        // 3) Yerel seçim (Easy / Medium / Hard-offline)
        int[] choice;
        if (difficultyLevel == 1) {
            choice = mediumMove(empties);
        } else if (difficultyLevel == 2) {
            choice = minimaxMove();
        } else {
            choice = easyMove(empties);
        }

        applyBotMove(choice);
    }

    // Kolay: tamamen rastgele
    private int[] easyMove(List<int[]> empties) {
        return empties.get(new Random().nextInt(empties.size()));
    }

    // Orta: önce kazanabilecekse kazan, yoksa X'in kazanmasını block et, yoksa rastgele
    private int[] mediumMove(List<int[]> empties) {
        // 1) O kazanırsa
        for (int[] c : empties) {
            buttons[c[0]][c[1]].setText("O");
            if (isWinning("O")) {
                buttons[c[0]][c[1]].setText("");
                return c;
            }
            buttons[c[0]][c[1]].setText("");
        }
        // 2) X kazanacaksa block et
        for (int[] c : empties) {
            buttons[c[0]][c[1]].setText("X");
            if (isWinning("X")) {
                buttons[c[0]][c[1]].setText("");
                return c;
            }
            buttons[c[0]][c[1]].setText("");
        }
        // 3) Değilse rastgele
        return easyMove(empties);
    }

    // Zor: Minimax ile ideal hamle
    private int[] minimaxMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] move = null;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().isEmpty()) {
                    buttons[i][j].setText("O");
                    int score = minimax(0, false);
                    buttons[i][j].setText("");
                    if (score > bestScore) {
                        bestScore = score;
                        move = new int[]{i, j};
                    }
                }
            }
        }
        return move;
    }

    private int minimax(int depth, boolean isMaximizing) {
        if (isWinning("O")) return 10 - depth;
        if (isWinning("X")) return depth - 10;
        if (moveCount + depth == 9) return 0;

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    if (buttons[i][j].getText().toString().isEmpty()) {
                        buttons[i][j].setText("O");
                        best = Math.max(best, minimax(depth + 1, false));
                        buttons[i][j].setText("");
                    }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    if (buttons[i][j].getText().toString().isEmpty()) {
                        buttons[i][j].setText("X");
                        best = Math.min(best, minimax(depth + 1, true));
                        buttons[i][j].setText("");
                    }
            return best;
        }
    }

    /** YARDIMCI: Belirli bir oyuncunun kazanıp kazanmadığını kontrol eder **/
    private boolean isWinning(String player) {
        // Satırlar
        for (int i = 0; i < 3; i++)
            if (buttons[i][0].getText().toString().equals(player) &&
                    buttons[i][1].getText().toString().equals(player) &&
                    buttons[i][2].getText().toString().equals(player))
                return true;
        // Sütunlar
        for (int i = 0; i < 3; i++)
            if (buttons[0][i].getText().toString().equals(player) &&
                    buttons[1][i].getText().toString().equals(player) &&
                    buttons[2][i].getText().toString().equals(player))
                return true;
        // Çaprazlar
        if (buttons[0][0].getText().toString().equals(player) &&
                buttons[1][1].getText().toString().equals(player) &&
                buttons[2][2].getText().toString().equals(player))
            return true;
        if (buttons[0][2].getText().toString().equals(player) &&
                buttons[1][1].getText().toString().equals(player) &&
                buttons[2][0].getText().toString().equals(player))
            return true;
        return false;
    }

    /** MEVCUT OLANLAR **/
    private void checkWinner() {
        // (aynı isimdeki eski metodunuz; kazananı tespit edip announceWinner çağırır)
        for (int i = 0; i < 3; i++) {
            if (checkThree(buttons[i][0], buttons[i][1], buttons[i][2])) {
                announceWinner(buttons[i][0].getText().toString(),
                        buttons[i][0], buttons[i][1], buttons[i][2]);
                return;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (checkThree(buttons[0][i], buttons[1][i], buttons[2][i])) {
                announceWinner(buttons[0][i].getText().toString(),
                        buttons[0][i], buttons[1][i], buttons[2][i]);
                return;
            }
        }
        if (checkThree(buttons[0][0], buttons[1][1], buttons[2][2])) {
            announceWinner(buttons[0][0].getText().toString(),
                    buttons[0][0], buttons[1][1], buttons[2][2]);
            return;
        }
        if (checkThree(buttons[0][2], buttons[1][1], buttons[2][0])) {
            announceWinner(buttons[0][2].getText().toString(),
                    buttons[0][2], buttons[1][1], buttons[2][0]);
        }
    }

    private boolean checkThree(Button b1, Button b2, Button b3) {
        return !b1.getText().toString().isEmpty()
                && b1.getText().toString().equals(b2.getText().toString())
                && b2.getText().toString().equals(b3.getText().toString());
    }

    private void announceWinner(String winner, Button b1, Button b2, Button b3) {
        textViewStatus.setText(
                getString(R.string.msg_wins, winner)
        );
        gameOver = true;
        disableButtons();
        highlightWinningButtons(b1, b2, b3);

        // sadece X (kullanıcı) kazandıysa puanla
        if (winner.equals("X")) {
            // 1) hamle skoru
            int rawMoveScore = (9 - moveCount + 1) * 100;

            // 2) zaman bonusu
            long elapsedSec = (System.currentTimeMillis() - gameStartTime) / 1000;
            int timeBonus = (int)Math.max(0, 60 - elapsedSec);

            // 3) zorluk çarpanı
            double mult;
            switch (difficultyLevel) {
                case 1:  // Medium
                    mult = 1.5;
                    break;
                case 2:  // Hard
                    mult = 2.0;
                    break;
                case 0:  // Easy
                default:
                    mult = 1.0;
            }

            // 4) toplam puan
            currentScore = (int)((rawMoveScore + timeBonus) * mult);

            // 5) high score güncelle
            if (currentScore > highScore) {
                highScore = currentScore;
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit()
                        .putInt(KEY_HIGH_SCORE, highScore)
                        .apply();
                // kullanıcıyı bilgilendir
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.toast_new_high_score))
                        .setMessage(getString(R.string.label_score) + currentScore +
                                "\n" + getString(R.string.label_high_score) + highScore)
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                // normal kazanç bildirimi
                Toast.makeText(this,
                        getString(R.string.label_score) + currentScore, Toast.LENGTH_SHORT).show();
            }
        } else {
            // kaybettiyse currentScore sıfırla
            currentScore = 0;
        }

        updateScoreDisplay();
        moveCount = 0;
    }

    private void disableButtons() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setEnabled(false);
                // Orijinal kodunuzdaki güvenli ColorDrawable kontrolü
                if (buttons[i][j].getBackground() instanceof ColorDrawable) {
                    buttons[i][j].setBackgroundColor(
                            ContextCompat.getColor(this, android.R.color.darker_gray));
                } else {
                    buttons[i][j].setBackgroundColor(
                            ContextCompat.getColor(this, android.R.color.darker_gray));
                }
            }
    }

    private void resetGame() {
        gameOver = false;
        playerXTurn = true;
        moveCount = 0;

        // her maç başında zaman sıfırla
        gameStartTime = System.currentTimeMillis();

        // skor metinlerini güncelle
        updateScoreDisplay();

        if (vsBot) {                               // (2)
            textViewStatus.setText(
                    getString(R.string.status_x_turn)
            );
        } else {
            textViewStatus.setText(
                    getString(R.string.status_o_turn)
            );
        }
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
                buttons[i][j].setBackgroundColor(
                        ContextCompat.getColor(this, R.color.purple_200));
            }
    }

    private void highlightWinningButtons(Button b1, Button b2, Button b3) {
        int highlight = ContextCompat.getColor(this, android.R.color.holo_orange_light);
        b1.setBackgroundColor(highlight);
        b2.setBackgroundColor(highlight);
        b3.setBackgroundColor(highlight);
    }

    private void updateScoreDisplay() {
        tvScore.setText(
                getString(R.string.label_score) + currentScore
        );
        tvHighScore.setText(
                getString(R.string.label_high_score) + highScore
        );
    }


    private void setLocale(String langCode) {
        // 1) Locale’i uygula
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources res = getResources();
        Configuration cfg = res.getConfiguration();
        cfg.setLocale(locale);
        res.updateConfiguration(cfg, res.getDisplayMetrics());

        // 2) Tercihi kaydet
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, langCode)
                .apply();

        // 3) Activity’yi yeniden başlat
        recreate();
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(PREFS, MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANGUAGE, Locale.getDefault().getLanguage());

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration cfg = newBase.getResources().getConfiguration();
        cfg.setLocale(locale);
        Context ctx = newBase.createConfigurationContext(cfg);
        super.attachBaseContext(ctx);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isConnected();
    }
    /** API üzerinden “O” hamlesi iste, hata veya timeout olursa fallback → Minimax **/
    private void requestBotMoveFromAPI() {
        // Mevcut board durumunu JSON’a dök
        JSONArray boardArr = new JSONArray();
        for (int i = 0; i < 3; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < 3; j++) {
                String cell = buttons[i][j].getText().toString();
                row.put(cell.isEmpty() ? " " : cell);
            }
            boardArr.put(row);
        }

        new OpenAIBot().requestNextMove(boardArr.toString(), new OpenAIBot.MoveCallback() {
            @Override
            public void onMove(int row, int col) {
                handler.post(() -> {
                    applyBotMove(new int[]{row, col});
                });
            }

            @Override
            public void onError(Exception e) {
                // API çağrısı başarısız oldu → Minimax’e dön
                handler.post(() -> {
                    int[] fallback = minimaxMove();
                    applyBotMove(fallback);
                });
            }
        });
    }

    /** Ortak: Hamleyi UI ve game state’e uygular **/
    private void applyBotMove(int[] choice) {
        if (choice == null) return;
        Button b = buttons[choice[0]][choice[1]];
        b.setText("O");
        moveCount++;
        playerXTurn = !playerXTurn;
        textViewStatus.setText(getString(R.string.status_x_turn));
        checkWinner();

        // Beraberlik kontrolü
        if (!gameOver && moveCount == 9) {
            textViewStatus.setText(getString(R.string.msg_draw));
            disableButtons();
        }
    }
}
