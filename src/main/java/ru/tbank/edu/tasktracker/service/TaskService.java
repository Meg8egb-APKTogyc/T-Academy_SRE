package ru.tbank.edu.tasktracker.service;

import ru.tbank.edu.tasktracker.dto.TaskRequest;
import ru.tbank.edu.tasktracker.dto.TaskResponse;
import ru.tbank.edu.tasktracker.exception.NotFoundException;
import ru.tbank.edu.tasktracker.model.Project;
import ru.tbank.edu.tasktracker.model.Task;
import ru.tbank.edu.tasktracker.model.TaskStatus;
import ru.tbank.edu.tasktracker.repository.ProjectRepository;
import ru.tbank.edu.tasktracker.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public List<TaskResponse> findAll(TaskStatus status, Long projectId) {
        List<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findAllByStatus(status);
        } else if (projectId != null) {
            tasks = taskRepository.findAllByProjectId(projectId);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream().map(TaskResponse::from).toList();
    }

    public TaskResponse findById(Long id) {
        return TaskResponse.from(getEntity(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = getEntity(id);
        applyRequest(task, request);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        Task task = getEntity(id);
        taskRepository.delete(task);
    }

    private void applyRequest(Task task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setProject(resolveProject(request.projectId()));
    }

    private Project resolveProject(Long projectId) {
        if (projectId == null) {
            return null;
        }
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект с id=" + projectId + " не найден"));
    }

    private Task getEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Задача с id=" + id + " не найдена"));
    }
}
