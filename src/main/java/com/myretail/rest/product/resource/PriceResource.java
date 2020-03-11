package com.myretail.rest.product.resource;

import static com.myretail.rest.product.enums.ResourceError.ACCESS_ERROR;
import static com.myretail.rest.product.enums.ResourceError.READ_ERROR;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.myretail.model.Price;
import com.myretail.rest.product.enums.EventAddress;
import com.myretail.rest.product.message.PriceMessage;
import com.myretail.rest.product.message.ProductIdMessage;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Price resource endpoint.
 * <p>
 * Product price data is stored in a Google Cloud Firestore. This resource provides read/write
 * handlers for event bus requests.
 * <p>
 * Connections to Firestore use a Service Account credentials. When running on GCP these credentials
 * will most likely be provided. To run locally you must have the Service Account access key. For
 * details about the credential process {@see https://cloud.google.com/docs/authentication/production#providing_credentials_to_your_application}.
 * After you have the Service Account JSON key set the environment variable {@code
 * GOOGLE_APPLICATION_CREDENTIALS} to the absolute or relative path of the file.
 * <p>
 * This verticle communicates on the event bus at {@link EventAddress#PRICE}. To find a product
 * price send an {@link EventBus#request(String, Object, Handler)} to the address with the product
 * id as a string as the message data. On success, this verticle will respond with a Price object
 * instance. On failure message (String) and code will be returned.
 */
public class PriceResource {

  public static final Logger logger = Logger.getLogger(PriceResource.class.getName());
  // Firestore connection instance
  private final Firestore db;
  // Connection factory
  private FirestoreOptions firestoreOptions;

  public PriceResource() throws Exception {
    // Connect to firestore service
    firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("myretail-example")
        .build();

    // Get connection instance
    db = firestoreOptions.getService();
  }

  public void close() {
    // Close the Firestore connection and invalidate factory
    //
    // Once closed the connection factory will no longer be valid
    try {
      db.close();
      firestoreOptions = null;
    } catch (Exception e) {
      // Simply log errors because we should only be closing when done with the price resource
      logger.log(Level.WARNING, "Exception on closing Firestore connection", e);
    }
  }

  public void getPrice(Message<ProductIdMessage> message) {
    logger.info("Received get price message");

    ProductIdMessage id = message.body();

    try {
      // Fetch document reference
      DocumentReference reference = db.collection("product")
          .document("tcin_" + id.value);
      ApiFuture<DocumentSnapshot> query = reference.get();
      // ...
      // query.get() blocks on response but we're in a worker verticle
      DocumentSnapshot document = query.get();
      Price price = null;

      if (document.exists()) {
        double value = document.getDouble("price_value");
        String currencyCode = document.getString("price_currency_code");

        price = new Price();
        price.setValue(value);
        price.setCurrency(currencyCode);
      }

      message.reply(price);
    } catch (InterruptedException | ExecutionException e) {
      logger.log(Level.WARNING, "Unable to access Firebase", e);
      ACCESS_ERROR.replyTo(message);
    } catch (NullPointerException e) {
      logger.log(Level.WARNING, "Error reading price value from Firebase", e);
      READ_ERROR.replyTo(message);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Unknown error", e);
      ACCESS_ERROR.replyTo(message);
    }
  }

  public void setPrice(Message<PriceMessage> message) {
    logger.info("Received set price message");

    PriceMessage priceMessage = message.body();
    System.out.println(priceMessage);
    try {
      DocumentReference docRef = db.collection("product").document("tcin_" + priceMessage.id.value);
      Map<String, Object> data = new HashMap<>();
      data.put("price_value", priceMessage.value);
      data.put("price_currency_code", priceMessage.currency);
      //asynchronously write data
      ApiFuture<WriteResult> result = docRef.set(data);

      message.reply(result.get().getUpdateTime().toString());
    } catch (InterruptedException | ExecutionException e) {
      logger.log(Level.WARNING, "Unable to access Firebase", e);
      ACCESS_ERROR.replyTo(message);
    } catch (NullPointerException e) {
      logger.log(Level.WARNING, "Error reading price value from Firebase", e);
      READ_ERROR.replyTo(message);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Unknown error", e);
      ACCESS_ERROR.replyTo(message);
    }
  }
}
