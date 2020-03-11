package com.myretail.rest.product.verticles;

import static com.myretail.rest.product.enums.ResourceError.RESOURCE_MISSING;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.myretail.model.Price;
import com.myretail.model.Product;
import com.myretail.model.util.InvalidJsonData;
import com.myretail.rest.product.enums.EventAddress;
import com.myretail.rest.product.message.PriceMessage;
import com.myretail.rest.product.message.ProductIdMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The product REST service.
 * <p>
 * This verticle acts as the front end controller of the service. It will create a {@code
 * HttpServer} and lilsten on the configured port. This service responds to the resource path {@code
 * /rest/product}.
 * <p>
 * The available endpoints are:
 * <ul>
 *   <li>GET {@code /rest/product/:id}
 *   <li>POST {@code /rest/product/:id}
 * <p>
 * The post body must contain the product price and currency as JSON.
 * <p>
 * <pre>
 * {
 *    "value": 1.00,
 *    "currency": "USD"
 * }
 * </pre>
 * </ul>
 */
public class ServiceVerticle extends AbstractVerticle {

  // This services endpoint
  private static final String ENDPOINT = "/rest/product";

  private static final Logger logger = Logger.getLogger(ServiceVerticle.class.getName());

  private EventBus eventBus;

  @Override
  public void start(Promise<Void> promise) {
    logger.info("Starting product resource verticle");

    eventBus = vertx.eventBus();

    // Add handlers for routes
    Router route = Router.router(vertx);

    route.get(ENDPOINT + "/:id")
        .handler(this::getProduct);

    route.post(ENDPOINT + "/:id")
        .handler(BodyHandler.create())
        .handler(this::updateProductPrice);

    // Create http server and reply to launcher on complete
    vertx.createHttpServer()
        .requestHandler(route)
        .listen(config().getInteger("port"), asyncResult -> {
          if (asyncResult.succeeded()) {
            promise.complete();
          } else {
            promise.fail(asyncResult.cause());
          }
        });
  }

  /**
   * Compose a {@link Product} from the {@link ProductVerticle} and {@link PriceVerticle}.
   *
   * @param context the Vertx web context being handled
   */
  private void getProduct(RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();

    ProductIdMessage id = ProductIdMessage.valueOf(request.getParam("id"));

    logger.info("Received request for " + id);

    // Request product and price from workers
    Future<Message<Product>> productFuture = Future
        .future(promise -> eventBus.request(EventAddress.GET_PRODUCT.name(), id, promise));
    Future<Message<Price>> priceFuture = Future
        .future(promise -> eventBus.request(EventAddress.GET_PRICE.name(), id, promise));

    CompositeFuture.join(productFuture, priceFuture)
        .onFailure(asyncResult -> {
          ReplyException exception = (ReplyException) asyncResult;

          if (exception.failureCode() == RESOURCE_MISSING.getCode()) {
            response.setStatusCode(NOT_FOUND.code())
                .putHeader("Content-Type", "application/json")
                .end(RESOURCE_MISSING.toJson());
          } else {
            response.setStatusCode(INTERNAL_SERVER_ERROR.code())
                .putHeader("Content-Type", "application/json")
                .end(errorResponseJson(1, asyncResult.getMessage()));
          }
        })
        .onSuccess(asyncResult -> {
          Product product = asyncResult.<Message<Product>>resultAt(0).body();
          Optional<Price> priceOptional = Optional
              .ofNullable(asyncResult.<Message<Price>>resultAt(1).body());

          // If we have a price set it in the product
          priceOptional.ifPresent(product::setPrice);

          // Send response
          response.putHeader("Content-Type", "application/json")
              .end(Json.encode(product));
        });
  }

  /**
   * Update the price of a product.
   *
   * @param context the Vertx web context being handled
   */
  private void updateProductPrice(RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();

    PriceMessage priceMessage;

    // Parse price data from body
    try {
      JsonObject priceObject = context.getBodyAsJson();
      if (!priceObject.containsKey("value") || !priceObject.containsKey("currency")) {
        throw new InvalidJsonData("Price json missing value or currency");
      }
      priceMessage = priceObject.mapTo(PriceMessage.class);
    } catch (InvalidJsonData e) {
      response.setStatusCode(BAD_REQUEST.code())
          .putHeader("Content-Type", "application/json")
          .end(errorResponseJson(2, e.getMessage()));
      return;
    } catch (NullPointerException | ClassCastException e) {
      logger.log(Level.SEVERE, "Unable to parse json body", e);
      response.setStatusCode(BAD_REQUEST.code())
          .putHeader("Content-Type", "application/json")
          .end(errorResponseJson(2, "Error parsing price json"));
      return;
    }

    priceMessage.id = ProductIdMessage.valueOf(request.getParam("id"));

    logger.info("Received update price for " + priceMessage);

    // Call price verticle
    eventBus.<String>request(EventAddress.UPDATE_PRICE.name(), priceMessage, asyncResult -> {
      response.putHeader("Content-Type", "application/json");

      if (asyncResult.succeeded()) {
        JsonObject respObj = new JsonObject();
        respObj.put("id", priceMessage.id.value);
        respObj.put("timestamp", asyncResult.result().body());
        response.end(respObj.encode());
      } else {
        response.end(errorResponseJson(3, asyncResult.cause().getMessage()));
      }
    });
  }

  // Generate JSON when replying with an error
  private String errorResponseJson(int errorCode, String reason) {
    JsonObject object = new JsonObject();
    object.put("errorCode", errorCode);
    object.put("reason", reason);
    return object.encode();
  }
}
