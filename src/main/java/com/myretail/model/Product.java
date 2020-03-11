package com.myretail.model;

import io.vertx.core.Promise;

/**
 * Model to represent a product.
 *
 * Products are immutable except for their price.
 */
public class Product {
  private int id;
  private String title;
  private Price price;

  public Product(int id, String title) {
    this.id = id;
    this.title = title;
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setPrice(Price price) {
    this.price = price;
  }

  public Price getPrice() {
    return price;
  }
}
