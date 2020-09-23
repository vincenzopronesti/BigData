#!/bin/bash
cd /data
hdfs dfs -put hierarchical/categories.txt hdfs:///categories
hdfs dfs -put hierarchical/items.txt hdfs:///items
hadoop jar mapreduce-design-patterns-1.0.jar designpattern.hierarchical.TopicItemsHierarchy hdfs:///categories hdfs:///items hdfs:///outhierarchical
