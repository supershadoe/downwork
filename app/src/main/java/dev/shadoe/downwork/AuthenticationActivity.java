package dev.shadoe.downwork;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class AuthenticationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Toolbar toolbar = findViewById(R.id.auth_toolbar);
        setSupportActionBar(toolbar);
        View root = findViewById(R.id.auth_act);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            root.setPadding(0, insets.top, 0, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayShowTitleEnabled(false);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, new OnboardingFragment())
                .commit();
    }
}