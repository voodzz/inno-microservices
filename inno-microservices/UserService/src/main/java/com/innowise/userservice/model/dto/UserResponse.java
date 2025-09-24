package com.innowise.userservice.model.dto;

import java.time.LocalDate;

public record UserResponse(String name, String surname, LocalDate birthDate, String email) {}
