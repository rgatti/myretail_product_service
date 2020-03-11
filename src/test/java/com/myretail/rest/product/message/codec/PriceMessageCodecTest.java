package com.myretail.rest.product.message.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.myretail.model.Price;
import com.myretail.rest.product.message.PriceMessage;
import com.myretail.rest.product.message.ProductIdMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import org.junit.jupiter.api.Test;

public class PriceMessageCodecTest {
  private static final int RAW_JSON_LENGTH = 47;
  private static final String RAW_JSON = "{\"id\":{\"value\":1},\"value\":1.0,\"currency\":\"USD\"}";

  private static final int PRICE_PRODUCT_ID = 1;
  private static final double PRICE_VALUE = 1.00;
  private static final String PRICE_CURRENCY_CODE = "USD";

  private static PriceMessageCodec codec = new PriceMessageCodec();

  @Test
  void encode() {
    Buffer buf = new BufferImpl();

    PriceMessage priceMessage = new PriceMessage();
    priceMessage.id = new ProductIdMessage();
    priceMessage.id.value = PRICE_PRODUCT_ID;
    priceMessage.value = PRICE_VALUE;
    priceMessage.currency = PRICE_CURRENCY_CODE;

    codec.encodeToWire(buf, priceMessage);

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

    PriceMessage priceMessage = codec.decodeFromWire(0, buf);

    assertEquals(PRICE_PRODUCT_ID, priceMessage.id.value);
    assertEquals(PRICE_VALUE, priceMessage.value);
    assertEquals(PRICE_CURRENCY_CODE, priceMessage.currency);
  }
}
