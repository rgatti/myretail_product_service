package com.myretail.rest.product;

import com.myretail.model.Price;
import com.myretail.model.Product;
import com.myretail.rest.product.messages.PriceMessageCodec;
import com.myretail.rest.product.messages.ProductMessageCodec;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Launcher for the vertx product service.
 * <p>
 * This class simply launches vertx verticles which handle all processing of requests and data
 * access.
 */
public class Launcher {

  public static final String WORKERS_PACKAGE = "workers";
  public static final List<String> WORKERS = List.of("PriceVerticle", "ProductVerticle");
  private static Logger logger = Logger.getLogger(Launcher.class.getName());

  public static void main(String[] args) {
    logger.info("Starting service");
    Vertx vertx = Vertx.vertx();

    // Register default codes for models
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
        name -> {
          Future<String> future = Future.future(promise -> vertx
              .deployVerticle(verticleName(WORKERS_PACKAGE, name), workerOptions, promise));
          workers.add(future);
        });

    // Wait for workers to start up, then deploy front end product resource verticle
    CompositeFuture.join(workers)
        .onFailure(result -> {
          result.printStackTrace();
          vertx.close();
          logger.severe("Failed to start all worker verticles ... service stopped");
        })
        .onSuccess(result -> vertx.deployVerticle(verticleName("ProductResourceVerticle"),
            e -> logger.info("Service started")));
  }

  // Utility methods to build verticle page names

  private static String verticleName(String packageName, String className) {
    return verticleName(packageName + "." + className);
  }

  private static String verticleName(String className) {
    String packageName = Launcher.class.getPackageName();
    return packageName + "." + className;
  }
}
