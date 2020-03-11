package com.myretail.rest.product.message.codec;

import com.myretail.model.Price;
import com.myretail.rest.product.message.PriceMessage;
import com.myretail.rest.product.message.ProductIdMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class PriceMessageCodec implements MessageCodec<PriceMessage, PriceMessage> {

  @Override
  public void encodeToWire(Buffer buffer, PriceMessage priceMessage) {
    String data = Json.encode(priceMessage);
    int length = data.getBytes().length;
    buffer.appendInt(length);
    buffer.appendString(data);
  }

  @Override
  public PriceMessage decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    String data = buffer.getString(pos + 4, length + 4);

    JsonObject object = new JsonObject(data);
    PriceMessage priceMessage = new PriceMessage();
    priceMessage.id = object.getJsonObject("id").mapTo(ProductIdMessage.class);
    priceMessage.value = object.getDouble("value");
    priceMessage.currency = object.getString("currency");

    return priceMessage;
  }

  @Override
  public PriceMessage transform(PriceMessage priceMessage) {
    return priceMessage;
  }

  @Override
  public String name() {
    // Each codec must have a unique name.
    // This is used to identify a codec when sending a message and for unregistering codecs.
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    // -1 for user codec
    return -1;
  }
}
