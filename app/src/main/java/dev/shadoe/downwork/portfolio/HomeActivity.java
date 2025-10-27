package dev.shadoe.downwork.portfolio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import dev.shadoe.downwork.Constants;
import dev.shadoe.downwork.DatabaseHelper;
import dev.shadoe.downwork.DownworkApp;
import dev.shadoe.downwork.MainActivity;
import dev.shadoe.downwork.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        View root = findViewById(R.id.home_act);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            root.setPadding(0, insets.top, 0, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        final DownworkApp app = (DownworkApp) getApplicationContext();
        final SharedPreferences prefs = app.getPrefs();
        final int userType = prefs.getInt(Constants.prefs_user_type, -1);

        Fragment fragment;
        if (userType == DatabaseHelper.USER_TYPE_CUSTOMER) {
            fragment = new CustomerHomeFragment();
        } else {
            fragment = new ProfessionalHomeFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_menu_btn) {
            final DownworkApp app = (DownworkApp) getApplicationContext();
            app.getPrefs().edit()
                    .remove(Constants.prefs_logged_in_user)
                    .remove(Constants.prefs_user_type)
                    .apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}