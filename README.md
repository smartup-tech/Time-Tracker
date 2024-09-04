# Timetracker
Timetracker - сервис учета рабочего времени. Продукт предназначен для компаний, стремящихся наладить почасовую оплату и сделать прозрачным прогресс по решению задач.

&copy; Разработано [Smart Up](https://smartup.ru/ "Smartup")

### Преимущества
- Быстрое внедрение системы учета времени сотрудников в свой бизнес
- Беспрепятсвенное расширение продукта

### Функционал
- Отписывание часов
- Недельный отчет отработанного времени
- Подробный отчет по каждому сотруднику за рабочий период
- Персональный и проектный отчет
- Производственный календарь

### Репозитории
Timetracker - это клиент-серверное приложение с двумя репозиториями:
- [Timetracker-UI](https://github.com/smartup-tech/Time-Tracker-UI "Timetracker-UI")
- [Timetracker-BE](https://github.com/smartup-tech/Time-Tracker "Timetracker-BE")

#### Аналоги
[BigTime Tracking Software](https://www.bigtime.net/features/time-and-expense-tracking-software/)

## Как использовать Timetracker-BE

### Локальнный запуск
Для развертывания приложения подготовлен ``docker-compose.yml`` в директории ``./ops-tools/docker``

Не забудьте установить значения переменных в `./ops-tools/docker/.env`

1) Установите [Docker](https://docs.docker.com/engine/install/ "Docker") для своей операционной системы.

2) Перейдите в директорию ``./ops-tools/docker``

3) Запуск проекта локально возможен двумя способами:
- Запуск PostgreSQL при помощи docker контейнера (для запуска приложения используйте IDE) ``docker compose up db``
- Запуск в docker контейнере PostgreSQL и  Timetracker-BE.
  - ``docker compose build``
  - ``docker compose up``

4) Полезные команды
- Очистка базы данных:
  - ``docker volume rm docker_timetracker_data_volume``
- Проверить состояние запущенного контейнера:
  - ``docker compose ps``
- Остановить приложение:
  - ``docker compose stop``
- Остановить базу данных:
  - ``docker compose down db``

### CI/CD
1) Откройте директорию ``./ops-tools``
2) Запустите скрипт ``./build-image.sh`` для сборки и публикации docker образа. Вы должны изменить имя репозитория, чтобы иметь возможность опубликовать изображение.
3) Запустите ``./run-image.sh`` для запуска подготовленного образа. Вы можете изменять переменные окружения как вам необходимо.

# Swagger

[http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)
