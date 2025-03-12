package ru.smartup.timetracker.service.notification.notifierImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.pojo.notice.NoticeFreeze;
import ru.smartup.timetracker.service.notification.notifier.Notifier;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static ru.smartup.timetracker.utils.DateUtils.*;

@Slf4j
@Component
public class MatterMostNotifier implements Notifier {

    private final HttpClient httpClient;
    private final String WEBHOOK_URL;
    private final String timeTrackerUrl;

    public MatterMostNotifier(HttpClient httpClient,
                              @Value("${spring.mattermost.webhook-url}") String webhookUrl,
                              @Value("${spring.mattermost.time-tracker-url}") String timeTrackerUrl) {
        this.httpClient = httpClient;
        this.WEBHOOK_URL = webhookUrl;
        this.timeTrackerUrl = timeTrackerUrl;
    }

    @Override
    public void send(List<Employee> recipients, Notice message) {

        if (!(message.getData() instanceof NoticeFreeze)) {
            throw new IllegalArgumentException("Некорректный формат данных в Notice");
        }

        NoticeFreeze noticeFreeze = (NoticeFreeze) message.getData();

        String dateString = noticeFreeze.getDate();

        LocalDateTime freezeDate = parseStringDateToLocal(dateString);

        String formattedDate = formatDate(freezeDate.toLocalDate());


        String nextDay = getNextDay(freezeDate);

        StringBuilder jsonPayloadBuilder = new StringBuilder();
        jsonPayloadBuilder.append("{ \"text\": \"")
                .append("Всем привет! \\n:warning: \\n")
                .append("Напоминаем о следующей блокировке ").append(formattedDate).append(".\\n")
                .append("Пожалуйста, внесите и засабмитьте время до 00:01 ").append(nextDay).append(" за отработанный период. \\n")
                .append("Обратите внимание, что вам нужно затрекать время по ").append(formattedDate).append(" включительно.\\n")
                .append("[Time Tracker](").append(timeTrackerUrl).append(") \\n")
                .append(":warning: \\n")
                .append("Хорошего дня!\" }");

        String jsonPayload = jsonPayloadBuilder.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(WEBHOOK_URL))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

