package com.gogym.image.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
  private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

  public String upload(MultipartFile multipartFile, String dirName) {
    validateFileSize(multipartFile);

    try {
      String originalFileName = multipartFile.getOriginalFilename();
      validateExtension(originalFileName);

      String randomName = UUID.randomUUID().toString();
      String fileName = dirName + "/" + randomName + "." + originalFileName;

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket)
          .key(fileName)
          .contentType(multipartFile.getContentType())
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

      GetUrlRequest request = GetUrlRequest.builder()
          .bucket(bucket)
          .key(fileName)
          .build();

      return s3Client.utilities().getUrl(request).toString();
    } catch (Exception e) {
      throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  private void validateFileSize(MultipartFile file) {
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED, "파일 크기가 허용된 최대 크기를 초과했습니다.");
    }
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
}