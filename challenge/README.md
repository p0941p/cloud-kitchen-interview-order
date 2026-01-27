# README

Author: `Wen-Han(George) Pang

## How to run

The `Dockerfile` defines a self-contained Java/Gradle reference environment.
Build and run the program using [Docker](https://docs.docker.com/get-started/get-docker/):
```
$ docker build -t challenge .
$ docker run --rm -it challenge --auth=uy4bbtwjtpmy
```

If java `21` or later is installed locally, run the program directly for convenience:
```
$ ./gradlew run --args="--auth=uy4bbtwjtpmy"
```

## Discard criteria

The order which will be discard first is the one with a nearest expiaration time. When multiple orders expires or will expire at the same time, the one with a lesser price is discarded first. 
