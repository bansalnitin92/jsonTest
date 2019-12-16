package com.jsonTest.jsonTest;

import java.util.UUID;

public class VerifyAnnotation {

  /**
   * Verify uuid.
   */
  public static void verifyUuid(String uuid) throws ValidationException {
    try {
      UUID.fromString(uuid);
    } catch (IllegalArgumentException e) {
      throwVerifyException(uuid + "not a uuid");
    }

  }

  private static void throwVerifyException(String message) throws ValidationException {
    throw new RuntimeException("verify failed - " + message);
  }

}
