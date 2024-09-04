package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.entity.field.pk.UserRolePK;

import java.util.List;
import java.util.Set;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRolePK> {
    List<UserRole> findAllByUserId(int userId);

    List<UserRole> findAllByUserIdIn(List<Integer> userIds);

    List<UserRole> findAllByRoleId(UserRoleEnum roleId);

    List<UserRole> findAllByRoleIdIn(Set<UserRoleEnum> roleIds);

    void deleteAllByUserId(int userId);
}
