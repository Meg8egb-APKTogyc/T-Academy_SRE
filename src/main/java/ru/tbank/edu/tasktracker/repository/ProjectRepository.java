package ru.tbank.edu.tasktracker.repository;

import ru.tbank.edu.tasktracker.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
