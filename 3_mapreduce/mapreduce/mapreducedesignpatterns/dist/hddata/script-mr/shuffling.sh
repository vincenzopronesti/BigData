#!/bin/bash
cd /data
hdfs dfs -put sorted_dates.txt hdfs:///sorteddates
hadoop jar mapreduce-design-patterns-1.0.jar designpattern.ordering.Shuffling hdfs:///sorteddates hdfs:///outshuffling
