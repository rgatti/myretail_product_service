package com.myretail.rest.product.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

public class ActionCodecTests {

  private static final int RAW_JSON_LENGTH = 30;
  private static final String RAW_JSON = "{\"type\":\"GET\",\"data\":{\"id\":1}}";

  private static final String TYPE_VALUE = "GET";
  private static final JsonObject DATA_VALUE = new JsonObject("{\"id\":1}");

  private static ActionCodec codec = new ActionCodec();

  @Test
  void encode() {
    Buffer buf = new BufferImpl();

    Action action = new Action("GET", new JsonObject("{\"id\":1}"));

    codec.encodeToWire(buf, action);

    int length = buf.getInt(0);
    String jsonString = buf.getString(4, 4 + length);

    assertEquals(RAW_JSON_LENGTH, length);
    assertEquals(RAW_JSON, jsonString);
  }

  @Test
  void decode() {
    Buffer buf = new BufferImpl();

    buf.appendInt(RAW_JSON_LENGTH);
    buf.appendString(RAW_JSON);

    Action action = codec.decodeFromWire(0, buf);

    assertEquals(TYPE_VALUE, action.getType());
    assertEquals(DATA_VALUE, action.getData());
  }

}
