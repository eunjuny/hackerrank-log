package com.hackerrank.log.service;

import com.hackerrank.log.dto.SubmissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * HackerRank 제출을 처리하고 파일로 저장하는 서비스
 */
@Service
public class HackerRankService {
    
    private static final Logger log = LoggerFactory.getLogger(HackerRankService.class);
    
    @Value("${hackerrank.solution.path:./solutions}")
    private String solutionBasePath;
    
    // 언어별 파일 확장자 매핑
    private static final Map<String, String> LANGUAGE_EXTENSIONS = new HashMap<>();
    
    static {
        LANGUAGE_EXTENSIONS.put("python", "py");
        LANGUAGE_EXTENSIONS.put("python3", "py");
        LANGUAGE_EXTENSIONS.put("java", "java");
        LANGUAGE_EXTENSIONS.put("cpp", "cpp");
        LANGUAGE_EXTENSIONS.put("c++", "cpp");
        LANGUAGE_EXTENSIONS.put("c", "c");
        LANGUAGE_EXTENSIONS.put("javascript", "js");
        LANGUAGE_EXTENSIONS.put("typescript", "ts");
        LANGUAGE_EXTENSIONS.put("go", "go");
        LANGUAGE_EXTENSIONS.put("rust", "rs");
        LANGUAGE_EXTENSIONS.put("kotlin", "kt");
        LANGUAGE_EXTENSIONS.put("swift", "swift");
        LANGUAGE_EXTENSIONS.put("ruby", "rb");
        LANGUAGE_EXTENSIONS.put("php", "php");
        LANGUAGE_EXTENSIONS.put("csharp", "cs");
        LANGUAGE_EXTENSIONS.put("c#", "cs");
        LANGUAGE_EXTENSIONS.put("sql", "sql");
        LANGUAGE_EXTENSIONS.put("scala", "scala");
        LANGUAGE_EXTENSIONS.put("r", "r");
        LANGUAGE_EXTENSIONS.put("bash", "sh");
        LANGUAGE_EXTENSIONS.put("shell", "sh");
    }
    
    // 언어별 주석 스타일
    private static final Map<String, String> COMMENT_STYLES = new HashMap<>();
    
    static {
        COMMENT_STYLES.put("py", "#");
        COMMENT_STYLES.put("java", "//");
        COMMENT_STYLES.put("cpp", "//");
        COMMENT_STYLES.put("c", "//");
        COMMENT_STYLES.put("js", "//");
        COMMENT_STYLES.put("ts", "//");
        COMMENT_STYLES.put("go", "//");
        COMMENT_STYLES.put("rs", "//");
        COMMENT_STYLES.put("kt", "//");
        COMMENT_STYLES.put("swift", "//");
        COMMENT_STYLES.put("rb", "#");
        COMMENT_STYLES.put("php", "//");
        COMMENT_STYLES.put("cs", "//");
        COMMENT_STYLES.put("sql", "--");
        COMMENT_STYLES.put("scala", "//");
        COMMENT_STYLES.put("r", "#");
        COMMENT_STYLES.put("sh", "#");
    }
    
    /**
     * HackerRank 제출을 파일로 저장
     * 
     * @param request 제출 정보
     * @return 저장된 파일의 절대 경로
     * @throws IOException 파일 저장 실패 시
     */
    public String saveSolution(SubmissionRequest request) throws IOException {
        log.info("HackerRank 제출 저장 시작: {}", request);
        
        // 파일 경로 생성
        String extension = getFileExtension(request.getLanguage());
        String fileName = sanitizeFileName(request.getProblemName()) + "." + extension;
        
        // 언어별 디렉토리 생성
        Path languageDir = Paths.get(solutionBasePath, request.getLanguage().toLowerCase());
        Files.createDirectories(languageDir);
        
        Path filePath = languageDir.resolve(fileName);
        
        // 파일 내용 생성 (메타데이터 + 코드)
        String fileContent = createFileContent(request, extension);
        
        // 파일 저장
        Files.writeString(filePath, fileContent);
        
        String absolutePath = filePath.toAbsolutePath().toString();
        log.info("✅ 파일 저장 완료: {}", absolutePath);
        
        return absolutePath;
    }
    
    /**
     * 파일 내용 생성 (메타데이터 헤더 + 코드)
     */
    private String createFileContent(SubmissionRequest request, String extension) {
        String comment = COMMENT_STYLES.getOrDefault(extension, "#");
        StringBuilder sb = new StringBuilder();
        
        // 메타데이터 헤더
        sb.append(comment).append(" Problem: ").append(request.getProblemName()).append("\n");
        sb.append(comment).append(" Language: ").append(request.getLanguage()).append("\n");
        sb.append(comment).append(" Submitted: ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
          .append("\n");
        
        if (request.getChallengeId() != null && !request.getChallengeId().isBlank()) {
            sb.append(comment).append(" Challenge ID: ").append(request.getChallengeId()).append("\n");
            sb.append(comment).append(" URL: https://www.hackerrank.com/challenges/")
              .append(request.getChallengeId()).append("\n");
        }
        
        if (request.getDifficulty() != null && !request.getDifficulty().isBlank()) {
            sb.append(comment).append(" Difficulty: ").append(request.getDifficulty()).append("\n");
        }
        
        if (request.getTags() != null && request.getTags().length > 0) {
            sb.append(comment).append(" Tags: ").append(String.join(", ", request.getTags())).append("\n");
        }
        
        sb.append(comment).append("\n\n");
        
        // 코드
        sb.append(request.getCode());
        
        return sb.toString();
    }
    
    /**
     * 파일 확장자 반환
     */
    private String getFileExtension(String language) {
        return LANGUAGE_EXTENSIONS.getOrDefault(language.toLowerCase(), "txt");
    }
    
    /**
     * 파일명에 사용할 수 없는 문자 제거
     */
    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "unknown";
        }
        
        // 파일명에 사용할 수 없는 문자 제거
        String sanitized = name.replaceAll("[<>:\"/\\\\|?*]", "_");
        
        // 공백을 대시로 변경
        sanitized = sanitized.replaceAll("\\s+", "-");
        
        // 연속된 언더스코어나 대시 제거
        sanitized = sanitized.replaceAll("[-_]+", "-");
        
        // 앞뒤 공백 및 특수문자 제거
        sanitized = sanitized.replaceAll("^[-_]+|[-_]+$", "");
        
        return sanitized.isEmpty() ? "unknown" : sanitized;
    }
}

