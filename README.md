# Task Tracker

Учебное клиент-серверное приложение «Трекер задач»: задачи, сгруппированные
по проектам, с фильтрацией по статусу.

- **Бэкенд**: Spring Boot 3.5 (Web, Data JPA, Validation, Actuator), Java 21, Maven
- **БД**: PostgreSQL 16, миграции — Flyway
- **Фронтенд**: HTML + CSS + ванильный JavaScript (раздаётся Spring Boot как статика)
- **Окружение**: Docker, Docker Compose

Полное описание доменной области, стека, REST API, сущностей и соответствия
12 факторам — в [`Отчёт.md`](Отчёт.md).

---

## Требования

- Docker + Docker Compose (рекомендуемый способ)
- Либо для локальной разработки: JDK 21, Maven 3.9+, PostgreSQL 16

---

## Запуск

Всё в Docker:

```bash
cp .env.example .env      # опционально, есть дефолты
docker compose up --build
```

Локально (БД в Docker, приложение из IDE):

```bash
docker compose up -d db
mvn spring-boot:run
```

После старта:

- Веб-интерфейс: http://localhost:8080/
- API: http://localhost:8080/api/tasks
- Health: http://localhost:8080/actuator/health

Остановить: `docker compose down` (с `-v` — вместе с volume БД).

## Конфигурация

Всё, что меняется между окружениями, читается из переменных окружения
(см. `application.yml` — только `${ENV:default}`, секретов в коде нет).
Шаблон — `.env.example`, реальный `.env` в `.gitignore`.

Основные переменные: `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`DB_NAME`, `INSTANCE_ID`. Полная таблица — в `Отчёт.md`.

## Разработка

```bash
mvn test                # тесты
mvn clean package       # сборка JAR → target/task-tracker-1.0.0.jar
java -jar target/task-tracker-1.0.0.jar

docker compose logs -f app
docker compose exec db psql -U tasktracker -d tasktracker
```

Схема БД управляется Flyway (`src/main/resources/db/migration`),
Hibernate в режиме `validate` схему не меняет.

## Структура

```text
src/main/java/ru/tbank/edu/tasktracker/
  controller/   REST-контроллеры
  service/      бизнес-логика (@Transactional)
  repository/   Spring Data JPA
  model/        JPA-сущности и enum'ы
  dto/          request/response records
  exception/    NotFoundException + @RestControllerAdvice
src/main/resources/
  application.yml
  db/migration/ Flyway-миграции
  static/       фронтенд (index.html, style.css, app.js)
```