package io.github.j_yuhanwang.food_ordering_app.aws.controller;

import io.github.j_yuhanwang.food_ordering_app.aws.services.AwsS3Service;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Global File Management Controller.
 * This acts as a centralized hub for all media uploads (Dish images, User avatars, etc.)
 * following the "Pre-upload" architectural pattern.
 * @author YuhanWang
 * @Date 25/03/2026 11:25 am
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/files")
@Slf4j
public class FileController {
    private final AwsS3Service awsS3Service;

    /**
     * Uploads a file to a specific folder in S3.
     *
     * @param file   The binary file from frontend.
     * @param folder The target directory in S3 (e.g., "dishes", "users").
     * @return The public URL of the uploaded file.
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Response<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value="folder",defaultValue = "general") String folder){
        log.info("Received upload request for file: [{}] into folder: [{}]",
                file.getOriginalFilename(), folder);
        //automatically generate the unique file name (avoid overlapping due to the same filename)
        //format: folder/UUID_filename.jpg
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString()+"_"+originalFileName;
        String keyName = folder+"/"+uniqueFileName;

        String fileUrl = awsS3Service.uploadFile(keyName,file);
        return Response.ok(fileUrl);
    }


}
