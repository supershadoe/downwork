package dev.shadoe.downwork;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PortfolioActivity extends AppCompatActivity {
    private TextView aboutText;
    private EditText aboutEditText;
    private LinearLayout skillsContainer, servicesContainer, editButtons;
    private Button editBtn, saveBtn, cancelBtn;
    private DatabaseHelper dbHelper;
    private int profileUid;
    private int currentUserId;
    private boolean isOwnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        Toolbar toolbar = findViewById(R.id.portfolio_toolbar);
        setSupportActionBar(toolbar);
        View root = findViewById(R.id.portfolio_act);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            root.setPadding(0, insets.top, 0, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        final DownworkApp app = (DownworkApp) getApplicationContext();
        dbHelper = app.getDbHelper();

        final SharedPreferences prefs = app.getPrefs();
        currentUserId = prefs.getInt(Constants.prefs_logged_in_user, -1);

        profileUid = getIntent().getIntExtra("uid", -1);
        isOwnProfile = (profileUid == currentUserId);

        aboutText = findViewById(R.id.portfolio_about);
        aboutEditText = findViewById(R.id.portfolio_about_edit);
        skillsContainer = findViewById(R.id.portfolio_skills_container);
        servicesContainer = findViewById(R.id.portfolio_services_container);
        editButtons = findViewById(R.id.edit_buttons);
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

        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(prof.username);
        actionBar.setSubtitle(String.format(Locale.getDefault(), "★ %d/5", prof.rating));

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
        if (skills.isEmpty()) {
            final TextView emptyView = new TextView(this);
            emptyView.setText("No skills listed");
            emptyView.setTextColor(0xFF757575);
            emptyView.setPadding(16, 16, 16, 16);
            skillsContainer.addView(emptyView);
        } else {
            for (String skill : skills) {
                final TextView skillView = new TextView(this);
                skillView.setText("• " + skill);
                skillView.setTextSize(16);
                skillView.setTextColor(0xFF424242);
                skillView.setPadding(16, 12, 16, 12);
                skillsContainer.addView(skillView);
            }
        }

        // Load services
        servicesContainer.removeAllViews();
        final List<DatabaseHelper.Service> services = dbHelper.getServices(profileUid);
        if (services.isEmpty()) {
            final TextView emptyView = new TextView(this);
            emptyView.setText("No services listed");
            emptyView.setTextColor(0xFF757575);
            emptyView.setPadding(16, 16, 16, 16);
            servicesContainer.addView(emptyView);
        } else {
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
    }

    private void enterEditMode() {
        aboutText.setVisibility(View.GONE);
        aboutEditText.setVisibility(View.VISIBLE);
        editButtons.setVisibility(View.VISIBLE);
        editBtn.setVisibility(View.GONE);
    }

    private void exitEditMode() {
        aboutText.setVisibility(View.VISIBLE);
        aboutEditText.setVisibility(View.GONE);
        editButtons.setVisibility(View.GONE);
        editBtn.setVisibility(View.VISIBLE);
    }

    private void savePortfolio() {
        final String about = aboutEditText.getText().toString();
        dbHelper.updatePortfolio(profileUid, about);
        aboutText.setText(about.isEmpty() ? "No bio added yet" : about);
        exitEditMode();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}