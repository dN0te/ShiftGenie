package com.example.shiftgenie;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * UsersFragment is a fragment that displays a list of users in a RecyclerView.
 * It fetches user data from Firebase Realtime Database, including shifts count and total earnings,
 * and displays it in a list format. Users can click on a user item to view detailed information
 * about the selected user in a new activity.
 */
public class UsersFragment extends Fragment {

    private RecyclerView recyclerViewUsers; // RecyclerView for displaying the list of users.
    private UsersAdapter usersAdapter; // Adapter for managing the user data in the RecyclerView.
    private List<User> userList; // List to store the users fetched from the database.

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
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // Initialize the RecyclerView and set its layout manager.
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the user list and adapter.
        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userList, user -> {
            // Handle user item clicks to navigate to UserDetailsActivity.
            Intent intent = new Intent(getContext(), UserDetailsActivity.class);
            intent.putExtra("userId", user.getId());
            startActivity(intent);
        });
        recyclerViewUsers.setAdapter(usersAdapter);

        // Fetch the list of users from the database.
        fetchUsers();

        return view;
    }

    /**
     * Fetches the list of users from the Firebase Realtime Database.
     * Also, it fetches the shifts count and total earnings for each user.
     */
    private void fetchUsers() {
        // Reference to the "Users" node in the Firebase Realtime Database.
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the current user list.
                userList.clear();
                // Iterate over all users in the snapshot.
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Retrieve the User object from the snapshot.
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(userSnapshot.getKey());
                        // Fetch the shifts count and total earnings for the user.
                        fetchUserShiftsCount(user);
                        // Add the user to the list.
                        userList.add(user);
                    }
                }
                // Notify the adapter that the data has changed so the RecyclerView can be updated.
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show an error message if fetching users fails.
                Toast.makeText(getContext(), "Failed to fetch users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches the number of shifts and total earnings for a specific user from the Firebase Realtime Database.
     *
     * @param user The user for whom the shifts count and total earnings are being fetched.
     */
    private void fetchUserShiftsCount(User user) {
        // Reference to the "Shifts" node in the Firebase Realtime Database.
        DatabaseReference shiftsRef = FirebaseDatabase.getInstance().getReference("Shifts");
        shiftsRef.orderByChild("userEmail").equalTo(user.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Count the number of shifts and calculate the total earnings.
                long shiftsCount = snapshot.getChildrenCount();
                double totalEarnings = 0.0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Shift shift = dataSnapshot.getValue(Shift.class);
                    if (shift != null) {
                        totalEarnings += shift.getTotalEarnings();
                    }
                }
                // Set the shifts count and total earnings for the user.
                user.setShiftsCount((int) shiftsCount);
                user.setTotalEarnings(totalEarnings);
                // Notify the adapter of data change.
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show an error message if fetching shifts count and earnings fails.
                Toast.makeText(getContext(), "Failed to fetch shifts count and earnings for user: " + user.getUsername(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
