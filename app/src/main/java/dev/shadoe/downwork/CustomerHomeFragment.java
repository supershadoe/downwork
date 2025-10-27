package dev.shadoe.downwork;

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

public class CustomerHomeFragment extends Fragment {
    private LinearLayout professionalsContainer;
    private DatabaseHelper dbHelper;
    private int currentUserId;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_customer_home,
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

        professionalsContainer = view.findViewById(R.id.professionals_container);

        loadProfessionals();
    }

    private void loadProfessionals() {
        professionalsContainer.removeAllViews();
        final List<DatabaseHelper.Professional> professionals = dbHelper.getAllProfessionals();

        if (professionals.isEmpty()) {
            final TextView emptyText = new TextView(getContext());
            emptyText.setText(R.string.no_professionals);
            emptyText.setTextSize(16);
            emptyText.setPadding(0, 32, 0, 32);
            professionalsContainer.addView(emptyText);
            return;
        }

        for (DatabaseHelper.Professional prof : professionals) {
            final View profCard = createProfessionalCard(prof);
            professionalsContainer.addView(profCard);
        }
    }

    private View createProfessionalCard(DatabaseHelper.Professional prof) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View card = inflater.inflate(R.layout.item_professional, professionalsContainer, false);

        final TextView nameText = card.findViewById(R.id.prof_name);
        final TextView skillsText = card.findViewById(R.id.prof_skills);
        final TextView ratingText = card.findViewById(R.id.prof_rating);
        final Button viewBtn = card.findViewById(R.id.view_profile_btn);

        nameText.setText(prof.username);

        if (!prof.skills.isEmpty()) {
            skillsText.setText(String.join(", ", prof.skills));
        } else {
            skillsText.setText("No skills listed");
        }

        ratingText.setText(String.format(Locale.getDefault(), "★ %d/5", prof.rating));

        viewBtn.setOnClickListener(v -> {
            final Intent intent = new Intent(getActivity(), PortfolioActivity.class);
            intent.putExtra("uid", prof.uid);
            startActivity(intent);
        });

        return card;
    }
}