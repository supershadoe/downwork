package dev.shadoe.downwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class SignInFragment extends Fragment {
    private EditText emailInput, passwordInput;
    private TextView errorText;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_sign_in,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;

        final DownworkApp app = (DownworkApp) activity.getApplicationContext();
        dbHelper = app.getDbHelper();

        emailInput = view.findViewById(R.id.mail_id);
        passwordInput = view.findViewById(R.id.pwd);
        errorText = view.findViewById(R.id.sign_in_error);

        final Button btn = view.findViewById(R.id.sign_in_btn);
        btn.setOnClickListener(v -> handleSignIn());
    }

    private void handleSignIn() {
        final String email = emailInput.getText().toString();
        final String password = passwordInput.getText().toString();

        if (email.trim().isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }

        final DatabaseHelper.User user = dbHelper.signInUser(email, password);

        if (user != null) {
            // Save login state
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                final DownworkApp app = (DownworkApp) activity.getApplicationContext();
                final SharedPreferences prefs = app.getPrefs();
                prefs.edit()
                        .putInt(Constants.prefs_logged_in_user, user.uid)
                        .putInt(Constants.prefs_user_type, user.userType)
                        .apply();

                // Navigate to home
                startActivity(new Intent(activity, HomeActivity.class));
                activity.finish();
            }
        } else {
            showError("Invalid email or password");
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}