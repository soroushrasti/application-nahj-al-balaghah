package com.soroushrasti.nahj.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.soroushrasti.nahj.R;

import java.util.ArrayList;

public class FullscreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_NoActionBar);
        // Edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_fullscreen_image);

        Intent intent = getIntent();
        ArrayList<String> items = intent.getStringArrayListExtra("ITEMS");
        int start = intent.getIntExtra("START", 0);
        if (items == null) items = new ArrayList<>();
        if (items.isEmpty()) {
            items.add("res:header_banner");
        }

        ViewPager2 pager = findViewById(R.id.fullscreen_pager);
        HeroPagerAdapter adapter = new HeroPagerAdapter(this, items); // no click listener
        pager.setAdapter(adapter);
        pager.setCurrentItem(Math.max(0, Math.min(start, items.size() - 1)), false);

        // Subtle zoom on swipe
        pager.setPageTransformer((page, position) -> {
            float abs = Math.abs(position);
            float scale = 0.95f + (1f - abs) * 0.05f;
            page.setScaleX(scale);
            page.setScaleY(scale);
        });

        ImageButton close = findViewById(R.id.btn_close);
        // Inset the close button for status bar
        ViewCompat.setOnApplyWindowInsetsListener(close, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        close.setOnClickListener(v -> finish());
    }
}
