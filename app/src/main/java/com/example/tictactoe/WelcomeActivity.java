package com.example.tictactoe;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {
    private int selectedMode = 0;      // 0 = PvP, 1 = PvBot
    private int selectedDifficulty = 0; // 0 = Easy, 1 = Medium, 2 = Hard
    private static final String PREFS = "ticTacToePrefs";
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
        setContentView(R.layout.activity_welcome);

        TextView tv = findViewById(R.id.tvTapToStart);

        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(tv, "translationY", 0f, -50f);
        floatAnim.setDuration(1000);
        floatAnim.setRepeatMode(ValueAnimator.REVERSE);
        floatAnim.setRepeatCount(ValueAnimator.INFINITE);
        floatAnim.start();


        findViewById(R.id.rootLayout).setOnClickListener(v -> showModeDialog());
    }

    private void showModeDialog() {
        String[] modes = {
                getString(R.string.mode_pvp),
                getString(R.string.mode_pvb)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_mode)
                .setSingleChoiceItems(modes, selectedMode, (d,w)->selectedMode=w)
                .setPositiveButton(R.string.btn_next, (d, which) -> {
                    if (selectedMode == 0) {
                        // PvP başlat
                        Intent i = new Intent(this, MainActivity.class);
                        i.putExtra("mode", "PVP");
                        startActivity(i);
                        finish();
                    } else {
                        // PvBot için zorluk seçimi
                        showDifficultyDialog();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showDifficultyDialog() {
        String[] levels = {getString(R.string.level_easy), getString(R.string.level_medium), getString(R.string.level_hard)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_difficulty)
                .setSingleChoiceItems(levels, selectedDifficulty, (d, which) -> selectedDifficulty = which)
                .setPositiveButton(R.string.btn_start, (d, which) -> {
                    Intent i = new Intent(this, MainActivity.class);
                    i.putExtra("mode", "BOT");
                    //i.putExtra("difficulty", levels[selectedDifficulty]);
                    i.putExtra("difficulty_level", selectedDifficulty);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton(R.string.btn_back, (d, which) -> showModeDialog())
                .show();
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
}