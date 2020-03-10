package com.myretail.rest.product.messages;

import com.myretail.model.Product;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Marshall Product model across the message bus.
 */
public class ProductMessageCodec implements MessageCodec<Product, Product> {

  @Override
  public void encodeToWire(Buffer buffer, Product product) {
    JsonObject jsonToEncode = new JsonObject();
    jsonToEncode.put("id", product.getId());
    jsonToEncode.put("title", product.getTitle());

    // Encode object to string
    String data = jsonToEncode.encode();
    // Binary length of JSON data
    int length = data.getBytes().length;

    // Write data into given buffer
    buffer.appendInt(length);
    buffer.appendString(data);
  }

  @Override
  public Product decodeFromWire(int pos, Buffer buffer) {
    int _pos = pos;

    // Length of JSON
    int length = buffer.getInt(_pos);

    // Get JSON string by it`s length
    // Jump 4 because getInt() == 4 bytes
    String jsonStr = buffer.getString(_pos += 4, _pos += length);
    JsonObject contentJson = new JsonObject(jsonStr);

    // Get fields
    int id = contentJson.getInteger("id");
    String title = contentJson.getString("title");

    return new Product(id, title);
  }

  @Override
  public Product transform(Product product) {
    return product;
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
