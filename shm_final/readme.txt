#######################################################################################################################
#	Author : Piyush Nath | Archi Dsouza																				  #	
#   Readme : Shared Memory																						 	  #	
#	Scope  : Algorithm and Execution Instruction																	  #	
#																													  #	
#																													  #
#																													  #
#																													  #	
#																													  #
#																													  #
#																													  #		
#																													  #			
#######################################################################################################################

Shared Memory : 

Algoirthm 
	1) Initialize the thread pool of based on size passed. 
	2) Decides the byte processed by each thraed.
	3) Intiliaize the SortFile object with all data. 
	4) Add job to thread pool to execute. 
	5) SortFile worker thread will decides what job he needs to do SORT+MERGE or  MERGE
	6) IF SORT+MERGE job it first calculate total pages it needs to process based on PAZE_SIZE
	7) Using loop it reads data of PAGE_SIZE, sort it and creates the new file and writes data to new file and save the file name in 
	   filelist. 
	8) Once it sort the all the pages and it pass the filelist to the merge function.
	9) Merge merge sorted filelist in K-way merge 
	10) Once all thread complete the SORT+K_MERGE it deletes the old file and notify the main thread. 
	11) Main Thraed then look into sort dir and create the list of files
	12) And calls the SortFile merge function using worker thread.
	13) It merger the data same way as earlier. 

Sorted file created in SORTJOB directory. It will follow below convention SORT_<THREAD_COUNT>MERGE 
Some important function details 



void initSort(RandomAccessFile readInputFile, long totalPageInFile, List<String> fileList)    
	/***
	 * @author archi
	 * @param readInputFile
	 * @param totalPageInFile
	 * @param fileList
	 * This method initiate the sort. It read the page from file and sort using priority queue and 
	 * empty the priority queue and writes back to new file and insert file name into fileList
	 */

	

public byte[] sort(byte[] b)
	/***
	 * 
	 * @param b
	 * @return sorted byte
	 * @throws IOException
	 * 
	 * Convert the byte into lines. 
	 * Sort lines using priority queue and empty the queue and convert to byte array
	 */


void mergeAll(List<String> fileList)
	/***
	 * @author archi
	 * @param fileList
	 * @throws IOException
	 * 
	 * This function merge all the sorted file using the K-way merge. 
	 * It first creates the hashmap of file pointers. of the all file in fileList
	 * It also make priority queue of FileNode consists of line and filepointer.
	 * First it insert the all the file node consists of first line and filepointer in filelist
	 * 
	 * Then while priority queue empty it removes the head and write to new file. 
	 * it then checks if file has reached to end else it read next line store it in node line object and 
	 * insert the node again into priority queue.
	 * 
	 * The message "File Reached end" means one the file reached to end in k-way merge
	 */

	
Map<String, BufferedReader> createMapOfFilePointer(List<String> fileList)
	/***
	 * @author archi
	 * @param fileList
	 * @return filePointerMap
	 * 
	 * This method creates the hashmap of file pointer given by the fileList
	 * 
	 */


List<String> getLines(byte[] b) 
	/***
	 * 
	 * @param b
	 * @return lines
	 * @throws IOException
	 * 
	 * This method convert byte array to list of lines 
	 * 
	 */


byte[] getByte(List<String> lines)
	/***
	 * 
	 * @param lines
	 * @return
	 * @throws IOException
	 * 
	 * Convert the lines into byte 
	 */


PriorityQueue<FileNode> getPriorityQueue() 
	/****
	 * @author archi
	 * @return priorityQueue
	 * Creates the priority min queue.  
	 */


/**********************************Instruction to run shared memory sort ***************************************************/

$sudo apt-get install default-jdk
$mkdir shm
$export CLASSPATH = $CLASSPATH:$HOME/shm
$jar cvfe kwayExternalSort.jar TaskPool *.class
Copy the gensort,and valsort, shm_run.sh to one dir Run below command 
It will generate data using gensort and sort and validate using valsort

$./shm_run.sh

