package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.entity.TrackedProjectTask;
import ru.smartup.timetracker.pojo.TrackedProjectTaskForUser;
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
    public void removeObservationForTask(final int userId, final long taskId) {
        trackedProjectTaskRepository.deleteByUserIdAndTaskId(userId, taskId);
    }

    public List<TrackedProjectTaskForUser> getTrackedProjectTaskInfoByUser(final int userId) {
        return trackedProjectTaskRepository.findAllTrackedProjectTaskInfoByUserId(userId);
    }

    public Optional<TrackedProjectTask> getTrackedProjectTaskByUserIdAndTaskId(final int userId, final long taskId) {
        return trackedProjectTaskRepository.findByUserIdAndTaskId(userId, taskId);
    }
}
