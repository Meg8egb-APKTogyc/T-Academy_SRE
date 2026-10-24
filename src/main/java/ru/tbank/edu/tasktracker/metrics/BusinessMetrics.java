package ru.tbank.edu.tasktracker.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class BusinessMetrics {

    private final Counter tasksCreated;
    private final Counter tasksCompleted;
    private final Counter projectsCreated;
    private final AtomicLong tasksInProgress = new AtomicLong(0);

    public BusinessMetrics(MeterRegistry registry) {
        this.tasksCreated = Counter.builder("tasks_created_total")
                .description("Total number of tasks created")
                .register(registry);

        this.tasksCompleted = Counter.builder("tasks_completed_total")
                .description("Total number of tasks marked as DONE")
                .register(registry);

        this.projectsCreated = Counter.builder("projects_created_total")
                .description("Total number of projects created")
                .register(registry);

        Gauge.builder("tasks_in_progress", tasksInProgress, AtomicLong::get)
                .description("Current number of tasks with status IN_PROGRESS")
                .register(registry);
    }

    public void incrementTasksCreated() { tasksCreated.increment(); }
    public void incrementTasksCompleted() { tasksCompleted.increment(); }
    public void incrementProjectsCreated() { projectsCreated.increment(); }
    public void setTasksInProgress(long count) { tasksInProgress.set(count); }
}