package io.github.j_yuhanwang.food_ordering_app.aws.services;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author YuhanWang
 * @Date 21/02/2026 1:03 pm
 */
public interface AwsS3Service {
    String uploadFile(String keyName, MultipartFile file);

    void deleteFile(String keyName);
}
