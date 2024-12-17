package com.gogym.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

  private final ImageService imageService;

  @GetMapping("/presigned-url")
  public ResponseEntity<String> getPresignedUrl(
      @RequestParam("file-name") String fileName,
      @RequestParam("dir-name") String dirName) {

    String presignedUrl = imageService.getPresignedUrl(fileName, dirName);
    return ResponseEntity.ok(presignedUrl);
  }
}
