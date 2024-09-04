package ru.smartup.timetracker.core;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRelationObjectEnum;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.service.RelationUserRolesService;

public class CustomMethodSecurityExpressionRoot
        extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    private final RelationUserRolesService relationUserRolesService;

    private Object filterObject;

    private Object returnObject;

    private Object target;

    CustomMethodSecurityExpressionRoot(Authentication authentication, RelationUserRolesService relationUserRolesService) {
        super(authentication);
        this.relationUserRolesService = relationUserRolesService;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public SessionUserPrincipal getPrincipal() {
        return (SessionUserPrincipal) super.getPrincipal();
    }

    /**
     * Имеет ли пользователь в объекте типа projectRelationObjectEnum
     * с идентификатором objectId какие-то права из списка userRoles
     *
     * @param projectRelationObjectEnum тип объекта
     * @param objectId                  идентификатор объекта
     * @param userRoles                 роли пользователя
     * @return
     */
    public boolean hasRoleForObject(ProjectRelationObjectEnum projectRelationObjectEnum, long objectId,
                                    UserRoleEnum... userRoles) {
        // TODO
        return false;
    }
}