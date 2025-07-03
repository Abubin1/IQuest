package com.proj.quest.ui.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.proj.quest.R;

public class RiddlePreviewActivity extends AppCompatActivity {
    private void setThemeBackground(String themeUrl) {
        final RelativeLayout rootLayout = findViewById(R.id.riddlePreviewRoot);
        if (themeUrl != null && !themeUrl.isEmpty()) {
            Glide.with(this)
                .load(themeUrl)
                .placeholder(R.color.white)
                .error(R.color.white)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    @Override
                    public void onResourceReady(android.graphics.drawable.Drawable resource, com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                        rootLayout.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                        rootLayout.setBackground(placeholder);
                    }
                    @Override
                    public void onLoadFailed(@Nullable android.graphics.drawable.Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        rootLayout.setBackground(errorDrawable);
                    }
                });
        } else {
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.windowBackground, outValue, true);
            rootLayout.setBackgroundResource(outValue.resourceId);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riddle_preview);

        String themeUriStr = getIntent().getStringExtra("EVENT_THEME_URI");
        if (themeUriStr != null) {
            final RelativeLayout rootLayout = findViewById(R.id.riddlePreviewRoot);
            Glide.with(this)
                .load(android.net.Uri.parse(themeUriStr))
                .placeholder(R.color.white)
                .error(R.color.white)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    @Override
                    public void onResourceReady(android.graphics.drawable.Drawable resource, com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                        rootLayout.setBackground(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                        rootLayout.setBackground(placeholder);
                    }
                    @Override
                    public void onLoadFailed(@Nullable android.graphics.drawable.Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        rootLayout.setBackground(errorDrawable);
                    }
                });
        } else {
            String themeUrl = getIntent().getStringExtra("EVENT_THEME_URL");
            setThemeBackground(themeUrl);
        }

        TextView tvRiddleNumber = findViewById(R.id.tvRiddleNumber);
        TextView tvQuestion = findViewById(R.id.tvQuestion);
        Button btnClosePreview = findViewById(R.id.btnClosePreview);

        String riddleText = getIntent().getStringExtra("RIDDLE_TEXT");
        tvRiddleNumber.setText("Загадка 1");
        tvQuestion.setText(riddleText != null ? riddleText : "");

        btnClosePreview.setOnClickListener(v -> finish());
    }
} 