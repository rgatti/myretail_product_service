package com.myretail.rest.product.message.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.myretail.model.Price;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import org.junit.jupiter.api.Test;

public class PriceCodecTests {

  private static final int RAW_JSON_LENGTH = 30;
  private static final String RAW_JSON = "{\"value\":1.0,\"currency\":\"USD\"}";

  private static final double PRICE_VALUE = 1.00;
  private static final String PRICE_CURRENCY_CODE = "USD";

  private static PriceCodec codec = new PriceCodec();

  @Test
  void encode() {
    Buffer buf = new BufferImpl();

    Price price = new Price();
    price.setValue(PRICE_VALUE);
    price.setCurrency(PRICE_CURRENCY_CODE);

    codec.encodeToWire(buf, price);

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

    Price price = codec.decodeFromWire(0, buf);

    assertEquals(PRICE_VALUE, price.getValue());
    assertEquals(PRICE_CURRENCY_CODE, price.getCurrency());
  }
}
