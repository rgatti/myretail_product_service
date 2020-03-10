package com.myretail.rest.product;

import com.myretail.model.Price;
import com.myretail.model.Product;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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
//    route.post(ENDPOINT + "/:id/price").handler(this::updateProductPrice);

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

  /**
   * Handle
   * @param context
   */
  private void getProduct(RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();

    String id = request.getParam("id");

    logger.info("Received product get request for " + id);

    // Request product and price from workers
    Future<Message<Product>> productFuture = Future
        .future(promise -> eventBus.request(VerticleBusAddress.PRODUCT, id, promise));
    Future<Message<Price>> priceFuture = Future
        .future(promise -> eventBus.request(VerticleBusAddress.PRICE, id, promise));

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

//  private void updateProductPrice(RoutingContext context) {
//    String id = context.request().getParam("id");
//  }
}
