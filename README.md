# 할 일·메모 관리 API

Spring Boot로 할 일과 메모의 등록, 조회, 수정, 삭제를 구현한 실습 프로젝트다.
할 일은 제목과 완료 여부를, 메모는 제목과 내용을 저장한다.
데이터는 메모리에 저장하므로 서버를 재시작하면 초기화된다.
POST, GET, PUT, DELETE를 각각 두 개씩 구현했으며, 공통 응답 형식과 요청 로그를 적용했다.

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

| 구분 | 필드 | 타입 | 설명 |
| --- | --- | --- | --- |
| 공통 | `id` | 정수 | 등록 시 서버에서 부여하는 ID |
| 공통 | `title` | 문자열 | 할 일 또는 메모 제목 |
| 할 일 | `completed` | boolean | 완료 여부 |
| 메모 | `content` | 문자열 | 메모 내용 |

`id`는 요청 본문에 넣지 않는다. 할 일과 메모의 ID는 각각 1부터 증가하며, 목록은 ID 순서로 반환한다.

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
두 목록 조회 API에서 `simulate=500` 또는 `simulate=503`을 사용할 수 있다.
`practice.failure-simulation-enabled=false`로 설정하면 이 옵션을 끌 수 있다.

## 코드 구성

- `WsdTeachingApplication`: Spring Boot 애플리케이션 실행
- `task`, `note`: Controller에서 요청을 받고, Service에서 등록·조회·수정·삭제 처리
- `TaskRequest`, `NoteRequest`: 요청 데이터와 필수값·길이 검증
- `common`: `ApiResponse`로 응답을 감싸고, `GlobalExceptionHandler`에서 오류 처리. `FailureSimulator`는 500·503 응답 재현
- `config`: `WebConfig`에서 Interceptor를 등록하고, `JsonConfig`에서 JSON 값의 타입 검사 설정

Service는 `ConcurrentHashMap`에 데이터를 저장하고 `AtomicLong`으로 ID를 생성한다.
Controller에서 검증을 통과한 요청만 Service로 전달한다.

`LoggingInterceptor`를 `/api/**`에 연결해 요청 메소드, 경로, 쿼리와 응답 코드를 콘솔에 출력한다.
두 API는 `ApiResponse`와 `GlobalExceptionHandler`를 함께 사용해 응답 형식을 맞췄다.
등록 응답에는 생성한 데이터의 경로를 `Location` 헤더로 전달한다.
삭제도 같은 JSON 형식을 반환하도록 200을 사용했다.

예를 들어 할 일 목록을 조회하면 다음 로그가 남는다.

```text
[LoggingInterceptor] 요청 시작: GET /api/tasks
[LoggingInterceptor] 요청 완료: GET /api/tasks status=200
```

## 실행

JDK 17을 설정한 뒤 프로젝트 폴더에서 실행한다.
IntelliJ에서는 `WsdTeachingApplication`의 `main` 메소드를 실행해도 된다.

```bash
./gradlew bootRun
```

기본 주소는 `http://localhost:8080`이다. 실행 후 `/api/tasks` 또는 `/api/notes`에서 목록을 확인할 수 있다.

## 사용 예시

터미널에서 할 일을 등록한 뒤 조회·수정·삭제하는 예시다.
`-i` 옵션으로 응답 코드와 헤더도 함께 확인할 수 있다.

```bash
# 등록
curl -i -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"실습 과제 정리","completed":false}'

# 목록 조회
curl -i http://localhost:8080/api/tasks

# 수정
curl -i -X PUT http://localhost:8080/api/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"title":"실습 과제 정리 완료","completed":true}'

# 삭제
curl -i -X DELETE http://localhost:8080/api/tasks/1
```

수정·삭제 예시의 `1`은 등록 응답으로 받은 ID로 바꿔 사용한다.
메모도 `/api/notes`에 `title`, `content`를 보내 같은 방식으로 사용할 수 있다.

참고: [수업 Spring Boot 예제](https://github.com/JBNU-KSL-25-02/springboot-tutorial)
