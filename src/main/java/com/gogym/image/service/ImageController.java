package com.gogym.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

  private final ImageService imageService;

  @PostMapping
  public ResponseEntity<String> uploadImage(
      @RequestParam("file") MultipartFile file,
      @RequestParam("dirName") String dirName) {

    String imageUrl = imageService.upload(file, dirName);

    return ResponseEntity.ok(imageUrl);
  }
}
