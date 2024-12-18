# Simple-Messenger Сервер

MVP мессенджера с внешней аутентификацией. 

- Умеет запоминать пользователей только по OAuth
- История сообщений не хранится, а удаляется сразу при получении
  (что может вызвать некоторые проблемы при одновременном доступе двух клиентов)
- База данных прибита гвоздями - PostgreSQL по локальному адресу
- Серверная часть сделана на http4s
- Для соединения с базой используется Doobie

На данный момент состояние сыровато(и в целом соответствует статусу MVP), по хорошему надо: 
написать тесты, переосмыслить архитектуру сервисов, 
добавить middleware с авторизацией и конечно конфиг с настройками сервера