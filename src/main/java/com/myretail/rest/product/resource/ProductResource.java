package com.myretail.rest.product.resource;

import static com.myretail.rest.product.enums.ResourceError.READ_ERROR;
import static com.myretail.rest.product.enums.ResourceError.RESOURCE_MISSING;

import com.myretail.model.Product;
import com.myretail.model.util.InvalidJsonData;
import com.myretail.model.util.ProductApiMapper;
import com.myretail.rest.product.message.ProductIdMessage;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import java.util.logging.Logger;

public class ProductResource {

  private static final Logger logger = Logger.getLogger(ProductResource.class.getName());

  // API connection details
  private static final String API_HOST = "redsky.target.com";
  private static final String API_ENDPOINT = "/v2/pdp/tcin";
  private static final String API_QS = "?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics";
  // Timeout in milliseconds, after which throw TimeoutException
  private static final int API_TIMEOUT = 2000;

  private Vertx vertx;

  public ProductResource(Vertx vertx) {
    this.vertx = vertx;
  }


  public void findProduct(Message<ProductIdMessage> message) {
    logger.info("Received find product message");

    ProductIdMessage id = message.body();

    // Request product data from backing service
    WebClient.create(vertx)
        .get(443, API_HOST, buildRequest(id))
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
              READ_ERROR.replyTo(message);
            }
          } else {
            logger.warning(asyncResult.cause().getMessage());
            RESOURCE_MISSING.replyTo(message);
          }
        });
  }

  private String buildRequest(ProductIdMessage id) {
    String request = API_ENDPOINT + "/" + id.value + API_QS;
    logger.info("Building product api request: " + request);
    return request;
  }
}
