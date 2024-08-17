package com.example.shiftgenie;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * WorkplaceFragment allows users to input and save data related to their workplace, such as hourly wage,
 * vacation payments, and travel expenses. This data is stored in the Firebase Realtime Database.
 */
public class WorkplaceFragment extends Fragment {

    private EditText etWorkplaceName, etHourlyWage, etVacationPayments, etDeductionPreShift, etBonusesPreShift, etBreakTime, etDailyTravelExpenses, etMonthlyTravelExpenses; // Input fields for workplace details.
    private Button btnSave; // Button to save the workplace data.

    private FirebaseAuth auth; // Firebase Authentication instance to get the current user.
    private DatabaseReference databaseReference; // Reference to the Firebase Realtime Database.

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
        View view = inflater.inflate(R.layout.fragment_workplace, container, false);

        // Initialize the input fields and button.
        etWorkplaceName = view.findViewById(R.id.etWorkplaceName);
        etHourlyWage = view.findViewById(R.id.etHourlyWage);
        etVacationPayments = view.findViewById(R.id.etVacationPayments);
        etDeductionPreShift = view.findViewById(R.id.etDeductionPreShift);
        etBonusesPreShift = view.findViewById(R.id.etBonusesPreShift);
        etBreakTime = view.findViewById(R.id.etBreakTime);
        etDailyTravelExpenses = view.findViewById(R.id.etDailyTravelExpenses);
        etMonthlyTravelExpenses = view.findViewById(R.id.etMonthlyTravelExpenses);
        btnSave = view.findViewById(R.id.btnSave);

        // Initialize Firebase Authentication and Database Reference.
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Workplace");

        // Set an onClickListener to the save button to handle the saving of workplace data.
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveWorkplaceData();
            }
        });

        return view;
    }

    /**
     * Saves the workplace data to Firebase Realtime Database.
     * It checks if all the fields are filled before attempting to save the data.
     */
    private void saveWorkplaceData() {
        // Get the text from the input fields.
        String workplaceName = etWorkplaceName.getText().toString().trim();
        String hourlyWage = etHourlyWage.getText().toString().trim();
        String vacationPayments = etVacationPayments.getText().toString().trim();
        String deductionPreShift = etDeductionPreShift.getText().toString().trim();
        String bonusesPreShift = etBonusesPreShift.getText().toString().trim();
        String breakTime = etBreakTime.getText().toString().trim();
        String dailyTravelExpenses = etDailyTravelExpenses.getText().toString().trim();
        String monthlyTravelExpenses = etMonthlyTravelExpenses.getText().toString().trim();

        // Validate that none of the fields are empty.
        if (TextUtils.isEmpty(workplaceName) || TextUtils.isEmpty(hourlyWage) || TextUtils.isEmpty(vacationPayments) ||
                TextUtils.isEmpty(deductionPreShift) || TextUtils.isEmpty(bonusesPreShift) || TextUtils.isEmpty(breakTime) ||
                TextUtils.isEmpty(dailyTravelExpenses) || TextUtils.isEmpty(monthlyTravelExpenses)) {
            Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user from Firebase Authentication.
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String userEmail = user.getEmail();

            // Generate a unique ID for the workplace entry.
            String workplaceId = databaseReference.push().getKey();

            // Create a map to store the workplace data.
            Map<String, Object> workplaceData = new HashMap<>();
            workplaceData.put("workplaceName", workplaceName);
            workplaceData.put("hourlyWage", hourlyWage);
            workplaceData.put("vacationPayments", vacationPayments);
            workplaceData.put("deductionPreShift", deductionPreShift);
            workplaceData.put("bonusesPreShift", bonusesPreShift);
            workplaceData.put("breakTime", breakTime);
            workplaceData.put("dailyTravelExpenses", dailyTravelExpenses);
            workplaceData.put("monthlyTravelExpenses", monthlyTravelExpenses);
            workplaceData.put("userEmail", userEmail); // Include current user's email for reference.

            // Save the workplace data in the database.
            databaseReference.child(workplaceId).setValue(workplaceData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Show a success message and clear the fields.
                    Toast.makeText(getContext(), "Workplace data saved successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    // Show an error message if the save operation fails.
                    Toast.makeText(getContext(), "Failed to save workplace data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Clears all the input fields after the data is successfully saved.
     */
    private void clearFields() {
        etWorkplaceName.setText("");
        etHourlyWage.setText("");
        etVacationPayments.setText("");
        etDeductionPreShift.setText("");
        etBonusesPreShift.setText("");
        etBreakTime.setText("");
        etDailyTravelExpenses.setText("");
        etMonthlyTravelExpenses.setText("");
    }
}
