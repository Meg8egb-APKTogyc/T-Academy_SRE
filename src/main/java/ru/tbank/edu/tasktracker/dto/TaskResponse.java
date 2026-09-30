package ru.tbank.edu.tasktracker.dto;

import ru.tbank.edu.tasktracker.model.Task;
import ru.tbank.edu.tasktracker.model.TaskPriority;
import ru.tbank.edu.tasktracker.model.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Instant createdAt,
        Long projectId,
        String projectName
) {

    public static TaskResponse from(Task task) {
        Long projectId = task.getProject() != null ? task.getProject().getId() : null;
        String projectName = task.getProject() != null ? task.getProject().getName() : null;
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                projectId,
                projectName
        );
    }
}
