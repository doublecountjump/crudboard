# 코드 구조 개선 가이드

이 문서는 코드 구조 개선에 관한 내용을 담고 있습니다.

## 개선된 패키지 구조

```
src/main/java/test/crudboard/
├── domain/                  # 도메인 모듈
│   ├── post/                # 게시물 관련 클래스
│   │   ├── controller/      # 컨트롤러
│   │   ├── dto/             # 데이터 전송 객체
│   │   ├── entity/          # 엔티티
│   │   ├── repository/      # 리포지토리
│   │   └── service/         # 서비스
│   ├── user/                # 사용자 관련 클래스
│   ├── comment/             # 댓글 관련 클래스
│   └── like/                # 좋아요 관련 클래스
├── global/                  # 공통 모듈
│   ├── config/              # 설정 클래스
│   ├── error/               # 에러 처리
│   ├── security/            # 보안 관련
│   │   ├── filter/
│   │   ├── provider/
│   │   └── dto/
│   └── util/                # 유틸리티 클래스
└── infrastructure/          # 외부 인프라 연동
    ├── redis/               # Redis 관련
    └── storage/             # 파일 저장소 관련
```

## 변경 내역

1. 패키지 구조를 도메인 중심으로 재구성
2. 관련 클래스들을 함께 묶어 응집도 향상
3. 공통 코드를 global 패키지로 분리
4. 외부 인프라 연동 코드를 infrastructure 패키지로 분리
