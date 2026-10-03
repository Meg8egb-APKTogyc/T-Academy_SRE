INSERT INTO projects (name, description) VALUES
    ('Учёба', 'Учебные задачи и домашние задания'),
    ('Личное', 'Личные дела');

INSERT INTO tasks (title, description, status, priority, due_date, project_id) VALUES
    ('Сделать домашнее задание по SRE', 'Клиент-серверное приложение с PostgreSQL', 'IN_PROGRESS', 'HIGH', CURRENT_DATE + 7, 1),
    ('Прочитать про 12 факторов', 'https://12factor.net/ru/', 'NEW', 'MEDIUM', NULL, 1),
    ('Купить продукты', 'Молоко, хлеб, кофе', 'DONE', 'LOW', CURRENT_DATE, 2);
