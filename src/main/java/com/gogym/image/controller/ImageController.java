package com.gogym.image.controller;

import com.gogym.image.service.ImageService;
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
  public ResponseEntity<String> getPresignedUrl(@RequestParam("dir-name") String dirName,
      @RequestParam("file-name") String fileName) {
    String presignedUrl = imageService.getPresignedUrl(dirName, fileName);

    return ResponseEntity.ok(presignedUrl);
  }
}
