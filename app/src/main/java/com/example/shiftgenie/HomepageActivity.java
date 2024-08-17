package com.example.shiftgenie;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * The HomepageActivity class represents the main activity for the application's homepage. 
 * This activity handles the navigation between different fragments such as Workplace, 
 * Shift Manager, Monthly Report, Profile, and Users.
 */
public class HomepageActivity extends AppCompatActivity {

    private FrameLayout fragmentContainer;
    private ImageButton btnHome, btnShiftManager, btnMonthlyReport, btnProfile, btnUsers;

    /**
     * Called when the activity is first created. This is where you should do all of your 
     * normal static set up: create views, bind data to lists, etc. This method also 
     * initializes the fragment container and the navigation buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously 
     *                           being shut down then this Bundle contains the data it most 
     *                           recently supplied in onSaveInstanceState(Bundle). 
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_homepage);

        fragmentContainer = findViewById(R.id.fragment_container);
        btnHome = findViewById(R.id.btn_home);
        btnShiftManager = findViewById(R.id.btn_shift_manager);
        btnMonthlyReport = findViewById(R.id.btn_monthly_report);
        btnProfile = findViewById(R.id.btn_profile);
        btnUsers = findViewById(R.id.btn_users);

        // Set default fragment
        loadFragment(new WorkplaceFragment());
        updateButtonColors(btnHome);

        // Set onClick listeners for navigation buttons
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new WorkplaceFragment());
                updateButtonColors(btnHome);
            }
        });

        btnShiftManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new ShiftFragment());
                updateButtonColors(btnShiftManager);
            }
        });

        btnMonthlyReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new MonthlyReportFragment());
                updateButtonColors(btnMonthlyReport);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new ProfileFragment());
                updateButtonColors(btnProfile);
            }
        });

        btnUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new UsersFragment());
                updateButtonColors(btnUsers);
            }
        });
    }

    /**
     * Loads the specified fragment into the fragment container.
     *
     * @param fragment The fragment to be loaded into the container.
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Updates the color of the navigation buttons. The selected button will be highlighted 
     * while the others will be set to a default color.
     *
     * @param selectedButton The button that was selected.
     */
    private void updateButtonColors(ImageButton selectedButton) {
        btnHome.setColorFilter(getResources().getColor(R.color.dark_gray));
        btnShiftManager.setColorFilter(getResources().getColor(R.color.dark_gray));
        btnMonthlyReport.setColorFilter(getResources().getColor(R.color.dark_gray));
        btnProfile.setColorFilter(getResources().getColor(R.color.dark_gray));
        btnUsers.setColorFilter(getResources().getColor(R.color.dark_gray));

        selectedButton.setColorFilter(getResources().getColor(R.color.original_color));
    }
}
