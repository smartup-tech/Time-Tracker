package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.entity.TrackedProjectTask;
import ru.smartup.timetracker.pojo.TrackedProjectTaskForEmployee;
import ru.smartup.timetracker.repository.TrackedProjectTaskRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ObservationTaskService {
    private final TrackedProjectTaskRepository trackedProjectTaskRepository;

    @Transactional
    public void observeTask(final TrackedProjectTask trackedProjectTask) {
        trackedProjectTaskRepository.save(trackedProjectTask);
    }

    @Transactional
    public void removeObservationForTask(final int employeeId, final long taskId) {
        trackedProjectTaskRepository.deleteByEmployeeIdAndTaskId(employeeId, taskId);
    }

    public List<TrackedProjectTaskForEmployee> getTrackedProjectTaskInfoByEmployee(final int employeeId) {
        return trackedProjectTaskRepository.findAllTrackedProjectTaskInfoByEmployeeId(employeeId);
    }

    public Optional<TrackedProjectTask> getTrackedProjectTaskByEmployeeIdAndTaskId(final int employeeId, final long taskId) {
        return trackedProjectTaskRepository.findByEmployeeIdAndTaskId(employeeId, taskId);
    }
}
