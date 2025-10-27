package dev.shadoe.downwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final DownworkApp app = (DownworkApp) getApplicationContext();
        final SharedPreferences prefs = app.getPrefs();
        final int loggedInUid = prefs.getInt(Constants.prefs_logged_in_user, -1);
        if (loggedInUid == -1) {
            startActivity(new Intent(this, AuthenticationActivity.class));
        } else {
            startActivity(new Intent(this, HomeActivity.class));
        }
        finish();
    }
}
