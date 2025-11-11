package com.hackerrank.log.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Git 명령어를 실행하여 GitHub에 파일을 저장하는 서비스
 * auknowlog 프로젝트의 GitService를 기반으로 함
 */
@Service
public class GitService {
    
    private static final Logger log = LoggerFactory.getLogger(GitService.class);
    
    @Value("${hackerrank.git.remote:origin}")
    private String remoteName;
    
    @Value("${hackerrank.git.branch:main}")
    private String remoteBranch;
    
    /**
     * Git에 파일을 추가하고 커밋 및 푸시
     * 
     * @param absoluteFilePath 저장할 파일의 절대 경로
     * @param commitMessage 커밋 메시지
     * @return 성공 메시지
     */
    public Mono<String> commitAndPush(String absoluteFilePath, String commitMessage) {
        File workingDir = new File(".");
        
        log.info("Git add/commit/push 시작: {}", absoluteFilePath);
        
        return runGit(List.of("git", "add", absoluteFilePath), workingDir)
                .flatMap(addOut -> {
                    log.debug("Git add 완료: {}", addOut);
                    return runGit(List.of("git", "commit", "-m", commitMessage), workingDir)
                            .onErrorResume(e -> {
                                String msg = e.getMessage() == null ? "" : e.getMessage();
                                if (msg.contains("nothing to commit") || 
                                    msg.contains("no changes added") || 
                                    msg.contains("nothing added to commit")) {
                                    log.info("변경 사항 없음, 커밋 스킵");
                                    return Mono.just("nothing to commit");
                                }
                                return Mono.error(e);
                            });
                })
                .flatMap(commitOut -> {
                    log.debug("Git commit 완료: {}", commitOut);
                    if ("nothing to commit".equals(commitOut)) {
                        return Mono.just("변경 사항 없음");
                    }
                    return runGit(List.of("git", "push", remoteName, remoteBranch), workingDir);
                })
                .map(pushOut -> {
                    log.info("Git push 완료: {}", pushOut);
                    return "✅ GitHub에 저장 완료: " + absoluteFilePath;
                })
                .onErrorResume(e -> {
                    log.error("Git 저장 실패", e);
                    return Mono.just("❌ Git 저장 실패: " + e.getMessage());
                });
    }
    
    /**
     * Git 명령어 실행
     * 
     * @param command 실행할 Git 명령어 리스트
     * @param workingDir 작업 디렉토리
     * @return 명령어 실행 결과
     */
    private Mono<String> runGit(List<String> command, File workingDir) {
        return Mono.fromCallable(() -> {
            log.debug("Git 명령 실행: {} (작업 디렉토리: {})", 
                     String.join(" ", command), workingDir.getAbsolutePath());
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workingDir);
            pb.redirectErrorStream(true);
            
            Process proc = pb.start();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            try (InputStream is = proc.getInputStream()) {
                is.transferTo(baos);
            }
            
            int exit = proc.waitFor();
            String out = baos.toString(StandardCharsets.UTF_8).trim();
            
            if (exit != 0) {
                throw new IOException("Git 명령 실패: " + String.join(" ", command) + " → " + out);
            }
            
            return out;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Git 저장소가 올바르게 설정되어 있는지 확인
     * 
     * @return 설정 상태 메시지
     */
    public Mono<String> checkGitRepository() {
        File workingDir = new File(".");
        
        return runGit(List.of("git", "rev-parse", "--is-inside-work-tree"), workingDir)
                .map(result -> "Git 저장소 확인: " + result)
                .onErrorResume(e -> Mono.just("Git 저장소가 아닙니다: " + e.getMessage()));
    }
}

