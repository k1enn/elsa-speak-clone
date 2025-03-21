package com.example.elsa_speak_clone.classes;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.airbnb.lottie.LottieAnimationView;
import com.example.elsa_speak_clone.R;

public class PopupHelper {

    private PopupWindow confettiPopup;

    /**
     * Displays a congratulations popup with a confetti animation.
     *
     * @param activity        The activity where the popup is displayed.
     * @param navigationAction A callback to handle navigation after the popup is dismissed.
     */
    public void showCongratulationsPopup(Activity activity, Runnable navigationAction) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_congratulations, null);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = false;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Call the separate method to display the confetti popup ABOVE this one
        showConfettiPopup(activity);

        Button btnClose = popupView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            popupWindow.dismiss();
            if (navigationAction != null) {
                navigationAction.run();
            }

            // Ensure we dismiss the confetti if it's still visible
            if (confettiPopup != null && confettiPopup.isShowing()) {
                confettiPopup.dismiss();
            }
        });

        View rootView = activity.getWindow().getDecorView().getRootView();
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) rootView.getLayoutParams();

        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.5f;

        activity.getWindow().setAttributes(params);

        popupWindow.setOnDismissListener(() -> {
            params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            activity.getWindow().setAttributes(params);

            // Dismiss the confetti when popup is dismissed
            if (confettiPopup != null && confettiPopup.isShowing()) {
                confettiPopup.dismiss();
            }
        });
    }

    /**
     * Displays a confetti animation using a LottieAnimationView in a PopupWindow.
     *
     * @param activity The activity where the confetti animation is displayed.
     */
    private void showConfettiPopup(Activity activity) {
        FrameLayout confettiLayout = new FrameLayout(activity);
        confettiLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Create and configure the LottieAnimationView
        LottieAnimationView confettiView = new LottieAnimationView(activity);
        confettiView.setAnimation(R.raw.confetti); // Replace with your actual animation file
        confettiView.setScaleX(4.0f); // 400% bigger
        confettiView.setScaleY(4.0f);
        confettiView.setRepeatCount(0); // Play once
        confettiView.playAnimation();

        // Add the LottieAnimationView to the layout
        confettiLayout.addView(confettiView);

        // Create the PopupWindow with transparent background
        confettiPopup = new PopupWindow(
                confettiLayout,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                false);

        // Transparent background for popup
        confettiPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Show it on top of everything else
        confettiPopup.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Automatically dismiss after animation ends
        confettiView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) { }
            @Override public void onAnimationEnd(Animator animator) {
                if (confettiPopup != null && confettiPopup.isShowing()) {
                    confettiPopup.dismiss();
                }
            }
            @Override public void onAnimationCancel(Animator animator) { }
            @Override public void onAnimationRepeat(Animator animator) { }
        });
    }
}

