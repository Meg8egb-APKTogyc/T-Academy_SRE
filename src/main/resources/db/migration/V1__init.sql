CREATE TABLE projects (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(200)  NOT NULL UNIQUE,
    description VARCHAR(2000)
);

CREATE TABLE tasks (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title       VARCHAR(300)  NOT NULL,
    description VARCHAR(5000),
    status      VARCHAR(20)   NOT NULL DEFAULT 'NEW',
    priority    VARCHAR(20)   NOT NULL DEFAULT 'MEDIUM',
    due_date    DATE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    project_id  BIGINT REFERENCES projects (id) ON DELETE SET NULL
);

CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_tasks_project_id ON tasks (project_id);

INSERT INTO projects (name, description) VALUES
    ('Учёба', 'Учебные задачи и домашние задания'),
    ('Личное', 'Личные дела');

INSERT INTO tasks (title, description, status, priority, due_date, project_id) VALUES
    ('Сделать домашнее задание по SRE', 'Клиент-серверное приложение с PostgreSQL', 'IN_PROGRESS', 'HIGH', CURRENT_DATE + 7, 1),
    ('Прочитать про 12 факторов', 'https://12factor.net/ru/', 'NEW', 'MEDIUM', NULL, 1),
    ('Купить продукты', 'Молоко, хлеб, кофе', 'DONE', 'LOW', CURRENT_DATE, 2);
