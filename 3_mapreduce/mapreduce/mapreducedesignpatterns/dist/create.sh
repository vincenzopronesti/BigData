# Hadoop 3 default ports:
# Namenode Ports:
#  - namenode: 9820
#  - namenode.http.ui: 9870
#  - namenode.https.ui: 9871
# Secondary Namenode Ports:
#  - namenode: 9869
#  - namenode.http.ui: 9868
# Datanote Ports:
#  - datanode: 9866
#  - datanode.http.ui: 9864
#  - datanode.https.ui: 9865
#  - datanode.ipc: 9867
docker network create --driver bridge hadoop_network

docker run -t -i -p 9864:9864 -d --network=hadoop_network --name=slave1 effeerre/hadoop
docker run -t -i -p 9863:9864 -d --network=hadoop_network --name=slave2 effeerre/hadoop
docker run -t -i -p 9862:9864 -d --network=hadoop_network --name=slave3 effeerre/hadoop
docker run -t -i -p 9870:9870 -p 8088:8088 --network=hadoop_network --name=master -v $PWD/hddata:/data effeerre/hadoop
