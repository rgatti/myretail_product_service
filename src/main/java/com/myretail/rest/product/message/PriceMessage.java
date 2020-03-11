package com.myretail.rest.product.message;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "value", "currency"})
public class PriceMessage {

  public ProductIdMessage id;
  public double value;
  public String currency;

  @Override
  public String toString() {
    return "Price{" +
        "id=" + id +
        ", value=" + value +
        ", currency='" + currency + '\'' +
        '}';
  }
}
