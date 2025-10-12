package com.example.skydiary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;

public class ResizableImageView extends AppCompatImageView {

    private static final int HANDLE_SIZE = 50; // Larger handles for better touch
    private static final int BORDER_WIDTH = 4;

    private Paint borderPaint;
    private Paint handlePaint;
    private RectF borderRect;

    private RectF topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle;
    private RectF topHandle, bottomHandle, leftHandle, rightHandle;

    private boolean isResizing = false;
    private HandleType activeHandle = HandleType.NONE;
    private float lastTouchX, lastTouchY;

    private OnResizeListener resizeListener;

    public interface OnResizeListener {
        void onResize(int newWidth, int newHeight);
    }

    private enum HandleType {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
        TOP, BOTTOM, LEFT, RIGHT, NONE
    }

    public ResizableImageView(Context context) {
        super(context);
        init();
    }

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResizableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(BORDER_WIDTH);

        handlePaint = new Paint();
        handlePaint.setColor(Color.GREEN);
        handlePaint.setStyle(Paint.Style.FILL);

        borderRect = new RectF();
        createHandles();

        // Enable touch
        setClickable(true);
        setFocusable(true);
    }

    private void createHandles() {
        topLeftHandle = new RectF();
        topRightHandle = new RectF();
        bottomLeftHandle = new RectF();
        bottomRightHandle = new RectF();
        topHandle = new RectF();
        bottomHandle = new RectF();
        leftHandle = new RectF();
        rightHandle = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isResizing) {
            // Draw border
            canvas.drawRect(borderRect, borderPaint);

            // Draw handles
            canvas.drawRect(topLeftHandle, handlePaint);
            canvas.drawRect(topRightHandle, handlePaint);
            canvas.drawRect(bottomLeftHandle, handlePaint);
            canvas.drawRect(bottomRightHandle, handlePaint);
            canvas.drawRect(topHandle, handlePaint);
            canvas.drawRect(bottomHandle, handlePaint);
            canvas.drawRect(leftHandle, handlePaint);
            canvas.drawRect(rightHandle, handlePaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateBorderAndHandles();
    }

    private void updateBorderAndHandles() {
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        // Border around the image (slightly inside to be visible)
        borderRect.set(2, 2, width - 2, height - 2);

        // Corner handles
        topLeftHandle.set(0, 0, HANDLE_SIZE, HANDLE_SIZE);
        topRightHandle.set(width - HANDLE_SIZE, 0, width, HANDLE_SIZE);
        bottomLeftHandle.set(0, height - HANDLE_SIZE, HANDLE_SIZE, height);
        bottomRightHandle.set(width - HANDLE_SIZE, height - HANDLE_SIZE, width, height);

        // Edge handles (centered on edges)
        topHandle.set(width/2 - HANDLE_SIZE/2, 0, width/2 + HANDLE_SIZE/2, HANDLE_SIZE);
        bottomHandle.set(width/2 - HANDLE_SIZE/2, height - HANDLE_SIZE, width/2 + HANDLE_SIZE/2, height);
        leftHandle.set(0, height/2 - HANDLE_SIZE/2, HANDLE_SIZE, height/2 + HANDLE_SIZE/2);
        rightHandle.set(width - HANDLE_SIZE, height/2 - HANDLE_SIZE/2, width, height/2 + HANDLE_SIZE/2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeHandle = getHandleAt(x, y);
                if (activeHandle != HandleType.NONE) {
                    isResizing = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isResizing && activeHandle != HandleType.NONE) {
                    float deltaX = x - lastTouchX;
                    float deltaY = y - lastTouchY;

                    resizeImage(deltaX, deltaY);

                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isResizing) {
                    isResizing = false;
                    activeHandle = HandleType.NONE;
                    invalidate();

                    // Notify listener of final size
                    if (resizeListener != null) {
                        resizeListener.onResize(getWidth(), getHeight());
                    }
                    return true;
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private HandleType getHandleAt(float x, float y) {
        if (topLeftHandle.contains(x, y)) return HandleType.TOP_LEFT;
        if (topRightHandle.contains(x, y)) return HandleType.TOP_RIGHT;
        if (bottomLeftHandle.contains(x, y)) return HandleType.BOTTOM_LEFT;
        if (bottomRightHandle.contains(x, y)) return HandleType.BOTTOM_RIGHT;
        if (topHandle.contains(x, y)) return HandleType.TOP;
        if (bottomHandle.contains(x, y)) return HandleType.BOTTOM;
        if (leftHandle.contains(x, y)) return HandleType.LEFT;
        if (rightHandle.contains(x, y)) return HandleType.RIGHT;
        return HandleType.NONE;
    }

    private void resizeImage(float deltaX, float deltaY) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            // Create layout params if they don't exist
            params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            setLayoutParams(params);
        }

        int currentWidth = params.width;
        int currentHeight = params.height;

        // Use measured dimensions if using WRAP_CONTENT
        if (currentWidth <= 0) currentWidth = getMeasuredWidth();
        if (currentHeight <= 0) currentHeight = getMeasuredHeight();

        // Ensure we have valid dimensions
        if (currentWidth <= 0 || currentHeight <= 0) {
            currentWidth = 300; // Default fallback
            currentHeight = 300;
        }

        int newWidth = currentWidth;
        int newHeight = currentHeight;

        switch (activeHandle) {
            case TOP_LEFT:
                newWidth = Math.max(100, (int) (currentWidth - deltaX));
                newHeight = Math.max(100, (int) (currentHeight - deltaY));
                break;
            case TOP_RIGHT:
                newWidth = Math.max(100, (int) (currentWidth + deltaX));
                newHeight = Math.max(100, (int) (currentHeight - deltaY));
                break;
            case BOTTOM_LEFT:
                newWidth = Math.max(100, (int) (currentWidth - deltaX));
                newHeight = Math.max(100, (int) (currentHeight + deltaY));
                break;
            case BOTTOM_RIGHT:
                newWidth = Math.max(100, (int) (currentWidth + deltaX));
                newHeight = Math.max(100, (int) (currentHeight + deltaY));
                break;
            case TOP:
                newHeight = Math.max(100, (int) (currentHeight - deltaY));
                break;
            case BOTTOM:
                newHeight = Math.max(100, (int) (currentHeight + deltaY));
                break;
            case LEFT:
                newWidth = Math.max(100, (int) (currentWidth - deltaX));
                break;
            case RIGHT:
                newWidth = Math.max(100, (int) (currentWidth + deltaX));
                break;
            case NONE:
                break;
        }

        // Apply constraints
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int maxSize = (int) (500 * metrics.density); // Larger max size
        int minSize = (int) (80 * metrics.density);  // Smaller min size

        newWidth = Math.min(maxSize, Math.max(minSize, newWidth));
        newHeight = Math.min(maxSize, Math.max(minSize, newHeight));

        // Set new dimensions
        params.width = newWidth;
        params.height = newHeight;
        setLayoutParams(params);

        // Force layout
        requestLayout();

        // Update handles
        updateBorderAndHandles();

        // Notify listener
        if (resizeListener != null) {
            resizeListener.onResize(newWidth, newHeight);
        }
    }

    public void setResizeListener(OnResizeListener listener) {
        this.resizeListener = listener;
    }

    public void setResizingMode(boolean resizing) {
        this.isResizing = resizing;
        invalidate();
    }

    public boolean isResizing() {
        return isResizing;
    }
}