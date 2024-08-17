package com.example.shiftgenie;

/**
 * The Shift class represents a work shift, including details such as the shift ID, 
 * start and end times, workplace name, user email, and total earnings.
 */
public class Shift {
    private String shiftId;          // Unique identifier for the shift.
    private String fromDateTime;     // Start date and time of the shift.
    private String toDateTime;       // End date and time of the shift.
    private String workplaceName;    // Name of the workplace where the shift took place.
    private String userEmail;        // Email of the user who worked the shift.
    private double totalEarnings;    // Total earnings for the shift.

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Shift.class).
     */
    public Shift() {
        // No-argument constructor required for Firebase.
    }

    /**
     * Constructor to initialize all fields of the Shift object.
     *
     * @param shiftId       Unique identifier for the shift.
     * @param fromDateTime  Start date and time of the shift.
     * @param toDateTime    End date and time of the shift.
     * @param workplaceName Name of the workplace where the shift took place.
     * @param userEmail     Email of the user who worked the shift.
     * @param totalEarnings Total earnings for the shift.
     */
    public Shift(String shiftId, String fromDateTime, String toDateTime, String workplaceName, String userEmail, double totalEarnings) {
        this.shiftId = shiftId;
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
        this.workplaceName = workplaceName;
        this.userEmail = userEmail;
        this.totalEarnings = totalEarnings;
    }

    /**
     * Gets the unique identifier for the shift.
     *
     * @return The shift ID.
     */
    public String getShiftId() {
        return shiftId;
    }

    /**
     * Sets the unique identifier for the shift.
     *
     * @param shiftId The shift ID.
     */
    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    /**
     * Gets the start date and time of the shift.
     *
     * @return The start date and time of the shift.
     */
    public String getFromDateTime() {
        return fromDateTime;
    }

    /**
     * Sets the start date and time of the shift.
     *
     * @param fromDateTime The start date and time of the shift.
     */
    public void setFromDateTime(String fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    /**
     * Gets the end date and time of the shift.
     *
     * @return The end date and time of the shift.
     */
    public String getToDateTime() {
        return toDateTime;
    }

    /**
     * Sets the end date and time of the shift.
     *
     * @param toDateTime The end date and time of the shift.
     */
    public void setToDateTime(String toDateTime) {
        this.toDateTime = toDateTime;
    }

    /**
     * Gets the name of the workplace where the shift took place.
     *
     * @return The workplace name.
     */
    public String getWorkplaceName() {
        return workplaceName;
    }

    /**
     * Sets the name of the workplace where the shift took place.
     *
     * @param workplaceName The workplace name.
     */
    public void setWorkplaceName(String workplaceName) {
        this.workplaceName = workplaceName;
    }

    /**
     * Gets the email of the user who worked the shift.
     *
     * @return The user's email.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the email of the user who worked the shift.
     *
     * @param userEmail The user's email.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Gets the total earnings for the shift.
     *
     * @return The total earnings for the shift.
     */
    public double getTotalEarnings() {
        return totalEarnings;
    }

    /**
     * Sets the total earnings for the shift.
     *
     * @param totalEarnings The total earnings for the shift.
     */
    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }
}
