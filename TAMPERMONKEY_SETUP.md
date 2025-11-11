# Tampermonkey 스크립트 설치 가이드

## 1단계: Tampermonkey 설치

브라우저에 맞는 Tampermonkey 확장 프로그램을 설치하세요:

- **Chrome**: https://chrome.google.com/webstore/detail/tampermonkey/dhdgffkkebhmkfjojejmpbldmpobfkfo
- **Firefox**: https://addons.mozilla.org/firefox/addon/tampermonkey/
- **Edge**: https://microsoftedge.microsoft.com/addons/detail/tampermonkey/iikmkjmpaadaobahmlepeloendndfphd
- **Safari**: https://apps.apple.com/app/tampermonkey/id1482490089

## 2단계: 스크립트 추가

### 방법 1: 직접 추가 (권장)

1. **Tampermonkey 아이콘** 클릭 (브라우저 우측 상단)
2. **"새 스크립트 생성"** 또는 **"Create a new script"** 클릭
3. 기본 템플릿을 **모두 삭제**
4. 아래 스크립트를 **복사해서 붙여넣기**:

```javascript
// ==UserScript==
// @name         HackerRank GitHub Logger
// @namespace    http://tampermonkey.net/
// @version      1.0
// @description  HackerRank 제출을 자동으로 GitHub에 저장
// @author       You
// @match        https://www.hackerrank.com/challenges/*
// @grant        GM_xmlhttpRequest
// @connect      localhost
// ==/UserScript==

(function() {
    'use strict';

    const SERVER_URL = 'http://localhost:9090/api/hackerrank/submit';
    
    console.log('[HackerRank Logger] 스크립트 로드됨');

    // 원래 fetch 함수 저장
    const originalFetch = window.fetch;

    // fetch 함수 오버라이드
    window.fetch = async function(...args) {
        const [url, options] = args;
        
        // HackerRank 제출 API 호출 감지
        if (url && typeof url === 'string' && 
            (url.includes('/submissions') || url.includes('/contests/master/challenges'))) {
            
            console.log('[HackerRank Logger] 제출 요청 감지:', url);
            
            try {
                // 요청 데이터 파싱
                if (options && options.body) {
                    const body = JSON.parse(options.body);
                    console.log('[HackerRank Logger] 제출 데이터:', body);
                    
                    // 페이지에서 문제 정보 추출
                    const problemName = document.querySelector('.challenge-name')?.textContent?.trim() || 
                                      document.querySelector('h1')?.textContent?.trim() ||
                                      'unknown';
                    
                    const difficulty = document.querySelector('.difficulty-label')?.textContent?.trim() || '';
                    
                    // URL에서 challenge ID 추출
                    const challengeId = window.location.pathname.split('/challenges/')[1]?.split('/')[0] || '';
                    
                    // 제출 데이터 구성
                    const submissionData = {
                        problem_name: problemName,
                        code: body.code || body.source || '',
                        language: body.language || '',
                        challenge_id: challengeId,
                        difficulty: difficulty,
                        tags: [],
                        timestamp: new Date().toISOString()
                    };
                    
                    console.log('[HackerRank Logger] 서버로 전송:', submissionData);
                    
                    // 로컬 서버로 전송
                    GM_xmlhttpRequest({
                        method: 'POST',
                        url: SERVER_URL,
                        data: JSON.stringify(submissionData),
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        onload: function(response) {
                            console.log('[HackerRank Logger] 서버 응답:', response.responseText);
                            if (response.status === 200) {
                                console.log('[HackerRank Logger] ✓ GitHub에 저장 완료!');
                                // 성공 알림 (선택사항)
                                showNotification('GitHub에 저장되었습니다! ✓', 'success');
                            } else {
                                console.error('[HackerRank Logger] ✗ 저장 실패:', response.status);
                                showNotification('저장 실패: ' + response.status, 'error');
                            }
                        },
                        onerror: function(error) {
                            console.error('[HackerRank Logger] ✗ 요청 실패:', error);
                            showNotification('서버 연결 실패. 서버가 실행 중인지 확인하세요.', 'error');
                        }
                    });
                }
            } catch (error) {
                console.error('[HackerRank Logger] 에러:', error);
            }
        }
        
        // 원래 fetch 함수 호출
        return originalFetch.apply(this, args);
    };

    // 알림 표시 함수
    function showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            background-color: ${type === 'success' ? '#4CAF50' : type === 'error' ? '#f44336' : '#2196F3'};
            color: white;
            border-radius: 4px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
            z-index: 10000;
            font-family: Arial, sans-serif;
            font-size: 14px;
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.style.transition = 'opacity 0.5s';
            notification.style.opacity = '0';
            setTimeout(() => notification.remove(), 500);
        }, 3000);
    }

    console.log('[HackerRank Logger] 준비 완료!');
})();
```

5. **저장**: `Ctrl+S` (Mac: `Cmd+S`) 또는 **파일 > 저장** 클릭

### 방법 2: 파일에서 불러오기

1. Tampermonkey 대시보드 열기
2. **Utilities** 탭
3. **Import from file** → `userscript.js` 선택

## 3단계: 스크립트 활성화 확인

1. **Tampermonkey 아이콘** 클릭
2. **"HackerRank GitHub Logger"** 스크립트가 **켜짐(ON)** 상태인지 확인
3. 토글이 초록색이면 활성화됨

## 4단계: 테스트

1. **서버 실행 확인**:
   ```bash
   curl http://localhost:9090/api/hackerrank/health
   ```

2. **HackerRank 접속**: https://www.hackerrank.com/

3. **브라우저 콘솔 열기**: `F12` → **Console** 탭

4. 문제 페이지에서 다음 로그가 보여야 함:
   ```
   [HackerRank Logger] 스크립트 로드됨
   [HackerRank Logger] 준비 완료!
   ```

5. **코드 제출 후** 콘솔에서 확인:
   ```
   [HackerRank Logger] 제출 요청 감지: ...
   [HackerRank Logger] 서버로 전송: {...}
   [HackerRank Logger] ✓ GitHub에 저장 완료!
   ```

6. **알림 확인**: 우측 상단에 초록색 알림 "GitHub에 저장되었습니다! ✓"

## 트러블슈팅

### ❌ 스크립트가 로드되지 않음

- HackerRank 페이지 새로고침 (F5)
- Tampermonkey 아이콘에서 스크립트가 **활성화**되어 있는지 확인
- URL이 `https://www.hackerrank.com/challenges/*` 패턴인지 확인

### ❌ "서버 연결 실패" 알림

```bash
# 서버가 실행 중인지 확인
lsof -i :9090

# 서버 재실행
cd /Users/yeob-eunjun/eunjuny/project/hackerrank-log
./gradlew bootRun
```

### ❌ 제출이 감지되지 않음

1. **F12** → **Console** 탭 확인
2. 에러 메시지가 있는지 확인
3. 스크립트 재저장 후 페이지 새로고침

### ❌ CORS 에러

`application.properties`에 이미 설정되어 있음:
```properties
spring.webflux.cors.allowed-origins=*
```

---

## 📝 추가 정보

- **서버 URL**: `http://localhost:9090/api/hackerrank/submit`
- **저장 경로**: `./solutions/` (프로젝트 루트 기준)
- **지원 언어**: Python, Java, C++, JavaScript, Go 등 17개 이상

스크립트가 정상적으로 작동하면 HackerRank에서 제출할 때마다 자동으로 GitHub에 저장됩니다! 🎉

