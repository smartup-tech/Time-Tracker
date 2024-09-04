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

    public List<Notice> getNoticesByUserId(int userId) {
        return noticeRepository.findAllByUserIdOrderByCreatedDateDesc(userId);
    }

    public Optional<Notice> getNoticeByIdAndUserId(long id, int userId) {
        return noticeRepository.findByIdAndUserId(id, userId);
    }

    public int getNumberUnreadNotices(int userId) {
        return noticeRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void readAllNoticesByUserId(int userId) {
        noticeRepository.readAllByUserId(userId);
    }

    @Transactional
    public void readNoticesByIdsAndUserId(Set<Long> ids, int userId) {
        noticeRepository.readByIdInAndUserId(ids, userId);
    }

    @Transactional
    public void deleteAllNoticesByUserId(int userId) {
        noticeRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void deleteNoticesByIdsAndUserId(Set<Long> ids, int userId) {
        noticeRepository.deleteByIdInAndUserId(ids, userId);
    }
}
