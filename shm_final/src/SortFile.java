import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class FileNode {
	String line;
	//RandomAccessFile file;
	BufferedReader br;
}

public class SortFile implements Runnable {

	int pageSize;
	long byteCountProcess;
	long sortCount;
	String inputFileName;
	String outputFileDir;
	String jobTpe;
	CountDownLatch doneSignal;
	List<String> fileList;

	static final String MODE_RAED = "r";
	static final String MODE_READ_WRITE = "rw";
	static final String SORT_JOB = "SORT_JOB";
	static final String MERGE_JOB = "MERGE_JOB";
	static final String NEWLINE = "\n";
	static long MAX_RESULT_LIST = 10737;

	final static byte DEFAULT = 0;
	final static byte LEFT_RIGHT_PAGE_EXST = 1;
	final static byte LEFT_PAGE_EXST = 2;
	final static byte RIGHT_PAGE_EXST = 3;
	final static byte NO_MORE_LEFT_RIGHT_PAGE_EXST = 4;
	final static byte NO_MORE_LEFT_PAGE_EXST = 5;
	final static byte NO_MORE_RIGHT_PAGE_EXST = 6;

	public SortFile(int pageSize, long fileSize, long sortCount, String inputFileName, String outputFileDir,
			String jobType, CountDownLatch doneSignal) {
		this.pageSize = pageSize;
		this.byteCountProcess = fileSize;
		this.sortCount = sortCount;
		this.inputFileName = inputFileName;
		this.outputFileDir = outputFileDir;
		this.jobTpe = jobType;
		this.doneSignal = doneSignal;
	}

	public SortFile(String jobTpe, List<String> fileList, int pageSize, String outputFileDir, long sortCount) {
		this.jobTpe = jobTpe;
		this.fileList = fileList;
		this.pageSize = pageSize;
		this.sortCount = sortCount;
		this.outputFileDir = outputFileDir;
	}
	/***
	 * @author archi
	 * @param non
	 * @return no return 
	 * This method receives the job from main file. 
	 * Check what job it has received and calls Sort or Merge function
	 * This  Method is entry point for Thread. Each thread start reading file from its 
	 * specified position sortCount * byteCountProcess.  It reads the file sort it and write it into another file
	 * file. It store the newly created file in FileList and latter pass it to merge all function for k-way merge.
	 * 
	 */
	@Override
	public void run() {
		System.out.println("===============>Starting Sort Count :: " + this.sortCount + "<===================");

		RandomAccessFile readInputFile;
		List<String> fileList;
		long totalPageInFile;
		if (this.jobTpe == SORT_JOB) {
			try {
				readInputFile = getFile(this.inputFileName, MODE_RAED);

				readInputFile.seek(this.sortCount * byteCountProcess);

				totalPageInFile = (long) Math.ceil((double) this.byteCountProcess / (this.pageSize));

				traceMessage("Total Pages in file " + totalPageInFile);

				fileList = new ArrayList<>();

				initSort(readInputFile, totalPageInFile, fileList);

				traceMessage("*****Sort Completed*******");

				traceMessage("*****Initiating merge*******");

				//fileList = mergeFile(fileList);
				mergeAll(fileList);
				traceMessage("*****Deleting temp file*******");
				
				for(String file:fileList){
					new File(file).delete();
				}
				
				traceMessage("*****merge completed*******");
				ReadWriteLock lock = new ReentrantReadWriteLock();

				lock.writeLock().lock();
				try {
					doneSignal.countDown();

					traceMessage(" Current count | " + this.doneSignal.getCount());
				} finally {
					lock.writeLock().unlock();
				}

			} catch (Exception e) {
				traceMessage("EXT_CDE 1");
				e.printStackTrace();
			}
		} else if (jobTpe == MERGE_JOB) {
			try {
				mergeAll(this.fileList);
			} catch (IOException e) {
				traceMessage("EXT_CDE :: 3");
				e.printStackTrace();
			}
		}
	}
	/***
	 * @author archi
	 * @param readInputFile
	 * @param totalPageInFile
	 * @param fileList
	 * This method initiate the sort. It read the page from file and sort using priority queue and 
	 * empty the priority queue and writes back to new file and insert file name into fileList
	 */
	void initSort(RandomAccessFile readInputFile, long totalPageInFile, List<String> fileList) {
		long fCounter = 0;
		byte[] read;
		read = new byte[this.pageSize];
		RandomAccessFile sortFile;
		try {
			while (fCounter < totalPageInFile) {
				String sortFileName = this.outputFileDir + "SORT_" + this.sortCount + "_PAGE_" + fCounter;
				fileList.add(sortFileName);
				sortFile = getFile(sortFileName, MODE_READ_WRITE);
				readInputFile.read(read, 0, this.pageSize);
				sort(read);
				sortFile.write(read, 0, this.pageSize);
				sortFile.close();
				fCounter++;
				traceMessage("Creating file " + fCounter + " complete");
			}
		} catch (Exception e) {
			traceMessage("EXT CDE 2");
			e.printStackTrace();

		}

	}
	/***
	 * 
	 * @param fileName
	 * @param mode
	 * @return RandomAccessFile
	 * 
	 * returns onject of file based on mode specified
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
	 * 
	 * @param b
	 * @return sorted byte
	 * @throws IOException
	 * 
	 * convert the byte into lines. 
	 * Sort lines using priority queue and empty the queue and convert to byte array
	 */

	public byte[] sort(byte[] b) throws IOException {
		InputStream is;
		BufferedReader bfReader;
		is = new ByteArrayInputStream(b);
		bfReader = new BufferedReader(new InputStreamReader(is));

		List<String> lines;

		lines = new ArrayList<>();
		String line;
		while ((line = bfReader.readLine()) != null) {
			lines.add(line);
		}

		Collections.sort(lines, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
		});

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		for (String element : lines) {
			out.writeUTF(element + "\n");
			// out.writeChar('\n');
		}
		b = baos.toByteArray();
		return b;
	}
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
	void mergeAll(List<String> fileList) throws IOException {
		traceMessage("Sarting K Way Merge . File Size "+fileList.size());
		Map<String, BufferedReader> filePointerMap = createMapOfFilePointer(fileList);
		PriorityQueue<FileNode> priorityQueue = getPriorityQueue();
		String mergeFileName = this.outputFileDir + "/SORT_" + this.sortCount + "MERGE";
		BufferedWriter writer = getFileWriter(mergeFileName, "rw");

		Iterator<String> fileIterator = fileList.iterator();

		FileNode node;
		while (fileIterator.hasNext()) {
			String fileName = fileIterator.next();
			BufferedReader file = filePointerMap.get(fileName);
			
				node = new FileNode();
				node.line = file.readLine();
				node.br = file;
				priorityQueue.add(node);
			
		}
		BufferedReader file ;
		while (!priorityQueue.isEmpty()) {
			node = priorityQueue.poll();
			writer.write(node.line + NEWLINE);
			String nextLine = node.br.readLine();
			if (nextLine!=null) {
				node.line = nextLine;
				priorityQueue.add(node);
			}else{
				
				traceMessage("File Reached end "+node.br);
				node.br.close();
			}
		}
		writer.flush();
		writer.close();
		traceMessage("Completed K Way Merge  ");
	}

	void insertInPriorityQueue(List<String> fileList) {

	}

	/***
	 * @author archi
	 * @param fileList
	 * @return filePointerMap
	 * 
	 * This method creates the hashmap of file pointer given by the fileList
	 * 
	 */
	Map<String, BufferedReader> createMapOfFilePointer(List<String> fileList) {
		Map<String, BufferedReader> filePointerMap = new HashMap<>();
		Iterator<String> fileIterator = fileList.iterator();

		while (fileIterator.hasNext()) {
			String file = fileIterator.next();
			//filePointerMap.put(file, getFile(file, MODE_RAED));
			filePointerMap.put(file, getFileReader(file));
		}

		return filePointerMap;
	}
	/****
	 * @deprecated
	 * @param fileList
	 * @return
	 * @throws IOException
	 */
	List<String> mergeFile(List<String> fileList) throws IOException {
		List<String> oldMergeFList;
		List<String> mergeFileList;
		long level = 1;
		int maxByte = this.pageSize;

		byte[] right_b = new byte[maxByte];
		byte[] left_b = new byte[maxByte];
		List<String> resultList = new ArrayList<>();
		;
		oldMergeFList = fileList;

		int fileCount = 0;
		long mergeCount = 0;
		byte STATUS = DEFAULT;
		System.out.println("\n Total list Start" + oldMergeFList.size());
		do {
			traceMessage("<======================Start merge Process at level" + level + " =========================>");
			mergeFileList = new ArrayList<>();

			while (oldMergeFList.size() >= 2) {
				traceMessage("\n after " + mergeCount + " Total list " + oldMergeFList.size());

				String mergeFileName = this.outputFileDir + "SORT_" + this.sortCount + "_MERGE_" + level + ""
						+ (mergeCount++);

				RandomAccessFile leftFile = getFile(oldMergeFList.get(0), "r");
				RandomAccessFile rightFile = getFile(oldMergeFList.get(1), "r");
				BufferedWriter writer = getFileWriter(mergeFileName, "rw");

				long maxLP = leftFile.length() / (this.pageSize);
				long maxRP = rightFile.length() / (this.pageSize);

				traceMessage("Total left pages " + maxLP);
				traceMessage("Total right pages " + maxRP);
				long lpc = 0;
				long rpc = 0;

				List<String> leftLines;
				List<String> rightLines;

				traceMessage(" Read Left page |" + lpc + "| Pages Remaining " + (maxLP - lpc - 1));
				traceMessage(" Read Right page " + rpc + " Pages Remaining " + (maxRP - rpc - 1));
				leftFile.read(left_b);
				lpc++;
				rightFile.read(right_b);
				rpc++;

				leftLines = getLines(left_b);
				rightLines = getLines(right_b);

				int lit = 0;
				int rit = 0;

				String l_str = null;
				String r_str = null;
				STATUS = DEFAULT;

				while (true) {

					// make decision
					if (lit < leftLines.size() && rit < rightLines.size()) {
						STATUS = DEFAULT;
					} else if (lit == leftLines.size() && rit == rightLines.size()) {
						if (lpc == maxLP && rpc == maxRP) {
							STATUS = NO_MORE_LEFT_RIGHT_PAGE_EXST;
						} else if (lpc < maxLP && rpc < maxRP) {
							STATUS = LEFT_RIGHT_PAGE_EXST;
						} else if (lpc < maxLP) {
							STATUS = NO_MORE_RIGHT_PAGE_EXST;
						} else if (rpc < maxRP) {
							STATUS = NO_MORE_LEFT_PAGE_EXST;
						}
					} else if (lit == leftLines.size()) {
						if (lpc < maxLP) {
							STATUS = LEFT_PAGE_EXST;
						} else {
							STATUS = NO_MORE_LEFT_PAGE_EXST;
						}
					} else if (rit == rightLines.size()) {
						if (rpc < maxRP) {
							STATUS = RIGHT_PAGE_EXST;
						} else {
							STATUS = NO_MORE_RIGHT_PAGE_EXST;
						}
					} else {
						System.out.println("wrong state");
						break;
					}

					// action

					switch (STATUS) {
					case DEFAULT:
						l_str = leftLines.get(lit);
						r_str = rightLines.get(rit);

						if (l_str.compareTo(r_str) < 0) {
							writer.write(l_str + NEWLINE);
							lit++;
						} else {
							writer.write(r_str + NEWLINE);
							rit++;
						}

						break;

					case LEFT_RIGHT_PAGE_EXST: {
						traceMessage("Case LEFT_RIGHT_PAGE_EXST  Read Left page |" + lpc + "| Pages Remaining "
								+ (maxLP - lpc - 1));
						traceMessage("Case LEFT_RIGHT_PAGE_EXST Read Right page " + rpc + " Pages Remaining "
								+ (maxRP - rpc - 1));

						leftFile.read(left_b);
						lpc++;
						rightFile.read(right_b);
						rpc++;
						leftLines.clear();
						rightLines.clear();
						leftLines = getLines(left_b);
						rightLines = getLines(right_b);

						// reset line indexes
						lit = 0;
						rit = 0;

						// reset status
						STATUS = DEFAULT;
					}
						break;
					case LEFT_PAGE_EXST: {
						traceMessage("Case LEFT_PAGE_EXST  Read Left page |" + lpc + "| Pages Remaining "
								+ (maxLP - lpc - 1));

						leftFile.read(left_b);
						lpc++;
						leftLines.clear();
						leftLines = getLines(left_b);
						lit = 0;
						STATUS = DEFAULT;
					}
						break;
					case RIGHT_PAGE_EXST: {
						traceMessage("Case RIGHT_PAGE_EXST Read Right page " + rpc + " Pages Remaining "
								+ (maxRP - rpc - 1));
						rightFile.read(right_b);
						rpc++;
						rightLines.clear();
						rightLines = getLines(right_b);
						// reset line indexes
						rit = 0;
						// reset status
						STATUS = DEFAULT;
					}
						break;
					case NO_MORE_LEFT_PAGE_EXST: {
						// copy every line from right page
						while (rit < rightLines.size()) {
							writer.write(rightLines.get(rit) + NEWLINE);
							rit += 1;
						}
						traceMessage("Right Page Append " + (maxLP - rpc));
						while (rpc < maxRP) {
							traceMessage("Case NO_MORE_LEFT_PAGE_EXST Read Right page " + rpc + " Pages Remaining "
									+ (maxRP - rpc - 1));
							rightFile.read(right_b);
							String buf = new String(right_b);
							traceMessage("Right Page size Writing " + buf.length());
							writer.write(buf.toString().trim());
							rpc++;
						}

					}
						break;
					case NO_MORE_RIGHT_PAGE_EXST: {
						while (lit < leftLines.size()) {
							writer.write(leftLines.get(lit) + NEWLINE);
							lit += 1;
						}
						traceMessage("Left Page Append " + (maxLP - lpc));
						while (lpc < maxLP) {
							traceMessage("Case NO_MORE_RIGHT_PAGE_EXST Read Right page " + rpc + " Pages Remaining "
									+ (maxRP - rpc - 1));
							leftFile.read(left_b);
							String buf = new String(left_b);
							traceMessage("Left Page size Writing " + buf.length());
							writer.write(buf.toString().trim());
							lpc++;
						}

					}
						break;
					case NO_MORE_LEFT_RIGHT_PAGE_EXST: {

					}
						break;
					}

					if (STATUS == NO_MORE_LEFT_RIGHT_PAGE_EXST || STATUS == NO_MORE_LEFT_PAGE_EXST
							|| STATUS == NO_MORE_RIGHT_PAGE_EXST) {
						traceMessage("Two file merge complete");
						break; // break while(true) loop
					}
					writer.flush();
				}
				rightLines.clear();
				leftLines.clear();

				System.gc();
				writer.close();

				leftFile.close();
				rightFile.close();

				deleteFile(oldMergeFList.get(0));
				deleteFile(oldMergeFList.get(1));
				// end delete file
				mergeFileList.add(mergeFileName);
				oldMergeFList.remove(0);
				oldMergeFList.remove(0);
				traceMessage("\n New Merge file created :  " + mergeFileName);
			}

			if (oldMergeFList.size() == 1) {
				mergeFileList.add(oldMergeFList.get(0));
			}

			// assign newly created file to oldMergeList
			oldMergeFList = mergeFileList;
			level++;

		} while (mergeFileList.size() != 1);
		return mergeFileList;
	}
	/***
	 * 
	 * @param b
	 * @return lines
	 * @throws IOException
	 * 
	 * This method convert byte array to list of lines 
	 * 
	 */
	List<String> getLines(byte[] b) throws IOException {
		InputStream is;
		BufferedReader bfReader;
		is = new ByteArrayInputStream(b);
		bfReader = new BufferedReader(new InputStreamReader(is));

		List<String> lines = null;

		lines = new ArrayList<>();
		String line;
		while ((line = bfReader.readLine()) != null) {
			lines.add(line);
		}

		return lines;
	}
	/***
	 * 
	 * @param lines
	 * @return
	 * @throws IOException
	 * 
	 * Convert the lines into byte 
	 */
	byte[] getByte(List<String> lines) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		for (String element : lines) {
			/*
			 * element +="\n"; out.write(element.getBytes());
			 */out.writeUTF(element + "\n");
		}
		byte[] b = baos.toByteArray();
		return b;
	}
	
	void writeToMergeFile(List<String> resultList, RandomAccessFile mergefile) throws IOException {
		byte[] b = getByte(resultList);
		// mergefile.seek(mergefile.length());
		mergefile.write(b);
		System.out.print("Writinging to file");
	}

	void deleteFile(String fileName) {
		new File(fileName).delete();
	}

	BufferedWriter getFileWriter(String fileName, String mode) throws IOException {
		FileWriter writer = new FileWriter(fileName);
		BufferedWriter bufferedWriter = new BufferedWriter(writer, this.pageSize);
		return bufferedWriter;
	}

	void writeToFile(List<String> resultList, BufferedWriter bufferedWriter) throws IOException {
		if (resultList.size() <= 0)
			return;
		for (String line : resultList) {
			bufferedWriter.write(line);
		}
		bufferedWriter.flush();
	}

	void traceMessage(String str) {
		System.out.println("Trace : Sort Count <" + this.sortCount + "> | " + str);
	}
	/****
	 * @author archi
	 * @return priorityQueue
	 * Creates the priority min queue.  
	 */
	PriorityQueue<FileNode> getPriorityQueue() {
		PriorityQueue<FileNode> priorityQueue = new PriorityQueue<FileNode>(1000000, new Comparator<FileNode>() {

			@Override
			public int compare(FileNode node1, FileNode node2) {
				return node1.line.compareTo(node2.line);
			}
		});
		return priorityQueue;
	}
	
	BufferedReader getFileReader(String file){
		BufferedReader br = null;
	       try{	
	           br = new BufferedReader(new FileReader(file));	
	       }catch(Exception e){
	    	   
	       }
	      return br;
	}

}
