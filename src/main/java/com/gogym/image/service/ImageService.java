package com.gogym.image.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

  public String getPresignedUrl(String originalFileName, String dirName) {
    validateExtension(originalFileName);

    String randomName = UUID.randomUUID().toString();
    String fileName = dirName + "/" + randomName + "." + originalFileName;

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(fileName)
        .contentType(getContentType(originalFileName))
        .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder ->
        builder.putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofMinutes(10))
    );

    URL url = presignedRequest.url();
    return url.toString();
  }

  private void validateExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
    }
    String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

    if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
      throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
    }
  }

  private String getContentType(String fileName) {
    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (fileName.endsWith(".png")) {
      return "image/png";
    } else {
      throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
    }
  }
}