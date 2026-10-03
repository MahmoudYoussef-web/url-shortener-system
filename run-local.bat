@echo off
REM Run the URL Shortener backend locally.
REM Uses the Docker MySQL shards (3307/3308 per application.properties and docker-compose.yml) and local Redis (6379).
REM App port is 8081 so it does not clash with the Docker setup on 8080.
REM
REM DB credentials are read from the .env file next to this script.
REM .env is git-ignored and must never be committed.
setlocal
if not exist "%~dp0.env" (
  echo [ERROR] Missing .env file. Copy .env.example to .env and set DB_PASSWORD.
  exit /b 1
)
for /f "usebackq tokens=1* delims==" %%a in ("%~dp0.env") do set %%a=%%b
if "%DB_PASSWORD%"=="" (
  echo [ERROR] DB_PASSWORD is not set in .env
  exit /b 1
)
set SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3307/url_shortener_0
set SPRING_DATASOURCE_USERNAME=dev_user
set SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD%
set APP_SHARDS_DATASOURCE_0_URL=jdbc:mysql://127.0.0.1:3307/url_shortener_0
set APP_SHARDS_DATASOURCE_1_URL=jdbc:mysql://127.0.0.1:3308/url_shortener_1
set SERVER_PORT=8081
set APP_BASE_URL=http://localhost:8081/api/v1/urls/
java -jar "%~dp0target\url-shortener.jar"
