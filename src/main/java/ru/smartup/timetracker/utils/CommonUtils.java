package ru.smartup.timetracker.utils;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.smartup.timetracker.core.SessionUserPrincipal;

@UtilityClass
public class CommonUtils {
    public static int getCurrentUserId() {
        int currentUserId = 0;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            currentUserId = ((SessionUserPrincipal) authentication.getPrincipal()).getId();
        }
        return currentUserId;
    }
}
