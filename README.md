# 할 일·메모 관리 API

Spring Boot로 할 일과 메모의 등록, 조회, 수정, 삭제를 구현한 실습 프로젝트다.
할 일은 제목과 완료 여부를, 메모는 제목과 내용을 저장한다.
데이터는 메모리에 저장하므로 서버를 재시작하면 초기화된다.

Java 17 / Spring Boot 4.1.1 / Gradle

## API

| 메소드 | 주소 | 기능 |
| --- | --- | --- |
| POST | `/api/tasks` | 할 일 등록 |
| GET | `/api/tasks` | 할 일 목록 조회 |
| PUT | `/api/tasks/{id}` | 할 일 수정 |
| DELETE | `/api/tasks/{id}` | 할 일 삭제 |
| POST | `/api/notes` | 메모 등록 |
| GET | `/api/notes` | 메모 목록 조회 |
| PUT | `/api/notes/{id}` | 메모 수정 |
| DELETE | `/api/notes/{id}` | 메모 삭제 |

등록할 때 받은 `id`로 수정하거나 삭제한다. PUT은 전체 수정이므로 모든 필드를 보내야 한다.
없는 ID로 수정하거나 삭제하면 404를 반환한다.

## 요청 데이터

할 일 등록·수정:

```json
{
  "title": "실습 과제 정리",
  "completed": false
}
```

메모 등록·수정:

```json
{
  "title": "수업 메모",
  "content": "HTTP 메소드별로 API 구현하기"
}
```

제목은 100자, 메모 내용은 10,000자까지 입력할 수 있다. 앞뒤 공백을 제거한 뒤 길이를 검사한다.
필수 필드가 빠졌거나 공백만 입력된 경우는 400으로 처리한다.
`completed`는 문자열이 아닌 boolean 값으로 받는다.
POST와 PUT의 요청 본문은 `application/json`을 사용한다.

## 응답

등록·수정·삭제는 해당 데이터를, 목록 조회는 배열을 `data`에 담는다.

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "title": "실습 과제 정리",
    "completed": false
  }
}
```

오류가 발생한 경우:

```json
{
  "status": "error",
  "data": {
    "code": "NOT_FOUND",
    "message": "해당 할 일을 찾을 수 없습니다.",
    "details": {}
  }
}
```

입력 검증에 실패하면 `details`에 필드별 오류 메시지가 들어간다.

| 코드 | 사용한 상황 |
| --- | --- |
| 200 | 조회, 수정, 삭제 성공 |
| 201 | 등록 성공 |
| 400 | 입력값이나 ID 형식 오류 |
| 404 | 없는 데이터나 경로 요청 |
| 500 | `GET /api/tasks?simulate=500` |
| 503 | `GET /api/notes?simulate=503` |

500과 503은 오류 응답을 확인하기 위한 옵션이다.
`practice.failure-simulation-enabled=false`로 설정하면 이 옵션을 끌 수 있다.

## 코드 구성

- `task`, `note`: 각 API의 Controller, Service, 요청 DTO와 데이터 모델
- `common`: 공통 응답과 예외 처리
- `config`: 요청 로그 Interceptor와 JSON 입력 타입 설정

`LoggingInterceptor`를 `/api/**`에 연결해 요청 메소드, 경로, 쿼리와 응답 코드를 콘솔에 출력한다.
두 API는 `ApiResponse`와 `GlobalExceptionHandler`를 함께 사용해 응답 형식을 맞췄다.
등록 응답에는 생성한 데이터의 경로를 `Location` 헤더로 전달한다.
삭제도 같은 JSON 형식을 반환하도록 200을 사용했다.

## 실행

```bash
./gradlew bootRun
```

주소: `http://localhost:8080`

참고: [수업 Spring Boot 예제](https://github.com/JBNU-KSL-25-02/springboot-tutorial)
