package com.myretail.rest.product.messages;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class ActionCodec implements MessageCodec<Action, Action> {

  @Override
  public void encodeToWire(Buffer buffer, Action action) {
    String data = Json.encode(action);
    int length = data.getBytes().length;
    buffer.appendInt(length);
    buffer.appendString(data);
  }

  @Override
  public Action decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    String jsonData = buffer.getString(pos + 4, length + 4);

    JsonObject object = new JsonObject(jsonData);
    String type = object.getString("type");
    JsonObject data = object.getJsonObject("data");
    return new Action(type, data);
  }

  @Override
  public Action transform(Action action) {
    return action;
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
