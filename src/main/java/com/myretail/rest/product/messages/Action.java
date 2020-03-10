package com.myretail.rest.product.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.JsonDeserializationContext;
import io.vertx.core.json.JsonObject;

/**
 * Generic action for worker verticles to perform.
 * <p>
 * These actions are representative of CRUD operations on access objects. Since all properites
 */
@JsonPropertyOrder({"type", "data"})
public class Action {

  public static final String GET = "GET";
  public static final String UPDATE = "UPDATE";

  private String type;
  private JsonObject data;

  public Action(String action, JsonObject data) {
    this.type = action;
    this.data = data;
  }

  public String getType() {
    return type;
  }

  public JsonObject getData() {
    return data;
  }

  /**
   * Returns a new Action with type GET.
   * @param data the action data
   * @return an Action
   */
  public static Action get(JsonObject data) {
    return new Action(GET, data);
  }

  /**
   * Returns a new Action with type UPDATE.
   * @param data the action data
   * @return an Action
   */
  public static Action update(JsonObject data) {
    return new Action(UPDATE, data);
  }

}
