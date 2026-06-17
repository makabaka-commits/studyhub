USE studyhub;

INSERT INTO user (username, password, nickname, status)
VALUES
('test03', '123456', '测试用户3', 1),
('user_b', '123456', '用户B', 1)
ON DUPLICATE KEY UPDATE username = username;

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