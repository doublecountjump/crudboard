package test.crudboard.service.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file) {

        try {
            // 업로드 디렉토리가 없으면 생성
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                log.error("error");
                uploadPath.mkdirs();
            }

            // 파일명 중복 방지를 위해 UUID 사용
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 파일 저장
            File dest = new File(uploadPath.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);

            // 저장된 파일의 URL 반환
            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
} 