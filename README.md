# HackerRank GitHub Logger 🚀

HackerRank에서 문제를 제출할 때 자동으로 GitHub에 저장하는 Java Spring Boot 기반 시스템입니다.

## ✨ 주요 기능

- ✅ HackerRank 제출을 자동으로 감지하여 저장
- ✅ Git 명령어를 통한 자동 커밋 & 푸시
- ✅ 언어별 자동 분류 (17개 이상 언어 지원)
- ✅ 문제 메타데이터 자동 기록 (문제명, 난이도, URL 등)
- ✅ 비동기 처리로 빠른 응답
- ✅ Tampermonkey 브라우저 스크립트로 편리한 사용

## 📋 시스템 요구사항

- **Java 17 이상**
- **Gradle** (포함됨)
- **Git** (시스템에 설치되어 있어야 함)
- **브라우저 확장**: [Tampermonkey](https://www.tampermonkey.net/)

## 🛠️ 설치 및 설정

### 1. Git 저장소 설정

이 프로젝트는 Git 명령어를 사용하여 직접 커밋 및 푸시합니다.
**반드시 프로젝트가 Git 저장소 내에 있어야 합니다!**

```bash
# HackerRank 저장소가 없으면 생성
cd /path/to/your/workspace
git clone https://github.com/YOUR_USERNAME/hackerrank.git
cd hackerrank

# 또는 새로운 저장소 초기화
mkdir hackerrank && cd hackerrank
git init
git remote add origin https://github.com/YOUR_USERNAME/hackerrank.git

# 프로젝트 클론
cd hackerrank
# 이 프로젝트를 hackerrank 저장소 안에 배치
```

### 2. 프로젝트 빌드

```bash
cd hackerrank-log

# Gradle 빌드
./gradlew build

# 또는 IntelliJ IDEA / Eclipse에서 프로젝트 열기
```

### 3. 설정 파일 수정 (선택사항)

`src/main/resources/application.properties` 파일을 편집:

```properties
# 서버 포트 (기본값: 8080)
server.port=8080

# 솔루션 저장 경로 (기본값: ./solutions)
hackerrank.solution.path=./solutions

# Git 원격 저장소 이름 (기본값: origin)
hackerrank.git.remote=origin

# Git 브랜치 이름 (기본값: main)
hackerrank.git.branch=main
```

### 4. 서버 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 JAR 파일 빌드 후 실행
./gradlew bootJar
java -jar build/libs/hackerrank-log-1.0.0.jar
```

서버가 `http://localhost:8080`에서 실행됩니다!

### 5. Tampermonkey 스크립트 설치

1. **Tampermonkey 설치**
   - [Chrome](https://chrome.google.com/webstore/detail/tampermonkey/)
   - [Firefox](https://addons.mozilla.org/firefox/addon/tampermonkey/)

2. **스크립트 추가**
   - Tampermonkey 아이콘 클릭 → "새 스크립트 생성"
   - `userscript.js` 파일의 내용을 복사 붙여넣기
   - 서버 URL 확인: `SERVER_URL = 'http://localhost:8080/api/hackerrank/submit'`
   - Ctrl+S (Cmd+S) 저장

## 📖 사용 방법

### 서버 실행 확인

```bash
# 상태 확인
curl http://localhost:8080/api/hackerrank/health

# Git 저장소 상태 확인
curl http://localhost:8080/api/hackerrank/git/status
```

### HackerRank 문제 풀기

1. 서버가 실행 중인지 확인
2. HackerRank 웹사이트에서 문제 풀기
3. 코드 작성 후 "Submit Code" 클릭
4. 자동으로 저장 및 Git 푸시!
5. 브라우저 알림으로 성공/실패 확인

### 수동 테스트

cURL로 직접 테스트할 수 있습니다:

```bash
curl -X POST http://localhost:8080/api/hackerrank/submit \
  -H "Content-Type: application/json" \
  -d '{
    "problemName": "Two Sum",
    "code": "def twoSum(nums, target):\n    return [0, 1]",
    "language": "python3",
    "challengeId": "two-sum",
    "difficulty": "Easy",
    "tags": ["array", "hash-table"]
  }'
```

## 📁 저장 구조

```
solutions/
├── python3/
│   ├── Two-Sum.py
│   └── Reverse-String.py
├── java/
│   └── Valid-Parentheses.java
└── cpp/
    └── Binary-Search.cpp
```

각 파일에는 메타데이터가 포함됩니다:

```python
# Problem: Two Sum
# Language: python3
# Submitted: 2025-11-11 15:30:00
# Challenge ID: two-sum
# URL: https://www.hackerrank.com/challenges/two-sum
# Difficulty: Easy
# Tags: array, hash-table
#

def twoSum(nums, target):
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []
```

## 🌐 API 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/hackerrank/health` | 서버 상태 확인 |
| GET | `/api/hackerrank/git/status` | Git 저장소 상태 확인 |
| POST | `/api/hackerrank/submit` | 제출 저장 및 Git 푸시 |
| POST | `/api/hackerrank/log` | 로컬 저장만 (Git 푸시 없음) |

### 요청 예시

```json
{
  "problemName": "Two Sum",
  "code": "def solution(): pass",
  "language": "python3",
  "challengeId": "two-sum",
  "difficulty": "Easy",
  "tags": ["array", "hash-table"]
}
```

### 응답 예시

```json
{
  "success": true,
  "message": "HackerRank 제출이 GitHub에 저장되었습니다",
  "filePath": "/path/to/solutions/python3/Two-Sum.py",
  "gitMessage": "✅ GitHub에 저장 완료: /path/to/solutions/python3/Two-Sum.py"
}
```

## 🎯 지원 언어

Python, Java, C++, C, JavaScript, TypeScript, Go, Rust, Kotlin, Swift, Ruby, PHP, C#, SQL, Scala, R, Bash 등 17개 이상

## 🔧 트러블슈팅

### 1. 서버 연결 실패

**문제**: "서버 연결 실패" 알림

**해결**:
```bash
# 서버 실행 확인
curl http://localhost:8080/api/hackerrank/health

# 포트 충돌 확인
lsof -i :8080

# 로그 확인
./gradlew bootRun
```

### 2. Git 푸시 실패

**문제**: "Git 저장 실패" 메시지

**해결**:
```bash
# Git 저장소 확인
git status

# 원격 저장소 확인
git remote -v

# 인증 설정 (SSH 키 또는 Personal Access Token)
git config --global user.name "Your Name"
git config --global user.email "your@email.com"

# SSH 키 설정 또는 HTTPS 인증 설정
```

### 3. 제출이 감지되지 않음

**문제**: 제출해도 저장되지 않음

**해결**:
1. Tampermonkey 활성화 확인
2. 브라우저 콘솔(F12) → Console 탭에서 `[HackerRank Logger]` 로그 확인
3. 서버 URL이 올바른지 확인: `http://localhost:8080/api/hackerrank/submit`
4. HackerRank 페이지 새로고침

### 4. Git 인증 문제

**해결 방법 1: SSH 키 사용 (권장)**
```bash
# SSH 키 생성
ssh-keygen -t ed25519 -C "your@email.com"

# 공개 키를 GitHub에 등록
cat ~/.ssh/id_ed25519.pub

# Git 원격 저장소를 SSH로 변경
git remote set-url origin git@github.com:YOUR_USERNAME/hackerrank.git
```

**해결 방법 2: Personal Access Token 사용**
```bash
# GitHub에서 Personal Access Token 생성
# Settings → Developer settings → Personal access tokens → Generate new token
# repo 권한 선택

# Git 자격증명 저장
git config --global credential.helper store

# 첫 푸시 시 토큰 입력
git push
# Username: YOUR_USERNAME
# Password: ghp_xxxxxxxxxxxxxxxxxxxx (Personal Access Token)
```

## 🏗️ 프로젝트 구조

```
hackerrank-log/
├── src/
│   ├── main/
│   │   ├── java/com/hackerrank/log/
│   │   │   ├── HackerRankLogApplication.java    # 메인 애플리케이션
│   │   │   ├── controller/
│   │   │   │   └── HackerRankController.java    # REST API 컨트롤러
│   │   │   ├── service/
│   │   │   │   ├── HackerRankService.java       # 비즈니스 로직
│   │   │   │   └── GitService.java              # Git 연동
│   │   │   └── dto/
│   │   │       ├── SubmissionRequest.java       # 요청 DTO
│   │   │       └── SubmissionResponse.java      # 응답 DTO
│   │   └── resources/
│   │       └── application.properties           # 설정 파일
│   └── test/
│       └── java/com/hackerrank/log/
│           └── HackerRankLogApplicationTests.java
├── build.gradle                                 # Gradle 빌드 파일
├── settings.gradle
├── gradlew                                      # Gradle Wrapper
├── userscript.js                                # Tampermonkey 스크립트
└── README.md
```

## 🔐 보안 주의사항

1. **Git 인증 정보 보호**
   - Personal Access Token은 절대 공개하지 마세요
   - SSH 키 사용을 권장합니다

2. **CORS 설정**
   - 프로덕션 환경에서는 CORS 설정을 제한하세요
   - `application.properties`에서 `spring.webflux.cors.allowed-origins` 수정

3. **포트 설정**
   - 필요시 `application.properties`에서 포트 변경
   - 방화벽 설정 확인

## 📝 라이선스

MIT License

## 🤝 기여

버그 리포트나 기능 제안은 이슈로 등록해주세요!

## 💡 팁

1. **IDE 사용**: IntelliJ IDEA나 Eclipse로 프로젝트를 열면 더 편리합니다
2. **로그 확인**: 서버 콘솔에서 실시간 로그를 확인할 수 있습니다
3. **Git Subtree**: auknowlog 프로젝트처럼 subtree를 사용하려면 `GitService`를 확장하세요
4. **자동 시작**: 시스템 부팅 시 자동 실행하도록 설정 가능합니다

---

**Made with ☕ and Spring Boot**
