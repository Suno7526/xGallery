# xGallery

Spring Boot 기반 X 사용자 및 게시물 갤러리 프로젝트입니다.

## 요구 사항

- Java 19 이상
- MySQL

## 실행

MySQL에 `xgallery` 데이터베이스를 만든 다음, Git Bash에서 접속 정보를 환경변수로 설정합니다.

```bash
export DB_URL='jdbc:mysql://localhost:3306/xgallery?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8&serverTimezone=Asia/Seoul'
export DB_USERNAME='your-mysql-user'
export DB_PASSWORD='your-mysql-password'
./gradlew bootRun
```
