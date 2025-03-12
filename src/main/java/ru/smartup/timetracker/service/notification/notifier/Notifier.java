package ru.smartup.timetracker.service.notification.notifier;

import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.Employee;

import java.util.List;

public interface Notifier {
    void send(List<Employee> recipients, Notice message);
}
