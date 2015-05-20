package TEST1;
/**
 * This program analysis the log reprots
 * @author tangmi
 *
 */
import java.util.*;
import java.io.*;

public class LogAnalysis {
	private String str = null;
	
	public List<ThreadPacks> parseFile(File file){
		final String FLAG1 = "ResponseContextInterceptor";
		final String FLAG2 = "将response加入ThreadLocal";
		final String FLAG3 = "将response从ThreadLocal中清除";
		boolean isNewBlock = false;
		
		List<RecordPacks> listRp = new ArrayList<RecordPacks>();
		List<BlockPacks> listBp = new ArrayList<BlockPacks>();
		List<ThreadPacks> listTp = new ArrayList<ThreadPacks>();
		List<String> listThread = new ArrayList<String>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			while ((str = br.readLine()) != null ) {
				if (str.startsWith("[")) {
					RecordPacks recordPack = new RecordPacks(str);
					recordPack.addRecordLine(str);
					isNewBlock =  ((str.indexOf(FLAG1)  != -1) && (str.indexOf(FLAG2) != -1)) ;
					if (listTp.size() != 0) {
						int indexOfTp = listThread.indexOf(recordPack.getThreadId());
						if (indexOfTp != -1){
							if (!isNewBlock){
								listTp.get(indexOfTp).getLastBlockPack().addRecordPack(recordPack);
							}else{
								BlockPacks newBp = new BlockPacks(recordPack.getThreadId(),recordPack);
								recordPack.setBlockId(newBp.getBlockId());
								recordPack.setThreadId(recordPack.getThreadId());
								listBp.add(newBp);
								listTp.get(indexOfTp).addBlockPack(newBp);
							}
								
						}
						
					}
				}
					
			}
			
			
			
			
		}catch (IOException e){
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
}


class RecordPacks{
 	private int blockId;
	private long recordPackId;
	private static long recordPackNum = 0;
	private String threadId;
	private List<String> recordBody = new ArrayList<String>();
	
	public RecordPacks(){
		this.blockId = 0;
		this.recordPackId = ++recordPackNum;
	}
	
	public RecordPacks(String recordLine) {
		this.blockId = 0;
		this.recordBody.add(recordLine);
	}
	public RecordPacks(int blockId,String threadId,String recordBody){
		this.blockId = blockId;
		this.threadId = threadId;
		this.recordBody.add(recordBody);
		this.recordPackId = ++recordPackNum;
	}
	
	public void setRecordPackId(long recordPackId){
		this.recordPackId = recordPackId;
	}
	
	public long getRecordPackId(){
		return this.recordPackId;
	}
	
	public static long getRecordNum(){
		return recordPackNum;
	}
	
	public void setBlockId(int blockId){
		this.blockId = blockId;
	}
	
	public int getBlockId(){
		return blockId;
	}
	
	public void setThreadId(String threadId){
		this.threadId = threadId;
	}
	
	public String getThreadId(){
		return threadId;
	}
	
	public void setRecordBody(List<String> recordBody){
		this.recordBody = recordBody;
	}
	
	public List<String> getRecordBody(){
		return this.recordBody;
	}
	
	public void addRecordLine(String recordLine){
	this.recordBody.add(recordLine);
	}
	
	public String toString(){
		return ("[BLOCKID=" + this.blockId + "] " +  this.recordBody.get(0));
	}

	public boolean isFirstLine(){
		return this.recordBody.get(0).startsWith("[");
	}
	
	public String getTimeLable(){
		String[] strArr = this.recordBody.get(0).split("\\s+",4);
		return strArr[0];
	}
	
	public String getThreadIdLable(){
		String[] strArr = this.recordBody.get(0).split("\\s+",4);
		return strArr[1];
	}
	
	public String getStatusLable(){
		String[] strArr = this.recordBody.get(0).split("\\s+",4);
		return strArr[2];
	}
	
	public String getRecordText(){
		String[] strArr = this.recordBody.get(0).split("\\s+",4);
		return strArr[3];
	}
	
}

class BlockPacks{
	private int blockId;
	private static int blockNum = 0;
	private String threadId;
	private List<RecordPacks> listRecordPack = new ArrayList<RecordPacks>();
	
	public BlockPacks(){
		this.blockId =++blockNum;
		this.threadId = null;
	}
	
	public BlockPacks(String threadId,RecordPacks recordPack){
		this.blockId = ++blockNum;
		this.threadId = threadId;
		this.listRecordPack.add(recordPack);
	}
	
	
	/**
	public void setBlockId(int blockId){
		this.blockId = blockId;
	}
	*/
	
	public int getBlockId(){
		return blockId;
	}
	
	public void setThreadId(String threadId){
		this.threadId = threadId;
	}
	
	public String getThreadId(){
		return threadId;
	}
	
	public List<RecordPacks> getListRecordPack(){
		return listRecordPack;
	}
	
	public void setListRecordPack(List<RecordPacks> listRecordPack){
		this.listRecordPack = listRecordPack;
	}
	
	public void addRecordPack(RecordPacks recordPack){
		this.listRecordPack.add(recordPack);
	}
}

class ThreadPacks{
	private String threadPackId;
	private List<BlockPacks> blockPack= new ArrayList<BlockPacks>();
	
	public ThreadPacks(){
		
	}
	
	public ThreadPacks(String threadPackId, List<BlockPacks> blockPack){
		this.threadPackId = threadPackId;
		this.blockPack = blockPack;
	}
	
	public String getThreadPackId(){
		return this.threadPackId;
	}
	
	public void setThreadPackId(String threadPackId){
		this.threadPackId = threadPackId;
	}
	
	public void addBlockPack(BlockPacks blockPack){
		this.blockPack.add(blockPack);
	}
	
	public BlockPacks getLastBlockPack(){
		int indexOfLastBp = this.blockPack.size() - 1;
		return this.blockPack.get(indexOfLastBp);
	}
}