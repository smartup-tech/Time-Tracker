package ru.smartup.timetracker.service.notification;

import org.springframework.stereotype.Service;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.service.notification.strategy.NoticeCreationStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FreezeTracksSuccessNoticeCreationService {
    private final Map<EmployeeRoleEnum, NoticeCreationStrategy> noticeCreationStrategies;

    public FreezeTracksSuccessNoticeCreationService(List<NoticeCreationStrategy> strategies) {
        noticeCreationStrategies = new HashMap<>();
        for (NoticeCreationStrategy strategy : strategies) {
            noticeCreationStrategies.put(strategy.getRole(), strategy);
        }
    }

    public Optional<NoticeCreationStrategy> getStrategy(EmployeeRoleEnum employeeRole) {
        return Optional.of(noticeCreationStrategies.get(employeeRole));
    }
}
