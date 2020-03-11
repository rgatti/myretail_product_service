# myretail product service

## Table of Contents

* [Summary](#summary)
* [Architecture](#arch)
* [Build](#build)

## <a href="#summary">Summary</a>

This is a case study project of a RESTful service broken into three separate microservices using the Virt.x framework. The three services are a front end controller and two backend resource managers. The backend resource manager handle asynchronous calls to remote APIs and a cloud database (Google Cloud Firestore).


## <a href="#arch">Architecture</a>

Below is a high-level overview of the architecture.
![architecture](https://raw.githubusercontent.com/rgatti/myretail_product_service/master/doc/architecture0.png)

In Vert.x, `Verticle`s are used to isolate responsibility. A `Launcher` class configures the runtime environment and deploys the three verticles. The `ServiceVerticle` is deployed a standard verticle while `ProductVerticle` and `PriceVerticle` are deployed as workers. Worker verticles are background processes with separate thread isolation and resource limits. A single instance of each verticle is spawned.

The `ServiceVertice` is the entry point for the entire service. It interacts with the two worker verticles across the Vert.x event bus. The event bus provides an efficient method of interprocess communication.

## <a href="#build">Build</a>

The application is built with Maven. Some unit and itegration tests are automatically run with the surefire and failsafe plugins, respectfully.

All dependencies are copied into a `lib` directory. This makes it easy to deploy or build as a container.

To build and run locally a service credential file is required in order to access the Google Cloud Firestore database.

```
$ mvn clean package
$ GOOGLE_APPLICATION_CREDENTIALS=.env/service-key.json java -jar target/product-service-1.0.jar 
```

 Docker can be used to package a product ready container. The container image is built with a multistage process. First, the offical Maven container builds the application in a sandbox environment. Next, the office OpenJDK image is pulled and the compiled artifacts are copied from the build stage.
 
 To build the container image  
 
 ```
$ docker build -t myretail/product-service:1.0 .
```

Alternatively, Google Cloud Build and Cloud Run services can be used to deploy this container.

First, submit the build to Cloud Build. This will create the final container image and store it in Google Container Registery. 

```
$ gcloud builds submit --tag gcr.io/myretail-example/product-service
```

Next, deploy the image to Cloud Run.

```
$ gcloud run deploy --image gcr.io/myretail-example/product-service --platform managed
``` 

When running on Google Cloud Platform all service credentials are provided.