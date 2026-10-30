package ru.tbank.edu.tasktracker.repository;

import ru.tbank.edu.tasktracker.model.Task;
import ru.tbank.edu.tasktracker.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByStatus(TaskStatus status);

    List<Task> findAllByProjectId(Long projectId);

    long countByStatus(TaskStatus status);

    @Query("SELECT t.project.id, COUNT(t) FROM Task t WHERE t.project IS NOT NULL GROUP BY t.project.id")
    List<Object[]> countTasksGroupByProject();

    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countTasksGroupByPriority();

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksGroupByStatus();
}
