package ru.smartup.timetracker.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.repository.NoticeBatchRepository;
import ru.smartup.timetracker.repository.NoticeRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class NoticeService {
    public static final String TEXT_PROJECT_UPDATE = "Изменение информации о проекте";
    public static final String TEXT_PROJECT_ROLE_CHANGE = "Ваша роль на проекте изменилась";
    public static final String TEXT_PROJECT_ROLE_GRANTED = "Вы добавлены на проект";
    public static final String TEXT_ADMIN_ADDED = "Добавлен администратор";
    public static final String TEXT_ADMIN_REMOVED = "Удален администратор";
    public static final String TEXT_HOURS_REJECTED = "Отказано в согласовании времени";
    public static final String TEXT_FREEZE_SUCCESS = "Успешная заморозка";
    public static final String TEXT_FREEZE_PREPARE = "Блокировка часов";
    public static final String TEXT_FREEZE_CANCEL = "Блокировка отменена";
    public static final String TEXT_UN_FREEZE = "Блокировка снята";
    public static final String TEXT_FREEZE_ERROR = "Заморозка не выполнена";

    private final NoticeRepository noticeRepository;
    private final NoticeBatchRepository noticeBatchRepository;

    public void createNotice(Notice notice) {
        noticeRepository.save(notice);
    }

    public void createNotices(List<Notice> notices) {
        if (notices.size() > 1) {
            noticeBatchRepository.createNotices(notices);
        } else {
            createNotice(notices.get(0));
        }
    }

    public List<Notice> getNoticesByEmployeeId(int employeeId) {
        return noticeRepository.findAllByEmployeeIdOrderByCreatedDateDescWithoutDeleted(employeeId);
    }

    public Optional<Notice> getNoticeByIdAndEmployeeId(long id, int employeeId) {
        return noticeRepository.findByIdAndEmployeeId(id, employeeId);
    }

    public int getNumberUnreadNotices(int employeeId) {
        return noticeRepository.countByEmployeeIdAndReadFalse(employeeId);
    }

    @Transactional
    public void readAllNoticesByEmployeeId(int employeeId) {
        noticeRepository.readAllByEmployeeId(employeeId);
    }

    @Transactional
    public void readNoticesByIdsAndEmployeeId(Set<Long> ids, int employeeId) {
        noticeRepository.readByIdInAndEmployeeId(ids, employeeId);
    }

    @Transactional
    public void deleteAllNoticesByEmployeeId(int employeeId) {
        noticeRepository.deleteAllByEmployeeId(employeeId);
    }

    @Transactional
    public void deleteNoticesByIdsAndEmployeeId(Set<Long> ids, int employeeId) {
        noticeRepository.setDeletedByIdInAndEmployeeId(ids, employeeId);
    }
}
