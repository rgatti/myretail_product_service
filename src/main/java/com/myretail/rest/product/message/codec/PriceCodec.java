package com.myretail.rest.product.message.codec;

import com.myretail.model.Price;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Marshall Product model across the event bus.
 *
 * @see io.vertx.core.eventbus.MessageCodec
 */
public class PriceCodec implements MessageCodec<Price, Price> {

  @Override
  public void encodeToWire(Buffer buffer, Price price) {
    JsonObject jsonToEncode = new JsonObject();
    jsonToEncode.put("value", price.getValue());
    jsonToEncode.put("currency", price.getCurrency());

    // Encode object to string
    String data = jsonToEncode.encode();
    // Binary length of JSON data
    int length = data.getBytes().length;

    // Write data into given buffer
    buffer.appendInt(length);
    buffer.appendString(data);
  }

  @Override
  public Price decodeFromWire(int pos, Buffer buffer) {
    int _pos = pos;

    // Length of JSON
    int length = buffer.getInt(_pos);

    // Get JSON string by it`s length
    // Jump 4 because getInt() == 4 bytes
    String jsonStr = buffer.getString(_pos += 4, _pos += length);
    JsonObject contentJson = new JsonObject(jsonStr);

    // Get fields
    double value = contentJson.getDouble("value");
    String currency = contentJson.getString("currency");

    Price price = new Price();
    price.setValue(value);
    price.setCurrency(currency);
    return price;
  }

  @Override
  public Price transform(Price price) {
    return price;
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
