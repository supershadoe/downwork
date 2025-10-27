package dev.shadoe.downwork;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AuthenticationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, new OnboardingFragment())
                .commit();
    }
}