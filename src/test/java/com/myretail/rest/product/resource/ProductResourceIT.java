package com.myretail.rest.product.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.myretail.model.Product;
import com.myretail.rest.product.message.ProductIdMessage;
import com.myretail.rest.product.message.codec.ProductCodec;
import com.myretail.rest.product.message.codec.ProductIdCodec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class ProductResourceIT {

  public static final String ADDRESS = "A";

  @Test
  void findValidProduct(Vertx vertx, VertxTestContext testContext) throws Throwable {
    // Create product resource to test
    ProductResource productResource = new ProductResource(vertx);
    // Setup event bus
    EventBus eventBus = vertx.eventBus();
    eventBus.registerDefaultCodec(ProductIdMessage.class, new ProductIdCodec());
    eventBus.registerDefaultCodec(Product.class, new ProductCodec());
    eventBus.consumer(ADDRESS, productResource::findProduct);

    // Create message
    ProductIdMessage id = new ProductIdMessage();
    id.value = 13860428;

    // Send to resource
    eventBus.request(ADDRESS, id, testContext.completing());

    assertTrue(testContext.awaitCompletion(3, TimeUnit.SECONDS));

    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
}
