# Task Tracker

Учебное клиент-серверное приложение «Трекер задач»: задачи, сгруппированные
по проектам, с фильтрацией по статусу.

- **Бэкенд**: Spring Boot 3.5 (Web, Data JPA, Validation, Actuator), Java 21, Maven
- **Фронтенд**: отдельное приложение — HTML + CSS + ванильный JavaScript, раздаётся nginx
- **БД**: PostgreSQL 16, миграции — Flyway (отдельный одноразовый контейнер)
- **Окружение**: Docker, Docker Compose

Полное описание доменной области, стека, REST API, сущностей и соответствия
12 факторам — в [`Отчёт.md`](Отчёт.md).

---

## Топология

Четыре сервиса в одной compose-сети, общение по DNS-именам:

| Сервис | Образ | Порт на хосте | Назначение |
|---|---|---|---|
| `frontend` | свой (`./frontend`, `nginx:1.29.8-alpine`) | `${FRONTEND_PORT}` → 80 | UI + reverse-proxy `/api` на backend |
| `backend` | свой (многостадийный `Dockerfile`) | `${BACKEND_PORT}` → `${SERVER_PORT}` | REST API |
| `migrate` | `flyway/flyway:11.7.2` | — | одноразовый накат миграций, `restart: "no"` |
| `db` | `postgres:16.15-alpine` | `${DB_HOST_PORT}` → 5432 | PostgreSQL |

Порядок старта задан через `depends_on`:
`db` (healthy) → `migrate` (exit 0) → `backend` (healthy) → `frontend` (healthy).
Если миграция упала — `backend` не стартует, причина видна в `docker compose ps -a`
и `docker compose logs migrate`.

---

## Требования

- Docker + Docker Compose (рекомендуемый способ)
- Либо для локальной разработки: JDK 21, Maven 3.9+, PostgreSQL 16

---

## Запуск

Всё в Docker:

```bash
cp .env.example .env      # обязательные DB_* заданы, при необходимости измените
docker compose up --build
```

После старта:

- Веб-интерфейс: http://localhost:8080/
- API напрямую: http://localhost:8081/api/tasks
- API через фронтенд-прокси: http://localhost:8080/api/tasks
- Health: http://localhost:8081/actuator/health

Остановить: `docker compose down` (с `-v` — вместе с volume БД).

## Конфигурация

Всё, что меняется между окружениями, читается из переменных окружения . Шаблон — `.env.example`.

| Переменная | Обязательна | Назначение |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | **да** | адрес и креды БД; значений по умолчанию нет |
| `DB_URL` | **да** (только локальный запуск вне Docker) | JDBC-URL; в Docker собирается из `DB_HOST`/`DB_PORT`/`DB_NAME` |
| `SERVER_PORT` | нет (`8080`) | порт backend внутри контейнера |
| `FRONTEND_PORT` | нет (`8080`) | порт UI на хосте |
| `BACKEND_PORT` | нет (`8081`) | порт API на хосте |
| `DB_HOST_PORT` | нет (`5432`) | порт postgres на хосте (для psql и IDE) |
| `INSTANCE_ID` | нет (`local`) | id экземпляра в `/api/info` |
| `SEED_DEMO` | нет | `1` — накатить демо-данные вместе со схемой |

## Миграции

- Схема (таблицы, индексы): `src/main/resources/db/migration`
- Демо-данные: `src/main/resources/db/seed` — **отдельно** от схемы,
  накатываются только при включенном `SEED_DEMO`
- Выполняет контейнер `migrate`, один раз за запуск;
  в самом приложении Flyway отключён, `spring.jpa.hibernate.ddl-auto: validate`
  контролирует соответствие схемы и JPA-модели
- Повторный прогон: `docker compose run --rm migrate`

⚠️ Если БД была поднята до того, как `V1__init.sql` был изменён (из него
убраны INSERT'ы — они переехали в `db/seed/V2__demo_data.sql`), Flyway упадёт
на checksum mismatch. Лечится одним из двух:

- `docker compose down -v` — снести volume и мигрировать с нуля;
- `docker compose run --rm migrate repair` — починить историю (данные не
  тронутся, но `V2` при этом не накатится — применяйте её отдельно).

То же правило действует, если включить `SEED_DEMO=1` на уже мигрированной БД.
## Разработка

```bash
mvn clean package       # сборка JAR → target/task-tracker-1.0.0.jar

docker compose logs -f backend     # логи API
docker compose logs -f migrate     # логи наката миграций
docker compose exec db psql -U "$DB_USER" -d "$DB_NAME"
```

Локальный запуск (БД в Docker, приложение из IDE):

```bash
docker compose up -d db
docker compose run --rm migrate
export DB_URL=jdbc:postgresql://localhost:5432/tasktracker \
       DB_USER=tasktracker DB_PASSWORD=tasktracker
mvn spring-boot:run
```

## Структура

```text
frontend/              отдельное приложение: nginx раздаёт статику и проксирует /api
  Dockerfile, nginx.conf.template, index.html, style.css, app.js
src/main/java/ru/tbank/edu/tasktracker/
  controller/          REST-контроллеры
  service/             бизнес-логика (@Transactional)
  repository/          Spring Data JPA
  model/               JPA-сущности и enum'ы
  dto/                 request/response records
  exception/           NotFoundException + @RestControllerAdvice
src/main/resources/
  application.yml      конфигурация (только чтение env-переменных)
  db/migration/        схема БД (Flyway)
  db/seed/             демо-данные (Flyway, отдельно от схемы)
Dockerfile             backend: стадии build (Maven) и run (JRE)
docker-compose.yml     топология: db, migrate, backend, frontend
```
