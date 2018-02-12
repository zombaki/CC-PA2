run hadoop Script with following instruction to scuessfully complete the experimenet.

There are few things which was not possible to be done by the script. Those include:-

Crettion of EC2 instance

Modification of Public DNS Adding those values as per the instance. as they are allocated dynamically.

When doing multiple node server we have to take care for stablish the hadoop network, with one as master and other as slave.

For this we have modified conffi file of .ssh folder . So that it would be easier for us to connect to all the nodes at ease.

Configuration:


hdfs-site:
Setting block size higher for 1TB data for faster sorting
<property> 
    <name>dfs.block.size<name> 
    <value>134217728<value> 
    <description>Block size<description> 
<property>

In the same we have the configuration to set the path for datanode and name node..


mapred-site:

helps with the tracking, we can see the details oh the running job with this help.

Core-site:

here we set tmp file path and also the master node path.


once we have the configuration done as per our requirement , we should be able to see

14176 NameNode
14852 NodeManager
14712 ResourceManager
14346 DataNode
14541 SecondaryNameNode
15182 JobHistoryServer
23230 Jps


With that we will use URL : http://ec2-18-216-188-31.us-east-2.compute.amazonaws.com:9006/cluster to see Job progress with details.

Reference for configuration and code : http://www.novixys.com/blog/setup-apache-hadoop-cluster-aws-ec2/ 

