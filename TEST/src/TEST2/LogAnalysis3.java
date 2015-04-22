package TEST2;
/**
 * This program analysis the log reprots
 * @author tangmi
 *
 */
import java.util.*;
import java.io.*;

public class LogAnalysis3 {

	private static class ThreadsStorage {
		public Map<String,ThreadPacks> packs=new HashMap<String,ThreadPacks>();
		public ThreadPacks last=null;
		public ThreadPacks getPack(String threadId) {
			last=packs.get(threadId);
			if(last==null) {
				last=new ThreadPacks(threadId);
				packs.put(threadId, last);
			}
			return last;
		}
	}
	
	private Map<String,ThreadPacks> parseFile(List<File> listInputFile,final String FLAG1){

		boolean isNewBlock = false;
		String str = null;
		
		ThreadsStorage ths=new ThreadsStorage();
		//List<ThreadPacks> listTp = new ArrayList<ThreadPacks>();
		//List<String> listThread = new ArrayList<String>();
		//ThreadPacks lastTp=null;
		
		try{
			int fileIndex = 0;
			while (fileIndex < listInputFile.size()){
				BufferedReader br = new BufferedReader(new FileReader(listInputFile.get(fileIndex)));
				fileIndex++;
				while ((str = br.readLine()) != null ) {
					if (str.startsWith("[")) {    // if it's the real record with [timestamp] field
						RecordPacks recordPack = new RecordPacks(str);
						recordPack.setThreadId(recordPack.getThreadIdLable());
					
						isNewBlock =  (str.matches(FLAG1)) ;
						// find the correct ThreadPacks index
						ThreadPacks tps=ths.getPack(recordPack.getThreadId());

						// If the threadPack with certain threadId is existed 
						if(tps.getBlockNum()>0) {
							//If it's not a new block 
							if (!isNewBlock){
								// add the recordPack to the specific threadPack's last block
								tps.getLastBlockPack().addRecordPack(recordPack);
							}else{   // If it's a new block
								// Create a new blockPack object, and add the current recordPack to it
								BlockPacks newBp = new BlockPacks(recordPack);
								// Add the new blockPack into the specific ThreadPack
								tps.addBlockPack(newBp);
							}		
						}else{ //If the threadPack with certain threadId isn't existed 
							// Create a new BlockPack object and add the current recordPack to it
							BlockPacks newBp = new BlockPacks(recordPack);
							tps.addBlockPack(newBp);
						}
						
					}else {   // the record just read is not the first line
						// if there is no ThreadPacks object created before, just withdraw the record just read
						if(ths.last==null) {
							break;
						} else {
							ths.last.getLastBlockPack().getLastRecordPack().addRecordLine(str);
						}
					}		
				}
				br.close();
			}
		}catch (IOException e){
			e.printStackTrace();
		}
		return ths.packs;
	}
	

private List<FeaturePacks> errorFilter(Collection<ThreadPacks> listTp, String[] featureNameArr){
	List<FeaturePacks> listFeaturePack = new ArrayList<FeaturePacks>();

	
	// go over every threadPack 
	for(ThreadPacks tp:listTp) {
	//for (int i = 0; i < listTp.size(); i++){
		List<String> listFeature = new ArrayList<String>();
		List<Integer> listFeatureNum = new ArrayList<Integer>();
		List<String> listFeatureBlockId = new ArrayList<String>();
		
		//ThreadPacks tp = listTp.get(i);
		//go over every blockPack 
		for (int j = 0; j < tp.getBlockNum(); j++) {
			int[] featureArr = new int[featureNameArr.length];
			BlockPacks bp = tp.getSpecificBlockPack(j);
			// go over every recordPack
			for (int ii = 0; ii < bp.getListRecordPack().size();ii++){
				RecordPacks rp = bp.getListRecordPack().get(ii);
				//go over with every record line
				for (int jj = 0; jj < rp.getRecordBody().size();jj++){
					for (int z = 0; z < featureNameArr.length; z++){
						String str = featureNameArr[z];
						if (rp.getRecordBody().get(jj).indexOf(str) != -1) 
							featureArr[z]++;
					}	
				}
			}
			String featureType = "";
			

			// int featureExistLable = 0;
			for (int y = 0; y < featureArr.length; y++) {
				featureType = featureType + featureArr[y] + " "; 
			// 	featureExistLable += featureArr[y];
			}
			/**  filter out all 0 type 
			  
				if (featureExistLable == 0) 
				continue;
			 */
			int indexOfFeature = listFeature.indexOf(featureType); 
			if (indexOfFeature != -1){
				int listFeatureNumber = listFeatureNum.get(indexOfFeature) + 1;
				listFeatureNum.set(indexOfFeature, listFeatureNumber);
				String featureBlockId = listFeatureBlockId.get(indexOfFeature) + j + " ";
				listFeatureBlockId.set(indexOfFeature, featureBlockId);
			}else{
				listFeature.add(featureType);	
				listFeatureNum.add( 1);
				listFeatureBlockId.add(j + " ");
			}
		}
		FeaturePacks fp = new FeaturePacks(tp.getThreadPackId(),listFeature,listFeatureNum);
		fp.setListFeatureBlock(listFeatureBlockId);
		listFeaturePack.add(fp);
	}
	return listFeaturePack;
}

private TotalCountValue countResult(List<FeaturePacks> listFp,String[] featureType){
	List<String> totalListFeature = new ArrayList<String>();
	List<Integer> totalListFeatureNum = new ArrayList<Integer>();
	List<String> listPercentageOfSpecifcType = new ArrayList<String>();
	List<String> listPercentageOfAllType = new ArrayList<String>();
	
	int totalNumOfSpecificType = 0;
	int totalNumOfAllType = 0;
	int totalNumOfSpecificNum = 0;
	String percentageOfSpecificNum = null;
	
	//  Count all feature type information through the threadId type
	for (int i = 0; i < listFp.size(); i++){
		for (int j = 0; j < listFp.get(i).getListFeature().size(); j++){
			int indexOfFeature = totalListFeature.indexOf(listFp.get(i).getListFeature().get(j));
			if (indexOfFeature != -1){
				int totalFeatureNum = totalListFeatureNum.get(indexOfFeature) +listFp.get(i).getListFeatureNum().get(j);
				totalListFeatureNum.set(indexOfFeature, totalFeatureNum);
			}else{
				totalListFeature.add(listFp.get(i).getListFeature().get(j));
				totalListFeatureNum.add(listFp.get(i).getListFeatureNum().get(j));
			}	
		}
	}
	for ( int i = 0; i < totalListFeature.size(); i++){
		totalNumOfAllType += totalListFeatureNum.get(i);
		String[] strArr = totalListFeature.get(i).split("\\s+",2);
		if (Integer.valueOf(strArr[0]) != 0) 
			totalNumOfSpecificType += totalListFeatureNum.get(i);
	}
	
	for (int i = 0; i < totalListFeature.size();i++){
		String percentageForSpecific = null;
		String percentageForAll = null;
		String[] strArr = totalListFeature.get(i).split("\\s+",2);
		if (Integer.valueOf(strArr[0]) != 0){
			percentageForSpecific = String.format("%4.2f", Double.valueOf(totalListFeatureNum.get(i)) / Double.valueOf(totalNumOfSpecificType) * 100); 
			totalNumOfSpecificNum += totalListFeatureNum.get(i);
		}else 
			percentageForSpecific = null;
		percentageForAll = String.format("%4.2f",Double.valueOf(totalListFeatureNum.get(i)) / Double.valueOf(totalNumOfAllType) * 100);
		listPercentageOfSpecifcType.add(percentageForSpecific);
		listPercentageOfAllType.add(percentageForAll);
	}
	percentageOfSpecificNum = String.format("%4.2f",Double.valueOf(totalNumOfSpecificNum) / Double.valueOf(totalNumOfAllType) * 100);
	TotalCountValue tcv = new TotalCountValue(percentageOfSpecificNum,totalListFeature,totalListFeatureNum,listPercentageOfSpecifcType,listPercentageOfAllType);
	return tcv;
}


private void printResult(Collection<ThreadPacks> listTp, List<FeaturePacks> listFp,String[] featureType,TotalCountValue tcv,File file){
	try{
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		String strLine = "                                  Feature Type Counting Table";
		bw.write(strLine);
		bw.newLine();
		strLine = " ------------------------------------------------------------------------";
		bw.write(strLine);
		bw.newLine();
		for (int i =0;i < featureType.length; i++){
			bw.write("Lable" + i + ": " + featureType[i]);
			bw.newLine();
		}
		
		bw.write(strLine);
		bw.newLine();
		
		for(int i = 0; i < featureType.length; i++){
			bw.write("    " + "Lable" + i);
		}
		bw.write("    number");
		bw.newLine();
/**		
		List<String> totalListFeature = new ArrayList<String>();
		List<Integer> totalListFeatureNum = new ArrayList<Integer>();
*/		
		for (int i = 0; i < listFp.size(); i++){
			bw.write(" ThreadId = " + listFp.get(i).getThreadId());
			bw.newLine();
			for(int j = 0; j < featureType.length; j++){
				bw.write("    " + "Lable" + j);
			}
			bw.write("    number");
			bw.newLine();
			
			for (int j = 0; j < listFp.get(i).getListFeature().size(); j++){
				String[] strArr = listFp.get(i).getListFeature().get(j).split("\\s+");
				String s = "";
				for (String str:strArr)
					s += String.format("%10d", Integer.valueOf(str));
				s = s + String.format("%10d",listFp.get(i).getListFeatureNum().get(j));
				bw.write(s);
				bw.newLine();
/**				
				// count the total number
				int indexOfFeature = totalListFeature.indexOf(listFp.get(i).getListFeature().get(j));
				if (indexOfFeature != -1){
					int totalFeatureNum = totalListFeatureNum.get(indexOfFeature) +listFp.get(i).getListFeatureNum().get(j);
					totalListFeatureNum.set(indexOfFeature, totalFeatureNum);
				}else{
					totalListFeature.add(listFp.get(i).getListFeature().get(j));
					totalListFeatureNum.add(listFp.get(i).getListFeatureNum().get(j));
				}
*/				
			}
			bw.write("-----------------------------------------------------------------------");
			bw.newLine();
/** Print each block in error
			for (int j = 0; j < listFp.get(i).getListFeatureBlockId().size(); j++){	
				String[] strArr = listFp.get(i).getListFeatureBlockId().get(j).split("\\s+");
				for (int l = 0; l < strArr.length; l++){
					bw.write("BLOCKID=" + strArr[l]);
					bw.newLine();
					bw.write("-----------------------------------------------------------------------");
					bw.newLine();
					List<RecordPacks> rp = listTp.get(i).getSpecificBlockPack(Integer.valueOf(strArr[l])).getListRecordPack();
					int numOfRecordPack = rp.size();
						for (int k =0; k < numOfRecordPack; k++){
							int numOfRecordLine = rp.get(k).getRecordBody().size();
							for (int m = 0; m < numOfRecordLine; m++){
								bw.write(rp.get(k).getRecordBody().get(m));
								bw.newLine();
							}
						}
				}
			}
*/
		}
		bw.write(" -----------------------Total number------------------------------ ");
		bw.newLine();
		bw.write("Specific PCT = " + tcv.getPercentageOfSpecificNum() + "%");
		bw.newLine();
		
		for(int i = 0; i < featureType.length; i++){
			bw.write("    " + "Lable" + i);
		}
		bw.write("    number   PCT1(%)   PCT2(%)");
		bw.newLine();
		
		for (int i = 0; i < tcv.getTotalListFeature().size(); i++){
			String[] strArr = tcv.getTotalListFeature().get(i).split("\\s+");
			String s = "";
			for (String str:strArr)
				s += String.format("%10d", Integer.valueOf(str));
				s = s + String.format("%10d",tcv.getTotalListFeatureNum().get(i));
				s += String.format("%10.2f",Double.valueOf(tcv.getListPercentageOfAllType().get(i)));
				if (tcv.getListPercentageOfSpecificType().get(i) != null )
					s += String.format("%10.2f", Double.valueOf(tcv.getListPercentageOfSpecificType().get(i)));
				
			bw.write(s);
			bw.newLine();
		}
		bw.close();
	}catch (IOException e){
		e.printStackTrace();
	}
}

private void PrintTotal(String[] featureType,TotalCountValue tcv,File file){
	try {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file,true));
		String strLine = " ------------------------------------------------------------------------";
		bw.write(strLine);
		bw.newLine();
		for (int i =0;i < featureType.length; i++){
			bw.write("Lable" + i + ": " + featureType[i]);
			bw.newLine();
		}
		
		bw.write(strLine);
		bw.newLine();
		
		bw.write(" -----------------------Total number------------------------------ ");
		bw.newLine();
		bw.write("Specific PCT = " + tcv.getPercentageOfSpecificNum() + "%");
		bw.newLine();
		
		for(int i = 0; i < featureType.length; i++){
			bw.write("    " + "Lable" + i);
		}
		bw.write("    number   PCT1(%)   PCT2(%)");
		bw.newLine();
		
		for (int i = 0; i < tcv.getTotalListFeature().size(); i++){
			String[] strArr = tcv.getTotalListFeature().get(i).split("\\s+");
			String s = "";
			for (String str:strArr)
				s += String.format("%10d", Integer.valueOf(str));
				s = s + String.format("%10d",tcv.getTotalListFeatureNum().get(i));
				s += String.format("%10.2f",Double.valueOf(tcv.getListPercentageOfAllType().get(i)));
				if (tcv.getListPercentageOfSpecificType().get(i) != null )
					s += String.format("%10.2f", Double.valueOf(tcv.getListPercentageOfSpecificType().get(i)));
				
			bw.write(s);
			bw.newLine();
		}
	bw.close();	
	}catch(IOException e){
		e.fillInStackTrace();
	}
	
}
private ConfigureInfo readConfFile(File confFile){
	List<String> listConfLine = new ArrayList<String>();
	String blockDelimiter = null;
	try{
		BufferedReader br = new BufferedReader(new FileReader(confFile));
		String str = null;
		boolean firstLine = true;
		while ((str = br.readLine()) != null) {
			if (firstLine){
				blockDelimiter = str;
				firstLine = false;
			}else{

			listConfLine.add(str);
			}
		}
		br.close();
	}catch(IOException e){
		e.fillInStackTrace();
	}	
	ConfigureInfo configureInfo = new ConfigureInfo(blockDelimiter,listConfLine);
	return configureInfo;
}

public static void main(String[] args){
	final String label1 = ".*ResponseContextInterceptor.*将response加入ThreadLocal.*";

	int i =0;
	List<File> listInputFile = new ArrayList<File>();
	File confFile = null;
	String outFileName = null;
	
	// Read the arguments for input file, output file and configuration file
	while (i < args.length){
		switch (args[i]){
			case "-i" : while(!args[i+1].startsWith("-")) {
								listInputFile.add(new File(args[i+1]));
								i++;
								} 
								i++;
								break;
			case "-c" : confFile = new File(args[i+1]);
								i += 2;
								break;
			case "-o" : outFileName = args[i+1];		
								i += 2;
								break;
			default :	System.out.println("Wrong parameters inserted!");
								System.out.println("LogAnalysis3 -i  input1.log input2.log input 3.log -c debug.conf -o debug_out.log");
								System.exit(0);
		}
	}
	if ((listInputFile.size() == 0) || confFile == null || outFileName == null){
		System.out.println("Wrong parameters inserted!");
		System.out.println("LogAnalysis3 -i  input1.log input2.log input 3.log -c debug.conf -o debug_out.log");
		System.exit(0);
	}
	
	LogAnalysis3 la = new LogAnalysis3();
	new ArrayList<ThreadPacks>();	
		
	List<FeaturePacks> listFp;
	// String[] featureNameArr = {"etdz","SessionExpireException","状态机状态->Error"};
	ConfigureInfo cfi = la.readConfFile(confFile);
	List<String> confLines = cfi.getListFeatureTypeLine();

	Collection<ThreadPacks> listTp  = la.parseFile(listInputFile,cfi.getKeyBlockDelimiter()).values();
	
	File outTotalCountFile = new File(outFileName + "_totalCount.log");
	try{
	BufferedWriter bw = new BufferedWriter(new FileWriter(outTotalCountFile));
	bw.write("--------------------Total value for All feature types-------------------");
	bw.newLine();
	bw.close();
	}catch (IOException e){
		e.fillInStackTrace();
	}
	
	for (int j = 0; j < confLines.size(); j++){	
		File outFile = new File(outFileName + j + ".log");
		String[] featureNameArr = confLines.get(j).split("@@");
		listFp = la.errorFilter(listTp, featureNameArr);
		TotalCountValue tcv = la.countResult(listFp,featureNameArr);
		la.printResult(listTp,listFp,featureNameArr,tcv,outFile);
		la.PrintTotal(featureNameArr,tcv,outTotalCountFile);
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
	
	public int getRcordPackNum(){
		return this.listRecordPack.size();
	}
	
}

class ThreadPacks{
	private String threadPackId;
	private List<BlockPacks> listBlockPack= new ArrayList<BlockPacks>();
	
	public ThreadPacks(String threadPackId){
		this.threadPackId =threadPackId;
	}
	
	/*public ThreadPacks(String threadPackId, BlockPacks blockPack){
		this.threadPackId = threadPackId;
		this.listBlockPack.add(blockPack);
	}*/
	
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
	
	public int getBlockNum(){
		return this.listBlockPack.size();
	}
	
	public BlockPacks getSpecificBlockPack(int i){
		return this.listBlockPack.get(i);
	}
}

class FeaturePacks{
	private String threadId;
	private 	List<String> listFeature = new ArrayList<String>();
	private List<String> listFeatureBlockId = new ArrayList<String>();
	private List<Integer> listFeatureNum = new ArrayList<Integer>();
	
	FeaturePacks(){
		
	}
	
	FeaturePacks(String threadId, List<String> listFeature,List<Integer> listFeatureNum){
		this.threadId = threadId;
		this.listFeature =listFeature;
		this.listFeatureNum = listFeatureNum;
	}
	
	public void setThreadId(String threadId){
		this.threadId = threadId;
	}
	
	public String getThreadId(){
		return this.threadId;
	}
	
	public List<String> getListFeature(){
		return this.listFeature;
	}
	
	public void setListFeature(List<String> listFeature){
		this.listFeature = listFeature;
	}
	
	public List<Integer> getListFeatureNum(){
		return this.listFeatureNum;
	}
	
	public void setListFeatureNum(List<Integer> listFeatureNum){
		this.listFeatureNum = listFeatureNum;
	}
	
	public void setListFeatureBlock(List<String> listFeatureBlock){
		this.listFeatureBlockId = listFeatureBlock;
	}
	
	public List<String> getListFeatureBlockId(){
		return this.listFeatureBlockId;
	}
}

class TotalCountValue{
	private String percentageOfSpecifcNum;
	private List<String> totalListFeature = new ArrayList<String>();
	private List<Integer> totalListFeatureNum = new ArrayList<Integer>();
	private List<String> listPercentageOfSpecifcType = new ArrayList<String>();
	private List<String> listPercentageOfAllType = new ArrayList<String>();
	
	TotalCountValue(){
		
	}
	
	TotalCountValue(List<String> tlf,List<Integer> tlfn, List<String> lpost, List<String> lpoat){
		this.totalListFeature = tlf;
		this.totalListFeatureNum = tlfn;
		this.listPercentageOfSpecifcType = lpost;
		this.listPercentageOfAllType = lpoat;
	}
	
	TotalCountValue(String posn,List<String> tlf,List<Integer> tlfn, List<String> lpost, List<String> lpoat){
		this.percentageOfSpecifcNum = posn;
		this.totalListFeature = tlf;
		this.totalListFeatureNum = tlfn;
		this.listPercentageOfSpecifcType = lpost;
		this.listPercentageOfAllType = lpoat;
	}
	
	public String getPercentageOfSpecificNum(){
		return this.percentageOfSpecifcNum;
	}
	
	public void setPercentageOfSpecificNum(String posn){
		this.percentageOfSpecifcNum = posn;
	}
	
	public void setTotalListFeature(List<String> totalListFeature){
		this.totalListFeature = totalListFeature;
	}
	
	public List<String> getTotalListFeature(){
		return this.totalListFeature;
	}
	
	public void setTotalListFeatureNum(List<Integer> totalListFeatureNum){
		this.totalListFeatureNum = totalListFeatureNum;
	}
	
	public List<Integer> getTotalListFeatureNum(){
		return this.totalListFeatureNum;
	}
	
	public void setListPercentageOfSpecifcType(List<String> listPercentageOfSpecifcType){
		this.listPercentageOfSpecifcType = listPercentageOfSpecifcType;
	}
	
	public List<String> getListPercentageOfSpecificType(){
		return this.listPercentageOfSpecifcType;
	}
	
	public void setListPercentageOfAllType(List<String> listPercentageOfAllType){
		this.listPercentageOfAllType = listPercentageOfAllType;
	}
	
	public List<String> getListPercentageOfAllType(){
		return this.listPercentageOfAllType;
	}
}

class ConfigureInfo{
	private  List<String> listFeatureTypeLine = new ArrayList<String>();
	private  String blockDelimiter;
	
	private ConfigureInfo(){
	}
	
	ConfigureInfo(String keyBlockDelimiter, List<String> listFeatureTypeLine){
		this.blockDelimiter = keyBlockDelimiter;
		this.listFeatureTypeLine = listFeatureTypeLine;
	}
	public  List<String> getListFeatureTypeLine(){
		return this.listFeatureTypeLine;
	}
	
	public String getKeyBlockDelimiter(){
		return this.blockDelimiter;
	}
}

