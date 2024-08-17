package com.example.shiftgenie;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * The SignupFragment class handles the user registration process, including input validation, 
 * user agreement display, and saving user details to Firebase Authentication and Firebase Realtime Database.
 */
public class SignupFragment extends Fragment {

    private EditText usernameEditText; // Input field for the user's username.
    private EditText emailEditText; // Input field for the user's email.
    private EditText passwordEditText; // Input field for the user's password.
    private EditText ageEditText; // Input field for the user's age.
    private EditText phoneNumberEditText; // Input field for the user's phone number.
    private ProgressBar progressBar; // Progress bar to indicate the signup process is ongoing.
    private FirebaseAuth mAuth; // Firebase Authentication instance for user registration.

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Initialize UI components.
        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        ageEditText = view.findViewById(R.id.ageEditText);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText);
        // Button to trigger the signup process.
        Button signupButton = view.findViewById(R.id.signupButton);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize Firebase Authentication.
        mAuth = FirebaseAuth.getInstance();

        // Set an onClickListener for the signup button to trigger the registration process.
        signupButton.setOnClickListener(v -> registerWithEmailPassword());

        return view;
    }

    /**
     * Displays a user agreement dialog with options to agree or cancel.
     */
    private void showUserAgreementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("User Agreement");
        builder.setMessage("By clicking 'I Agree', you agree to the terms and conditions of this application. The owners of the application do not take responsibility for the data collected.");

        // If the user agrees, dismiss the dialog.
        builder.setPositiveButton("I Agree", (dialog, which) -> {
            dialog.dismiss();
        });

        // If the user cancels, also dismiss the dialog.
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Handles the user registration process using email and password.
     * Validates input fields, shows progress, and manages success or failure scenarios.
     */
    private void registerWithEmailPassword() {
        // Retrieve user input from the EditText fields.
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        // Validate input fields.
        if (username.isEmpty()) {
            showToast("Please enter a username.");
            return;
        }
        if (email.isEmpty()) {
            showToast("Please enter an email.");
            return;
        }
        if (password.isEmpty()) {
            showToast("Please enter a password.");
            return;
        }
        if (age.isEmpty()) {
            showToast("Please enter your age.");
            return;
        }
        if (phoneNumber.isEmpty()) {
            showToast("Please enter your phone number.");
            return;
        }

        // Show the progress bar while the registration is in progress.
        progressBar.setVisibility(View.VISIBLE);

        // Create a new user with email and password using Firebase Authentication.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    // Hide the progress bar once the registration is complete.
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Clear text fields after successful registration.
                        usernameEditText.setText("");
                        emailEditText.setText("");
                        passwordEditText.setText("");
                        ageEditText.setText("");
                        phoneNumberEditText.setText("");

                        // Show success message.
                        showToast("Registration successful.");

                        // Get the current Firebase user.
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save user details in the Firebase Realtime Database.
                        saveUserDetails(username, email, age, phoneNumber);

                        // Redirect the user to the HomepageActivity after successful registration.
                        startActivity(new Intent(getActivity(), HomepageActivity.class));
                        getActivity().finish();
                    } else {
                        // Handle registration failures.
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            // Email already exists.
                            showToast("Email already exists.");
                        } else {
                            // Other authentication failures.
                            showToast("Authentication failed.");
                        }
                    }
                });
    }

    /**
     * Saves the user's details in the Firebase Realtime Database under the "Users" node.
     *
     * @param username    The user's username.
     * @param email       The user's email.
     * @param age         The user's age.
     * @param phoneNumber The user's phone number.
     */
    private void saveUserDetails(String username, String email, String age, String phoneNumber) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (getContext() == null || user == null) {
            // Fragment is not attached to an activity or the user is null.
            return;
        }

        // Reference to the "Users" node in Firebase Realtime Database.
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        String userId = user.getUid();

        // Create a map to hold the user's details.
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("age", age);
        userMap.put("phoneNumber", phoneNumber);

        // Save the user's details in the database.
        usersRef.child(userId).setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Data successfully saved to the database.
                    showToast("User details saved.");
                })
                .addOnFailureListener(e -> {
                    // Failed to save data.
                    showToast("Failed to save user details.");
                });
    }

    /**
     * Displays a toast message to the user.
     *
     * @param message The message to be displayed.
     */
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
