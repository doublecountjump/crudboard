# CRUD 기능의 커뮤니티 사이트

인벤, 디시인사이드의 기능을 참고하였습니다.
Spring Boot를 활용한 게시판 서비스로, 사용자 인증(로컬 및 OAuth2), 게시글 작성/조회/삭제, 댓글 및 대댓글, 좋아요 기능을 제공합니다.

## 프로젝트 개요

이 프로젝트는 Spring Boot 기반의 웹 애플리케이션으로, 전통적인 게시판 기능을 구현하면서 현대적인 웹 개발의 다양한 기술요소를 적용했습니다.

### 주요 기능

- **사용자 관리**
  - 로컬 회원가입/로그인
  - GitHub OAuth2 소셜 로그인
  - JWT 토큰 기반 인증
  - 리프레시 토큰을 통한 인증 연장

- **게시글 관리**
  - 게시글 작성, 조회, 삭제
  - 조회수 관리 (Redis 활용)
  - 페이지네이션

- **댓글 시스템**
  - 댓글 작성 및 삭제
  - 대댓글(답글) 기능 지원
  - 댓글 소유자 확인을 통한 권한 관리

- **좋아요 기능**
  - 게시글에 대한 좋아요 추가/삭제
  - 사용자별 좋아요 상태 관리

## 기술 스택

- **Backend**
  - Java 17
  - Spring Boot 3.4.1
  - Spring Security
  - Spring Data JPA
  - Spring Data Redis
  - JWT
  
- **Database**
  - MySQL (메인 데이터베이스)
  - Redis (조회수 캐싱)

- **Frontend**
  - Thymeleaf
  - HTML/CSS/JavaScript
  
- **인증/인가**
  - OAuth2 (GitHub)
  - JWT 토큰
  - 커스텀 AOP를 통한 리소스 접근 제어

## 프로젝트 구조
![image](https://github.com/user-attachments/assets/98f4bf6a-f484-43b0-9e72-d810d3024209)


```
src/main/java/test/crudboard/
├── annotation            # 커스텀 어노테이션 (AOP)
├── controller            # 컨트롤러 계층
├── entity                # JPA 엔티티 및 DTO
│   └── dto               # 데이터 전송 객체
│   └── enumtype          # 열거형 상수
├── filter                # 필터 (JWT 인증)
├── provider              # 인증 제공자 (로컬, OAuth2)
│   ├── local             # 로컬 인증
│   └── oauth             # OAuth2 인증
├── repository            # 데이터 접근 계층
├── service               # 비즈니스 로직 계층
└── config                # 설정 클래스
```

## 주요 기능 설명

### 인증 시스템

1. **다중 인증 방식 지원**
   - 로컬 계정을 통한 이메일/비밀번호 인증
   - GitHub OAuth2를 통한 소셜 로그인
   - JWT 토큰 기반의 stateless 인증

2. **JWT 인증 흐름**
   - 로그인 성공 후 `LoginSuccessHandler`에서 JWT 토큰 발급
   - 쿠키를 통한 JWT 토큰 전달
   - `JwtAuthenticationFilter`에서 토큰 검증 및 인증 처리
   - 리프레시 토큰을 통한 JWT 갱신 지원

### 게시판 기능

1. **게시글 관리**
   - 제목, 내용, 작성자 정보 포함
   - Redis를 활용한 조회수 캐싱으로 성능 최적화
   - 사용자별 게시글 접근 권한 관리

2. **댓글 시스템**
   - 계층형 댓글 구조 (댓글-대댓글)
   - 댓글 작성/삭제 기능
   - 소유권 확인을 통한 접근 제어

3. **좋아요 기능**
   - 게시글에 대한 좋아요/취소 기능
   - 사용자별 좋아요 상태 표시
  
### 성능 개선

1. **요청 속도 개선**
   - 쿼리 개선을 통하여 조회속도 개선
   - 게시글 목록 조회 기준 평균 조회 속도 120ms -> 약 8ms로 개선
   - 게시글 상세조회(댓글 90개 기준) 조회속도 150ms -> 약 20ms로 개선
  
3. **부하테스트**
   - 게시글 100만개 기준
   - 게시글 목록 조회는 약 1000개의 요청까지 평균 500ms내외를 기록했음
   - 게시글 상세조회(댓글 90개 기준)는 약 400개의 요청까지 평균 500ms 내외를 기록했음

### MCP를 사용한 게시글 불러오기
   - MCP 서버를 만들어서 특정 명령어 실행 시 API를 통한 데이터 전달(오늘 인기있는 게시글 알려줘, XX게시글 보여줘)
     ![image](https://github.com/user-attachments/assets/ff5dcabe-746f-49a5-a1cc-04114315bcff)


