package com.myretail.model;

/**
 * Represents a price value and currency code.
 */
public class Price {

  private double value;
  private String currency;

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }
}
