package com.example.shiftgenie;

/**
 * The User class represents a user of the ShiftGenie application. 
 * It contains information about the user such as their ID, username, profile image, 
 * age, phone number, email, the number of shifts they have worked, and their total earnings.
 */
public class User {
    private String id;                // Unique identifier for the user.
    private String username;          // The username chosen by the user.
    private String profileImage;      // URL of the user's profile image.
    private String age;               // The user's age.
    private String phoneNumber;       // The user's phone number.
    private String email;             // The user's email address.
    private int shiftsCount;          // Number of shifts the user has completed.
    private double totalEarnings;     // The total earnings the user has accumulated.

    /**
     * Default constructor for creating a User object.
     */
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * Gets the unique identifier for the user.
     *
     * @return The user's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the user.
     *
     * @param id The user's ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the username of the user.
     *
     * @return The user's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The user's username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the URL of the user's profile image.
     *
     * @return The URL of the profile image.
     */
    public String getProfileImage() {
        return profileImage;
    }

    /**
     * Sets the URL of the user's profile image.
     *
     * @param profileImage The URL of the profile image.
     */
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * Gets the age of the user.
     *
     * @return The user's age.
     */
    public String getAge() {
        return age;
    }

    /**
     * Sets the age of the user.
     *
     * @param age The user's age.
     */
    public void setAge(String age) {
        this.age = age;
    }

    /**
     * Gets the phone number of the user.
     *
     * @return The user's phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number of the user.
     *
     * @param phoneNumber The user's phone number.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the email address of the user.
     *
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email The user's email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the number of shifts the user has completed.
     *
     * @return The number of shifts completed.
     */
    public int getShiftsCount() {
        return shiftsCount;
    }

    /**
     * Sets the number of shifts the user has completed.
     *
     * @param shiftsCount The number of shifts completed.
     */
    public void setShiftsCount(int shiftsCount) {
        this.shiftsCount = shiftsCount;
    }

    /**
     * Gets the total earnings the user has accumulated.
     *
     * @return The total earnings.
     */
    public double getTotalEarnings() {
        return totalEarnings;
    }

    /**
     * Sets the total earnings the user has accumulated.
     *
     * @param totalEarnings The total earnings.
     */
    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }
}
