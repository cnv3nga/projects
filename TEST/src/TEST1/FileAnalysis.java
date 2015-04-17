package TEST1;
/**
 * This program analysis the log file
 * @author tangmi
 *
 */
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
public class FileAnalysis {

	public List<ThreadPacks> parseFile(File file) {
		final String FLAG1 = "ResponseContextInterceptor";
		final String FLAG2 = "将response加入ThreadLocal";
		final String FLAG3 = "将response从ThreadLocal中清除";
		String str = null;
		int blockId = 0;
		String strThreadBody ="";
		String oldThreadId = null;
		boolean isNewBlock = false;
				
		List<ThreadPacks> list = new ArrayList<ThreadPacks>();
		List<String> listThread = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			while ((str = br.readLine()) != null) {
				if (str.startsWith("[")){
					// int index = str.lastIndexOf("|\\w+|");
					// String strHead = str.substring(0, index);
					// String strThreadBody = str.substring(index + 1);
					String[] strArr = str.split("\\s+",4);
					// String threadTimeLable = strArr[0];
					String threadId = strArr[1];
					// String threadLableId = strArr[2];
					if ((str.indexOf(FLAG1)  != -1) && (str.indexOf(FLAG2) != -1)) {
						isNewBlock = true;
					} else 
						isNewBlock = false;
					
					if (listThread.size() == 0 ) {
						listThread.add(threadId);
						ThreadPacks threadPacks = new ThreadPacks();			
						threadPacks.setThreadId(threadId);
						// threadPacks.addLableId(threadLableId);
						if (isNewBlock) {
							blockId = threadPacks.getBlockId() + 1;
						} else 
							blockId = threadPacks.getBlockId();
						
						threadPacks.setBlockId(blockId);
						oldThreadId = threadId;
						strThreadBody = "[BLOCKID=" + blockId + "] " + str;
						threadPacks.addRecordBody(strThreadBody);
						list.add(threadPacks);
					} else if (listThread.indexOf(threadId) != -1) {
							int i = listThread.indexOf(threadId);
							if (isNewBlock) {
								blockId = list.get(i).getBlockId() + 1;
							} else 
								blockId = list.get(i).getBlockId();
							
							list.get(i).setBlockId(blockId);
							oldThreadId = threadId;
							strThreadBody = "[BLOCKID=" + blockId + "] " +  str;						
							list.get(i).addRecordBody(strThreadBody);
						} else {
							listThread.add(threadId);
							ThreadPacks threadPacks = new ThreadPacks();
							threadPacks.setThreadId(threadId);
								if (isNewBlock) {
									blockId = threadPacks.getBlockId() + 1;
								} else 
									blockId = threadPacks.getBlockId();
								
								threadPacks.setBlockId(blockId);
								oldThreadId = threadId;
								strThreadBody = "[BLOCKID=" + blockId + "] " + str;						
								threadPacks.addRecordBody(strThreadBody);
								list.add(threadPacks);
							}
				} else {
					if (listThread.size() == 0 ) break;
					else {
						int i = listThread.indexOf(oldThreadId);
						blockId = list.get(i).getBlockId();
						strThreadBody = "[BLOCKID=" + blockId + "] " + str;
						list.get(i).addRecordBody(strThreadBody);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			}
		return list;
	}
	
	public List<String> errorFilter(List<ThreadPacks> list, String threadId){
		final String WARNTAG = "|WARN|";
		final String ERRORTAG = "|ERROR|";
		List<String> errorOutput = new ArrayList<String>();
		ThreadPacks threadPacks = new ThreadPacks();
		int index = -1;
		
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getThreadId().equals(threadId)) {
				index = i;
				break;
			}
		}

		
		threadPacks = list.get(index);
		
		for (int j = 0; j < threadPacks.getRecordBody().size(); j++ ) {
			String[] strArr = threadPacks.getRecordBody().get(j).split("\\s+",5);
			if (strArr.length > 4) {
				if (strArr[3].equals(ERRORTAG) || strArr[3].equals(WARNTAG)) 
					errorOutput.add(threadPacks.getRecordBody().get(j));
				}
		}
		return errorOutput;
	}
	
	public List<BlockAndFeature> blockBuild(List<ThreadPacks> list, int featureTypeNum) {
		int index = 0;
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < list.get(i).getBlockId(); j++){
				BlockAndFeature blockBuilt = new BlockAndFeature(j,list.get(i).getThreadId(), featureTypeNum);
					String str = list.get(i).getRecordBody().get(index);
					String[] strArr = str.split("\\s+",5);
					String currentBlockId = strArr[0].substring(strArr[0].indexOf('=')+1,strArr[0].indexOf(']')-1);
					while (j == Integer.valueOf(currentBlockId)) {
						blockBuilt.addBlockBody(str);
						index++;
					}
			}
		}	
	}
	
	
	public static void main(String[] args){
		String[] featureTab = {"etdz","SessionExpireException","状态机状态->Error"};
		FileAnalysis fa = new FileAnalysis();
		List<ThreadPacks> list = new ArrayList<ThreadPacks>();
		List<String> errorRecordList = new ArrayList<String>();
		File file = new File("debug.log");
		list = fa.parseFile(file);
		errorRecordList = fa.errorFilter(list, "[http-bio-8980-exec-7]");
	}
}

class ThreadPacks {
	private String threadId;
	private  int blockId = 0;
	private List<String> recordBody = new ArrayList<String>();
	
	/**
	public void addTimeLable(String timeLable) {
		this.timeLable.add( timeLable);
	}   
 	*/
	
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	
	public String getThreadId(){
		return threadId;
	}
	
	/**
	public void addLableId(String lableId) {
		this.lableId.add(lableId);
	}
	*/
	
	
	public void setBlockId( int blockId) {
		this.blockId = blockId;
	}
	
	public int getBlockId(){
		return blockId;
	}	
	
	public void addRecordBody(String recordBody){
		this.recordBody.add(recordBody);
	}
	
	public List<String> getRecordBody(){
		return recordBody;
	}
}

class ErrorAnalysis{
	private List<ThreadPacks>  listReorg;
	
	ErrorAnalysis(List<ThreadPacks> listReorg){
		this.listReorg = listReorg;
	}
	
	public void logFilter(List<ThreadPacks> listReorg){
		
	}
}

class BlockAndFeature{
	private int blockId;
	private String threadId;
	private List<ThreadPacks> threadPacks = new ArrayList<ThreadPacks>();
	private List<String> blockBody = new ArrayList<String>();
	private int[] featureValue;
	
	public BlockAndFeature(int blockId,String threadId, int featureTypeNum){
		this.featureValue = new int[featureTypeNum];
		this.threadId = threadId;
		this.blockId = blockId;
		
		for (int i = 0; i < featureTypeNum; i++)
			featureValue[i] = 0;
	}
	
	public void setBlockId(int blockId){
		this.blockId = blockId;
	}
	
	public int getBlockId(){
		return blockId;
	}
	
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	
	public String getThreadId(){
		return threadId;
	}
	
	public void addBlockBody(String strBlockBody){
		this.blockBody.add(strBlockBody);
	}
	
	public List<String> getBlockBody(){
		return blockBody;
	}
	
	public int[] countFeatureValue(String[] featureTypeArr){
		for (int i = 0; i < blockBody.size(); i++){
			for (int j = 0; j < featureTypeArr.length; j++){
				if (blockBody.get(i).indexOf(featureTypeArr[j]) != -1) 
					featureValue[j]++;
			}
		}
		return featureValue;
	}
}