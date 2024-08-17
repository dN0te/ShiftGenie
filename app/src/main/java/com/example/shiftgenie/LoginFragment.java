package com.example.shiftgenie;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * The LoginFragment class handles user authentication in the application, allowing users to sign in
 * with their email and password or through Google Sign-In. It also manages password reset functionality
 * and additional user information collection if the user signs in with Google.
 */
public class LoginFragment extends Fragment {

    // Firebase Authentication instance to handle login and registration.
    private FirebaseAuth mAuth;
    // GoogleSignInClient for Google Sign-In functionality.
    private GoogleSignInClient mGoogleSignInClient;
    // Request code for Google Sign-In.
    private static final int RC_SIGN_IN = 9001;

    // UI components for login, including buttons, input fields, and a progress bar.
    private Button loginButton;
    private TextInputEditText emailEditText, passwordEditText;
    private ProgressBar progressBar;
    private Button forgetPasswordButton;
    private SignInButton googleSignInButton;

    // GoogleApiClient used for connecting to Google services.
    private GoogleApiClient googleApiClient;

    /**
     * Inflates the fragment's view and initializes UI components and Firebase authentication.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize UI components.
        loginButton = view.findViewById(R.id.loginButton);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        progressBar = view.findViewById(R.id.progressBar);
        forgetPasswordButton = view.findViewById(R.id.btn_forget_password);
        googleSignInButton = view.findViewById(R.id.btn_google_sign_in);

        // Initialize Firebase Authentication.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If the user is already logged in, redirect to the HomepageActivity.
        if (currentUser != null) {
            startActivity(new Intent(getActivity(), HomepageActivity.class));
            getActivity().finish();  // Close the current activity.
        }

        // Set up Google Sign-In button click listener.
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initiate the Google Sign-In process.
                registerWithGoogle();
            }
        });

        // Set up email/password login button click listener.
        loginButton.setOnClickListener(v -> loginUser());

        // Set up forget password button click listener to show a dialog.
        forgetPasswordButton.setOnClickListener(v -> showForgetPasswordDialog());

        return view;
    }

    /**
     * Initiates the Google Sign-In process.
     * Configures Google Sign-In options and initiates the sign-in intent.
     */
    private void registerWithGoogle() {
        // Configure Google Sign-In options to request the user's ID token and email.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Retrieve the web client ID from your Firebase console.
                .requestEmail()
                .build();

        // Build the GoogleApiClient with access to GoogleSignInAPI and the options above.
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Connect the GoogleApiClient to Google's servers.
        googleApiClient.connect();

        // Revoke previous access to ensure a fresh sign-in.
        GoogleSignIn.getClient(getActivity(), gso).revokeAccess().addOnCompleteListener(task -> {
            // Create a sign-in intent and start the Google Sign-In activity.
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    /**
     * Handles the result from the Google Sign-In intent and proceeds with Firebase authentication.
     *
     * @param requestCode The request code passed to startActivityForResult().
     * @param resultCode  The result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from the Google Sign-In activity.
        if (requestCode == RC_SIGN_IN) {
            // Retrieve the GoogleSignInAccount from the intent data.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // If sign-in was successful, authenticate with Firebase.
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // If sign-in failed, log the error and show a message to the user.
                Log.w("GoogleSignIn", "Google sign in failed", e);
                Toast.makeText(getActivity(), "Google Sign In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Authenticates the user with Firebase using Google account credentials.
     *
     * @param acct The GoogleSignInAccount object containing the signed-in user's information.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        // Get the authentication credential using the Google account's ID token.
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        // Sign in with the credential and listen for success or failure.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // If sign-in is successful, check if the user already exists in the database.
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserExists(user);
                        } else {
                            // If the user is null, show an error message.
                            Toast.makeText(getActivity(), "User is null. Unable to save details.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign-in fails, show an error message.
                        Toast.makeText(getActivity(), "Firebase Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Checks if the signed-in user already exists in the Firebase Realtime Database.
     * If the user does not exist, prompts the user to provide additional information.
     *
     * @param user The currently authenticated FirebaseUser.
     */
    private void checkUserExists(FirebaseUser user) {
        // Reference to the "Users" node in the Firebase Realtime Database.
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        // Check if the user already exists in the database.
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // If the user exists, start the HomepageActivity.
                    startActivity(new Intent(getActivity(), HomepageActivity.class));
                    getActivity().finish();
                } else {
                    // If the user does not exist, prompt for additional information.
                    showAdditionalInfoDialog(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If there is a database error, show a message to the user.
                Toast.makeText(getActivity(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a dialog to collect additional information from the user, such as age and phone number,
     * if they are signing in for the first time using Google Sign-In.
     *
     * @param user The currently authenticated FirebaseUser.
     */
    private void showAdditionalInfoDialog(FirebaseUser user) {
        // Create an AlertDialog to prompt for additional information.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Additional Information");

        // Inflate the dialog layout, which includes fields for age and phone number.
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_additional_info, null);
        final TextInputEditText ageEditText = view.findViewById(R.id.ageEditText);
        final TextInputEditText phoneEditText = view.findViewById(R.id.phoneEditText);
        builder.setView(view);

        // Set up the "Save" button to save the additional information provided by the user.
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Retrieve the age and phone number from the input fields.
                String age = ageEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                // Save the user details to the database.
                saveUserDetails(user, age, phone);
            }
        });

        // Set up the "Cancel" button to dismiss the dialog.
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog to the user.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Saves the additional user details (age and phone number) to the Firebase Realtime Database.
     *
     * @param user  The currently authenticated FirebaseUser.
     * @param age   The user's age.
     * @param phone The user's phone number.
     */
    private void saveUserDetails(FirebaseUser user, String age, String phone) {
        // Get the user's display name and email from the FirebaseUser object.
        String username = user.getDisplayName();
        String email = user.getEmail();
        // Reference to the "Users" node in the Firebase Realtime Database.
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        String userId = user.getUid();

        // Create a map to hold the user's details.
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("age", age);
        userMap.put("phoneNumber", phone);

        // Save the user's details to the database.
        usersRef.child(userId).setValue(userMap);

        // Start the HomepageActivity and close the current fragment.
        startActivity(new Intent(getActivity(), HomepageActivity.class));
        getActivity().finish();
    }

    /**
     * Shows a dialog allowing the user to request a password reset email.
     * The user must provide their email address.
     */
    private void showForgetPasswordDialog() {
        // Create an AlertDialog to prompt for the user's email address.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reset Password");

        // Inflate the dialog layout, which includes a field for the email address.
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_password, null);
        final TextInputLayout textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail);
        final TextInputEditText emailEditText = view.findViewById(R.id.editTextEmail);
        builder.setView(view);

        // Set up the "Reset" button to initiate the password reset process.
        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Retrieve the email address from the input field.
                String email = emailEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(email)) {
                    // If the email field is not empty, initiate the password reset process.
                    resetPassword(email);
                } else {
                    // If the email field is empty, show an error message.
                    textInputLayoutEmail.setError("Please enter your email");
                }
            }
        });

        // Set up the "Cancel" button to dismiss the dialog.
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog to the user.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Sends a password reset email to the user using Firebase Authentication.
     *
     * @param email The email address of the user requesting the password reset.
     */
    private void resetPassword(String email) {
        // Send a password reset email using Firebase Authentication.
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // If the email was sent successfully, show a success message.
                            Toast.makeText(getActivity(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sending the email failed, show an error message.
                            Toast.makeText(getActivity(), "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Authenticates the user using email and password provided by the user.
     * On successful authentication, the user is redirected to the HomepageActivity.
     */
    private void loginUser() {
        // Retrieve the email and password from the input fields.
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate the email field.
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        // Validate the password field.
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        // Show the progress bar while the login process is ongoing.
        progressBar.setVisibility(View.VISIBLE);

        // Sign in with email and password using Firebase Authentication.
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    // Hide the progress bar after the task is complete.
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // If login is successful, start the HomepageActivity.
                        startActivity(new Intent(getActivity(), HomepageActivity.class));
                        getActivity().finish();
                    } else {
                        // If login fails, show an error message.
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
