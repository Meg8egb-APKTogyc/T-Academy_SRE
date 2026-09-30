package ru.tbank.edu.tasktracker.dto;

import ru.tbank.edu.tasktracker.model.TaskPriority;
import ru.tbank.edu.tasktracker.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Заголовок задачи обязателен")
        @Size(max = 300)
        String title,

        @Size(max = 5000)
        String description,

        @NotNull(message = "Статус обязателен")
        TaskStatus status,

        @NotNull(message = "Приоритет обязателен")
        TaskPriority priority,

        LocalDate dueDate,

        Long projectId
) {
}
