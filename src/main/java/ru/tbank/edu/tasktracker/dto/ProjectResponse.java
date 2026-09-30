package ru.tbank.edu.tasktracker.dto;

import ru.tbank.edu.tasktracker.model.Project;

public record ProjectResponse(Long id, String name, String description) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription());
    }
}
