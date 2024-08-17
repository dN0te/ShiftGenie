package com.example.shiftgenie;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import java.util.Locale;

/**
 * The ShiftFragment class allows users to create a new shift by selecting a workplace, 
 * specifying start and end times, and calculating the total earnings based on the selected workplace's hourly wage.
 */
public class ShiftFragment extends Fragment {

    private Spinner workplaceSpinner; // Spinner for selecting a workplace.
    private EditText etFrom, etTo; // EditTexts for selecting start and end times.
    private Button btnCreate; // Button to create a new shift.
    private DatabaseReference databaseShifts, workplacesRef; // Firebase references for shifts and workplaces.
    private FirebaseAuth auth; // Firebase authentication instance.
    private ArrayList<String> workplaceList; // List of workplace names.
    private String selectedWorkplace; // Selected workplace name.
    private Calendar fromCalendar, toCalendar; // Calendars for managing date and time selection.
    private SimpleDateFormat dateTimeFormat; // Format for displaying and parsing date-time strings.
    private double hourlyWage; // Hourly wage of the selected workplace.

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_shift, container, false);

        // Initialize UI components.
        workplaceSpinner = view.findViewById(R.id.workplaceSpinner);
        etFrom = view.findViewById(R.id.etFrom);
        etTo = view.findViewById(R.id.etTo);
        btnCreate = view.findViewById(R.id.btnCreate);

        // Initialize Firebase instances.
        auth = FirebaseAuth.getInstance();
        databaseShifts = FirebaseDatabase.getInstance().getReference("Shifts");
        workplacesRef = FirebaseDatabase.getInstance().getReference("Workplace");

        workplaceList = new ArrayList<>();
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        // Load workplaces into the spinner.
        loadWorkplaces();

        // Handle selection of a workplace from the spinner.
        workplaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < workplaceList.size()) {
                    selectedWorkplace = workplaceList.get(position);
                    fetchHourlyWage(selectedWorkplace); // Fetch hourly wage for the selected workplace.
                } else {
                    selectedWorkplace = null;
                    hourlyWage = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedWorkplace = null;
                hourlyWage = 0;
            }
        });

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();

        // Show date-time picker for the "From" field.
        etFrom.setOnClickListener(v -> showDateTimePicker(etFrom, fromCalendar));

        // Show time picker for the "To" field, ensuring "From" is selected first.
        etTo.setOnClickListener(v -> {
            if (TextUtils.isEmpty(etFrom.getText().toString())) {
                Toast.makeText(requireContext(), "Please select 'From' date and time first.", Toast.LENGTH_SHORT).show();
            } else {
                showTimePicker(toCalendar);
            }
        });

        // Handle the create shift button click.
        btnCreate.setOnClickListener(v -> createShift());

        return view;
    }

    /**
     * Loads the list of workplaces from Firebase and populates the workplace spinner.
     */
    private void loadWorkplaces() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            Query userWorkplacesQuery = workplacesRef.orderByChild("userEmail").equalTo(userEmail);
            userWorkplacesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String workplaceName = dataSnapshot.child("workplaceName").getValue(String.class);
                            if (workplaceName != null) {
                                workplaceList.add(workplaceName);
                            }
                        }
                        // Set up the workplace spinner with the retrieved list of workplaces.
                        if (isAdded() && getActivity() != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                    android.R.layout.simple_spinner_item, workplaceList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            workplaceSpinner.setAdapter(adapter);
                        }
                    } else {
                        if (isAdded() && getActivity() != null) {
                            Toast.makeText(requireContext(), "No workplaces found for the user", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (isAdded() && getActivity() != null) {
                        Toast.makeText(requireContext(), "Failed to fetch workplaces: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            if (isAdded() && getActivity() != null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Fetches the hourly wage for the selected workplace from Firebase.
     *
     * @param workplace The selected workplace name.
     */
    private void fetchHourlyWage(String workplace) {
        Query workplaceQuery = workplacesRef.orderByChild("workplaceName").equalTo(workplace);
        workplaceQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String wage = dataSnapshot.child("hourlyWage").getValue(String.class);
                        if (wage != null) {
                            hourlyWage = Double.parseDouble(wage); // Parse and store the hourly wage.
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to fetch hourly wage: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a date-time picker dialog for selecting a date and time.
     *
     * @param editText The EditText where the selected date-time will be displayed.
     * @param calendar The Calendar instance to set the selected date-time.
     */
    private void showDateTimePicker(final EditText editText, final Calendar calendar) {
        // Date picker dialog listener.
        DatePickerDialog.OnDateSetListener dateListener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Time picker dialog for selecting the time.
            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                editText.setText(dateTimeFormat.format(calendar.getTime())); // Display the selected date-time in the EditText.

                toCalendar.setTime(calendar.getTime()); // Set the "To" calendar to the selected time.
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        };

        // Show the date picker dialog.
        new DatePickerDialog(requireContext(), dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Shows a time picker dialog for selecting the end time.
     *
     * @param calendar The Calendar instance to set the selected time.
     */
    private void showTimePicker(final Calendar calendar) {
        // Time picker dialog for selecting the end time.
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            etTo.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)); // Display the selected time in the EditText.
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    /**
     * Validates the input fields and creates a new shift in Firebase.
     */
    private void createShift() {
        String fromDateTime = etFrom.getText().toString();
        String toTime = etTo.getText().toString();

        if (TextUtils.isEmpty(fromDateTime) || TextUtils.isEmpty(toTime) || selectedWorkplace == null) {
            Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] fromDateTimeParts = fromDateTime.split(" ");
        String toDateTime = fromDateTimeParts[0] + " " + toTime;

        if (fromCalendar.getTimeInMillis() > toCalendar.getTimeInMillis()) {
            Toast.makeText(requireContext(), "'To' time should be after 'From' time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for overlapping shifts before creating a new one.
        checkForOverlappingShifts(fromDateTime, toDateTime, selectedWorkplace);
    }

    /**
     * Checks for overlapping shifts in the selected workplace and creates the shift if no overlaps are found.
     *
     * @param fromDateTime The start date and time of the shift.
     * @param toDateTime   The end date and time of the shift.
     * @param workplace    The selected workplace name.
     */
    private void checkForOverlappingShifts(String fromDateTime, String toDateTime, String workplace) {
        DatabaseReference shiftsRef = FirebaseDatabase.getInstance().getReference().child("Shifts");

        Query query = shiftsRef.orderByChild("workplaceName").equalTo(workplace);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isOverlapping = false;

                for (DataSnapshot shiftSnapshot : snapshot.getChildren()) {
                    String existingFromDateTime = shiftSnapshot.child("fromDateTime").getValue(String.class);
                    String existingToDateTime = shiftSnapshot.child("toDateTime").getValue(String.class);

                    // Check if the new shift overlaps with an existing shift.
                    if (existingFromDateTime != null && existingToDateTime != null &&
                            existingFromDateTime.equals(fromDateTime) && existingToDateTime.equals(toDateTime)) {
                        isOverlapping = true;
                        break;
                    }
                }

                if (isOverlapping) {
                    Toast.makeText(requireContext(), "Shift already Occupied", Toast.LENGTH_SHORT).show();
                } else {
                    saveShift(fromDateTime, toDateTime, workplace);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to check for overlapping shifts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves the new shift to Firebase if there are no overlapping shifts.
     *
     * @param fromDateTime The start date and time of the shift.
     * @param toDateTime   The end date and time of the shift.
     * @param workplace    The selected workplace name.
     */
    private void saveShift(String fromDateTime, String toDateTime, String workplace) {
        String shiftId = databaseShifts.push().getKey();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : "";

        if (shiftId != null) {
            double totalHours = calculateTotalHours(fromDateTime, toDateTime);
            double totalEarnings = totalHours * hourlyWage;

            // Create a new Shift object and save it to Firebase.
            Shift shift = new Shift(shiftId, fromDateTime, toDateTime, workplace, userEmail, totalEarnings);
            databaseShifts.child(shiftId).setValue(shift).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "Shift created successfully", Toast.LENGTH_SHORT).show();
                    clearFields(); // Clear the input fields after creating the shift.
                } else {
                    Toast.makeText(requireContext(), "Failed to create shift", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Calculates the total hours worked between the start and end times.
     *
     * @param fromDateTime The start date and time.
     * @param toDateTime   The end date and time.
     * @return The total hours worked.
     */
    private double calculateTotalHours(String fromDateTime, String toDateTime) {
        long fromMillis = 0;
        long toMillis = 0;

        try {
            fromMillis = dateTimeFormat.parse(fromDateTime).getTime();
            toMillis = dateTimeFormat.parse(toDateTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return (double) (toMillis - fromMillis) / (1000 * 60 * 60); // Convert milliseconds to hours.
    }

    /**
     * Clears the input fields after a shift is created.
     */
    private void clearFields() {
        etFrom.setText("");
        etTo.setText("");
        workplaceSpinner.setSelection(0);
    }
}
