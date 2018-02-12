
//DOWNLOAD LATEST FILES FOR
wget http://apache.claz.org/spark/spark-2.2.0/spark-2.2.0-bin-hadoop2.7.tgz
tar -xzvf spark-2.2.0-bin-hadoop2.7.tgz
cp spark-2.2.0-bin-hadoop2.7
mv spark-2.2.0-bin-hadoop2.7 /usr/local/spark 


CODE TO EXECUTE THE SORTING ALGO
 spark-shell -i sortSpark.scala

To monitor the Job we can use 

http://ec2-18-217-198-64.us-east-2.compute.amazonaws.com:4040/stages/

Post it runs sucessfully, Spark produces differnt chunks of sorted data

we can call validator to varifie the sort of the output.

hdfs dfs -getmerge /output /data/final_output
