package ru.smartup.timetracker.core;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRelationObjectEnum;
import ru.smartup.timetracker.service.RelationEmployeeRolesService;

public class CustomMethodSecurityExpressionRoot
        extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    private final RelationEmployeeRolesService relationEmployeeRolesService;

    private Object filterObject;

    private Object returnObject;

    private Object target;

    CustomMethodSecurityExpressionRoot(Authentication authentication, RelationEmployeeRolesService relationEmployeeRolesService) {
        super(authentication);
        this.relationEmployeeRolesService = relationEmployeeRolesService;
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
    public SessionEmployeePrincipal getPrincipal() {
        return (SessionEmployeePrincipal) super.getPrincipal();
    }

    /**
     * Имеет ли пользователь в объекте типа projectRelationObjectEnum
     * с идентификатором objectId какие-то права из списка employeeRoles
     *
     * @param projectRelationObjectEnum тип объекта
     * @param objectId                  идентификатор объекта
     * @param employeeRoles                 роли пользователя
     * @return
     */
    public boolean hasRoleForObject(ProjectRelationObjectEnum projectRelationObjectEnum, long objectId,
                                    EmployeeRoleEnum... employeeRoles) {
        // TODO
        return false;
    }
}