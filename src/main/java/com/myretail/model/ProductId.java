package com.myretail.model;

import io.vertx.core.json.JsonObject;

/**
 * Model a product id.
 * <p>
 * Products are uniquely referenced by their ID and providing a model allows various forms of
 * communication. Models can easily be sent across a message bus. Models also provide compile-time
 * validation of data.
 */
public class ProductId {

  public int value;

  public static ProductId valueOf(String s) {
    ProductId id = new ProductId();
    id.value = Integer.parseInt(s);
    return id;
  }

  @Override
  public String toString() {
    return "ProductId{" +
        "value=" + value +
        '}';
  }
}
