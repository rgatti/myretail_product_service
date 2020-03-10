package com.myretail.rest.product;

import com.myretail.model.Price;
import com.myretail.model.Product;
import com.myretail.rest.product.messages.PriceMessageCodec;
import com.myretail.rest.product.messages.ProductMessageCodec;
import com.myretail.rest.product.verticles.PriceVerticle;
import com.myretail.rest.product.verticles.ProductResourceVerticle;
import com.myretail.rest.product.verticles.ProductVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Launch the product service.
 * <p>
 * This class is a wrapper to configure and deploy vertx verticles which handle all processing. All
 * verticles are deployed relative to this class.
 */
public class Launcher {

  // Worker verticles
  private static final List<Class<? extends Verticle>> WORKERS = List.of(
      PriceVerticle.class,
      ProductVerticle.class);
  // Front end verticles
  private static final List<Class<? extends Verticle>> FRONTEND = List.of(
      ProductResourceVerticle.class);

  private static final Logger logger = Logger.getLogger(Launcher.class.getName());

  public static void main(String[] args) {
    logger.info("Starting product service");
    Vertx vertx = Vertx.vertx();

    // Register default model codecs
    //
    // These allow concrete models to be passed as messages between verticles.
    vertx.eventBus().registerDefaultCodec(Product.class, new ProductMessageCodec());
    vertx.eventBus().registerDefaultCodec(Price.class, new PriceMessageCodec());

    /* Deploy verticles
     *
     * Capture the result of start up. If any of the verticles fail stop the product service.
     */

    // Futures for all verticles
    //
    // We have to use a raw type because the CompositeFuture can not join on polymorphic types.
    @SuppressWarnings("rawtypes") final var verticles = new ArrayList<Future>();

    BiConsumer<Class, DeploymentOptions> deployVerticle = (cls, options) -> {
      Future<String> future = options == null ?
          Future.future(promise -> vertx.deployVerticle(cls.getName(), promise)) :
          Future.future(promise -> vertx.deployVerticle(cls.getName(), options, promise));

      verticles.add(future);
    };

    // Deploy verticles
    DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
    WORKERS.forEach(cls -> deployVerticle.accept(cls, workerOptions));
    FRONTEND.forEach(cls -> deployVerticle.accept(cls, null));

    // Wait for workers to start up, then deploy front end product resource verticle
    CompositeFuture.join(verticles)
        // if any worker fails stop service
        .onFailure(result -> {
          result.printStackTrace();
          vertx.close();
          logger.severe("Failed to start all verticles ... service stopped");
        })
        .onSuccess(r -> logger.info("Service started"));
  }
}
