package dev.shadoe.downwork;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
}