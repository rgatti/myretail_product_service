package com.myretail.rest.product;

import com.myretail.model.Price;
import com.myretail.model.Product;
import com.myretail.rest.product.message.PriceMessage;
import com.myretail.rest.product.message.ProductIdMessage;
import com.myretail.rest.product.message.codec.PriceCodec;
import com.myretail.rest.product.message.codec.PriceMessageCodec;
import com.myretail.rest.product.message.codec.ProductCodec;
import com.myretail.rest.product.message.codec.ProductIdCodec;
import com.myretail.rest.product.verticles.PriceVerticle;
import com.myretail.rest.product.verticles.ProductVerticle;
import com.myretail.rest.product.verticles.ServiceVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Launch the product service.
 * <p>
 * This class is a wrapper to configure and deploy vertx verticles which handle all processing.
 */
public class Launcher {

  // Default listen port
  //
  // This must be a string because it will be parsed in readConfigProperties()
  public static final String DEFAULT_PORT = "8080";

  // Worker verticles
  private static final List<Class<? extends Verticle>> WORKERS = List.of(
      PriceVerticle.class,
      ProductVerticle.class);
  // Front end verticles
  private static final List<Class<? extends Verticle>> FRONTEND = List.of(
      ServiceVerticle.class);
  private static final Logger logger = Logger.getLogger(Launcher.class.getName());

  public static void main(String[] args) {
    logger.info("Starting product service");

    Vertx vertx = Vertx.vertx();
    EventBus eventBus = vertx.eventBus();

    /* Register default codecs
     *
     * These allow message passing of custom models on the event bus
     */
    eventBus.registerDefaultCodec(ProductIdMessage.class, new ProductIdCodec());
    eventBus.registerDefaultCodec(PriceMessage.class, new PriceMessageCodec());
    eventBus.registerDefaultCodec(Product.class, new ProductCodec());
    eventBus.registerDefaultCodec(Price.class, new PriceCodec());

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
    //

    DeploymentOptions workerOptions = new DeploymentOptions()
        .setWorker(true)
        .setInstances(1);

    DeploymentOptions frontendOptions = new DeploymentOptions()
        .setConfig(new JsonObject().put("port", getServerPort()));

    WORKERS.forEach(cls -> deployVerticle.accept(cls, workerOptions));
    FRONTEND.forEach(cls -> deployVerticle.accept(cls, frontendOptions));

    // Monitor startup
    CompositeFuture.join(verticles)
        .onFailure(result -> {
          result.printStackTrace();
          vertx.close();
          logger.severe("Failed to start all verticles ... service stopped");
        })
        .onSuccess(r -> logger.info("Service started"));
  }

  /* Get the server port or use the DEFAULT_PORT.
   *
   * This method will attempt to first read the environment variable $PORT. If no value exists
   * the system property {@code server.port} will be read. If that neither an environment variable
   * or system property exist, or if any exceptions are thrown, a default port of 8080 will be used.
   */
  private static int getServerPort() {
    String port;
    try {
      port = System.getenv("PORT");
      if (port == null) {
        port = System.getProperty("server.port");
      }
      if (port == null) {
        port = DEFAULT_PORT;
      }
    } catch (Exception e) {
      port = DEFAULT_PORT;
    }

    return Integer.parseInt(port);
  }
}
