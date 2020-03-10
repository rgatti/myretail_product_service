package com.myretail.rest.product;

import com.myretail.model.Price;
import com.myretail.model.Product;
import com.myretail.rest.product.messages.Action;
import com.myretail.rest.product.messages.ActionCodec;
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
import io.vertx.core.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
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
    EventBus eventBus = vertx.eventBus();

    /* Register default codecs
     *
     * These allow message passing on the event bus with custom models.
     */

    eventBus.registerDefaultCodec(Product.class, new ProductMessageCodec());
    eventBus.registerDefaultCodec(Price.class, new PriceMessageCodec());
    eventBus.registerDefaultCodec(Action.class, new ActionCodec());

    /* Deploy verticles
     *
     * Verticles will be deployed asynchronously. Capture start up status and if any verticles fails
     * stop the product service.
     */

    // We have to use a raw type because the CompositeFuture can not join on polymorphic types
    @SuppressWarnings("rawtypes") final var verticles = new ArrayList<Future>();

    // Deployment action
    BiConsumer<Class, DeploymentOptions> deployVerticle = (cls, options) -> {
      Future<String> future = options == null ?
          Future.future(promise -> vertx.deployVerticle(cls.getName(), promise)) :
          Future.future(promise -> vertx.deployVerticle(cls.getName(), options, promise));

      verticles.add(future);
    };

    // Deploy workers and frontend verticles
    DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
    WORKERS.forEach(cls -> deployVerticle.accept(cls, workerOptions));
    FRONTEND.forEach(cls -> deployVerticle.accept(cls, null));

    // Monitor startup
    CompositeFuture.join(verticles)
        .onFailure(result -> {
          result.printStackTrace();
          vertx.close();
          logger.severe("Failed to start all verticles ... service stopped");
        })
        .onSuccess(r -> logger.info("Service started"));
  }
}
