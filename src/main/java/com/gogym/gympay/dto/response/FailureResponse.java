package com.gogym.gympay.dto.response;

public record FailureResponse(String reason,
                              String pgCode,
                              String pgMessage) {

}
