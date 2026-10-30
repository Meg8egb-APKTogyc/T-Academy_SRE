package ru.tbank.edu.tasktracker.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import ru.tbank.edu.tasktracker.model.TaskPriority;
import ru.tbank.edu.tasktracker.model.TaskStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class BusinessMetrics {

    private final MeterRegistry registry;
    private final Counter tasksCreated;
    private final Counter tasksCompleted;
    private final Counter projectsCreated;
    private final AtomicLong tasksInProgress = new AtomicLong(0);
    private final Map<String, AtomicLong> tasksPerProjectGauges = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> tasksByPriorityGauges = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> tasksByStatusGauges = new ConcurrentHashMap<>();

    public BusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
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

    public void updateTasksPerProject(List<Object[]> groupedCounts) {
        refresh(tasksPerProjectGauges, "tasks_per_project", "project_id", toMap(groupedCounts));
    }

    public void updateTasksByPriority(List<Object[]> groupedCounts) {
        Map<String, Long> counts = toMap(groupedCounts);
        for (TaskPriority priority : TaskPriority.values()) {
            counts.putIfAbsent(priority.name(), 0L);
        }
        refresh(tasksByPriorityGauges, "tasks_by_priority", "priority", counts);
    }

    public void updateTasksByStatus(List<Object[]> groupedCounts) {
        Map<String, Long> counts = toMap(groupedCounts);
        for (TaskStatus status : TaskStatus.values()) {
            counts.putIfAbsent(status.name(), 0L);
        }
        refresh(tasksByStatusGauges, "tasks_by_status", "status", counts);
    }

    private void refresh(Map<String, AtomicLong> gauges, String name, String tagKey, Map<String, Long> counts) {
        counts.forEach((key, count) ->
                gauges.computeIfAbsent(key, k -> {
                    AtomicLong value = new AtomicLong();
                    Gauge.builder(name, value, AtomicLong::get)
                            .tag(tagKey, k)
                            .register(registry);
                    return value;
                }).set(count));
        gauges.forEach((key, value) -> {
            if (!counts.containsKey(key)) {
                value.set(0);
            }
        });
    }

    private static Map<String, Long> toMap(List<Object[]> groupedCounts) {
        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : groupedCounts) {
            counts.put(String.valueOf(row[0]), (Long) row[1]);
        }
        return counts;
    }
}
