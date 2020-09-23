#!/bin/bash
cd /data
hdfs dfs -put input.txt hdfs:///input
hadoop jar mapreduce-design-patterns-1.0.jar designpattern.filtering.DistinctWords hdfs:///input hdfs:///outdistword
