package TEST1;
/**
 * This program analysis the log reprots
 * @author tangmi
 *
 */
import java.util.*;
import java.io.*;

public class LogAnalysis2 {
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
		ThreadPacks lastTp;
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			while ((str = br.readLine()) != null ) {
				if (str.startsWith("[")) {    // if it's the real record with [timestamp] field
					RecordPacks recordPack = new RecordPacks(str);
					recordPack.addRecordLine(str);
					recordPack.setThreadId(recordPack.getThreadIdLable());
					
					isNewBlock =  ((str.indexOf(FLAG1)  != -1) && (str.indexOf(FLAG2) != -1)) ;
					// find the correct ThreadPacks index
					int indexOfTp = listThread.indexOf(recordPack.getThreadId());
					// If the threadPack with certain threadId is existed 
					if (indexOfTp != -1){
						//If it's not a new block 
						if (!isNewBlock){
							// add the recordPack to the specific threadPack's last block
							listTp.get(indexOfTp).getLastBlockPack().addRecordPack(recordPack);
						}else{   // If it's a new block
							// Create a new blockPack object, and add the current recordPack to it
							BlockPacks newBp = new BlockPacks(recordPack);
							// Add the new blockPack into the specific ThreadPack
							listTp.get(indexOfTp).addBlockPack(newBp);
						}		
					}else{ //If the threadPack with certain threadId isn't existed 

						// Create a new BlockPack object and add the current recordPack to it
						BlockPacks newBp = new BlockPacks(recordPack);
						// Create a new threadPack object and add the newBp to it
						ThreadPacks threadPack = new ThreadPacks(recordPack.getThreadId(),newBp);
						// Add the current threadId into the listThread for later reference
						listThread.add(recordPack.getThreadId());
						lastTp = threadPack;	
					}
						
				}else {   // the record just read is not the first line
					// if there is no ThreadPacks object created before, just withdraw the record just read
					if (listThread.size() == 0 ) break;
					else {
						
						lastTp.getLastBlockPack();
					}
				}
					
			}
			
			
			
			
		}catch (IOException e){
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
}


class RecordPacks{
	private String threadId;
	private List<String> recordBody = new ArrayList<String>();
	
	public RecordPacks(){
	}
	
	public RecordPacks(String recordLine) {
		this.recordBody.add(recordLine);
	}
	public RecordPacks(String threadId,String recordBody){
		this.threadId = threadId;
		this.recordBody.add(recordBody);
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
	private List<RecordPacks> listRecordPack = new ArrayList<RecordPacks>();
	
	public BlockPacks(){
	}
	
	public BlockPacks(RecordPacks recordPack){
		this.listRecordPack.add(recordPack);
	}
	
	
	public void setBlockId(int blockId){
		this.blockId = blockId;
	}
	
	public int getBlockId(){
		return blockId;
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
	
	public RecordPacks getLastRecordPack(){
		int indexOfLastRp = this.listRecordPack.size() -1;
		return this.listRecordPack.get(indexOfLastRp);
	}
}

class ThreadPacks{
	private String threadPackId;
	private List<BlockPacks> listBlockPack= new ArrayList<BlockPacks>();
	
	public ThreadPacks(){
		
	}
	
	public ThreadPacks(String threadPackId){
		this.threadPackId =threadPackId;
	}
	
	public ThreadPacks(String threadPackId, BlockPacks blockPack){
		this.threadPackId = threadPackId;
		this.listBlockPack.add(blockPack);
	}
	
	public String getThreadPackId(){
		return this.threadPackId;
	}
	
	public void setThreadPackId(String threadPackId){
		this.threadPackId = threadPackId;
	}
	
	public void addBlockPack(BlockPacks blockPack){
		this.listBlockPack.add(blockPack);
	}
	
	public BlockPacks getLastBlockPack(){
		int indexOfLastBp = this.listBlockPack.size() - 1;
		return this.listBlockPack.get(indexOfLastBp);
	}
}