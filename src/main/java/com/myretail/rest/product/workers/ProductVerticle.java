package com.myretail.rest.product.workers;

import com.myretail.model.Product;
import com.myretail.model.util.InvalidJsonData;
import com.myretail.model.util.ProductApiMapper;
import com.myretail.rest.product.VerticleBusAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import java.util.logging.Logger;

/**
 * Worker verticle that interacts with a remote product API.
 * <p>
 * Read-only product details are accessed from the redsky API. This verticle provides access based
 * on requests from the event bus. Requests for product details should be sent to
 */
public class ProductVerticle extends AbstractVerticle {

  private static final String API_HOST = "redsky.target.com";
  private static final String API_ENDPOINT = "/v2/pdp/tcin";
  private static final String API_QS = "?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics";
  private static Logger logger = Logger.getLogger(ProductVerticle.class.getName());

  @Override
  public void start(Promise<Void> startPromise) {
    logger.info("Starting product worker verticle");

    // Register a handler for messages to this verticle
    EventBus bus = vertx.eventBus();
    bus.consumer(VerticleBusAddress.PRODUCT, this::findProduct);

    startPromise.complete();
  }

  private void findProduct(Message<String> message) {
    logger.info("Received find product message");
    int productId;

    try {
      productId = Integer.parseInt(message.body());
    } catch (NumberFormatException e) {
      message.fail(1, "unable to read product id in message");
      return;
    }

    WebClient.create(vertx)
        .get(443, API_HOST, buildRequest(productId))
        .ssl(true)
        .putHeader("Accept", "application/json")
        .as(BodyCodec.jsonObject())
        .expect(ResponsePredicate.SC_OK)
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
