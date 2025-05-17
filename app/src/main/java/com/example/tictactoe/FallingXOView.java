package com.example.tictactoe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FallingXOView extends View {
    private static final int PARTICLE_COUNT = 40;
    private List<Particle> particles;
    private Paint paint;
    private int width, height;
    private Random rnd = new Random();

    private class Particle {
        float x, y;
        float speed;
        String text;
        float textSize;
        int color;
    }

    public FallingXOView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0x66FFFFFF); // yarı saydam beyaz
        paint.setStyle(Paint.Style.FILL_AND_STROKE);   // hem içi hem kenarı dolu çiz
        paint.setStrokeWidth(3f);                      // kenar kalınlığı
        paint.setFakeBoldText(true);                   // bold yazı
        particles = new ArrayList<>();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(createParticle());
        }
    }

    private Particle createParticle() {
        Particle p = new Particle();
        p.text = rnd.nextBoolean() ? "X" : "O";
        p.textSize = rnd.nextInt(24) + 48;      // 48–72sp arası
        p.speed = rnd.nextInt(4) + 2;           // 2–6 px/frame
        p.x = rnd.nextFloat() * width;
        p.y = rnd.nextFloat() * -height;
        // Rastgele yarı saydam renk atama (alpha = 170 / ~66%)
        p.color = Color.argb(
                230,
                rnd.nextInt(256),
                rnd.nextInt(256),
                rnd.nextInt(256)
        );
        return p;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(createParticle());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Particle p : particles) {
            paint.setTextSize(p.textSize);
            paint.setColor(p.color);
            canvas.drawText(p.text, p.x, p.y, paint);

            // konumu güncelle
            p.y += p.speed;
            if (p.y > height + p.textSize) {
                // tekrar yukarıdan başlat
                p.y = -p.textSize;
                p.x = rnd.nextFloat() * width;
                p.speed = rnd.nextInt(4) + 2;
                p.text = rnd.nextBoolean() ? "X" : "O";
                p.textSize = rnd.nextInt(24) + 24;
            }
        }
        // 16ms sonra tekrar çiz -> ~60fps
        postInvalidateDelayed(16);
    }
}
