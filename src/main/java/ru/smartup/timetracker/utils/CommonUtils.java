package ru.smartup.timetracker.utils;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;

@UtilityClass
public class CommonUtils {
    public static int getCurrentEmployeeId() {
        int currentEmployeeId = 0;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            currentEmployeeId = ((SessionEmployeePrincipal) authentication.getPrincipal()).getId();
        }
        return currentEmployeeId;
    }
}
