package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public List<Task> getTasksFromProject(int projectId) {
        return taskRepository.findAllByProjectId(projectId);
    }

    public Optional<Task> getTask(long taskId) {
        return taskRepository.findById(taskId);
    }

    public Optional<Task> getNotArchivedTask(long taskId) {
        return taskRepository.findByIdAndIsArchivedFalse(taskId);
    }

    public boolean isNotUnique(int projectId, String taskName) {
        return taskRepository.isNotUnique(projectId, taskName);
    }

    public boolean isNotUnique(int projectId, long taskId, String taskName) {
        return taskRepository.isNotUnique(projectId, taskId, taskName);
    }

    @Transactional
    public void createTask(Task task) {
        taskRepository.save(task);
    }

    @Transactional
    public void updateTask(Task task) {
        taskRepository.save(task);
    }

    @Transactional
    public void archiveTask(long taskId) {
        taskRepository.archiveTask(taskId);
    }
}
