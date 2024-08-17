package com.example.shiftgenie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The ProfileFragment class allows users to view and update their profile information,
 * including their profile picture, name, email, mobile number, and age.
 */
public class ProfileFragment extends Fragment {

    private CircleImageView profileImage; // Circular ImageView for displaying the user's profile picture.
    private TextView userName, userMobile, userEmail, userAge, user_email_title; // TextViews for displaying user information.
    private DatabaseReference databaseReference; // Reference to the user's data in Firebase Realtime Database.
    private StorageReference storageReference; // Reference to Firebase Storage for storing profile images.
    private FirebaseAuth auth; // Firebase Authentication instance.
    private FirebaseUser currentUser; // Currently logged-in user.
    private Button logoutButton; // Button for logging out the user.

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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI components.
        profileImage = view.findViewById(R.id.profile_image);
        userName = view.findViewById(R.id.user_name);
        userMobile = view.findViewById(R.id.user_mobile);
        userEmail = view.findViewById(R.id.user_email);
        user_email_title = view.findViewById(R.id.user_email_title);
        userAge = view.findViewById(R.id.user_age);
        logoutButton = view.findViewById(R.id.log);

        // Initialize Firebase Authentication and get the current user.
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize Firebase Realtime Database reference for the current user's data.
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        // Initialize Firebase Storage reference for storing profile images.
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages");

        // Set an onClick listener for the profile image to allow the user to change it.
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open an image picker for the user to select and crop a new profile image.
                ImagePicker.Companion.with(ProfileFragment.this)
                        .crop() // Crop image (optional).
                        .compress(1024) // Compress image to less than 1 MB (optional).
                        .maxResultSize(1080, 1080) // Set max resolution (optional).
                        .start(); // Start the image picker activity.
            }
        });

        // Load user information from Firebase Realtime Database.
        loadUserInfo();

        // Set an onClick listener for the logout button to sign the user out.
        logoutButton.setOnClickListener(v -> {
            auth.signOut(); // Sign out the user from Firebase Authentication.
            // Redirect the user to the MainActivity.
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish(); // Close the current activity.
        });

        return view;
    }

    /**
     * Loads the user's information from Firebase Realtime Database and updates the UI.
     */
    private void loadUserInfo() {
        // Add a listener to retrieve user data from the database.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve user information from the database snapshot.
                    String name = snapshot.child("username").getValue(String.class);
                    String mobile = snapshot.child("phoneNumber").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String age = snapshot.child("age").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImage").getValue(String.class);

                    // Update the UI with the retrieved information.
                    userName.setText(name);
                    user_email_title.setText(name);
                    userMobile.setText(mobile);
                    userEmail.setText(email);
                    userAge.setText(age);

                    // Load the profile image using Glide if the URL is available.
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(ProfileFragment.this).load(profileImageUrl).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show an error message if the user info fails to load.
                Toast.makeText(getActivity(), "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the result from the image picker activity.
     *
     * @param requestCode The request code originally supplied to startActivityForResult().
     * @param resultCode  The result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            // Get the URI of the selected image.
            Uri uri = data.getData();
            profileImage.setImageURI(uri); // Display the selected image in the ImageView.
            try {
                uploadImageToFirebase(uri); // Upload the selected image to Firebase Storage.
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Uploads the selected profile image to Firebase Storage and updates the database.
     *
     * @param uri The URI of the selected image.
     */
    private void uploadImageToFirebase(Uri uri) throws IOException {
        // Convert the image URI to a Bitmap.
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Compress the bitmap to JPEG format.
        byte[] data = baos.toByteArray(); // Convert the bitmap to a byte array.

        // Create a reference to the profile image location in Firebase Storage.
        StorageReference profileImageRef = storageReference.child(currentUser.getUid() + ".jpg");
        // Upload the image data to Firebase Storage.
        UploadTask uploadTask = profileImageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the download URL of the uploaded image and update the user's profile image URL in the database.
            profileImageRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put("profileImage", uri1.toString());
                databaseReference.updateChildren(map); // Update the user's profile image URL in the database.
                Toast.makeText(getActivity(), "Profile image uploaded", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // Show an error message if the upload fails.
            Toast.makeText(getActivity(), "Failed to upload profile image", Toast.LENGTH_SHORT).show();
        });
    }
}
