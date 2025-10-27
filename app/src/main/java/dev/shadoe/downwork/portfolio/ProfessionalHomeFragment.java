package dev.shadoe.downwork.portfolio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.List;
import java.util.Locale;

import dev.shadoe.downwork.Constants;
import dev.shadoe.downwork.DatabaseHelper;
import dev.shadoe.downwork.DownworkApp;
import dev.shadoe.downwork.R;

public class ProfessionalHomeFragment extends Fragment {
    private LinearLayout servicesContainer;
    private DatabaseHelper dbHelper;
    private TextView homeTitle;
    private int currentUserId;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_professional_home,
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

        servicesContainer = view.findViewById(R.id.services_container);

        final Button addServiceBtn = view.findViewById(R.id.add_service_btn);
        addServiceBtn.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_fragment_container, new AddServiceFragment())
                    .addToBackStack(null)
                    .commit();
        });

        final Button portfolioBtn = view.findViewById(R.id.portfolio_btn);
        portfolioBtn.setOnClickListener(v -> {
            final Intent intent = new Intent(activity, PortfolioActivity.class);
            intent.putExtra("uid", currentUserId);
            startActivity(intent);
        });

        homeTitle = view.findViewById(R.id.home_title);
        setUserGreeting(currentUserId);
        loadServices();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadServices();
    }

    private void setUserGreeting(int currentUserId) {
        final List<DatabaseHelper.Professional> allProfs = dbHelper.getAllProfessionals();
        DatabaseHelper.Professional prof = null;
        for (DatabaseHelper.Professional p : allProfs) {
            if (p.uid == currentUserId) {
                prof = p;
                break;
            }
        }
        assert prof != null;
        homeTitle.setText("Hi, " + prof.username + "!");
    }

    private void loadServices() {
        servicesContainer.removeAllViews();
        final List<DatabaseHelper.Service> services = dbHelper.getServices(currentUserId);

        if (services.isEmpty()) {
            final TextView emptyText = new TextView(getContext());
            emptyText.setText(R.string.no_services_yet);
            emptyText.setTextSize(16);
            emptyText.setPadding(0, 32, 0, 32);
            servicesContainer.addView(emptyText);
            return;
        }

        for (DatabaseHelper.Service service : services) {
            final View serviceCard = createServiceCard(service);
            servicesContainer.addView(serviceCard);
        }
    }

    private View createServiceCard(DatabaseHelper.Service service) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View card = inflater.inflate(R.layout.item_service, servicesContainer, false);

        final TextView nameText = card.findViewById(R.id.service_name);
        final TextView descText = card.findViewById(R.id.service_desc);
        final TextView rateText = card.findViewById(R.id.service_rate);

        nameText.setText(service.name);
        descText.setText(service.description);
        rateText.setText(String.format(Locale.getDefault(), "$%.2f/hr", service.rate));

        return card;
    }
}