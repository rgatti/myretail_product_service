package com.myretail.rest.product.message.codec;

import com.myretail.rest.product.message.ProductIdMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class ProductIdCodec implements MessageCodec<ProductIdMessage, ProductIdMessage> {

  @Override
  public void encodeToWire(Buffer buffer, ProductIdMessage productId) {
    buffer.appendInt(productId.value);
  }

  @Override
  public ProductIdMessage decodeFromWire(int pos, Buffer buffer) {
    ProductIdMessage id = new ProductIdMessage();
    id.value = buffer.getInt(pos);
    return id;
  }

  @Override
  public ProductIdMessage transform(ProductIdMessage productId) {
    return productId;
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
