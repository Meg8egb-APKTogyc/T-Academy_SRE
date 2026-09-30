package ru.tbank.edu.tasktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Название проекта обязательно")
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description
) {
}
