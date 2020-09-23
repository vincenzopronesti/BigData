#!/bin/bash
cd /data
hdfs dfs -put input.txt hdfs:///input
hadoop jar mapreduce-design-patterns-1.0.jar designpattern.summarizations.AverageWordLengthByInitialLetter hdfs:///input hdfs:///outawl
