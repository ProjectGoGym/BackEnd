package com.gogym.image.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import java.time.Duration;
import java.time.LocalDate;
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

  private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

  public String getPresignedUrl(String dirName, String fileName) {
    LocalDate today = LocalDate.now();

    validateFileExtension(fileName);
    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

    String randomFileName = UUID.randomUUID() + "-" + fileName;
    String filePath =
        dirName + "/" + today.toString().replace("-", "/") + "/" + randomFileName;

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(filePath)
        .contentType(getContentType(extension))
        .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder ->
        builder.putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofMinutes(10))
    );

    return presignedRequest.url().toString();
  }

  private void validateFileExtension(String fileName) {
    if (!fileName.contains(".")) {
      throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION, "확장자가 없습니다.");
    }

    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
      throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION, "허용되지 않은 파일 확장자입니다.");
    }
  }

  private String getContentType(String fileExtension) {
    return switch (fileExtension.toLowerCase()) {
      case "jpg", "jpeg" -> "image/jpeg";
      case "png" -> "image/png";
      case "gif" -> "image/gif";
      default -> throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION, "유효하지 않은 파일 확장자");
    };
  }
}