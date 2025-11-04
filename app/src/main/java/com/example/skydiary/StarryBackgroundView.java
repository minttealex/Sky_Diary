package com.example.skydiary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class StarryBackgroundView extends View {
    private int starCount = 100;
    private float minStarSize = 0.5f;
    private float maxStarSize = 3.0f;
    private float minBrightness = 0.1f;
    private float maxBrightness = 0.9f;
    private long minTwinkleSpeed = 4000L;
    private long maxTwinkleSpeed = 8000L;
    private int refreshRate = 80;

    private int startColor = 0xFF0A0E2A;
    private int endColor = 0xFF1A1F4B;

    private final Star[] stars;
    private final Paint paint = new Paint();
    private final Paint gradientPaint = new Paint();
    private final Random random = new Random();
    private boolean starsInitialized = false;

    public StarryBackgroundView(Context context) {
        this(context, null);
    }

    public StarryBackgroundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarryBackgroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StarryBackgroundView);
            startColor = a.getColor(R.styleable.StarryBackgroundView_startColor, startColor);
            endColor = a.getColor(R.styleable.StarryBackgroundView_endColor, endColor);
            starCount = a.getInt(R.styleable.StarryBackgroundView_starCount, starCount);
            a.recycle();
        }

        stars = new Star[starCount];
        init();
    }

    private void init() {
        paint.setColor(0xFFFFFFFF);
        paint.setAntiAlias(true);

        gradientPaint.setStyle(Paint.Style.FILL);
        gradientPaint.setAntiAlias(true);

        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            LinearGradient gradient = new LinearGradient(
                    0, 0, w, h,
                    new int[]{startColor, endColor},
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP
            );
            gradientPaint.setShader(gradient);

            initializeStars(w, h);
        }
    }

    private void initializeStars(int width, int height) {
        for (int i = 0; i < starCount; i++) {
            stars[i] = new Star(
                    random.nextFloat() * width,
                    random.nextFloat() * height,
                    random.nextFloat() * (maxStarSize - minStarSize) + minStarSize,
                    (long) (random.nextFloat() * (maxTwinkleSpeed - minTwinkleSpeed) + minTwinkleSpeed)
            );
        }
        starsInitialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), gradientPaint);

        if (!starsInitialized && getWidth() > 0 && getHeight() > 0) {
            initializeStars(getWidth(), getHeight());
        }

        long currentTime = System.currentTimeMillis();

        for (Star star : stars) {
            if (star != null) {
                float progress = (currentTime % star.twinkleSpeed) / (float) star.twinkleSpeed;
                float alpha = minBrightness + (float) Math.abs(Math.sin(progress * Math.PI * 2)) * (maxBrightness - minBrightness);

                paint.setAlpha((int) (alpha * 255));
                canvas.drawCircle(star.x, star.y, star.size, paint);
            }
        }

        postInvalidateDelayed(refreshRate);
    }

    private static class Star {
        float x, y, size;
        long twinkleSpeed;

        Star(float x, float y, float size, long twinkleSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.twinkleSpeed = twinkleSpeed;
        }
    }


    public void setColors(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        // Пересоздаем градиент
        if (getWidth() > 0 && getHeight() > 0) {
            LinearGradient gradient = new LinearGradient(
                    0, 0, getWidth(), getHeight(),
                    new int[]{startColor, endColor},
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP
            );
            gradientPaint.setShader(gradient);
            invalidate();
        }
    }

    public void setBrightnessRange(float minBrightness, float maxBrightness) {
        this.minBrightness = minBrightness;
        this.maxBrightness = maxBrightness;
        invalidate();
    }

    public void setStarSizeRange(float minSize, float maxSize) {
        this.minStarSize = minSize;
        this.maxStarSize = maxSize;
        starsInitialized = false;
        invalidate();
    }

    public void setTwinkleSpeed(long minSpeed, long maxSpeed) {
        this.minTwinkleSpeed = minSpeed;
        this.maxTwinkleSpeed = maxSpeed;
        starsInitialized = false;
        invalidate();
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    public void setStarCount(int count) {
        this.starCount = count;
        starsInitialized = false;
        invalidate();
    }
}