# A simple Apache Hadoop 3.1.2 Docker image

This is a very simple and not optimized Docker image containing Apache Hadoop. 

The main purpose of this container is to help who wants to start using Apache Hadoop and does not want to spend too much time for the initial installation and configuration process. 
As you will notice, the container is not optimized. However, we provide the Dockerfile and strongly encourage whoever wants to improve this Docker image.


# Pull the image

This image is also released as an official Docker image from Docker's automated build repository - you can always pull or refer the image when launching containers.

```
docker pull effeerre/hadoop
```

# Start a container

In order to use the Docker image you have just build or pulled use:

```
docker run -t -i -p 9870:9870 --network=hadoop_network --name=master effeerre/hadoop
```

## Create an isolated network with several datanodes

```
docker network create --driver bridge hadoop_network

  docker run -t -i -p 9864:9864 -d --network=hadoop_network --name=slave1 effeerre/hadoop
  docker run -t -i -p 9863:9864 -d --network=hadoop_network --name=slave2 effeerre/hadoop
  docker run -t -i -p 9862:9864 -d --network=hadoop_network --name=slave3 effeerre/hadoop
  docker run -t -i -p 9870:9870 --network=hadoop_network --name=master effeerre/hadoop

```

