package com.example.shiftgenie;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * MainActivity serves as the entry point of the application.
 * It allows users to navigate between the Login and Signup fragments.
 */
public class MainActivity extends AppCompatActivity {

    // UI components for login and signup buttons.
    private Button loginButton, signupButton;

    /**
     * Called when the activity is first created. This is where you should do all of your 
     * normal static set up: create views, bind data to lists, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously 
     *                           being shut down then this Bundle contains the data it most 
     *                           recently supplied in onSaveInstanceState(Bundle). 
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Remove the title bar and make the activity fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Set the layout for this activity.
        setContentView(R.layout.activity_main);

        // Initialize buttons for login and signup.
        loginButton = findViewById(R.id.button_login);
        signupButton = findViewById(R.id.button_signup);

        // Set click listener for the login button to load the LoginFragment.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the LoginFragment and update button styles.
                loadFragment(new LoginFragment());
                setButtonStyles(true);
            }
        });

        // Set click listener for the signup button to load the SignupFragment.
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the SignupFragment and update button styles.
                loadFragment(new SignupFragment());
                setButtonStyles(false);
            }
        });

        // Load the LoginFragment by default when the activity is first created.
        if (savedInstanceState == null) {
            loginButton.performClick(); // Simulate a click on the login button.
        }
    }

    /**
     * Loads the specified fragment into the fragment container.
     *
     * @param fragment The fragment to be loaded into the container.
     */
    private void loadFragment(Fragment fragment) {
        // Begin a fragment transaction.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // Set custom animations for the fragment transition.
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        
        // Replace the current fragment with the specified fragment.
        transaction.replace(R.id.fragment_container, fragment);
        
        // Optional: Add the transaction to the back stack so the user can navigate back.
        transaction.addToBackStack(null);
        
        // Commit the transaction to apply the changes.
        transaction.commit();
    }

    /**
     * Updates the styles of the login and signup buttons based on the selected state.
     *
     * @param isLoginSelected True if the login button is selected, false if the signup button is selected.
     */
    private void setButtonStyles(boolean isLoginSelected) {
        if (isLoginSelected) {
            // Set styles for the login button when it is selected.
            loginButton.setBackgroundResource(R.drawable.back);
            loginButton.setTextColor(getResources().getColor(android.R.color.white));
            
            // Set default styles for the signup button when it is not selected.
            signupButton.setBackgroundResource(R.drawable.btn_bg);
            signupButton.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            // Set styles for the signup button when it is selected.
            signupButton.setBackgroundResource(R.drawable.back);
            signupButton.setTextColor(getResources().getColor(android.R.color.white));
            
            // Set default styles for the login button when it is not selected.
            loginButton.setBackgroundResource(R.drawable.btn_bg);
            loginButton.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
}
