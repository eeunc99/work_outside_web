# Work Outside Web

사외 근무(외근) 관리 시스템 - Spring Boot 기반 웹 애플리케이션

## 기술 스택

- Java 11, Spring Boot 2.7.12
- MyBatis (PostgreSQL)
- Thymeleaf
- Gradle

## 실행 방법

1. PostgreSQL DB를 준비합니다.
2. `src/main/resources/application-local.properties`에서 아래 값을 본인 환경에 맞게 채워주세요.

   ```properties
   server.ip = localhost
   spring.datasource.url=jdbc:log4jdbc:postgresql://YOUR_DB_HOST/work_outside?charSet=UTF-8
   spring.datasource.username=YOUR_DB_USER
   spring.datasource.password=YOUR_DB_PASSWORD
   ```

3. 빌드 및 실행

   ```bash
   ./gradlew bootRun
   ```

기본 포트는 8080입니다.
