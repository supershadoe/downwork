package dev.shadoe.downwork.portfolio;

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

import dev.shadoe.downwork.Constants;
import dev.shadoe.downwork.DatabaseHelper;
import dev.shadoe.downwork.DownworkApp;
import dev.shadoe.downwork.R;

public class AddServiceFragment extends Fragment {
    private EditText serviceNameInput, serviceDescInput, rateInput;
    private TextView errorText;
    private DatabaseHelper dbHelper;
    private int currentUserId;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_add_service,
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

        final SharedPreferences prefs = app.getPrefs();
        currentUserId = prefs.getInt(Constants.prefs_logged_in_user, -1);

        serviceNameInput = view.findViewById(R.id.service_name_input);
        serviceDescInput = view.findViewById(R.id.service_desc_input);
        rateInput = view.findViewById(R.id.rate_input);
        errorText = view.findViewById(R.id.service_error);

        final Button saveBtn = view.findViewById(R.id.save_service_btn);
        saveBtn.setOnClickListener(v -> handleSaveService());

        final Button cancelBtn = view.findViewById(R.id.cancel_service_btn);
        cancelBtn.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void handleSaveService() {
        final String serviceName = serviceNameInput.getText().toString();
        final String serviceDesc = serviceDescInput.getText().toString();
        final String rateStr = rateInput.getText().toString();

        if (serviceName.trim().isEmpty()) {
            showError("Please enter a service name");
            return;
        }

        if (serviceDesc.trim().isEmpty()) {
            showError("Please enter a service description");
            return;
        }

        if (rateStr.trim().isEmpty()) {
            showError("Please enter a rate");
            return;
        }

        double rate;
        try {
            rate = Double.parseDouble(rateStr);
            if (rate <= 0) {
                showError("Rate must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid rate");
            return;
        }

        final boolean success = dbHelper.addService(currentUserId, serviceName, serviceDesc, rate);

        if (success) {
            getParentFragmentManager().popBackStack();
        } else {
            showError("Failed to add service. Please try again.");
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}