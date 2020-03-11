# myretail product service

## Table of Contents

* [Summary](#summary)
* [Architecture](#arch)

<a href="#summary"></a>
## Summary

This is a case study project of a RESTful service broken into three separate microservices using the Virt.x framework. The three services are a front end controller and two backend resource managers. The backend resource manager handle asynchronous calls to remote APIs and a cloud database (Google Cloud Firestore).

<a href="#arch"></a>
## Architecture

Below is a high-level overview of the architecture.
[placeholder]()

In Vert.x, `Verticle`s are used to isolate responsibility. A `Launcher` class configures the application runtime environment and deploys the three verticles. The `ServiceVerticle` is deployed a standard verticle while `ProductVerticle` and `PriceVerticle` are deployed as workers. Worker verticles are background processes with separate thread isolation and resource limits. A single instance of each verticle is spawned.

The `ServiceVertice` is the entrypoint for the entire service. It interacts with the two worker verticles across the Vert.x event bus. The event bus provides an efficent method of interprocess communication.



 