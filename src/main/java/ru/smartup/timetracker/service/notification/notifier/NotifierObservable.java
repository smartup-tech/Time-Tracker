package ru.smartup.timetracker.service.notification.notifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.smartup.timetracker.core.notification.NotifierNames;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.User;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class NotifierObservable {
    private static final int THREAD_PULL_SIZE = 10;

    @Qualifier("notifiers")
    private final Map<String, Notifier> appNotifiers;
    private final ExecutorService executorService;

    public NotifierObservable(final Map<String, Notifier> appNotifiers) {
        this.appNotifiers = appNotifiers;
        this.executorService = Executors.newFixedThreadPool(THREAD_PULL_SIZE);
    }

    public void notifyAllChannels(final List<User> recipients, final Notice message) {
        invokeNotify(recipients, message, appNotifiers.values());
    }

    private void invokeNotify(final List<User> recipients, final Notice message, final Collection<Notifier> notifiers) {
        try {
            executorService.invokeAll(notifiersSending(recipients, message, notifiers));
        } catch (InterruptedException e) {
            // todo подумать над обработкой
            throw new RuntimeException(e);
        }
    }

    private List<Callable<Void>> notifiersSending(final List<User> recipients, final Notice message, final Collection<Notifier> notifiers) {
        return notifiers.stream()
                .map(subscriber ->
                        (Callable<Void>) () -> {
                            subscriber.send(recipients, message);
                            return null;
                        }
                )
                .collect(Collectors.toList());
    }

    public void notifyEmailChannel(final List<User> recipients, final Notice notice) {
        notifySpecificChannels(recipients, notice, NotifierNames.EMAIL_NOTIFIER);
    }

    public void notifyAppChannel(final List<User> recipients, final Notice notice) {
        notifySpecificChannels(recipients, notice, NotifierNames.DATABASE_NOTIFIER);
    }

    public void notifySpecificChannels(final List<User> recipients, final Notice message, final String ...notifierNames) {
        final List<Notifier> filtered = filterNotifierByName(notifierNames);

        invokeNotify(recipients, message, filtered);
    }

    private List<Notifier> filterNotifierByName(final String ...notifierNames) {
        final List<Notifier> filtered = new ArrayList<>();
        for (var name : notifierNames) {
            var notifier = appNotifiers.get(name);
            if (Objects.nonNull(notifier)) {
                filtered.add(notifier);
            }
        }
        return filtered;
    }
}
