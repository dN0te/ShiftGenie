package com.example.shiftgenie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * The ShiftsAdapter class is a RecyclerView.Adapter that binds Shift data to views
 * displayed in a RecyclerView. It helps in displaying a list of shifts with relevant
 * details such as workplace name, start and end times, and total earnings.
 */
public class ShiftsAdapter extends RecyclerView.Adapter<ShiftsAdapter.ShiftViewHolder> {

    private final List<Shift> shiftList; // List of Shift objects to be displayed.

    /**
     * Constructor to initialize the adapter with a list of shifts.
     *
     * @param shiftList A list of Shift objects.
     */
    public ShiftsAdapter(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ShiftViewHolder that holds a View for each shift item.
     */
    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a shift item and create a new ViewHolder.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shift_item, parent, false);
        return new ShiftViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method updates the
     * contents of the ViewHolder to reflect the Shift item at the given position.
     *
     * @param holder   The ViewHolder that should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        // Get the Shift object at the specified position and bind it to the ViewHolder.
        Shift shift = shiftList.get(position);
        holder.bind(shift);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of Shift items.
     */
    @Override
    public int getItemCount() {
        return shiftList.size();
    }

    /**
     * The ShiftViewHolder class holds references to the views for each shift item, making it
     * easier to update the views when binding data.
     */
    static class ShiftViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewWorkplaceName; // TextView for displaying the workplace name.
        private final TextView textViewFrom; // TextView for displaying the start date and time of the shift.
        private final TextView textViewTo; // TextView for displaying the end date and time of the shift.
        private final TextView totalEarningsTextView; // TextView for displaying the total earnings from the shift.

        /**
         * Constructor for initializing the ShiftViewHolder with references to the necessary views.
         *
         * @param itemView The item view representing a single shift in the RecyclerView.
         */
        public ShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the TextViews from the layout.
            textViewWorkplaceName = itemView.findViewById(R.id.workplaceNameTextView);
            textViewFrom = itemView.findViewById(R.id.fromDateTimeTextView);
            textViewTo = itemView.findViewById(R.id.toDateTimeTextView);
            totalEarningsTextView = itemView.findViewById(R.id.totalEarningsTextView);
        }

        /**
         * Binds the Shift data to the TextViews in the ViewHolder.
         *
         * @param shift The Shift object containing the data to be displayed.
         */
        public void bind(Shift shift) {
            // Set the text of each TextView to the corresponding data from the Shift object.
            textViewWorkplaceName.setText(shift.getWorkplaceName());
            textViewFrom.setText(shift.getFromDateTime());
            textViewTo.setText(shift.getToDateTime());
            totalEarningsTextView.setText(String.format(Locale.getDefault(), " $%.2f", shift.getTotalEarnings()));
        }
    }
}
