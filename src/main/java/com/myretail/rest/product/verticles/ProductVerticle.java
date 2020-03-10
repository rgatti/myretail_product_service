package com.myretail.rest.product.verticles;

import com.myretail.model.Product;
import com.myretail.model.util.InvalidJsonData;
import com.myretail.model.util.ProductApiMapper;
import com.myretail.rest.product.VerticleBusAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import java.util.logging.Logger;

/**
 * Worker verticle for interacting with remote API for product data.
 * <p>
 * Read-only product details are accessed from the Redsky API. This verticle communicates on the
 * event bus at {@link VerticleBusAddress#PRODUCT}. To find a product send an {@link
 * EventBus#request(String, Object, Handler)} to the address with the product id as a string as the
 * message data. This verticle will respond with a Product object. On success, this verticle will
 * respond with a Product object instance. On failure message (String) and code will be returned.
 * <p>
 * <pre>
 * eventBus.&lt;Product&gt;request(VerticleBusAddress.PRODUCT, "1", ar -> {
 *    Product p = ar.result().body();
 * });
 * </pre>
 */
public class ProductVerticle extends AbstractVerticle {

  // API connection details
  private static final String API_HOST = "redsky.target.com";
  private static final String API_ENDPOINT = "/v2/pdp/tcin";
  private static final String API_QS = "?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics";

  private static final Logger logger = Logger.getLogger(ProductVerticle.class.getName());

  /**
   * Start the verticle instance.
   * <p>
   * After the verticle has been started the promise will be completed.
   *
   * @param startPromise the future
   * @see io.vertx.core.Verticle#start(Promise)
   */
  @Override
  public void start(Promise<Void> startPromise) {
    logger.info("Starting product worker verticle");

    // Register an even bus handler
    EventBus bus = vertx.eventBus();
    bus.consumer(VerticleBusAddress.PRODUCT, this::findProduct);

    startPromise.complete();
  }

  private void findProduct(Message<String> message) {
    logger.info("Received find product message");
    int productId;

    // Parse product id from message
    try {
      productId = Integer.parseInt(message.body());
    } catch (NumberFormatException e) {
      message.fail(1, "unable to read product id in message");
      return;
    }

    // Request product data from backing service
    WebClient.create(vertx)
        .get(443, API_HOST, buildRequest(productId))
        .ssl(true)
        .putHeader("Accept", "application/json")
        .as(BodyCodec.jsonObject())// decode response as json
        .expect(ResponsePredicate.SC_OK)// response is considered valid if 200 OK
        .send(asyncResult -> {
          if (asyncResult.succeeded()) {
            try {
              JsonObject body = asyncResult.result().body();
              Product product = ProductApiMapper.parseApiJson(body);
              message.reply(product);
            } catch (InvalidJsonData e) {
              logger.warning(e.getMessage());
              message.fail(2, "unable to read product data");
            }
          } else {
            logger.warning(asyncResult.cause().getMessage());
            message.fail(3, "product api failed");
          }
        });
  }

  private String buildRequest(int productId) {
    String request = API_ENDPOINT + "/" + productId + API_QS;
    logger.info("Building product api request: " + request);
    return request;
  }
}
