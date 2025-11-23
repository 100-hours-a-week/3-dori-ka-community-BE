# 📝 개인 프로젝트 - 커뮤니티 구현
본 프로젝트는 
**회원가입/로그인**, **게시글 CRUD**, **댓글 CRUD** 등을 포함한 풀스택 개인 프로젝트입니다. Spring Boot + Vanilla JS 기반의 커뮤니티 웹 서비스를 구현했습니다.

---

## 📚 기술 스택

### Backend
- Java 21
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Security
- MySQL
- AWS S3

### Frontend
- HTML / CSS
- Vanilla JS
---

## ✨ 주요 기능

### 🔐 인증/인가
- 로그인 / 로그아웃
- JWT Access Token / Refresh Token 발급
- Spring Security + JWTAuthenticationFilter 적용
- RefreshToken HTTP ONLY 쿠키 적용

### 주요 기능
- 회원가입
- 로그인 / 로그아웃
- 게시글 CRUD
- 댓글 CRUD
- Presigned-URL을 통해 Aws S3 파일 업로드
- 캐싱 기능 + Batch Update를 통한 조회수 Update
---

## 📁 프로젝트 구조
```markdown
### Backend (Spring Boot)

com.example.community
├── CommunityApplication.java
│
├── common
│   ├── BasicTimeEntity.java
│   │
│   ├── annotation
│   │   └── AuthUser.java
│   │
│   ├── aop
│   │   └── LoggingAspect.java
│   │
│   ├── exception
│   │   ├── ApiExceptionHandler.java
│   │   ├── ErrorMessage.java
│   │   ├── custom
│   │   │   ├── BadRequestException.java
│   │   │   ├── DuplicatedException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── UnauthorizedException.java
│   │   │   └── ForbiddenException.java
│   │   └── GlobalExceptionHandler.java
│   │
│   ├── util
│   │   └── ImageUtil.java
│   │
│   └── WebMvcConfig.java
│
├── config
│   ├── AwsS3Config.java
│   ├── CacheConfig.java
│   ├── JwtConfig.java
│   └── SecurityConfig.java
│
├── controller
│   ├── AuthController.java
│   ├── CommentController.java
│   ├── PostController.java
│   └── UserController.java
│
├── domain
│   ├── Comment.java
│   ├── Post.java
│   ├── PostImage.java
│   ├── RefreshToken.java
│   └── User.java
│
├── dto
│   ├── request
│   │   ├── user
│   │   │   ├── ChangePasswordDto.java
│   │   │   ├── UserLoginDto.java
│   │   │   ├── UserSignUpDto.java
│   │   │   └── UserUpdateDto.java
│   │   ├── post
│   │   │   ├── PostCreateDto.java
│   │   │   ├── PostUpdateDto.java
│   │   │   └── PostSearchCondition.java
│   │   └── comment
│   │       └── CommentCreateDto.java
│   │
│   ├── response
│   │   ├── user
│   │   │   ├── LoginResponse.java
│   │   │   └── UserDetailResponse.java
│   │   ├── post
│   │   │   ├── PostDetailResponse.java
│   │   │   ├── PostListResponse.java
│   │   │   └── PostImageResponse.java
│   │   └── comment
│   │       └── CommentResponse.java
│
├── repository
│   ├── comment
│   │   └── CommentRepository.java
│   ├── post
│   │   ├── PostImageRepository.java
│   │   └── PostRepository.java
│   ├── token
│   │   └── RefreshTokenRepository.java
│   └── user
│       └── UserRepository.java
│
├── security
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── jwt
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtInterceptor.java
│   │   ├── JwtUtil.java
│   │   └── TokenProvider.java (없으면 무시)
│
├── service
│   ├── auth
│   │   └── AuthServiceImpl.java
│   ├── user
│   │   └── UserServiceImpl.java
│   ├── post
│   │   └── PostServiceImpl.java
│   └── comment
│       └── CommentServiceImpl.java
```
### 핵심 기능
🔐 인증/인가 - JWT 기반

- Access Token + Refresh Token 구조로 인증 처리
- Access Token 만료 시 Refresh Token으로 재발급
- Refresh Token은 HttpOnly Cookie 로 발급해 보안 강화
- Spring Security + JWT AuthenticationFilter 로 인증 흐름 구성


⸻

🗂️ AWS S3 + Presigned-URL 기반 이미지 업로드

사용 이유
1.	서버 부하 감소: 이미지를 서버로 받지 않고 클라이언트 → S3 직접 업로드
2.	보안성 향상: S3 키를 클라이언트에 직접 노출하지 않음
3.	실시간 업로드 속도 개선: Presigned URL 은 짧은 시간만 유효하며 바로 업로드 가능
4.	서버는 메타데이터만 저장: 객체 키만 DB에 저장해 구조 단순화

적용 방식
- 서버에서 createdPresignedUrl로 Presigned-URL 생성
- 프론트엔드가 해당 URL로 이미지 업로드
- 업로드된 S3 객체 키를 사용해 이미지 URL 생성 후 게시글/프로필과 연결

⸻

⚡ 조회수 캐싱 + 배치 업데이트 (Spring Cache + Scheduled)

사용 이유
- 게시글이 많아질수록 조회수 UPDATE 쿼리 폭증 문제(=10000번 조회 → 10000 UPDATE) 발생
- 즉시 업데이트 방식은 성능 저하와 DB 부하를 초래함

해결 방법

✔ 1. Spring Cache 에 조회수 저장 (ConcurrentHashMap 기반)
- 게시글 조회 시 DB UPDATE 대신 캐시에서 +1 증가

✔ 2. 일정 주기(1분) 또는 임계치 도달 시 일괄 DB 업데이트
- Scheduler 로 60초마다 캐시 내용을 모두 DB에 반영
- 업데이트는 JDBCTemplate Batch Update를 통해 쿼리 수 최소화

### 💡트러블 슈팅
https://www.notion.so/0-2ab34a3a75ae80bb8c7ecb910793d525?v=2a734a3a75ae8171b217000cc28a7890&source=copy_link

### 시연 영상