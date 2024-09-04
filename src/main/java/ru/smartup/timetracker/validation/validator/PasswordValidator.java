package ru.smartup.timetracker.validation.validator;

import org.apache.commons.lang3.StringUtils;
import org.passay.*;
import ru.smartup.timetracker.validation.ValidPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private static final int MIN_PASSWORD_LENGTH = 5;
    public static final int MAX_PASSWORD_LENGTH = 30;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        List<Rule> rules = new ArrayList<>();
        rules.add(new LengthRule(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH));
        rules.add(new WhitespaceRule());
        rules.addAll(getCharacterRules());

        org.passay.PasswordValidator validator = new org.passay.PasswordValidator(rules);
        RuleResult ruleResult = validator.validate(new PasswordData(value));
        if (ruleResult.isValid()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        customMessage(context, String.join(StringUtils.SPACE, validator.getMessages(ruleResult)));
        return false;
    }

    private void customMessage(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    public static List<CharacterRule> getCharacterRules() {
        return List.of(new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 2),
                new CharacterRule(EnglishCharacterData.Digit, 1));
    }
}
