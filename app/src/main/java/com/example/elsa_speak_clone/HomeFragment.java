package com.example.elsa_speak_clone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "HomeFragment";

    private BottomNavigationView bottomNavigationView;
    private boolean isLoggedIn;
    private String username;
    private int userId;
    private int userStreak;
    private Button btnLogin;
    private CardView cvVocabulary;
    private CardView cvGrammar;
    private CardView cvPronunciation;
    private TextView tvDayStreak;
    private TextView tvXPPoint;
    private TextView tvWelcome;
    private ImageView ivPronunciation;
    private ImageView profileImage;
    private LearningAppDatabase db;
    private int user_id;
    private IconDrawable iconPronunciation;
    private IconDrawable iconProfile;
    private boolean loginCheck;
    private SessionManager sessionManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeUI(view);
        initializeVariables();

        WelcomeUsername();
        loadUserProgress();
        setupSpeechToTextButton();
        setupGrammarButton();
        setupVocabularyButton();

        return view;
    }

    private void initializeUI(View view) {
        try {
            btnLogin = view.findViewById(R.id.btnLogin);
            cvPronunciation = view.findViewById(R.id.cvPronunciation);
            cvGrammar = view.findViewById(R.id.cvGrammar);
            cvVocabulary = view.findViewById(R.id.cvVocabulary);
            tvDayStreak = view.findViewById(R.id.tvDayStreak);
            tvXPPoint = view.findViewById(R.id.tvXPPoint);
            tvWelcome = view.findViewById(R.id.tvWelcome);
            bottomNavigationView = view.findViewById(R.id.bottom_navigation);
            bottomNavigationView.setOnItemSelectedListener(navListener);

            // Set default selection
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } catch (Exception e) {
            Log.d(TAG, "initializedUI components");
        }

        try {
            ivPronunciation = view.findViewById(R.id.ivPronunciation);
            iconPronunciation = new IconDrawable(requireContext(), Iconify.IconValue.zmdi_volume_up)
                    .colorRes(R.color.real_purple)  // Set color
                    .sizeDp(48); // Set size
            iconPronunciation.setStyle(Paint.Style.FILL);
            ivPronunciation.setImageDrawable(iconPronunciation);

            profileImage = view.findViewById(R.id.profileImage);
            iconProfile = new IconDrawable(requireContext(), Iconify.IconValue.zmdi_account_circle)
                    .colorRes(R.color.gray)  // Set color
                    .sizeDp(70); // Set size
            iconProfile.setStyle(Paint.Style.FILL);
            profileImage.setImageDrawable(iconProfile);
        } catch (NullPointerException npe) {
            Log.d(TAG, "Can't find icons" + npe);
        }
    }

    private void initializeVariables() {
        try {
            db = new LearningAppDatabase(requireContext());
            db.updateUserStreak(requireContext());
            // Use SessionManager instead of direct SharedPreferences
            sessionManager = new SessionManager(requireContext());
            if (sessionManager.isLoggedIn()) {
                isLoggedIn = true;
                username = sessionManager.getUserDetails().get("username");
                userId = Integer.parseInt(sessionManager.getUserDetails().get("userId"));
            } else {
                isLoggedIn = false;
                username = null;
                userId = -1;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeVariables: ", e);
        }
    }

    private void WelcomeUsername() {
        try {
            if (userStreak <= 1) {
                tvWelcome.setText("Welcome " + username + "!");
            } else {
                tvWelcome.setText("Welcome back " + username + "!");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error on WelcomeUsername" + e.getMessage());
        }
    }


    private void loadUserProgress() {
        try {
            if (tvDayStreak.getText() == "-1" || tvXPPoint.getText() == "-1") {
                tvXPPoint.setText(String.valueOf(db.getUserXp(requireContext())));
                tvDayStreak.setText(String.valueOf(db.getUserStreak(requireContext())));
                Log.d(TAG, "Loaded new progress");
            } else {
                tvXPPoint.setText(String.valueOf(db.getUserXp(requireContext())));
                tvDayStreak.setText(String.valueOf(db.getUserStreak(requireContext())));
                Log.d(TAG, "Loaded progress");
            }


        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProgress: ", e);
            // Set default values in case of any error
            tvXPPoint.setText("0");
            tvDayStreak.setText("0");
        }
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            try {
                if (sessionManager.isLoggedIn()) {
                    sessionManager.logout();
                    navigateToLogin();
                } else {
                    navigateToLogin();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error in setupLoginButton", e);
            }
        });
    }

    private void setupSpeechToTextButton() {
        cvPronunciation.setOnClickListener(v -> {
            checkLogin();
            try {
                if (username != null) {
                    try {
                        navigateToSpeechToText();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                } else {
                    try {
                        navigateToLogin();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "SpeechToText button error: " + e);
            }
        });
    }

    private void setupGrammarButton() {
        cvGrammar.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), QuizActivity.class);
            startActivity(intent);
        });
    }

    private void setupVocabularyButton() {
        cvVocabulary.setOnClickListener(v -> {
            checkLogin();
            try {
                if (username != null) {
                    try {
                        navigateToSpeechToText();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    } finally {
                        Log.d(TAG, "Clicked vocabulary button");
                    }
                } else {
                    try {
                        navigateToLogin();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Vocabulary button error: " + e);
            }
        });
    }

    private void checkLogin() {
        try {
            if (!isLoggedIn) {
                navigateToLogin();
                Log.d(TAG, "Haven't logged in yet");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error in check login:", e);
        }
    }

    private void navigateToSpeechToText() {
        Intent intent = new Intent(requireActivity(), SpeechToText.class);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        return true;
    };
}