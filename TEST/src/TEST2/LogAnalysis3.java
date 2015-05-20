package TEST2;
/**
 * This program analysis the log reprots
 * @author tangmi
 *
 */
import java.util.*;
import java.io.*;

public class LogAnalysis3 {
	private static List<String[]> listFeatureNameArr = new ArrayList<String[]>();
	
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
	
	private static class FeatureNumStorage {
		public Map<String,List<int[]>> packs = new HashMap<String,List<int[]>>();
		
		public List<int[]> getAndRefreshListNumArr(String threadId){
			return packs.remove(threadId);
		}
		
		public Map<String,List<int[]>>  getMapPack(){
			return this.packs;
		}
		
		public List<int[]> addNumArrByThreadId(String threadId, List<int[]> listNumArr){
			List<int[]> last = packs.get(threadId);
			if (last != null){
				for (int i = 0; i < last.size(); i++){
					for (int j = 0; j < last.get(i).length; j++){
						last.get(i)[j] += listNumArr.get(i)[j];
					}
				}
			}else{
				last = new ArrayList<int[]>();
				for(int[] is:listNumArr) {
					last.add(is.clone());
				}
				packs.put(threadId, last);
			}
			return last;
		}
	}
	
	private int[] countFeatureTypeArrByOneLine(String str,String[] featureTypeName){
		int[] featureNumArr = new int[featureTypeName.length];
		for (int i = 0; i < featureTypeName.length;i++){
			if (str.indexOf(featureTypeName[i]) != -1) {
				featureNumArr[i] = 1;
			}
		}
		return featureNumArr;
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
				try(BufferedReader br = new BufferedReader(new FileReader(listInputFile.get(fileIndex)))) {
					fileIndex++;
					
					List<int[]> listOfLastFeatureNumArr = new ArrayList<int[]>();
					

					
					FeatureNumStorage fns = new FeatureNumStorage();
					String lastThreadId = null;
					
					while ((str = br.readLine()) != null ) {
						List<int[]> listOfNewFeatureNumArr = new ArrayList<int[]>();
						for (int i = 0;i < listFeatureNameArr.size(); i ++){	
							int[] newFeatureNumArr = countFeatureTypeArrByOneLine(str,listFeatureNameArr.get(i));
							listOfNewFeatureNumArr.add(newFeatureNumArr);
						}
						if (str.startsWith("[")) {    // if it's the real record with [timestamp] field
							RecordPacks recordPack = new RecordPacks(str);
							recordPack.setThreadId(recordPack.getThreadIdLable());
						
							isNewBlock =  (str.matches(FLAG1)) ;
							// find the correct ThreadPacks index
							ThreadPacks tps=ths.getPack(recordPack.getThreadId());
	
							// If the threadPack with certain threadId is existed 
							//if(tps.getBlockNum()>0) {
							//if(tps.getListFeaturePack().size() > 0){
							//If it's not a new block 
							if (!isNewBlock){
								// add the recordPack to the specific threadPack's last block
								//tps.getLastBlockPack().addRecordPack(recordPack);

								listOfLastFeatureNumArr = fns.addNumArrByThreadId(recordPack.getThreadId(), listOfNewFeatureNumArr);
								
							}else{   // If it's a new block
								List<String> listOfFeatureType = new ArrayList<String>();
								listOfLastFeatureNumArr = fns.getAndRefreshListNumArr(recordPack.getThreadId());
								if (listOfLastFeatureNumArr != null){
									for (int i = 0; i < listFeatureNameArr.size(); i++){
										String featureType = "";
										for (int j = 0; j < listFeatureNameArr.get(i).length; j ++){
											featureType += listOfLastFeatureNumArr.get(i)[j] + " ";
										}
										listOfFeatureType.add(featureType);
										if (tps.getListFeaturePack().size() == listFeatureNameArr.size()){
											tps.getListFeaturePack().get(i).addSpecificFeatureNum(featureType, 1);
										}else {
											tps.getListFeaturePack().add(new FeaturePacks(featureType,1));
										}
									}
								}
								
								listOfLastFeatureNumArr=fns.addNumArrByThreadId(recordPack.getThreadId(),listOfNewFeatureNumArr);
								// Create a new blockPack object, and add the current recordPack to it
								//BlockPacks newBp = new BlockPacks(recordPack);
								// Add the new blockPack into the specific ThreadPack
								//tps.addBlockPack(newBp);
								
							}		
							//}else{ //If the threadPack with certain threadId isn't existed 
								// Create a new BlockPack object and add the current recordPack to it
								// BlockPacks newBp = new BlockPacks(recordPack);
								//tps.addBlockPack(newBp);
								
							//}
							
						}else {   // the record just read is not the first line
							// if there is no ThreadPacks object created before, just withdraw the record just read
							if(ths.last==null) {
								break;
							} else {
								//ths.last.getLastBlockPack().getLastRecordPack().addRecordLine(str);
								listOfLastFeatureNumArr=fns.addNumArrByThreadId(ths.last.getThreadPackId(),listOfNewFeatureNumArr);
							}
						}
					}
					for (Map.Entry<String,List<int[]>> ety:fns.getMapPack().entrySet()){
						List<String> listOfFeatureType = new ArrayList<String>();
						ThreadPacks tps=ths.getPack(ety.getKey());
						listOfLastFeatureNumArr = ety.getValue();
						if (listOfLastFeatureNumArr != null){
							for (int i = 0; i < listFeatureNameArr.size(); i++){
								String featureType = "";
								for (int j = 0; j < listFeatureNameArr.get(i).length; j ++){
									featureType +=  listOfLastFeatureNumArr.get(i)[j] + " ";
								}
								listOfFeatureType.add(featureType);
								if (tps.getListFeaturePack().size() == listFeatureNameArr.size()){
									tps.getListFeaturePack().get(i).addSpecificFeatureNum(featureType, 1);
								}else {
									tps.getListFeaturePack().add(new FeaturePacks(featureType,1));
								}
							}
						}
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return ths.packs;
	}
	
/**
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
	/**
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

*/
	

	private List<List<String>> countTotalResult(Collection<ThreadPacks> listTp){

		List<List<String>> totalCountResult = new ArrayList<List<String>>();
		
		for ( int i = 0; i < listFeatureNameArr.size(); i++){
			List<String> listTotalFeatureTypeNum = new ArrayList<String>();
			Map<String,Integer> featureTypeNum = new HashMap<String,Integer>();
			int totalNum = 0;
			int totalNumForSpecific = 0;
			for (ThreadPacks tp:listTp){
				for (Map.Entry<String,Integer> ep:tp.getListFeaturePack().get(i).getFparks().entrySet()){
					if (featureTypeNum.get(ep.getKey()) == null){
						featureTypeNum.put(ep.getKey(),ep.getValue());
					}else{
						featureTypeNum.put(ep.getKey(),featureTypeNum.get(ep.getKey()) + ep.getValue());
					}
				}
			}
			for (Map.Entry<String,Integer> ep:featureTypeNum.entrySet()){
				totalNum += ep.getValue();
				if (!ep.getKey().startsWith("0")){
					totalNumForSpecific += ep.getValue();
				}
			}
			String firstLine = "Specific PCT = " + String.format("%4.2f", (Double.valueOf(totalNumForSpecific) / Double.valueOf(totalNum)) * 100.0);
			listTotalFeatureTypeNum.add(firstLine);
			
			for (Map.Entry<String, Integer> ep:featureTypeNum.entrySet()){
				String strLine = "";
				String[] strArr = ep.getKey().split("\\s+");
				for (String str:strArr){
					strLine += String.format("%10d", Integer.valueOf(str));
				}
				strLine += String.format("%10d",ep.getValue());
				strLine += String.format("%10.2f", (Double.valueOf(ep.getValue()) / Double.valueOf(totalNum) )* 100.0);
				if (!ep.getKey().startsWith("0")){
					strLine += String.format("%10.2f",(Double.valueOf(ep.getValue()) / Double.valueOf(totalNumForSpecific)) * 100.0 );
				}
				listTotalFeatureTypeNum.add(strLine);
			}

			
			totalCountResult.add(listTotalFeatureTypeNum);
		}
		return totalCountResult;
	}
	
	
	

	private void printResult(List<List<String>> totalResult, List<File> listFile, Collection<ThreadPacks> listTp) throws IOException {
		for (int indexOutFile = 0; indexOutFile < listFile.size(); indexOutFile++){
			File file = listFile.get(indexOutFile);
			List<String> thisTotalResult = totalResult.get(indexOutFile);
			String[] featureType = listFeatureNameArr.get(indexOutFile);
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
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
				
				for (ThreadPacks tp:listTp){
					bw.write("TheradId = " + tp.getThreadPackId());
					bw.newLine();
					
					for (int i = 0; i < featureType.length; i++){
						bw.write("    " + "Lable"+ i);
					}
					bw.write("    number");
					bw.newLine();
					
					for (Map.Entry<String,Integer> fp:tp.getListFeaturePack().get(indexOutFile).getFparks().entrySet()){
						String[] strArr = fp.getKey().split("\\s+");
						String s = "";
						for (String str:strArr){
							s += String.format("%10d",Integer.valueOf(str));
						}
						s += String.format("%10d",fp.getValue());
						bw.write(s);
						bw.newLine();
					}
					
					bw.write("-----------------------------------------------------------------------");
					bw.newLine();
					
				}

				bw.write(" -----------------------Total number------------------------------ ");
				bw.newLine();
				bw.write(thisTotalResult.get(0));
				bw.newLine();
				
				for(int i = 0; i < featureType.length; i++){
					bw.write("    " + "Lable" + i);
				}
				bw.write("    number   PCT1(%)   PCT2(%)");
				bw.newLine();
				
				for (int i = 1; i < thisTotalResult.size(); i++){
					bw.write(thisTotalResult.get(i));
					bw.newLine();
				}
			}
		}
	}
	
	private void printTotal(List<List<String>> totalResult, File file) throws IOException{
	//private void PrintTotal(String[] featureType,TotalCountValue tcv,File file){
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

			for (int indexOfFile = 0; indexOfFile < listFeatureNameArr.size(); indexOfFile++){
				String[] featureType = listFeatureNameArr.get(indexOfFile);
				List<String> thisTotalResult = totalResult.get(indexOfFile);
				
				bw.write("--------------------Total value for All feature types-------------------");
				bw.newLine();
				String strLine = " ------------------------------------------------------------------------";
				bw.write(strLine);
				bw.newLine();
				
				for (int i = 0; i < featureType.length; i ++){
					bw.write("Lable" + i + ": " + featureType[i]);
					bw.newLine();
				}
				
				bw.write(strLine);
				bw.newLine();
				
				bw.write(" -----------------------Total number------------------------------ ");
				bw.newLine();
				bw.write(thisTotalResult.get(0));
				bw.newLine();
				
				for (int i = 0; i < featureType.length; i ++){
					bw.write("    Lable" + i);
				}
				
				bw.write("    number   PCT1(%)   PCT2(%)");
				bw.newLine();
				
				for (int i = 1; i < thisTotalResult.size(); i++){
					bw.write(thisTotalResult.get(i));
					bw.newLine();
				}
			
			}
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
	
	public static void main(String[] args) {
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
			
	
		// String[] featureNameArr = {"etdz","SessionExpireException","状态机状态->Error"};
		ConfigureInfo cfi = la.readConfFile(confFile);
		
		List<String> confLines = cfi.getListFeatureTypeLine();
		for (int j=0; j  < confLines.size(); j++){
			listFeatureNameArr.add(confLines.get(j).split("@@"));
		}
		Collection<ThreadPacks> listTp  = la.parseFile(listInputFile,cfi.getKeyBlockDelimiter()).values();
		
		File outTotalCountFile = new File(outFileName + "_totalCount.log");
		try{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(outTotalCountFile))){
				bw.write("--------------------Total value for All feature types-------------------");
				bw.newLine();
			}
		}catch (IOException e){
			e.fillInStackTrace();
		}
		
		List<File> listOutFile = new ArrayList<File>();
		for (int j = 0; j < confLines.size(); j++){
			File outFile = new File(outFileName + j + ".log");
			listOutFile.add(outFile);
		}
		
		List<List<String>> totalResult = la.countTotalResult(listTp);
		try{
			la.printResult(totalResult, listOutFile, listTp);
			la.printTotal(totalResult, outTotalCountFile);
		}catch(IOException e){
			e.printStackTrace();
		}
	/**	
		for (int j = 0; j < confLines.size(); j++){	
			File outFile = new File(outFileName + j + ".log");
			String[] featureNameArr = confLines.get(j).split("@@");
	       //listFp = la.errorFilter(listTp, featureNameArr);
			TotalCountValue tcv = la.countResult(listFp,featureNameArr);
			la.printResult(listTp,listFp,featureNameArr,tcv,outFile);
			la.PrintTotal(featureNameArr,tcv,outTotalCountFile);
		}
	}
	*/	
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

	/**
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
*/	
	class ThreadPacks{
		private String threadPackId;
		//private List<BlockPacks> listBlockPack= new ArrayList<BlockPacks>();
		//private BlockPacks lastBlock = null;
		private List<FeaturePacks> listFeaturePack = new ArrayList<FeaturePacks>();
		
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
		
		public List<FeaturePacks> getListFeaturePack(){
			return this.listFeaturePack;
		}
		
		public void addFeaturePack(FeaturePacks fp){
			this.listFeaturePack.add(fp);
		}
		/**	
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
	 	*/
	}

	class FeaturePacks{
		// private List<String> listFeature = new ArrayList<String>();
		// private List<String> listFeatureBlockId = new ArrayList<String>();
		// private List<Integer> listFeatureNum = new ArrayList<Integer>();
		private Map<String,Integer> fparks = new HashMap<String,Integer>();
		
		FeaturePacks(){
			
		}
		/**
		FeaturePacks(String threadId, List<String> listFeature,List<Integer> listFeatureNum){
			this.threadId = threadId;
			this.listFeature =listFeature;
			this.listFeatureNum = listFeatureNum;
		}
		*/
		
		FeaturePacks(String featureType, int featureNum){
			this.fparks.put(featureType, featureNum);
		}
	
		public int getSpecificFeatureNum(String featureType){
			return this.fparks.get(featureType);
		}
		
		public void setSpecificFeatureNum(String featureType,int featureNum){
			this.fparks.put(featureType, featureNum);
		}
		
		public Map<String,Integer> getFparks(){
			return this.fparks;
		}
		
		public void addSpecificFeatureNum(String featureType,int featureNum){
			if( this.fparks.get(featureType) != null) {
				this.fparks.put(featureType, this.fparks.get(featureType) + featureNum);
			} else{
				this.fparks.put(featureType,featureNum);
			}
			
		}
		/**
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
		*/
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


