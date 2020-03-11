package com.myretail.rest.product.message;

/**
 * Model a product id.
 * <p>
 * Products are uniquely referenced by their ID and providing a model allows various forms of
 * communication. Models can easily be sent across a message bus. Models also provide compile-time
 * validation of data.
 */
public class ProductIdMessage {

  public int value;

  public static ProductIdMessage valueOf(String s) {
    ProductIdMessage id = new ProductIdMessage();
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
