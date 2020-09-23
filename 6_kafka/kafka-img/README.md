# A simple Apache Kakfa 1.1.0 Docker image

This is a very simple Docker image containing Apache Kafka. 

The main purpose of this container is to help who wants to start using Apache Kafka and does not want to spend too much time for the initial installation and configuration process. 
We provide the Dockerfile and strongly encourage whoever wants to improve this Docker image.


# Build the image

You can build your own image, using the Dockerfile. Just run the following command: 

```
docker build  -t yourkafka:1.1.0 .
```
# Pull the image

This image is also released as an official Docker image from Docker's automated build repository - you can always pull or refer the image when launching containers.

```
docker pull effeerre/hadoop
```

# An Apache Kafka cluster using containers

This Docker image requires:
- An (containerized) instance of ZooKeeper that can be reached using the hostname "zookeeper"
- To be executed by setting the environment variable BROKER_ID at startup.

To run the image:
```
docker network create kafkanet
docker run --name zookeeper --network kafkanet --rm -d zookeeper
docker run -e BROKER_ID=0 --network kafkanet -d --rm -p 9092:9092 --name kafka effeerre/kafka
```

To terminate the containerized environment:
```
docker kill zookeeper
docker kill kafka
docker network rm kafkanet
```

## Launch containers using Docker Compose
Alternatively, you can rely on Docker Compose to more easily launch your Kafka cluster. 
For this purpose, we provide an simple docker-compose.yml file within the repository.

To launch the composition, you can run the following command: 
```
docker-compose up -f docker-compose.yml -d 
```


To stop the composiion, you can run the following command: 
```
docker-compose down -f docker-compose.yml
```
