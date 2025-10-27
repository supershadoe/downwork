package dev.shadoe.downwork;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class PortfolioActivity extends AppCompatActivity {
    private TextView usernameText, aboutText, ratingText;
    private EditText aboutEditText;
    private LinearLayout skillsContainer, servicesContainer, viewMode, editMode;
    private Button editBtn, saveBtn, cancelBtn;
    private DatabaseHelper dbHelper;
    private int profileUid;
    private int currentUserId;
    private boolean isOwnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        final DownworkApp app = (DownworkApp) getApplicationContext();
        dbHelper = app.getDbHelper();

        final SharedPreferences prefs = app.getPrefs();
        currentUserId = prefs.getInt(Constants.prefs_logged_in_user, -1);

        profileUid = getIntent().getIntExtra("uid", -1);
        isOwnProfile = (profileUid == currentUserId);

        usernameText = findViewById(R.id.portfolio_username);
        aboutText = findViewById(R.id.portfolio_about);
        ratingText = findViewById(R.id.portfolio_rating);
        aboutEditText = findViewById(R.id.portfolio_about_edit);
        skillsContainer = findViewById(R.id.portfolio_skills_container);
        servicesContainer = findViewById(R.id.portfolio_services_container);
        viewMode = findViewById(R.id.portfolio_view_mode);
        editMode = findViewById(R.id.portfolio_edit_mode);
        editBtn = findViewById(R.id.edit_portfolio_btn);
        saveBtn = findViewById(R.id.save_portfolio_btn);
        cancelBtn = findViewById(R.id.cancel_portfolio_btn);

        if (isOwnProfile) {
            editBtn.setVisibility(View.VISIBLE);
            editBtn.setOnClickListener(v -> enterEditMode());
            saveBtn.setOnClickListener(v -> savePortfolio());
            cancelBtn.setOnClickListener(v -> exitEditMode());
        } else {
            editBtn.setVisibility(View.GONE);
        }

        loadPortfolio();
    }

    private void loadPortfolio() {
        // Load user info
        final DatabaseHelper.User user = dbHelper.signInUser("temp@temp.com", "temp"); // We need a better way
        // For now, let's query directly
        final List<DatabaseHelper.Professional> allProfs = dbHelper.getAllProfessionals();
        DatabaseHelper.Professional prof = null;
        for (DatabaseHelper.Professional p : allProfs) {
            if (p.uid == profileUid) {
                prof = p;
                break;
            }
        }

        if (prof == null) {
            finish();
            return;
        }

        usernameText.setText(prof.username);
        ratingText.setText(String.format(Locale.getDefault(), "★ %d/5", prof.rating));

        final DatabaseHelper.Portfolio portfolio = dbHelper.getPortfolio(profileUid);
        if (portfolio != null && !portfolio.about.isEmpty()) {
            aboutText.setText(portfolio.about);
            aboutEditText.setText(portfolio.about);
        } else {
            aboutText.setText("No bio added yet");
            aboutEditText.setText("");
        }

        // Load skills
        skillsContainer.removeAllViews();
        final List<String> skills = dbHelper.getSkills(profileUid);
        for (String skill : skills) {
            final TextView skillView = new TextView(this);
            skillView.setText("• " + skill);
            skillView.setTextSize(16);
            skillView.setPadding(0, 8, 0, 8);
            skillsContainer.addView(skillView);
        }

        // Load services
        servicesContainer.removeAllViews();
        final List<DatabaseHelper.Service> services = dbHelper.getServices(profileUid);
        for (DatabaseHelper.Service service : services) {
            final View serviceCard = getLayoutInflater().inflate(
                    R.layout.item_service,
                    servicesContainer,
                    false
            );
            final TextView nameText = serviceCard.findViewById(R.id.service_name);
            final TextView descText = serviceCard.findViewById(R.id.service_desc);
            final TextView rateText = serviceCard.findViewById(R.id.service_rate);

            nameText.setText(service.name);
            descText.setText(service.description);
            rateText.setText(String.format(Locale.getDefault(), "$%.2f/hr", service.rate));

            servicesContainer.addView(serviceCard);
        }
    }

    private void enterEditMode() {
        viewMode.setVisibility(View.GONE);
        editMode.setVisibility(View.VISIBLE);
    }

    private void exitEditMode() {
        viewMode.setVisibility(View.VISIBLE);
        editMode.setVisibility(View.GONE);
    }

    private void savePortfolio() {
        final String about = aboutEditText.getText().toString();
        dbHelper.updatePortfolio(profileUid, about);
        aboutText.setText(about.isEmpty() ? "No bio added yet" : about);
        exitEditMode();
    }
}