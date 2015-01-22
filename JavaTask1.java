import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

class JavaTask1 {	
	static Map<Integer, Integer> LoginTime = new HashMap<Integer, Integer>();
	static Map<Integer, Integer> AuthorizedUsers = new HashMap<Integer, Integer>();
	
	public static void main(String[] args) throws IOException {    
		readLog("log.txt");
		printResult();
	}
	
	private static void printResult() throws IOException {		
		List<Map.Entry<Integer, Integer>> LoginTimeList = new ArrayList<>(LoginTime.entrySet());
		
		Collections.sort(LoginTimeList, new Comparator<Map.Entry<Integer, Integer>>() {
	       @Override
	        public int compare(Map.Entry<Integer, Integer> a, Map.Entry<Integer, Integer> b) {
 	           return (b.getValue() > a.getValue()) ? 1 : ((b.getValue() == a.getValue()) ? 0 : -1);
	        }
	    });
		
		Iterator itr = LoginTimeList.iterator();
		while(itr.hasNext()) {
      		Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) itr.next();
			System.out.println("Id=" + entry.getKey() + " Time=" + entry.getValue());
		}
	}
	
    private static void readLog(String FileName) throws IOException {
		int intChr = 0;
		String fileLine = "";
		RandomAccessFile aFile = new RandomAccessFile(FileName, "r");
		FileChannel inChannel = aFile.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		while (inChannel.read(buffer) > 0) {
			buffer.flip();
			for (int i=0; i < buffer.limit(); i++) {
				intChr = buffer.get();
				if (intChr == 10) {
					parseLine(fileLine);
					fileLine = "";
				}
				else {
					fileLine = fileLine + (char) intChr;
				}
			}
			parseLine(fileLine);
			buffer.clear();
		}
		inChannel.close();
		aFile.close();
    }
	
	private static void parseLine(String line) throws IOException {
		Scanner lineScanner = new Scanner(line);
		lineScanner.useDelimiter("\\s*,\\s*");
			
		int timeLength = 0;
		int time = lineScanner.nextInt();
		int userId = lineScanner.nextInt();
		String Command = lineScanner.next();
		
		if (Command.equals("login")) {
			AuthorizedUsers.put(userId, time);
		}
		else {
			timeLength = time - AuthorizedUsers.remove(userId);
			LoginTime.put(userId, timeLength + ((LoginTime.containsKey(userId)) ? LoginTime.get(userId) : 0));
		}
		
	}
}
