#############################################
# Script Name: sprintscript                 #
# Archi Dsouza                              #
# Piyush Nath                               #
#                                           #
#                                           #
#############################################



echo "******************Welcome To Hadoop Sort \n *****************************"

#UPDATE AND INSTALL REQUIRED SOFTWARE
sudo apt-get update && sudo apt-get dist-upgrade
sudo apt-get install python-software-properties
sudo add-apt-repository ppa:webupd8team/java

sudo apt-get install oracle-java8-installer
wget http://apache.claz.org/spark/spark-2.2.0/spark-2.2.0-bin-hadoop2.7.tgz

tar xvf spark-2.2.0-bin-hadoop2.7.tgz
source .bashrc

echo "mount Data"
 sudo mkdir -p dataMount
 sudo chown -R ubuntu:ubuntu dataMount
 sudo mkfs -t ext4 /dev/nvme0n1
 sudo mount /dev/nvme0n1 dataMount 
 sudo chmod 777 dataMount


echo "Configuration changes ,replace from content of folder. Please make sure the public DNS is modified accordingly"

#    core-site.xml
#    hadoop-env.sh
#    yarn-site.xml
#    hdfs-site.xml
#    mapred-site.xml
#    spark-env.sh

ssh-keygen -t rsa -P ''
cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys
cd ~/hadoop/sbin/
./start-dfs.sh
./start-yarn.sh
jps
start-all.sh

#HADOOP SHOULD BE UP AND RUNNING and move to sorce file
hadoop com.sun.tools.javac.Main *.java 
jar cf TeraSort.jar *.class

echo "Create file to be uploaded"
wget http://www.ordinal.com/try.cgi/gensort-linux-1.5.tar.gz
tar xvf gensort-linux-1.5.tar.gz
64/teragen -a <size based on data> dataMount/input.txt
hdfs dfs -mkdir /input
hdfs dfs -copyFromLocal dataMount/input.txt /input

hdfs dfs -rm -r /output*



#EXECUTION OF MAIN TERASORT CODE
spark-shell -i sortSpark.scala 

./valsort /code/part-r-00000

#########################################################################################3
echo "For Multi node Things which are diff"

#Copy key to all
scp /home/ubuntu/.ssh/pk_First.pem dnode6:/home/ubuntu/.ssh/pk_First.pem 
#adding core site to all
scp core-site.xml dnode1:~/hadoop/etc/hadoop/core-site.xml
scp core-site.xml dnode2:~/hadoop/etc/hadoop/core-site.xml
scp core-site.xml dnode3:~/hadoop/etc/hadoop/core-site.xml
scp core-site.xml dnode4:~/hadoop/etc/hadoop/core-site.xml
scp core-site.xml dnode5:~/hadoop/etc/hadoop/core-site.xml
scp core-site.xml dnode6:~/hadoop/etc/hadoop/core-site.xml
scp core-site.xml dnode7:~/hadoop/etc/hadoop/core-site.xml
#adding env to all
scp hadoop-env.sh  dnode1:~/hadoop/etc/hadoop/hadoop-env.sh
scp hadoop-env.sh  dnode2:~/hadoop/etc/hadoop/hadoop-env.sh
scp hadoop-env.sh  dnode3:~/hadoop/etc/hadoop/hadoop-env.sh
scp hadoop-env.sh  dnode4:~/hadoop/etc/hadoop/hadoop-env.sh
scp hadoop-env.sh  dnode5:~/hadoop/etc/hadoop/hadoop-env.sh
scp hadoop-env.sh  dnode6:~/hadoop/etc/hadoop/hadoop-env.sh
scp hadoop-env.sh  dnode7:~/hadoop/etc/hadoop/hadoop-env.sh

scp .bashrc dnode2:~/.bashrc
scp .bashrc dnode3:~/.bashrc
scp .bashrc dnode4:~/.bashrc
scp .bashrc dnode5:~/.bashrc
scp .bashrc dnode6:~/.bashrc
scp .bashrc dnode7:~/.bashrc
scp .bashrc dnode1:~/.bashrc
scp .bashrc dnode1:~/.bashrc
#making same dfs from data node

./start-dfs.sh
 ./start-yarn.sh
 ./mr-jobhistory-daemon.sh start historyserver

