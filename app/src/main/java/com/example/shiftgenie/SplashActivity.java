package com.example.shiftgenie;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashActivity is the initial screen that is displayed when the app is launched.
 * It shows a splash screen for a set duration before navigating to the MainActivity.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created. This is where you should do all of your normal static set up:
     * create views, bind data to lists, etc. This method also provides a Bundle containing the activity's
     * previously frozen state, if there was one.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in {@link #onSaveInstanceState}. <b>Note: Otherwise it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the title bar.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set the activity to full screen.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set the layout for the splash screen.
        setContentView(R.layout.activity_splash);

        // Use a Handler to delay the transition to MainActivity by 3 seconds (3000 milliseconds).
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start MainActivity after the delay.
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                // Close the SplashActivity so that the user cannot return to it by pressing the back button.
                finish();
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }
}
