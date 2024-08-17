package com.example.shiftgenie;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The MonthlyReportFragment class handles the display and management of monthly shift reports.
 * It allows users to view their shifts, total earnings, and hours worked within a selected month.
 */
public class MonthlyReportFragment extends Fragment {

    // UI components for displaying month name, total earnings, and total hours worked.
    private TextView monthNameTextView;
    private TextView totalMonthlyEarningsTextView;
    private TextView totalMonthlyHoursTextView;
    private GridView calendarGridView;  // GridView to display a monthly calendar.
    private CalendarAdapter calendarAdapter;  // Adapter for the GridView.
    private RecyclerView shiftsRecyclerView;  // RecyclerView to display shifts for a selected date.
    private ShiftsAdapter shiftsAdapter;  // Adapter for the RecyclerView.

    // Calendar to track the current month and manage date changes.
    private Calendar currentCalendar;
    // Formatter to display the month in a "MMMM yyyy" format.
    private SimpleDateFormat monthFormat;
    // Database reference to the "Shifts" node in Firebase Realtime Database.
    private DatabaseReference databaseShifts;
    // Firebase authentication instance to get the current user.
    private FirebaseAuth auth;

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
        View view = inflater.inflate(R.layout.fragment_monthly_report, container, false);

        // Initialize UI components.
        monthNameTextView = view.findViewById(R.id.monthNameTextView);
        totalMonthlyEarningsTextView = view.findViewById(R.id.totalMonthlyEarningsTextView);
        totalMonthlyHoursTextView = view.findViewById(R.id.totalMonthlyHoursTextView);
        calendarGridView = view.findViewById(R.id.calendarGridView);
        shiftsRecyclerView = view.findViewById(R.id.shiftsRecyclerView);

        // Set up the RecyclerView with a LinearLayoutManager and the ShiftsAdapter.
        shiftsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        shiftsAdapter = new ShiftsAdapter(new ArrayList<>());
        shiftsRecyclerView.setAdapter(shiftsAdapter);

        // Buttons to navigate between months and fetch the monthly report.
        ImageView prevMonthButton = view.findViewById(R.id.prevMonthButton);
        ImageView nextMonthButton = view.findViewById(R.id.nextMonthButton);
        Button monthlyReportButton = view.findViewById(R.id.monthlyReportButton);

        // Initialize the current calendar to the current date.
        currentCalendar = Calendar.getInstance();
        monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        // Initialize Firebase authentication and database reference.
        auth = FirebaseAuth.getInstance();
        databaseShifts = FirebaseDatabase.getInstance().getReference("Shifts");

        // Initialize the calendar adapter and set it to the GridView.
        calendarAdapter = new CalendarAdapter(requireContext(), currentCalendar, this::onDateClick);
        calendarGridView.setAdapter(calendarAdapter);

        // Update the month display to show the current month.
        updateMonthDisplay();

        // Set up click listeners for the previous and next month buttons.
        prevMonthButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);  // Move to the previous month.
            updateMonthDisplay();
        });

        nextMonthButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);  // Move to the next month.
            updateMonthDisplay();
        });

        // Set up the monthly report button to fetch and display shifts for the selected month.
        monthlyReportButton.setOnClickListener(v -> {
            fetchShiftsForMonth(currentCalendar.getTime());
        });

        return view;
    }

    /**
     * Updates the month display by setting the month name and updating the calendar view.
     */
    private void updateMonthDisplay() {
        // Set the month name text based on the current calendar.
        monthNameTextView.setText(monthFormat.format(currentCalendar.getTime()));
        
        // Update the calendar view if the adapter is initialized.
        if (calendarAdapter != null) {
            calendarAdapter.updateCalendar(currentCalendar);
        }
    }

    /**
     * Handles date clicks on the calendar, fetching shifts for the selected date.
     *
     * @param date The date that was clicked.
     */
    private void onDateClick(Date date) {
        fetchShiftsForDate(date);
    }

    /**
     * Fetches shifts for a specific date from the Firebase database and updates the RecyclerView.
     *
     * @param date The date for which to fetch shifts.
     */
    private void fetchShiftsForDate(Date date) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String selectedDate = dateFormat.format(date);

            // Query the database for shifts on the selected date for the current user.
            Query shiftsQuery = databaseShifts.orderByChild("userEmail").equalTo(userEmail);
            shiftsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<Shift> shifts = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Shift shift = dataSnapshot.getValue(Shift.class);
                        // Filter shifts by the selected date.
                        if (shift != null && shift.getFromDateTime().startsWith(selectedDate)) {
                            shifts.add(shift);
                        }
                    }
                    // Update the RecyclerView with the fetched shifts.
                    shiftsAdapter.updateShifts(shifts);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors in fetching shifts.
                    Toast.makeText(requireContext(), "Failed to fetch shifts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches shifts for the entire month from the Firebase database and updates the RecyclerView.
     *
     * @param month The month for which to fetch shifts.
     */
    private void fetchShiftsForMonth(Date month) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String selectedMonthYear = monthYearFormat.format(month);

            // Query the database for shifts in the selected month for the current user.
            Query shiftsQuery = databaseShifts.orderByChild("userEmail").equalTo(userEmail);
            shiftsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<Shift> shifts = new ArrayList<>();
                    double totalEarnings = 0.0;
                    double totalHours = 0.0;

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Shift shift = dataSnapshot.getValue(Shift.class);
                        // Filter shifts by the selected month and calculate totals.
                        if (shift != null && shift.getFromDateTime().startsWith(selectedMonthYear)) {
                            shifts.add(shift);
                            totalEarnings += shift.getTotalEarnings();
                            try {
                                totalHours += calculateShiftHours(shift);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    // Update the RecyclerView with the fetched shifts and totals.
                    shiftsAdapter.updateShifts(shifts);
                    totalMonthlyEarningsTextView.setText(String.format(Locale.getDefault(), "Month Earnings: $%.2f", totalEarnings));
                    totalMonthlyHoursTextView.setText(String.format(Locale.getDefault(), "Total Hours Worked: %.2f", totalHours));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors in fetching shifts.
                    Toast.makeText(requireContext(), "Failed to fetch shifts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Calculates the total hours worked for a given shift.
     *
     * @param shift The shift for which to calculate hours worked.
     * @return The total hours worked.
     */
    private double calculateShiftHours(Shift shift) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        // Parse the start and end times of the shift.
        Date fromDateTime = dateFormat.parse(shift.getFromDateTime());
        Date toDateTime = dateFormat.parse(shift.getToDateTime());

        if (fromDateTime != null && toDateTime != null) {
            // Calculate the difference in milliseconds and convert to hours.
            long diffInMillis = toDateTime.getTime() - fromDateTime.getTime();
            return (double) diffInMillis / (1000 * 60 * 60);
        }

        return 0.0;
    }

    /**
     * RecyclerView.Adapter for displaying a list of shifts.
     */
    private static class ShiftsAdapter extends RecyclerView.Adapter<ShiftsAdapter.ShiftViewHolder> {

        private final ArrayList<Shift> shifts;

        ShiftsAdapter(ArrayList<Shift> shifts) {
            this.shifts = shifts;
        }

        /**
         * Updates the shifts list and refreshes the RecyclerView.
         *
         * @param newShifts The new list of shifts to display.
         */
        void updateShifts(ArrayList<Shift> newShifts) {
            shifts.clear();
            shifts.addAll(newShifts);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for each shift item in the list.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shift_item, parent, false);
            return new ShiftViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
            // Bind the shift data to the views in the ViewHolder.
            Shift shift = shifts.get(position);
            holder.workplaceNameTextView.setText(shift.getWorkplaceName());
            holder.fromDateTimeTextView.setText(shift.getFromDateTime());
            holder.toDateTimeTextView.setText(shift.getToDateTime());
            holder.totalEarningsTextView.setText(String.format(Locale.getDefault(), "$%.2f", shift.getTotalEarnings()));
        }

        @Override
        public int getItemCount() {
            return shifts.size();
        }

        /**
         * ViewHolder for displaying individual shift details.
         */
        static class ShiftViewHolder extends RecyclerView.ViewHolder {

            TextView workplaceNameTextView;
            TextView fromDateTimeTextView;
            TextView toDateTimeTextView;
            TextView totalEarningsTextView;

            ShiftViewHolder(@NonNull View itemView) {
                super(itemView);
                workplaceNameTextView = itemView.findViewById(R.id.workplaceNameTextView);
                fromDateTimeTextView = itemView.findViewById(R.id.fromDateTimeTextView);
                toDateTimeTextView = itemView.findViewById(R.id.toDateTimeTextView);
                totalEarningsTextView = itemView.findViewById(R.id.totalEarningsTextView);
            }
        }
    }

    /**
     * BaseAdapter for displaying a calendar grid with clickable dates.
     */
    private static class CalendarAdapter extends BaseAdapter {

        private final ArrayList<Date> dates;  // List of dates to display in the calendar grid.
        private final Calendar calendar;  // Calendar instance to track the current month.
        private final LayoutInflater inflater;  // LayoutInflater to inflate the calendar day views.
        private final OnDateClickListener onDateClickListener;  // Listener for date clicks.

        private Date selectedDate;  // The currently selected date.

        /**
         * Interface to handle date clicks.
         */
        interface OnDateClickListener {
            void onDateClick(Date date);
        }

        CalendarAdapter(Context context, Calendar calendar, OnDateClickListener listener) {
            this.calendar = (Calendar) calendar.clone();
            this.dates = new ArrayList<>();
            this.inflater = LayoutInflater.from(context);
            this.onDateClickListener = listener;

            // Set the initially selected date to today.
            selectedDate = Calendar.getInstance().getTime();
            // Populate the calendar grid with dates.
            updateCalendar(calendar);
        }

        /**
         * Updates the calendar grid with the dates of the specified month.
         *
         * @param calendar The calendar representing the current month.
         */
        void updateCalendar(Calendar calendar) {
            dates.clear();
            Calendar tempCalendar = (Calendar) calendar.clone();
            tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1;
            tempCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

            // Populate the dates list with the days of the month.
            while (dates.size() < 42) {  // Ensure there are 6 rows of 7 days.
                dates.add(tempCalendar.getTime());
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            notifyDataSetChanged();  // Refresh the GridView.
        }

        @Override
        public int getCount() {
            return dates.size();
        }

        @Override
        public Object getItem(int position) {
            return dates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                // Inflate the view for each day in the calendar grid.
                view = inflater.inflate(R.layout.calendar_day_item, parent, false);
            }

            // Get the date for the current position.
            Date date = dates.get(position);
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            int dayOfMonth = dateCalendar.get(Calendar.DAY_OF_MONTH);

            // Set the day number in the TextView.
            TextView dayTextView = view.findViewById(R.id.dayTextView);
            dayTextView.setText(String.valueOf(dayOfMonth));

            Calendar todayCalendar = Calendar.getInstance();

            // Highlight the selected date.
            if (date.equals(selectedDate)) {
                dayTextView.setBackgroundColor(Color.parseColor("#FF3B30")); // Orange color
                dayTextView.setTextColor(Color.WHITE);
            } else if (dateCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                    dateCalendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                    dateCalendar.get(Calendar.DAY_OF_MONTH) == todayCalendar.get(Calendar.DAY_OF_MONTH)) {
                // Highlight today's date.
                dayTextView.setBackgroundColor(Color.parseColor("#FF3B30")); // Orange color
                dayTextView.setTextColor(Color.WHITE);
            } else {
                // Default styling for non-selected, non-today dates.
                dayTextView.setBackgroundColor(Color.TRANSPARENT);
                dayTextView.setTextColor(Color.WHITE);
            }

            // Handle clicks on the date to select it and trigger the listener.
            view.setOnClickListener(v -> {
                selectedDate = date;
                notifyDataSetChanged();  // Refresh the calendar grid to show the selection.
                onDateClickListener.onDateClick(date);  // Notify the listener of the click.
            });

            return view;
        }
    }
}
