mkdir -p /home/ubuntu/mpi
mkdir -p /home/ubuntu/mpi/sortJob
mkdir -p /home/ubuntu/mpi/tempDir

./gensort -a 1000000 /home/ubuntu/mpi/sort_inp

let 'PROCESS_SIZE = 2'

let FILE_SIZE=$(wc -c < /home/mpi/sort_inp)

echo "file size $FILE_SIZE"


let 'BLOCK_SIZE = (FILE_SIZE / PROCESS_SIZE)'

echo "Block size : $BLOCK_SIZE"

split -b $BLOCK_SIZE -p /home/ubuntu/mpi/sort_inp /home/ubuntu/mpi/PROCESS_

for i in `seq 0 $PROCESS_SIZE`
do
    echo "Making dir $i process directory"
    mkdir -p "/home/mpi/sortJob/PROCESS_SORT_JOB_${i}"
done

export CLASSPATH=$CLASSPATH:$HOME/mpi/

mpijava *.java

mpirun -np java -Xms8G -Xmx10G -Xss1G -Djava.io.tmpdir=/home/ubuntu/mpi/tempDir TaskPool /home/ubuntu/mpi/ /home/ubuntu/mpi/sortJob/PROCESS_SORT_JOB_ 2 50737410
