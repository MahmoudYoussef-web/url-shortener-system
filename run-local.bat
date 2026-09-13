@echo off
REM Run the URL Shortener backend + SnipLink UI locally.
REM Uses the Docker MySQL shards (3308/3309) and local Redis (6379).
REM App port is 8081 because 8080 is taken by another project on this machine.
setlocal
set DB_PASSWORD=CHANGE_ME
set SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3309/url_shortener_0
set SPRING_DATASOURCE_USERNAME=dev_user
set SPRING_DATASOURCE_PASSWORD=CHANGE_ME
set APP_SHARDS_DATASOURCE_0_URL=jdbc:mysql://127.0.0.1:3309/url_shortener_0
set APP_SHARDS_DATASOURCE_1_URL=jdbc:mysql://127.0.0.1:3308/url_shortener_1
set SERVER_PORT=8081
set APP_BASE_URL=http://localhost:8081/api/v1/urls/
java -jar "%~dp0target\url-shortener.jar"
