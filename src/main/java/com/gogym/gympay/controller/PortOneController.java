package com.gogym.gympay.controller;

import static org.springframework.http.HttpStatus.OK;

import com.gogym.gympay.service.PortOneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/port-one")
public class PortOneController {

  private final PortOneService portOneService;

  @GetMapping("/sign-in")
  @ResponseStatus(OK)
  public ResponseEntity<String> signIn() {
    var accessToken = portOneService.signIn();

    return ResponseEntity.ok(accessToken);
  }
}