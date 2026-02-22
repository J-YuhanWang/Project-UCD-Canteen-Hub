package io.github.j_yuhanwang.food_ordering_app.aws.controller;

import io.github.j_yuhanwang.food_ordering_app.aws.services.AwsS3Service;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @author YuhanWang
 * @Date 22/02/2026 9:16 am
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/aws")
@Profile("dev")
public class AwsTestController {
    private final AwsS3Service awsS3Service;

    @PostMapping("/upload-file")
    public Response<String> testUploadFile(@RequestParam("file") MultipartFile file){
        log.info("Smoke test: Received file upload request.");
        //To avoid the duplicate origin name for different files
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = "test-folder/"+ UUID.randomUUID().toString()+"_"+originalFileName;
        String savedFile = awsS3Service.uploadFile(uniqueFileName,file);
        return Response.ok(savedFile);
    }

    @DeleteMapping("/delete-file")
    public Response<String> testDeleteFile(@RequestParam String keyName){
        log.info("Smoke test: Received request to delete file. KeyName:{}",keyName);
        awsS3Service.deleteFile(keyName);
        return Response.ok("File deleted successfully: " + keyName);
    }
}
