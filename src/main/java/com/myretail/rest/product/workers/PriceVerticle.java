package com.myretail.rest.product.workers;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.myretail.model.Price;
import com.myretail.rest.product.VerticleBusAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import java.util.logging.Logger;

public class PriceVerticle extends AbstractVerticle {

  private static Logger logger = Logger.getLogger(PriceVerticle.class.getName());

  private FirestoreOptions firestoreOptions;

  @Override
  public void start(Promise<Void> startPromise) {
    logger.info("Starting price worker verticle");
    try {
      firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
          .setProjectId("myretail-example")
          .build();
    } catch (Exception e) {
      startPromise.fail("Unable to connect to Cloud Firestore database");
      return;
    }
    // Register a handler for messages to this verticle
    EventBus bus = vertx.eventBus();
    bus.consumer(VerticleBusAddress.PRICE, this::getPrice);

    startPromise.complete();
  }

  private void getPrice(Message<String> message) {
    logger.info("Received get price message");
    int productId;

    try {
      productId = Integer.parseInt(message.body());
    } catch (NumberFormatException e) {
      message.fail(1, "unable to read product id in message");
      return;
    }

    try (Firestore db = firestoreOptions.getService()) {
      DocumentReference reference = db.collection("product").document("tcin_" + productId);
      ApiFuture<DocumentSnapshot> query = reference.get();
      // ...
      // query.get() blocks on response but we're in a worker verticle
      DocumentSnapshot document = query.get();
      double value = document.getDouble("price_value");
      String currencyCode = document.getString("price_currency_code");

      Price price = new Price();
      price.setValue(value);
      price.setCurrency(currencyCode);

      message.reply(price);
    } catch (Exception e) {
      message.fail(1, "unable to read price from firestore");
      return;
    }
  }
}
