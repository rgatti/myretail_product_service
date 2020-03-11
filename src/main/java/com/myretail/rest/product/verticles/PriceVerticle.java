package com.myretail.rest.product.verticles;

import com.myretail.rest.product.enums.EventAddress;
import com.myretail.rest.product.resource.PriceResource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker verticle that manages the price resource.
 * <p>
 * This verticle listens on addresses {@link EventAddress#GET_PRICE} and {@link
 * EventAddress#UPDATE_PRICE}.
 * <p>
 * <pre>
 * eventBus.&lt;Price&gt;request(EventAddress.GET_PRICE.name(), "1", ar -> {
 *    Price p = ar.result().body();
 * });
 * </pre>
 *
 * @see PriceResource
 */
public class PriceVerticle extends AbstractVerticle {

  private static final Logger logger = Logger.getLogger(PriceVerticle.class.getName());

  private PriceResource resource;

  @Override
  public void start(Promise<Void> startPromise) {
    logger.info("Starting price worker verticle");

    try {
      resource = new PriceResource();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to connect to create price resource", e);
      startPromise.fail("Unable to connect to create price resource");
      return;
    }

    // Register handlers for messages to this verticle
    vertx.eventBus().consumer(EventAddress.GET_PRICE.name(), resource::getPrice);
    vertx.eventBus().consumer(EventAddress.UPDATE_PRICE.name(), resource::setPrice);

    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    // Make sure to close the price resource
    resource.close();
    stopPromise.complete();
  }
}
