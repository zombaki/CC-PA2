/***************************
/*ref :  https://www.tutorialspoint.com/apache_spark/apache_spark_quick_guide.htm
/*
*****************************/
/*FETCH FILE FROM DFS */
val file_inuput=sc.textFile("hdfs://ec2-13-58-159-178.us-east-2.compute.amazonaws.com/input/input.txt")
/** MAP MOD */
val file_to_sort=file_input.map(line => (line.take(10), line.drop(1)))
/** SORT DATA */
val sort = file_to_sort.sortByKey()
/** Map key and value  */
val lines=sort.map {case (key,value) => s"$key $value"}
/** Saving file  */
lines.saveAsTextFile("/output")
prnitln("Data Sorted")

