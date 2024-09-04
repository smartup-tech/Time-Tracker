package ru.smartup.timetracker.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.entity.field.sort.ProjectSortFieldEnum;
import ru.smartup.timetracker.entity.field.sort.SortField;
import ru.smartup.timetracker.entity.field.sort.UserSortFieldEnum;

@Component
public class PageableMaker {
    public Pageable make(final PageableRequestParamDto<? extends SortField> pageableParam) {
        Sort sort = Sort.by(pageableParam.getSortDirection(), pageableParam.getSortBy() == null ? SortField.idField.getValues() : pageableParam.getSortBy().getValues());
//        Sort sort = Sort.by(pageableParam.getSortDirection(),  SortField.idField.getValues() );
        return PageRequest.of(pageableParam.getPage(), pageableParam.getSize(), sort);
    }
}
