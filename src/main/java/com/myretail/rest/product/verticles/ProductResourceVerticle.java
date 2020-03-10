package com.myretail.rest.product.verticles;

import com.myretail.model.Price;
import com.myretail.model.Product;
import com.myretail.model.ProductId;
import com.myretail.rest.product.VerticleBusAddress;
import com.myretail.rest.product.messages.Action;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The entry point for the product REST service.
 * <p>
 * This verticle acts as the front end controller of the service.
 */
public class ProductResourceVerticle extends AbstractVerticle {

  // This services endpoint
  private static final String ENDPOINT = "/rest/product";
  private static final Logger logger = Logger.getLogger(ProductResourceVerticle.class.getName());

  private EventBus eventBus;

  @Override
  public void start(Promise<Void> promise) {
    logger.info("Starting product resource verticle");
    eventBus = vertx.eventBus();

    // Create routes to handlers
    Router route = Router.router(vertx);
    route.get(ENDPOINT + "/:id").handler(this::getProduct);
    route.post(ENDPOINT + "/:id/price")
        .handler(BodyHandler.create())
        .handler(this::updateProductPrice);

    // Create http server and reply to launcher on complete
    vertx.createHttpServer()
        .requestHandler(route)
        .listen(8080, asyncResult -> {
          if (asyncResult.succeeded()) {
            promise.complete();
          } else {
            promise.fail(asyncResult.cause());
          }
        });
  }

  private void getProduct(RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();

    // Create get action for requesting product and price data
    ProductId id = ProductId.valueOf(request.getParam("id"));
    Action action = Action.get(JsonObject.mapFrom(id));

    logger.info("Received request for " + id);

    // Request product and price from workers
    Future<Message<Product>> productFuture = Future
        .future(promise -> eventBus.request(VerticleBusAddress.PRODUCT, action, promise));
    Future<Message<Price>> priceFuture = Future
        .future(promise -> eventBus.request(VerticleBusAddress.PRICE, action, promise));

    CompositeFuture.join(productFuture, priceFuture)
        .onFailure(asyncResult -> response.setStatusCode(500)
            .putHeader("Content-Type", "plain/text")
            .end(asyncResult.getMessage()))
        .onSuccess(asyncResult -> {
          Product product = asyncResult.<Message<Product>>resultAt(0).body();
          Price price = asyncResult.<Message<Price>>resultAt(1).body();

          // Update the product model with the fetched price
          product.getPrice().setValue(price.getValue());
          product.getPrice().setCurrency(price.getCurrency());

          // Send response
          response.putHeader("Content-Type", "application/json")
              .end(Json.encode(product));
        });
  }

  private void updateProductPrice(RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();

    // Create action for updating price data
    ProductId id = ProductId.valueOf(request.getParam("id"));
    Price price;

    logger.info("Received update price for " + id);

    // Parse price data from body
    try {
      price = context.getBodyAsJson().mapTo(Price.class);
    } catch (NullPointerException | ClassCastException e) {
      logger.log(Level.SEVERE, "Unable to parse json body", e);
      response.setStatusCode(400)
          .end("Unable to read price json");
      return;
    }

    // Construct action
    JsonObject actionData = new JsonObject();
    actionData.put("id", JsonObject.mapFrom(id));
    actionData.put("price", JsonObject.mapFrom(price));
    Action action = Action.update(actionData);

    // Call price verticle
    eventBus.<String>request(VerticleBusAddress.PRICE, action, asyncResult -> {
      if (asyncResult.succeeded()) {
        response.end(asyncResult.result().body());
      } else {
        response.end(asyncResult.cause().getMessage());
      }
    });
  }
}
