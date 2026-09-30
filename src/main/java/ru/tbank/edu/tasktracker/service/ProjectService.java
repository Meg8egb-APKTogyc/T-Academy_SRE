package ru.tbank.edu.tasktracker.service;

import ru.tbank.edu.tasktracker.dto.ProjectRequest;
import ru.tbank.edu.tasktracker.dto.ProjectResponse;
import ru.tbank.edu.tasktracker.exception.NotFoundException;
import ru.tbank.edu.tasktracker.model.Project;
import ru.tbank.edu.tasktracker.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public ProjectResponse findById(Long id) {
        return ProjectResponse.from(getEntity(id));
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Project project = new Project(request.name(), request.description());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = getEntity(id);
        project.setName(request.name());
        project.setDescription(request.description());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id) {
        Project project = getEntity(id);
        projectRepository.delete(project);
    }

    private Project getEntity(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Проект с id=" + id + " не найден"));
    }
}
