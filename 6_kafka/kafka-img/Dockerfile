# Create a simple Kafka Image 
#
# docker build -t effeerre/kafka .

FROM anapsix/alpine-java:8
LABEL maintainer="effeerre"

USER root

# COPY kafka_2.11-2.2.0.tgz /kafka_2.11-2.2.0.tgz
# RUN cd / && tar -zxf kafka_2.11-2.2.0.tgz && mv kafka_2.11-2.2.0 kafka && rm kafka_2.11-2.2.0.tgz
RUN cd / && wget http://apache.panu.it/kafka/2.2.0/kafka_2.11-2.2.0.tgz && tar -zxf kafka_2.11-2.2.0.tgz && mv kafka_2.11-1.1.0 kafka && rm kafka_2.11-2.2.0.tgz

COPY config/server.properties /kafka/config/server.properties
COPY config/update-config.sh /update-config.sh
# RUN sh /update-config.sh

EXPOSE 9092

ENTRYPOINT ["/bin/bash", "-c"]
CMD ["sh /update-config.sh && /kafka/bin/kafka-server-start.sh /kafka/config/ds.properties"]
# CMD ["/bin/bash"]
