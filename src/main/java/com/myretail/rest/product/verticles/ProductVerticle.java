package com.myretail.rest.product.verticles;

import com.myretail.rest.product.enums.EventAddress;
import com.myretail.rest.product.resource.ProductResource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import java.util.logging.Logger;

/**
 * Worker verticle for interacting with remote API for product data.
 * <p>
 * Read-only product details are accessed from the Redsky API. This verticle communicates on the
 * event bus at {@link EventAddress#PRODUCT}. To find a product send an {@link
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

    ProductResource resource = new ProductResource(vertx);

    vertx.eventBus().consumer(EventAddress.GET_PRODUCT.name(), resource::findProduct);

    startPromise.complete();
  }
}
