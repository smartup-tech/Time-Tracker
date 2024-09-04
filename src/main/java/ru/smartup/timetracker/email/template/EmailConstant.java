package ru.smartup.timetracker.email.template;

public class EmailConstant {
    public static class PropertyName {
        public static final String NAME_PROPERTY = "name";
        public static final String LINK_PROPERTY = "link";
        public static final String TTL_PROPERTY = "ttl";

        public static final String FREEZING_TIMESTAMP_PROPERTY = "freezeTimestamp";
        public static final String UN_FREEZING_TIMESTAMP_PROPERTY = "unfreezeTimestamp";
    }
    public static class SubjectName {
        public static final String PASSWORD_RECOVERY_SUBJECT = "Восстановление пароля";
        public static final String USER_REGISTRATION_SUBJECT = "Добро пожаловать";
        public static final String FREEZE_SUBJECT = "Блокировка";
        public static final String SUCCESS_FREEZE_SUBJECT = "Блокировка успешно завершена";
        public static final String UN_FREEZE_SUBJECT = "Снятие блокировки";
        public static final String CANCEL_FREEZE_SUBJECT = "Отмена блокировки";
        public static final String PASSWORD_RESET_SUBJECT = "Сброс пароля";
    }

    public static final String HOUR = " часа";
    public static final String HOURS = " часов";

}
