package ru.smartup.timetracker.dto.profile.request;

import lombok.Data;
import ru.smartup.timetracker.validation.OptionalField;
import ru.smartup.timetracker.validation.RequiredField;

@Data
public class PersonalDataUpdateDto {
    @RequiredField(maxSize = 255)
    private String firstName;

    @OptionalField(maxSize = 255)
    private String middleName;

    @RequiredField(maxSize = 255)
    private String lastName;
}
