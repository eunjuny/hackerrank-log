// ==UserScript==
// @name         HackerRank GitHub Logger
// @namespace    http://tampermonkey.net/
// @version      1.3
// @description  HackerRank 제출을 자동으로 GitHub에 저장
// @author       You
// @match        https://www.hackerrank.com/*
// @grant        none
// @connect      localhost
// @inject-into  page
// @run-at       document-start
// ==/UserScript==

(function () {
    'use strict';

    const SERVER_URL = 'http://localhost:9090/api/hackerrank/submit';

    function log(...args) {
        console.log('[HackerRank Logger]', ...args);
    }

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
        document.addEventListener('DOMContentLoaded', () => {
            document.body.appendChild(notification);
            setTimeout(() => {
                notification.style.transition = 'opacity 0.5s';
                notification.style.opacity = '0';
                setTimeout(() => notification.remove(), 500);
            }, 3000);
        });
    }

    function buildSubmissionData(body) {
        const problemName =
            document.querySelector('.challenge-name')?.textContent?.trim() ||
            document.querySelector('h1')?.textContent?.trim() ||
            'unknown';
        const difficulty = document.querySelector('.difficulty-label')?.textContent?.trim() || '';
        const challengeId = window.location.pathname.split('/challenges/')[1]?.split('/')[0] || '';

        const code =
            body?.code ??
            body?.source ??
            body?.model?.code ??
            '';
        const language =
            body?.language ??
            body?.lang ??
            body?.model?.language ??
            '';

        return {
            problemName,
            code,
            language,
            challengeId,
            difficulty,
            tags: [],
            timestamp: new Date().toISOString(),
        };
    }

    async function sendToServer(submissionData) {
        try {
            const res = await fetch(SERVER_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(submissionData),
                mode: 'cors',
                cache: 'no-cache',
            });
            const text = await res.text();
            log('서버 응답:', res.status, text);
            if (res.ok) {
                showNotification('✅ GitHub에 저장되었습니다!', 'success');
            } else {
                showNotification(`❌ 저장 실패: ${res.status}`, 'error');
            }
        } catch (err) {
            console.error('[HackerRank Logger] 요청 실패:', err);
            showNotification('❌ 서버 연결 실패. 서버가 실행 중인지 확인하세요.', 'error');
        }
    }

    function interceptFetch() {
        const originalFetch = window.fetch;
        window.fetch = async function (...args) {
            try {
                const [input, init] = args;
                const url = typeof input === 'string' ? input : input?.url || '';
                if (typeof url === 'string' && (url.includes('/submissions') || url.includes('/contests/master/challenges'))) {
                    log('제출 요청 감지(fetch):', url);
                    if (init && typeof init.body === 'string') {
                        try {
                            const body = JSON.parse(init.body);
                            const submissionData = buildSubmissionData(body);
                            log('서버로 전송:', submissionData);
                            sendToServer(submissionData);
                        } catch (e) {
                            log('요청 본문 파싱 실패:', e);
                        }
                    }
                }
            } catch (e) {
                // ignore
            }
            return originalFetch.apply(this, args);
        };
        log('fetch hook 설치 완료');
    }

    function interceptXHR() {
        const openOrig = XMLHttpRequest.prototype.open;
        const sendOrig = XMLHttpRequest.prototype.send;

        XMLHttpRequest.prototype.open = function (method, url, ...rest) {
            this.__hr_url = url;
            return openOrig.call(this, method, url, ...rest);
        };

        XMLHttpRequest.prototype.send = function (body) {
            try {
                const url = this.__hr_url || '';
                if (typeof url === 'string' && (url.includes('/submissions') || url.includes('/contests/master/challenges'))) {
                    log('제출 요청 감지(XHR):', url);
                    if (typeof body === 'string') {
                        try {
                            const parsed = JSON.parse(body);
                            const submissionData = buildSubmissionData(parsed);
                            log('서버로 전송:', submissionData);
                            sendToServer(submissionData);
                        } catch (e) {
                            log('XHR 본문 파싱 실패:', e);
                        }
                    }
                }
            } catch (e) {
                // ignore
            }
            return sendOrig.call(this, body);
        };
        log('XHR hook 설치 완료');
    }

    try {
        log('스크립트 로드됨');
        interceptFetch();
        interceptXHR();
        log('준비 완료!');
        try { 
            window.__HR_LOGGER_INSTALLED__ = true; 
            window.HR_LOGGER_INSTALLED = true; 
        } catch (e) {}
    } catch (e) {
        console.error('[HackerRank Logger] 초기화 실패:', e);
    }
})();

