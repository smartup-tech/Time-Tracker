package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.pk.EmployeeRolePK;

import java.util.List;
import java.util.Set;

public interface EmployeeRoleRepository extends JpaRepository<EmployeeRole, EmployeeRolePK> {
    List<EmployeeRole> findAllByEmployeeId(int employeeId);

    List<EmployeeRole> findAllByEmployeeIdIn(List<Integer> employeeIds);

    List<EmployeeRole> findAllByRoleId(EmployeeRoleEnum roleId);

    List<EmployeeRole> findAllByRoleIdIn(Set<EmployeeRoleEnum> roleIds);

    void deleteAllByEmployeeId(int employeeId);
}
