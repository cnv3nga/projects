package TEST1;
/**
 * This program analysis the log file
 * @author tangmi
 *
 */
import java.util.*;
import java.io.*;
public class FileAnalysis {
	final String FLAG = "ResponseContextInterceptor";
	
	public List<ThreadPacks> parseFile(File file) {
		String str = null;
		String OldThreadId = null;
		boolean isExist = false;
		
		List<ThreadPacks> list = new ArrayList<ThreadPacks>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			ThreadPacks threadPacks = new ThreadPacks();			
			while ((str = br.readLine()) != null) {
				if (str.startsWith("[//d//d://d//d://d//d]")){
					int index = str.lastIndexOf("|\\w+|");
					String strHead = str.substring(0, index);
					String strThreadBody = str.substring(index + 1);
					String[] strArr = strHead.split("\\s+");
					String threadTimeLable = strArr[0];
					String threadId = strArr[1];
					String threadLableId = strArr[2];
					for (int i = 0; i < list.size(); i++ ) {
						if ( threadId.equals(list.get(i).getThreadId())) {
							isExist =true;
							break;
						}
					}
					if ((OldThreadId == null) || ( threadId.equals(OldThreadId))) {
						threadPacks.addTimeLable(threadTimeLable);
						threadPacks.setThreadId(threadId);
						threadPacks.addLableId(threadLableId);
						threadPacks.addRecordBody(strThreadBody);
						list.add(threadPacks);
						OldThreadId = threadId;
					}
					

				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();		
	}
}

class ThreadPacks {
	private List<String> timeLable = new ArrayList<String>();
	private String threadId;
	private List<String> lableId = new ArrayList<String>();
	private List<Integer> blockId = new ArrayList<Integer>();
	private List<String> recordBody = new ArrayList<String>();
	
	public void addTimeLable(String timeLable) {
		this.timeLable.add( timeLable);
	}
	
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	
	public String getThreadId(){
		return threadId;
	}
	
	public void addLableId(String lableId) {
		this.lableId.add(lableId);
	}
	
	public void addBlockId( int blockId) {
		this.blockId.add(blockId);
	}
	
	public void addRecordBody(String recordBody){
		this.recordBody.add(recordBody);
	}
	
	public List<Integer> getBlockId(){
		return blockId;
	}
}
