package com.innowise.authservice.model.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserDto(Long id, String name, String surname, LocalDate birthDate, String email) {}
