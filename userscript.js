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

    const SERVER_URL = 'http://localhost:8080/api/hackerrank/submit';
    
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

