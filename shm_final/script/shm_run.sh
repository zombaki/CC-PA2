#############################################
# Script Name: run.sh						#
# Archi Dsouza								#
# Piyush Nath								#
# 											#
#											#
#############################################


TEMPDIR=$HOME/tempDir
INPUTFILE=/home/archi/UbuntuComputer/newDevF/sortInput/sort_inp
OUTFILEDIR=$HOME/sortOutDir
THREADCOUNT=4
PAGESIZE=50737410
NOOFRECOREDS=1374389534
JARFILE=kwayExternalSort.jar


mkdir $OUTFILEDIR 

mkdir $TEMPDIR

echo "******************Welcome To External Sort \n *****************************"

echo "This file will generate the 128 GB Data in $INPUTFILE file."

echo " Generating data will take while to generate data "

./gensort -a 1374389534 $INPUTFILE

echo " Data Generation Complete Complete . \n Will Start Sort Now. This will take very long time based on Data size"

java -Xms8G -Xmx10G -Xss1G -Djava.io.tmpdir=$TEMPDIR -jar $JARFILE  $INPUTFILE $OUTFILEDIR $THREADCOUNT $PAGESIZE

echo " External Sort Complete"

echo " Validating Sort using valsort"

./valsort ${OUTFILEDIR}/SORT_${THREADCOUNT}MERGE



