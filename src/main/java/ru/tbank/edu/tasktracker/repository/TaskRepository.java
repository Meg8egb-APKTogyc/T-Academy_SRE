package ru.tbank.edu.tasktracker.repository;

import ru.tbank.edu.tasktracker.model.Task;
import ru.tbank.edu.tasktracker.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByStatus(TaskStatus status);

    List<Task> findAllByProjectId(Long projectId);
}
