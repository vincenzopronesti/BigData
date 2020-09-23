#!/bin/bash
cd /data
hdfs dfs -put input.txt hdfs:///input
hdfs dfs -put blacklist.txt hdfs:///blacklist
hadoop jar mapreduce-design-patterns-1.0.jar SophisticatedWordCount hdfs:///input hdfs:///output2 -skip hdfs:///blacklist
