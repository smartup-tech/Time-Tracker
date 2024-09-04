package ru.smartup.timetracker.service.notification.notifierImpl;

import lombok.RequiredArgsConstructor;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.service.notification.NoticeService;
import ru.smartup.timetracker.service.notification.notifier.Notifier;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DbNotifier implements Notifier {

    private final NoticeService noticeService;

    @Override
    public void send(final List<User> recipients, final Notice message) {
        sendMessages(recipients, message);
    }

    private void sendMessages(final List<User> recipients, final Notice message) {
        List<Notice> notices = recipients.stream()
                .map(recipient -> new Notice(message.getType(), recipient.getId(),
                        message.getText(), message.getData(), message.getCreatedBy()))
                .collect(Collectors.toList());

        noticeService.createNotices(notices);
    }

}
