package ru.smartup.timetracker.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@UtilityClass
public class CommonStringUtils {
    private static final String PERCENT = "%";
    private static final String UNDERSCORE = "_";
    private static final String PERCENT_REPLACEMENT = "\\\\%";
    private static final String UNDERSCORE_REPLACEMENT = "\\\\_";
    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final String HEXADECIMAL_FORMAT = "%02x";
    public static final String DASH = "-";

    public static final String WHITESPACE_REG_EXP = "\\s";
    public static final String WHITESPACE_CHAIN_REG_EXP = "\\s{2,}";

    public static String escapePercentAndUnderscore(String value) {
        return StringUtils.isNotBlank(value)
                ? value.replaceAll(PERCENT, PERCENT_REPLACEMENT).replaceAll(UNDERSCORE, UNDERSCORE_REPLACEMENT)
                : value;
    }

    public static String hashSHA256(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return bytesToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.warn("Can not hash input value to SHA-256.", e);
            return value;
        }
    }

    private String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format(HEXADECIMAL_FORMAT, b));
        }
        return sb.toString();
    }
}
