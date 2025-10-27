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

public class CustomerSignUpFormFragment extends Fragment {
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private TextView errorText;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_customer_sign_up_form,
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

        nameInput = view.findViewById(R.id.customer_name);
        emailInput = view.findViewById(R.id.customer_mail);
        passwordInput = view.findViewById(R.id.customer_pwd);
        confirmPasswordInput = view.findViewById(R.id.customer_confirm_pwd);
        errorText = view.findViewById(R.id.customer_error);

        final Button signUpBtn = view.findViewById(R.id.customer_signup_btn);
        signUpBtn.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        final String name = nameInput.getText().toString();
        final String email = emailInput.getText().toString();
        final String password = passwordInput.getText().toString();
        final String confirmPassword = confirmPasswordInput.getText().toString();

        // Validate inputs
        if (!dbHelper.isValidUsername(name)) {
            showError("Name must be at least 2 characters");
            return;
        }

        if (!dbHelper.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        if (dbHelper.emailExists(email)) {
            showError("This email is already registered");
            return;
        }

        if (!dbHelper.isValidPassword(password)) {
            showError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Sign up the user
        final int uid = dbHelper.signUpUser(
                email,
                password,
                name,
                DatabaseHelper.USER_TYPE_CUSTOMER
        );

        if (uid != -1) {
            // Save login state
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                final DownworkApp app = (DownworkApp) activity.getApplicationContext();
                final SharedPreferences prefs = app.getPrefs();
                prefs.edit()
                        .putInt(Constants.prefs_logged_in_user, uid)
                        .putInt(Constants.prefs_user_type, DatabaseHelper.USER_TYPE_CUSTOMER)
                        .apply();

                // Navigate to home
                startActivity(new Intent(activity, HomeActivity.class));
                activity.finish();
            }
        } else {
            showError("Sign up failed. Please try again.");
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}