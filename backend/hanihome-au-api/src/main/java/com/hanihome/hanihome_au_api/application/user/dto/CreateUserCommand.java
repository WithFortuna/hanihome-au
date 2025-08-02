package com.hanihome.hanihome_au_api.application.user.dto;

public class CreateUserCommand {
    private final String email;
    private final String name;
    private final String phoneNumber;
    private final String role;

    public CreateUserCommand(String email, String name, String phoneNumber, String role) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRole() { return role; }
}