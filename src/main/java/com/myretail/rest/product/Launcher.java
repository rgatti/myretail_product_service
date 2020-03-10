package com.myretail.rest.product;

import com.myretail.model.Price;
import com.myretail.model.Product;
import com.myretail.rest.product.messages.PriceMessageCodec;
import com.myretail.rest.product.messages.ProductMessageCodec;
import com.myretail.rest.product.workers.PriceVerticle;
import com.myretail.rest.product.workers.ProductVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Launch the product service.
 * <p>
 * This class is a wrapper to configure and deploy vertx verticles which handle all processing. All
 * verticles are deployed relative to this class.
 */
public class Launcher {

  // Worker verticles to deploy
  private static final List<Class<? extends Verticle>> WORKERS = List.of(PriceVerticle.class,
      ProductVerticle.class);

  private static final Logger logger = Logger.getLogger(Launcher.class.getName());

  public static void main(String[] args) {
    logger.info("Starting product service");
    Vertx vertx = Vertx.vertx();

    // Register default model codecs
    //
    // These allow concrete models to be passed as messages between verticles.
    vertx.eventBus().registerDefaultCodec(Product.class, new ProductMessageCodec());
    vertx.eventBus().registerDefaultCodec(Price.class, new PriceMessageCodec());

    /* Deploy worker verticles
     *
     * Capture the result of worker start up. If any of the workers fail to start stop the product
     * service.
     */

    // Futures for all worker verticles
    //
    // We have to use a raw type because the CompositeFuture can not join on polymorphic types.
    @SuppressWarnings("rawtypes") final var workers = new ArrayList<Future>();

    // Deploy workers
    DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
    WORKERS.forEach(
        cls -> {
          Future<String> future = Future.future(promise -> vertx
              .deployVerticle(cls, workerOptions, promise));

          workers.add(future);
        });

    // Wait for workers to start up, then deploy front end product resource verticle
    CompositeFuture.join(workers)
        // if any worker fails stop service
        .onFailure(result -> {
          result.printStackTrace();
          vertx.close();
          logger.severe("Failed to start all worker verticles ... service stopped");
        })
        // start front end product resource, log on complete
        .onSuccess(r -> vertx.deployVerticle(new ProductResourceVerticle(),
            r0 -> logger.info("Service started")));
  }
}
