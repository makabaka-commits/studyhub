USE studyhub;

INSERT INTO user (username, password, nickname, role, status)
VALUES
('test03', '$2a$10$wVYomiQoHVn13n9BUnp3nu9uK1ylQ.mOj8uDHPm/ycyqJxIeVO9BO', '测试用户3', 'ADMIN', 1),
('user_b', '$2a$10$wVYomiQoHVn13n9BUnp3nu9uK1ylQ.mOj8uDHPm/ycyqJxIeVO9BO', '用户B', 'USER', 1)
ON DUPLICATE KEY UPDATE
password = VALUES(password),
nickname = VALUES(nickname),
role = VALUES(role),
status = VALUES(status);

INSERT INTO note (user_id, title, content, view_count, like_count, favorite_count, status)
VALUES
(1, 'StudyHub 项目笔记', '这是 StudyHub 的测试笔记内容', 0, 0, 0, 1),
(1, 'Spring Boot 学习笔记', 'Spring Boot 可以帮助我们快速开发后端接口', 0, 0, 0, 1);

INSERT INTO tag (name)
VALUES
('Java'),
('Spring Boot'),
('MySQL'),
('Redis')
ON DUPLICATE KEY UPDATE name = name;
