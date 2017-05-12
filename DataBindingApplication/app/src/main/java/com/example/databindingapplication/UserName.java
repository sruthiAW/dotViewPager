package com.example.databindingapplication;

/**
 * Created by ssurendran on 4/24/2017.
 */

public class UserName {
    private String firstName;
    private String lastName;

    public UserName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
