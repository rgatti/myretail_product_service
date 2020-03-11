package com.myretail.rest.product.enums;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import java.util.Map;

/**
 * Error messages a resource can return.
 */
public enum ResourceError {
  RESOURCE_MISSING(0, "resource does not exist"),
  READ_ERROR(1, "error reading data"),
  ACCESS_ERROR(2, "unable to access backing data store"),
  PARSE_ERROR(3, "error parsing response");

  private int code;
  private String message;

  ResourceError(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public void replyTo(Message m) {
    m.fail(code, message);
  }

  public String toJson() {
    JsonObject object = new JsonObject();
    object.put("code", code);
    object.put("message", message);
    return object.encode();
  }
}
