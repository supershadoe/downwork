package dev.shadoe.downwork.portfolio;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.List;
import java.util.Locale;

import dev.shadoe.downwork.DatabaseHelper;
import dev.shadoe.downwork.DownworkApp;
import dev.shadoe.downwork.R;

public class CustomerHomeFragment extends Fragment {
    private LinearLayout professionalsContainer;
    private DatabaseHelper dbHelper;
    private EditText searchBar;
    private String currentSearchKeyword = null;

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

        professionalsContainer = view.findViewById(R.id.professionals_container);
        searchBar = view.findViewById(R.id.search_bar);

        setupSearchBar();
        loadProfessionals(null);
    }

    private void setupSearchBar() {
        // Real-time search as user types
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Called before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Called as text is changing (character by character)
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Called after text has changed - perform search here
                final String keyword = s.toString().trim();
                currentSearchKeyword = keyword.isEmpty() ? null : keyword;
                loadProfessionals(currentSearchKeyword);
            }
        });

        // Optional: Handle search action button on keyboard
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard when search button is pressed
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager)
                                getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    private void loadProfessionals(String searchKeyword) {
        professionalsContainer.removeAllViews();
        final List<DatabaseHelper.Professional> professionals;
        if (searchKeyword == null) {
            professionals = dbHelper.getAllProfessionals();
        } else {
            professionals = dbHelper.searchProfessionals(searchKeyword);
        }

        if (professionals.isEmpty()) {
            final TextView emptyText = new TextView(getContext());
            emptyText.setText(searchKeyword == null ?
                    getString(R.string.no_professionals) :
                    "No professionals found for your search");
            emptyText.setTextSize(16);
            emptyText.setPadding(0, 32, 0, 32);
            professionalsContainer.addView(emptyText);
            return;
        }

        for (DatabaseHelper.Professional prof : professionals) {
            final View profCard = createProfessionalCard(prof, searchKeyword);
            professionalsContainer.addView(profCard);
        }
    }

    private View createProfessionalCard(DatabaseHelper.Professional prof, String searchKeyword) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View card = inflater.inflate(R.layout.item_professional, professionalsContainer, false);

        final TextView nameText = card.findViewById(R.id.prof_name);
        final TextView skillsText = card.findViewById(R.id.prof_skills);
        final TextView ratingText = card.findViewById(R.id.prof_rating);
        final TextView servicesText = card.findViewById(R.id.prof_services);
        final Button viewBtn = card.findViewById(R.id.view_profile_btn);

        nameText.setText(prof.username);

        if (!prof.skills.isEmpty()) {
            skillsText.setText(String.join(", ", prof.skills));
        } else {
            skillsText.setText("No skills listed");
        }

        ratingText.setText(String.format(Locale.getDefault(), "★ %d/5", prof.rating));

        // Show matching services with rates if search keyword is present
        if (searchKeyword != null && !searchKeyword.isEmpty() && !prof.services.isEmpty()) {
            StringBuilder matchingServices = new StringBuilder();
            final String lowerKeyword = searchKeyword.toLowerCase();

            for (DatabaseHelper.Service service : prof.services) {
                if (service.name.toLowerCase().contains(lowerKeyword)) {
                    if (matchingServices.length() > 0) {
                        matchingServices.append("\n");
                    }
                    matchingServices.append(String.format(Locale.getDefault(),
                            "• %s - ₹%.2f/hr", service.name, service.rate));
                }
            }

            if (matchingServices.length() > 0) {
                servicesText.setVisibility(View.VISIBLE);
                servicesText.setText(matchingServices.toString());
            } else {
                servicesText.setVisibility(View.GONE);
            }
        } else {
            servicesText.setVisibility(View.GONE);
        }

        viewBtn.setOnClickListener(v -> {
            final Intent intent = new Intent(getActivity(), PortfolioActivity.class);
            intent.putExtra("uid", prof.uid);
            startActivity(intent);
        });

        return card;
    }
}