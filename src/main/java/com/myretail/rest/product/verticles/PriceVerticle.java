package com.myretail.rest.product.verticles;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.myretail.model.Price;
import com.myretail.rest.product.VerticleBusAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker verticle that interacts with Cloud Firestore for product price data.
 * <p>
 * Product price data is stored in a Google Cloud Firestore. This verticle provides read/write
 * access based on event bus requests.
 * <p>
 * Connections to Firestore use a Service Account credentials. When running on GCP these credentials
 * will most likely be provided. To run locally you must have the Service Account access key. For
 * details about the credential process {@see https://cloud.google.com/docs/authentication/production#providing_credentials_to_your_application}.
 * After you have the Service Account JSON key set the environment variable {@code
 * GOOGLE_APPLICATION_CREDENTIALS} to the absolute or relative path of the file.
 * <p>
 * This verticle communicates on the event bus at {@link VerticleBusAddress#PRICE}. To find a
 * product price send an {@link EventBus#request(String, Object, Handler)} to the address with the
 * product id as a string as the message data. On success, this verticle will respond with a Price
 * object instance. On failure message (String) and code will be returned.
 * <p>
 * <pre>
 * eventBus.&lt;Price&gt;request(VerticleBusAddress.PRICE, "1", ar -> {
 *    Price p = ar.result().body();
 * });
 * </pre>
 */
public class PriceVerticle extends AbstractVerticle {

  private static final Logger logger = Logger.getLogger(PriceVerticle.class.getName());

  // Connection factory
  private FirestoreOptions firestoreOptions;

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
    logger.info("Starting price worker verticle");

    // Connect to the Firebase instance
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

    // Parse product id from message
    try {
      productId = Integer.parseInt(message.body());
    } catch (NumberFormatException e) {
      message.fail(1, "unable to read product id in message");
      return; // stop processing
    }

    // Open new connection
    try (Firestore db = firestoreOptions.getService()) {
      // Fetch document reference
      DocumentReference reference = db.collection("product")
          .document("tcin_" + productId);
      ApiFuture<DocumentSnapshot> query = reference.get();
      // ...
      // query.get() blocks on response but we're in a worker verticle
      DocumentSnapshot document = query.get();
      Price price = new Price();

      if (document.exists()) {
        double value = document.getDouble("price_value");
        String currencyCode = document.getString("price_currency_code");

        price.setValue(value);
        price.setCurrency(currencyCode);
      }

      message.reply(price);
    } catch (InterruptedException | ExecutionException e) {
      logger.log(Level.WARNING, "Unable to access Firebase", e);
      message.fail(1, "unable to access Firestore");
    } catch (NullPointerException e) {
      logger.log(Level.WARNING, "Error reading price value from Firebase", e);
      message.fail(1, "unable to read price from Firestore");
    } catch (Exception e) {
      logger.log(Level.WARNING, "Unknown error", e);
      message.fail(1, "error accessing Firestore");
    }
  }

  private void setPrice(Message<String> message) {
    // Open new connection
    try (Firestore db = firestoreOptions.getService()) {
      DocumentReference docRef = db.collection("product").document("tcin_15643793");
      Map<String, Object> data = new HashMap<>();
      data.put("price_value", 5.00);
      data.put("price_currency_code", "USD");
      //asynchronously write data
      ApiFuture<WriteResult> result = docRef.set(data);
      // ...
      // result.get() blocks on response

    } catch (InterruptedException | ExecutionException e) {
      logger.log(Level.WARNING, "Unable to access Firebase", e);
      message.fail(1, "unable to access Firestore");
    } catch (NullPointerException e) {
      logger.log(Level.WARNING, "Error reading price value from Firebase", e);
      message.fail(1, "unable to read price from Firestore");
    } catch (Exception e) {
      logger.log(Level.WARNING, "Unknown error", e);
      message.fail(1, "error accessing Firestore");
    }
  }
}