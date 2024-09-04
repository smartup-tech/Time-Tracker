package ru.smartup.timetracker.core;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import ru.smartup.timetracker.service.RelationUserRolesService;

public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    private final RelationUserRolesService relationUserRolesService;

    public CustomMethodSecurityExpressionHandler(RelationUserRolesService relationUserRolesService) {
        this.relationUserRolesService = relationUserRolesService;
    }

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
                                                                              MethodInvocation invocation) {
        CustomMethodSecurityExpressionRoot customMethodSecurityExpressionRoot
                = new CustomMethodSecurityExpressionRoot(authentication, relationUserRolesService);
        customMethodSecurityExpressionRoot.setPermissionEvaluator(getPermissionEvaluator());
        customMethodSecurityExpressionRoot.setTrustResolver(authenticationTrustResolver);
        customMethodSecurityExpressionRoot.setRoleHierarchy(getRoleHierarchy());
        return customMethodSecurityExpressionRoot;
    }
}