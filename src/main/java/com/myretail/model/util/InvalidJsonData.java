package com.myretail.model.util;

/**
 * Thrown when parsing JSON data fails.
 */
public class InvalidJsonData extends Exception {

  public InvalidJsonData() {
  }

  public InvalidJsonData(String message) {
    super(message);
  }

  public InvalidJsonData(String message, Throwable cause) {
    super(message, cause);
  }
}
