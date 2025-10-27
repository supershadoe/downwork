package dev.shadoe.downwork;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class SignUpFragment extends Fragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_sign_up,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Handle customer card click
        final LinearLayout customerCard = view.findViewById(R.id.customer_card);
        customerCard.setOnClickListener(v -> {
            final FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, new CustomerSignUpFormFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Handle professional card click
        final LinearLayout professionalCard = view.findViewById(R.id.professional_card);
        professionalCard.setOnClickListener(v -> {
            final FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, new ProfessionalSignUpFormFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Handle sign in prompt
        final TextView signInText = view.findViewById(R.id.sign_in_text);
        final String signInPrompt = getString(R.string.sign_in_prompt);
        final String signInButtonText = getString(R.string.sign_in_prompt_btn);
        final int signInButtonIndex = signInPrompt.indexOf(signInButtonText);
        final SpannableString str = new SpannableString(signInPrompt);
        signInText.setMovementMethod(LinkMovementMethod.getInstance());
        str.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        final FragmentManager fragmentManager = getParentFragmentManager();
                        fragmentManager
                                .beginTransaction()
                                .replace(R.id.auth_fragment_container, new SignInFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        final FragmentActivity fragmentActivity = getActivity();
                        if (fragmentActivity != null) {
                            ds.setColor(ContextCompat.getColor(fragmentActivity, R.color.black));
                        }
                    }
                },
                signInButtonIndex,
                signInButtonIndex + signInButtonText.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
        );
        signInText.setText(str);
    }
}