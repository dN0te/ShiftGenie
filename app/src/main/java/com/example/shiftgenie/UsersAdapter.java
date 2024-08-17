package com.example.shiftgenie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

/**
 * The UsersAdapter class is a RecyclerView.Adapter that binds User data to views
 * displayed in a RecyclerView. It helps in displaying a list of users with relevant
 * details such as profile image, username, age, phone number, number of shifts, and total earnings.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> userList; // List of User objects to be displayed.
    private final OnUserClickListener onUserClickListener; // Listener for handling user click events.

    /**
     * Constructor to initialize the adapter with a list of users and a click listener.
     *
     * @param userList            A list of User objects.
     * @param onUserClickListener A listener to handle click events on a user item.
     */
    public UsersAdapter(List<User> userList, OnUserClickListener onUserClickListener) {
        this.userList = userList;
        this.onUserClickListener = onUserClickListener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new UserViewHolder that holds a View for each user item.
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a user item and create a new ViewHolder.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method updates the
     * contents of the ViewHolder to reflect the User item at the given position.
     *
     * @param holder   The ViewHolder that should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Get the User object at the specified position and bind it to the ViewHolder.
        User user = userList.get(position);
        holder.bind(user, onUserClickListener);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of User items.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * The UserViewHolder class holds references to the views for each user item, making it
     * easier to update the views when binding data.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageViewProfile; // ImageView for displaying the user's profile picture.
        private final TextView textViewUsername; // TextView for displaying the user's username.
        private final TextView textViewAge; // TextView for displaying the user's age.
        private final TextView textViewPhone; // TextView for displaying the user's phone number.
        private final TextView textViewShiftsCount; // TextView for displaying the user's total shifts count.
        private final TextView textViewTotalEarnings; // TextView for displaying the user's total earnings.

        /**
         * Constructor for initializing the UserViewHolder with references to the necessary views.
         *
         * @param itemView The item view representing a single user in the RecyclerView.
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views from the layout.
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewAge = itemView.findViewById(R.id.textViewAge);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
            textViewShiftsCount = itemView.findViewById(R.id.textViewShiftsCount);
            textViewTotalEarnings = itemView.findViewById(R.id.textViewTotalEarnings); // Initialize total earnings TextView
        }

        /**
         * Binds the User data to the views in the ViewHolder.
         *
         * @param user                 The User object containing the data to be displayed.
         * @param onUserClickListener  A listener to handle click events on the user item.
         */
        public void bind(User user, OnUserClickListener onUserClickListener) {
            // Load the user's profile image using Glide.
            Glide.with(itemView.getContext())
                    .load(user.getProfileImage())
                    .error(R.drawable.pp) // Fallback image in case of an error or if the image is missing.
                    .into(imageViewProfile);

            // Set the user's details in the respective TextViews.
            textViewUsername.setText(user.getUsername());
            textViewAge.setText("Age: " + user.getAge());
            textViewPhone.setText("Phone: " + user.getPhoneNumber());
            textViewShiftsCount.setText("Total Shifts: " + user.getShiftsCount());
            textViewTotalEarnings.setText(String.format(Locale.getDefault(), "Earnings: $%.2f", user.getTotalEarnings())); // Set total earnings

            // Set an OnClickListener on the itemView to handle click events.
            itemView.setOnClickListener(v -> onUserClickListener.onUserClick(user));
        }
    }

    /**
     * Interface for handling click events on a user item.
     */
    interface OnUserClickListener {
        void onUserClick(User user);
    }
}
