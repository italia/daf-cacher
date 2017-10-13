# daf-metabase-cacher

A microservice to cache Metabase plots

This microservice is used by [daf-dataportal](https://github.com/italia/daf-dataportal),
iterates over the various plots, takes a screenshot, and caches them.

## Build status

[![Build Status](https://travis-ci.org/taganaka/daf-metabase-cacher.svg?branch=master)](https://travis-ci.org/taganaka/daf-metabase-cacher)


## Features

* Easy to scale thanks to producer/consumer architecture backed by redis 
* It generates for each plot a customizable set of different thumbs size
* Embedding cached image in the page is easy as
 ```html
<img src="//proxy/plot/:plot_public_id/:geometry">
```
Es:
```html
<img src="/plot/cb638004-661c-4c11-802c-1fc1e2312577/356x280">
```

* New public plots are discovered and cached automatically

## Components

* API

It is used to serve cached images to the client

* Seeder

A recurring task would go through each available public plot by pinging Metabase's API and it will enqueue a new caching job

 
* Worker

Responsible to perform screenshot and thumbs, consuming jobs enqueued by the Seeder

## How to run locally with docker compose:

* Make sure to have a valid https://graph.daf.teamdigitale.it account
* Obtain your metabase token with the following command

```
$ curl -XPOST -H "Content-Type: application/json" -d  '{"username": "user@domain.com", "password": "xxxxx"}'   https://graph.daf.teamdigitale.it/api/session
```

* Make sure to have a jdk8 installed
* Make sure to have a modern maven version installed
* Copy ```config.properties.example``` to ```config-docker.properties``` and edit it accordingly
* Compile it with:

```
$ mvn clean dependency:copy-dependencies  package -Dmaven.test.skip=true
```

* Build the container with:
```
$ docker build -t italia/daf-metabase-cacher .
```

Run with:
```
$ docker-compose up
```

Once the service is up, a POC will be available at http://localhost:4567/

Live demo available at https://daf-cache.taganaka.com/


