package com.alp2.photonote.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NoteMarkerView extends View {
    private final Paint markerPaint;
    private final Paint highlightPaint;
    private final List<MarkerPoint> markers;
    private int highlightedMarker = -1;
    private static final float MARKER_RADIUS = 20f;
    private OnMarkerTouchListener markerTouchListener;

    public interface OnMarkerTouchListener {
        void onMarkerTouch(float x, float y);
    }

    public void setOnMarkerTouchListener(OnMarkerTouchListener listener) {
        this.markerTouchListener = listener;
    }

    public static class MarkerPoint {
        public float x;
        public float y;

        public MarkerPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public NoteMarkerView(@NonNull Context context) {
        this(context, null);
    }

    public NoteMarkerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteMarkerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(Color.RED);
        markerPaint.setStyle(Paint.Style.FILL);
        markerPaint.setAlpha(180);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(4f);

        markers = new ArrayList<>();
        setClickable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (markerTouchListener != null) {
                markerTouchListener.onMarkerTouch(event.getX() / getWidth(), event.getY() / getHeight());
            }
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void addMarker(float x, float y) {
        markers.add(new MarkerPoint(x, y));
        invalidate();
    }

    public void removeMarker(int position) {
        if (position >= 0 && position < markers.size()) {
            markers.remove(position);
            if (highlightedMarker == position) {
                highlightedMarker = -1;
            } else if (highlightedMarker > position) {
                highlightedMarker--;
            }
            invalidate();
        }
    }

    public void clearMarkers() {
        markers.clear();
        highlightedMarker = -1;
        invalidate();
    }

    public void highlightMarker(int position) {
        highlightedMarker = position;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        
        for (int i = 0; i < markers.size(); i++) {
            MarkerPoint marker = markers.get(i);
            float x = marker.x * getWidth();
            float y = marker.y * getHeight();
            
            canvas.drawCircle(x, y, MARKER_RADIUS, markerPaint);
            
            if (i == highlightedMarker) {
                canvas.drawCircle(x, y, MARKER_RADIUS * 1.5f, highlightPaint);
            }
        }
    }
} 