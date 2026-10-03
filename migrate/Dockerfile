FROM flyway/flyway:11.7.2

# Копируем SQL из репозитория в образ — как артефакт
COPY src/main/resources/db/migration /flyway/sql/migration
COPY src/main/resources/db/seed      /flyway/sql/seed

COPY migrate/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["migrate"]