package com.example.googlemaps;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.AttributeSet;

public class HorizontalLinearLayoutManager extends LinearLayoutManager {

    public HorizontalLinearLayoutManager(Context context) {
        super(context, HORIZONTAL, false);
    }

    public HorizontalLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}


