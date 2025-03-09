package com.example.elsa_speak_clone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageView ivProfilePicture;
    private TextView tvUsername, tvEmail, tvUserStreak, tvUserXP;
    private LearningAppDatabase db;
    private SessionManager sessionManager;
    private Button btnLogout;
    private Button btnShare;
    private Button btnSettings;
    private final String TAG = "ProfileFragment";

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);



        db = new LearningAppDatabase(requireContext());
        initializeUI(view);
        loadUserProfile();

        setupLogoutButton();
        setupSettingsButton();
        setupShareProfileButton();

        return view;
    }
    private void initializeUI(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvUserStreak = view.findViewById(R.id.tvDayStreak);
        tvUserXP = view.findViewById(R.id.tvXPPoint);
        // Initialize the button variables
        btnLogout = view.findViewById(R.id.btnLogout);
        btnShare = view.findViewById(R.id.btnShare);
        btnSettings = view.findViewById(R.id.btnSettings);
    sessionManager = new SessionManager(requireContext());
    }
    private void setupShareProfileButton() {
        btnShare.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Share button clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSettingsButton() {
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Settings button clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            try {
                sessionManager.logout();
                navigateToLogin();
            } catch (Exception e) {
                Log.d(TAG, "Logout button error", e);
            }
        });
    }

    private void navigateToLogin() {
        Activity main = null;
        try {
            main = requireActivity();
            Intent intent = new Intent(main, LoginActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Optional: finish the activity to prevent returning to it
        } catch (NullPointerException e) {
           Log.d(TAG, "Activity is NULL", e);
        } finally {
            Log.d(TAG, "Successful navigate to Login");
        }

    }
    private void loadUserProfile() {
        String username = sessionManager.getUserDetails().get("username");
        String streak = null;
        String xp = null;

        try {
            streak = String.valueOf(db.getUserStreak(requireContext()));
            xp = String.valueOf(db.getUserXp(requireContext()));
        } catch (Exception e) {
            Log.d(TAG, "Error in convert string at loadUserProfile()", e);
        }

        try {
            if (username != null) {
                tvUsername.setText(username);
                tvUserStreak.setText(streak);
                tvUserXP.setText(xp);
            } else {
                Log.d(TAG, "Username is NULL");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error when set text in loadUserProfile()");
        }
    }

}