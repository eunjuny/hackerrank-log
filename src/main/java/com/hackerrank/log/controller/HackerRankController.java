package com.hackerrank.log.controller;

import com.hackerrank.log.dto.SubmissionRequest;
import com.hackerrank.log.dto.SubmissionResponse;
import com.hackerrank.log.service.GitService;
import com.hackerrank.log.service.HackerRankService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * HackerRank 제출을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/hackerrank")
@CrossOrigin(origins = "*")
public class HackerRankController {
    
    private static final Logger log = LoggerFactory.getLogger(HackerRankController.class);
    
    private final HackerRankService hackerRankService;
    private final GitService gitService;
    
    public HackerRankController(HackerRankService hackerRankService, GitService gitService) {
        this.hackerRankService = hackerRankService;
        this.gitService = gitService;
    }
    
    /**
     * 서버 상태 확인
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> health() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return Mono.just(ResponseEntity.ok("✅ HackerRank Logger is running! (Timestamp: " + timestamp + ")"));
    }
    
    /**
     * Git 저장소 상태 확인
     */
    @GetMapping("/git/status")
    public Mono<ResponseEntity<String>> gitStatus() {
        return gitService.checkGitRepository()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.ok("Git 상태 확인 실패: " + e.getMessage())));
    }
    
    /**
     * HackerRank 제출 로그 수신 및 저장
     * 
     * @param request 제출 정보
     * @return 저장 결과
     */
    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SubmissionResponse>> submitSolution(@Valid @RequestBody SubmissionRequest request) {
        log.info("=" .repeat(60));
        log.info("📥 새로운 HackerRank 제출 수신");
        log.info("=" .repeat(60));
        log.info("문제명: {}", request.getProblemName());
        log.info("언어: {}", request.getLanguage());
        log.info("코드 길이: {} 글자", request.getCode() != null ? request.getCode().length() : 0);
        log.info("난이도: {}", request.getDifficulty());
        log.info("=" .repeat(60));
        
        // 1. 파일로 저장
        return Mono.fromCallable(() -> hackerRankService.saveSolution(request))
                .subscribeOn(Schedulers.boundedElastic())
                
                // 2. Git에 커밋 및 푸시
                .flatMap(filePath -> {
                    String commitMessage = String.format("Add solution: %s (%s)", 
                            request.getProblemName(), 
                            request.getLanguage());
                    
                    return gitService.commitAndPush(filePath, commitMessage)
                            .map(gitMsg -> {
                                log.info("✅ 저장 및 Git 푸시 완료!");
                                return SubmissionResponse.success(
                                        "HackerRank 제출이 GitHub에 저장되었습니다",
                                        filePath,
                                        gitMsg
                                );
                            });
                })
                
                // 3. 응답
                .map(ResponseEntity::ok)
                
                // 4. 에러 처리
                .onErrorResume(e -> {
                    log.error("❌ 제출 저장 실패", e);
                    SubmissionResponse errorResponse = SubmissionResponse.failure(
                            "저장 실패: " + e.getMessage()
                    );
                    return Mono.just(ResponseEntity.status(500).body(errorResponse));
                });
    }
    
    /**
     * 테스트용 엔드포인트 (간단한 로그만 기록)
     */
    @PostMapping(value = "/log", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SubmissionResponse>> logSubmission(@Valid @RequestBody SubmissionRequest request) {
        log.info("📝 제출 로그 수신: {} ({})", request.getProblemName(), request.getLanguage());
        
        return Mono.fromCallable(() -> hackerRankService.saveSolution(request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(filePath -> {
                    SubmissionResponse response = new SubmissionResponse(
                            true,
                            "로컬에 저장되었습니다 (Git 푸시 없음)",
                            filePath
                    );
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("로그 저장 실패", e);
                    return Mono.just(ResponseEntity.status(500).body(
                            SubmissionResponse.failure("저장 실패: " + e.getMessage())
                    ));
                });
    }
}

