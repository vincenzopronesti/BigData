#!/bin/bash
cd /data
hdfs dfs -put partitioning/dates.txt hdfs:///dates
hadoop jar mapreduce-design-patterns-1.0.jar designpattern.partitioning.PartitionDataByYear hdfs:///dates hdfs:///outpartitioning
