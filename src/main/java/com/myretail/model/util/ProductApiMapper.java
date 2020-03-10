package com.myretail.model.util;

import com.myretail.model.Product;
import io.vertx.core.json.JsonObject;

/**
 * Utility object for mapping raw data from the product data store to a Product object.
 */
public class ProductApiMapper {

  /**
   * Parse json into a Product
   * @param object json object to parse
   * @return a Product
   */
  public static Product parseApiJson(JsonObject object) throws InvalidJsonData {
    if(object.isEmpty())
      throw new InvalidJsonData("No JSON data");

    int id;
    String title;

    try {
      JsonObject item = object.getJsonObject("product").getJsonObject("item");
      id = Integer.parseInt(item.getString("tcin"));
      title = item.getJsonObject("product_description").getString("title");
    } catch (NullPointerException e) {
      throw new InvalidJsonData("The JSON data is unable to be parsed into a Product", e);
    }

    return new Product(id, title);
  }
}
