package com.example.shiftgenie;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDetailsActivity is an activity that displays detailed information about a user,
 * including their profile picture, username, age, phone number, and email.
 * It also displays a list of shifts associated with the user.
 */
public class UserDetailsActivity extends AppCompatActivity {

    private ImageView imageViewProfile; // ImageView for displaying the user's profile picture.
    private TextView textViewUsername, textViewAge, textViewPhone, textViewEmail, user_email_title; // TextViews for displaying user details.
    private RecyclerView recyclerViewShifts; // RecyclerView for displaying the user's shifts.
    private ShiftsAdapter shiftsAdapter; // Adapter for managing the shift data in the RecyclerView.
    private List<Shift> shiftList; // List to store the user's shifts.

    /**
     * Called when the activity is first created. This is where the activity initializes its user interface and data.
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

        // Set the layout for the activity.
        setContentView(R.layout.activity_user_details);

        // Initialize UI components.
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewAge = findViewById(R.id.textViewAge);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewEmail = findViewById(R.id.textViewEmail);
        recyclerViewShifts = findViewById(R.id.recyclerViewShifts);
        user_email_title = findViewById(R.id.user_email_title);

        // Set up the RecyclerView with a linear layout manager and an adapter.
        recyclerViewShifts.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the list and adapter for the shifts.
        shiftList = new ArrayList<>();
        shiftsAdapter = new ShiftsAdapter(shiftList);
        recyclerViewShifts.setAdapter(shiftsAdapter);

        // Retrieve the user ID passed from the previous activity.
        String userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            // Fetch the user's details if the user ID is available.
            fetchUserDetails(userId);
        } else {
            // Show an error message if the user ID is missing.
            Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Fetches and displays the user's details from the Firebase Realtime Database.
     *
     * @param userId The unique identifier of the user.
     */
    private void fetchUserDetails(String userId) {
        // Reference to the user's data in the Firebase Realtime Database.
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the User object from the snapshot.
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    // Load the user's profile image using Glide.
                    Glide.with(UserDetailsActivity.this).load(user.getProfileImage())
                            .error(R.drawable.pp) // Fallback image in case of an error or if the image is missing.
                            .into(imageViewProfile);

                    // Display the user's details in the respective TextViews.
                    textViewUsername.setText(user.getUsername());
                    user_email_title.setText(user.getUsername());
                    textViewAge.setText(user.getAge());
                    textViewPhone.setText(user.getPhoneNumber());
                    textViewEmail.setText(user.getEmail());

                    // Fetch the user's shifts based on their email.
                    fetchUserShifts(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show an error message if fetching user details fails.
                Toast.makeText(UserDetailsActivity.this, "Failed to fetch user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches and displays the list of shifts associated with the user from the Firebase Realtime Database.
     *
     * @param userEmail The email address of the user.
     */
    private void fetchUserShifts(String userEmail) {
        // Reference to the shifts data in the Firebase Realtime Database.
        DatabaseReference shiftsRef = FirebaseDatabase.getInstance().getReference("Shifts");
        shiftsRef.orderByChild("userEmail").equalTo(userEmail).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the current list of shifts.
                shiftList.clear();
                // Iterate over all the shifts in the snapshot and add them to the list.
                for (DataSnapshot shiftSnapshot : snapshot.getChildren()) {
                    Shift shift = shiftSnapshot.getValue(Shift.class);
                    if (shift != null) {
                        shiftList.add(shift);
                    }
                }
                // Notify the adapter that the data has changed so the RecyclerView can be updated.
                shiftsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show an error message if fetching shifts fails.
                Toast.makeText(UserDetailsActivity.this, "Failed to fetch shifts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
