package com.myretail.rest.product;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BodyHandler;

public class App {
    static class Product {
        int id;
        String title;

        Product(int id, String title) {
            this.id = id;
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }

    static class Price {
        double value;
        String currencyCode;

        Price(double value, String currencyCode) {
            this.value = value;
            this.currencyCode = currencyCode;
        }

        public double getValue() {
            return value;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }
    }

    public static void main(String[] args) {
        // Get vertx instance
        Vertx vertx = Vertx.vertx();
        // Create HTTP server
        HttpServer server = vertx.createHttpServer();
        // Add routing
        Router router = Router.router(vertx);
        // Get product
        router.get("/product/:id").handler(context -> {
            String id = context.request().getParam("id");

            // Request data from redsky
            WebClient.create(vertx)
                    .get(443, "redsky.target.com", "/v2/pdp/tcin/" + id + "?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics")
                    .ssl(true)
                    .putHeader("Accept", "application/json")
                    .as(BodyCodec.jsonObject())
                    .expect(ResponsePredicate.SC_OK)
                    .send(asyncResult -> {
                        if (asyncResult.succeeded()) {
                            HttpServerResponse response = context.response();

                            try {
                                JsonObject body = asyncResult.result().body();
                                JsonObject item = body.getJsonObject("product").getJsonObject("item");
                                String productId = item.getString("tcin");
                                String title = item.getJsonObject("product_description").getString("title");

                                Product product = new Product(Integer.valueOf(productId), title);
                                response.end(Json.encodePrettily(product));
                            } catch (Exception e) {
                                response.end("error parsing data " + e.getMessage());
                            }
                        } else {
                            HttpServerResponse response = context.response();
                            response.end("error");
                        }
                    });
        });

        // Update price
        router.post("/product/:id").handler(BodyHandler.create());
        router.post("/product/:id").handler(context -> {
            HttpServerRequest request = context.request();
            String id = request.getParam("id");
            System.out.printf("got id " + id);
            try {
                JsonObject body = context.getBodyAsJson();
                System.out.println("updating price " + body.toString() + " for product " + id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            context.response().end();
        });
        // Start server
        server.requestHandler(router).listen(8080);
    }
}
