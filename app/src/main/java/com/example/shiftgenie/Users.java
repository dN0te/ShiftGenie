package com.example.shiftgenie;

/**
 * The Users class represents a user in the ShiftGenie application.
 * It contains basic user information such as user ID, username, email,
 * phone number, and age.
 */
public class Users {
    private String userId;        // Unique identifier for the user.
    private String username;      // The username chosen by the user.
    private String email;         // The user's email address.
    private String phoneNumber;   // The user's phone number.
    private int age;              // The user's age.

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Users.class).
     */
    public Users() {
        // Default constructor
    }

    /**
     * Constructor to initialize all fields of the Users object.
     *
     * @param userId      The unique identifier for the user.
     * @param username    The username of the user.
     * @param email       The email address of the user.
     * @param phoneNumber The phone number of the user.
     * @param age         The age of the user.
     */
    public Users(String userId, String username, String email, String phoneNumber, int age) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.age = age;
    }

    /**
     * Gets the unique identifier for the user.
     *
     * @return The user's ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique identifier for the user.
     *
     * @param userId The user's ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
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
     * Gets the age of the user.
     *
     * @return The user's age.
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the age of the user.
     *
     * @param age The user's age.
     */
    public void setAge(int age) {
        this.age = age;
    }
}
