# java-shareit

Данное приложение - сервис для "шэринга вещей". Оно позволяет пользователям рассказывать какими вещами они готовы поделиться, находить нужную вещь и брать её в аренду на какое-то время. 

_**Используемый стэк**_: Spring Boot, Spring Data JPA, PostgreSQL, Lombok, JUnit, Mockito, Mapstruct, Docker, Docker Compose.

---

Приложение состоит из _двух сервисов_:
- шлюз (gateway). В него вынесены контроллеры, с которыми непосредственно взаимодейтсвуют пользователи, а также валидация данных;
- основной сервис.

_**Основные возможности приложения**_:
- добавление, редаткирование вещей, выставленных для аренды;
- поиск вещей по навзванию или описанию;
- добавление, подтверждение или отклонение запроса на бронирование;
- получение списка всех бронирований с фильтром по статусу;
- написание отзыва на вещь после того, как взяли ее в аренду;
- создание запроса на добавление вещи, когда пользователь не может найти нужную вещь, воспользовавшись поиском;
- пользователи могут просматривать подобные запросы и, если у них есть описанная вещь, добавлять нужную вещь в ответ на запрос;
- использование пагинации при просмотре бронирований и вещей;

---

Приложение написано на Java 11 с использованием Spring Boot 2.7.9 и базы данных PostgreSQL.
