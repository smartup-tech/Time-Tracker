DELETE FROM user_project_role upr
WHERE (upr.user_id, upr.project_id) IN (
    SELECT user_id, project_id
    FROM user_project_role upr
    GROUP BY user_id, project_id
    HAVING count(user_id) > 1
) AND upr.project_role_id = 'EMPLOYEE';

ALTER TABLE user_project_role DROP CONSTRAINT user_project_role_pkey;
ALTER TABLE user_project_role ADD PRIMARY KEY (user_id, project_id);