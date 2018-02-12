import java.io.File;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import mpi.*;

public class TaskPool {
	static  int PAZE_SIZE =   50737410; //2147483600;    //1073741800
	static long PAGE_COUNT = 0;
	//static String INNPUT_FILE_NAME = "/home/archi/UbuntuComputer/Study/Sem-III/CS-553/Assignment/Assign2/pennyinput";
	static String INNPUT_FILE_DIR ="/home/archi/UbuntuComputer/newDevF/sortInput/mpiInput/";
	static String INNPUT_FILE_NAME;
	static String PS_JOB_DIR_NAME="";
	static String DEST_FILE_DIR = "/home/archi/UbuntuComputer/newDevF/sortJob/PROCESS_SORT_JOB_";
	static int THARED_SIZE=2;
	static final String MODE_RAED="r";
	static final String MODE_READ_WRITE="rw";
	static long SORT_COUNT = 0;
	static final String SORT_JOB="SORT_JOB";
	static final String MERGE_JOB="MERGE_JOB";
	static int MY_PS_RANK=-1;
	/***
	 * @author archi
	 * @param args <input file> <out file dir> <thread count> <page size>  
	 * @throws IOException
	 * 
	 * This is starting point of Shared memory 
	 * File divided into no of threads and processed accordingly 
	 * Each thread Latter devies the thread into page size and processs the blocks 
	 * and merger using  k way merger. 
	 * Once all thread completes the Processing main thread perform k-way merge on data
	 */
	public static void main(String args[]) throws IOException,  MPIException{
		MPI.Init(args);
	String tempDir = DEST_FILE_DIR;	

		/*
		this will override the actual value. Above values for local test
		**/
    	INNPUT_FILE_NAME = args[0];
		DEST_FILE_DIR = args[1];
		THARED_SIZE = Integer.parseInt(args[2]);
		PAZE_SIZE 	= 	Integer.parseInt(args[3]);



		int myrank = MPI.COMM_WORLD.getRank();
		int size = MPI.COMM_WORLD.getSize() ;

		MY_PS_RANK = myrank;

		List<String> inpFileList = new TaskPool().getListFileInDir(INNPUT_FILE_DIR);
		INNPUT_FILE_NAME = inpFileList.get(myrank);
		System.out.println("My input File From List "+INNPUT_FILE_NAME);
		System.out.println("My input Direcory From List "+INNPUT_FILE_DIR);

		DEST_FILE_DIR = DEST_FILE_DIR+myrank;

		System.out.println("My input File "+INNPUT_FILE_NAME);
		System.out.println("Sort Job File "+DEST_FILE_DIR);

		System.out.println("Hello world from rank " + myrank + " of " + size);
		long startTime = System.currentTimeMillis();
		
		TaskPool taskPool = new TaskPool();
		RandomAccessFile inputFile = taskPool.getFile(INNPUT_FILE_NAME, MODE_RAED);
		RandomAccessFile sortFile;
		SortFile sortTask;
		List<String> sortFileList = new ArrayList<>();
		CountDownLatch doneSignal = new CountDownLatch(THARED_SIZE);
		
		ThreadPoolExecutor sortExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THARED_SIZE);
		ThreadPoolExecutor mergeExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		
		long fileSize = inputFile.length();
		long fileSizeByThread  =  (long) Math.ceil((double)fileSize /THARED_SIZE );
		
		System.out.println("\n Fi size by thread : "+fileSizeByThread);
		
		//PAGE_COUNT = (long) Math.ceil((double)fileSize / (double)PAZE_SIZE);
		
		System.out.println("Staring " ); 
		
		for(long i=0; i<THARED_SIZE; i++){
			String sortFileName = DEST_FILE_DIR+"/SORT_"+SORT_COUNT;
			sortFileList.add(sortFileName);
			sortTask = new SortFile(PAZE_SIZE, fileSizeByThread, SORT_COUNT, INNPUT_FILE_NAME, DEST_FILE_DIR,SORT_JOB,doneSignal);
			
			sortExecutor.execute(sortTask);
			System.out.println("Task submited for sorting Sort Count --> "+SORT_COUNT);
			SORT_COUNT++;
		}
		
		
		sortExecutor.shutdown();
		System.out.println("**********************");
		
		try{
			doneSignal.await();
		}catch(Exception e){
			System.out.println(" EXT_CDE 5");
			e.printStackTrace();
		}
		
		doneSignal = new CountDownLatch(1);

		mergeExecutor.execute(new SortFile(MERGE_JOB, new TaskPool().getListFileInDir(DEST_FILE_DIR),PAZE_SIZE,DEST_FILE_DIR,SORT_COUNT,doneSignal));
	
		
		long endTime   = System.currentTimeMillis();
		long totalTime = (endTime - startTime)/1000;
		System.out.println("Total Time taken in Seconds-----------> "+totalTime);
		System.out.println("***************Completed********************");
		int message[] = new int [1];;
		int count = 0;
		if(MY_PS_RANK==0){
			while(count < size)
				MPI.COMM_WORLD.recv(message, 1, MPI.INT, 1, ANY_SOURCE );
				count++;
			}
			System.out.println("Data received from Rank 1 "+message[0]);

			List<String> finalList = new ArrayList<>();

			System.out.println("File added "+DEST_FILE_DIR+"/SORT_"+THARED_SIZE+"MERGE");
			System.out.println("File added "+tempDir+"1"+"/SORT_"+THARED_SIZE+"MERGE");

			finalList.add(DEST_FILE_DIR+"//SORT_"+THARED_SIZE+"MERGE");
			finalList.add(tempDir+"1"+"//SORT_"+THARED_SIZE+"MERGE");
			doneSignal = new CountDownLatch(1);
			SORT_COUNT++;
			mergeExecutor.execute(new SortFile(MERGE_JOB, finalList,PAZE_SIZE,DEST_FILE_DIR,SORT_COUNT+1,doneSignal));
		}else{
		/*	MPI_Send(
		    void* data,
		    int count,
		    MPI_Datatype datatype,
		    int destination,
		    int tag,
		    MPI_Comm communicator)   */
		     MPI.COMM_WORLD.send(message,1, MPI.INT, 0, 1);


		}
			mergeExecutor.shutdown();
		MPI.Finalize();
	}
	
	/****
	 * @author archi
	 * @param fileName
	 * @param mode read/write - read-write
	 * @return fileoOject
	 * 
	 * This function return the object of Random access file 
	 */
	
	RandomAccessFile getFile(String fileName, String mode) {
		RandomAccessFile file = null;

		try {
			file = new RandomAccessFile(fileName, mode);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return file;
	}
	
	/***
	 * @author archi
	 * @param dirPath
	 * @return List of files to be merged by main
	 * 
	 * looks into destination directory for the File list to be merged. 
	 */
	
	List<String> getListFileInDir(String dirPath){
		List<String> fileList = new ArrayList<>();
		
		for(File file : new File(dirPath).listFiles()){
			if(file.isFile() &&  (file.getName().contains("PROCESS"))){
				fileList.add(dirPath+"/"+file.getName());
				System.out.println("File Added "+file.getName());
			}
		}
		
		System.out.println("File Size "+fileList.size());
		
		return fileList;
	}
	
	
}
