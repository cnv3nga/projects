package com.travelzen.etermface.test;
/**
 * 
 * @author tangmi
 *
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StressTest {
	
	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	
	/* class for Statistic collections*/
	private static class Statistic{
		private int taskCounter,errorCounter,totalTaskCounter;
		private double totalInboundThroughoutMount;
		private double inboundThroughoutMount;
		private double totalOutboundThroughoutMount;
		private double outboundThroughoutMount;
		private int maxTaskTime = 0,minTaskTime = Integer.MAX_VALUE;
		private int totalTaskTime = 0;
		private static Statistic instance;
		
		public Statistic(){
		}
		
		public synchronized void resetStatistic(){
			totalTaskCounter += taskCounter;
			taskCounter = 0;
			totalInboundThroughoutMount += inboundThroughoutMount;
			totalOutboundThroughoutMount += outboundThroughoutMount;
			inboundThroughoutMount = 0;
			outboundThroughoutMount = 0;
		}
		
		public int getTaskCounter(){
			return taskCounter;
		}
		
		public int getTotalTaskCounter(){
			return totalTaskCounter;
		}
		
		public double getInboundThroughoutMount(){
			return inboundThroughoutMount;
		}
		
		public double getOutboundThroughoutMount(){
			return outboundThroughoutMount;
		}
		
		public double getTotalInboundThroughoutMount(){
			return totalInboundThroughoutMount;
		}
		
		public double getTotalOutboundThroughoutMount(){
			return totalOutboundThroughoutMount;
		}
		
		public synchronized void addTaskCounter(){
			taskCounter++;
		}
		
		public synchronized void addErrorCounter(){
			errorCounter++;
		}
		
		public synchronized void addTaskTime(int taskTime){
			totalTaskTime += taskTime;
		}
		
		public synchronized void addThroughoutMount(double newInboundMount,double newOutboundMount){
			inboundThroughoutMount += newInboundMount;
			outboundThroughoutMount += newOutboundMount;
		}
		
		public synchronized void setTime(int threadTime){
			if (threadTime > maxTaskTime){
				maxTaskTime = threadTime;
			}
			if(threadTime < minTaskTime){
				minTaskTime = threadTime;
			}
			addTaskTime(threadTime);
			
			if (threadTime == 0) {
				System.out.println("Thread " + Thread.currentThread().getName() + " running time " + threadTime);
			}
		}
		
		
		public int getMaxTaskTime(){
			return this.maxTaskTime;
		}
		
		public int GetMinTaskTime(){
			return this.minTaskTime;
		}
		public static Statistic getInstance(){
			if (instance == null){
				synchronized (Statistic.class){
					if (instance == null){
						instance = new Statistic();
					}
				}
			}
			return instance;
		}
	}
	
	/* Testing Report class */
	private static class Report{
		private static ReportContent rc;
		private static AverageReport avgReport;
		
		public Report(int interval){
			executeFixedRate(interval);
		}
		
		private static class AverageReport{
			private double aveThroughout;
			private double aveTaskTime;
			private double aveConcurrency;
			
			public void setAveThroughout(int totalTime,double totalThoughout){
				this.aveThroughout = (totalThoughout / totalTime);
			}
			
			public void setAveTaskTime(int totalTaskNumber,int totalTaskTime){
				this.aveTaskTime = ((double)totalTaskTime / totalTaskNumber);
			}
			
			public void setAveConcurrency(int totalRunTime, int totalTaskNumber){
				this.aveConcurrency = ((double)totalTaskNumber / totalRunTime);
			}
			
			public double getAveThroughout(){
				return this.aveThroughout;
			}
			
			public double getAveTaskTime(){
				return this.aveTaskTime;
			}
			
			public double getAveConcurrency(){
				return this.aveConcurrency;
			}
		}
		
		private static class ReportContent{
			int totalRunningTime,maxTaskTime = 0,minTaskTime = Integer.MAX_VALUE; // 总运行时间，最大、最小响应时间
			double maxInboundThroughoutMount = 0,minInboundThroughoutMount = Double.MAX_VALUE; // 最大、最小吞吐量
			double maxOutboundThroughoutMount = 0,minOutboundThroughoutMount = Double.MAX_VALUE;
			double totalThroughoutMount;
			int maxConcurrency = 0,minConcurrency = Integer.MAX_VALUE;
			static ReportContent instance = null;
			
			/*
			public void runFinally(int totalTime){
				this.totalRunningTime = totalTime;
				
			}
			*/
			
			public AverageReport getAverageReport(int totalRunTime,int totalCount,int totalTaskTime,double totalThroughout){
				AverageReport aveReport = new AverageReport();
				this.totalRunningTime = totalRunTime;
				aveReport.setAveConcurrency(totalRunTime, totalCount);
				aveReport.setAveTaskTime(totalCount, totalTaskTime);
				aveReport.setAveThroughout(totalRunTime,totalThroughout);
				return aveReport;
			}
			
			public static ReportContent getInstance(){
				if (instance == null){
					instance = new ReportContent();
				}
				return instance;
			}
			
			public void setTaskTime(int maxTm,int minTm){
				if (maxTm > this.maxTaskTime){
					this.maxTaskTime = maxTm;
				}
				if (minTm < this.minTaskTime){
					this.minTaskTime = minTm;
				}
			}
			
			public void setConcurrency(int concurrency){
				if (concurrency > this.maxConcurrency){
					this.maxConcurrency = concurrency;
				}
				if (concurrency < this.minConcurrency){
					this.minConcurrency = concurrency;
				}
			}
			
			public void setThroughoutMount(double inboundThroughout,double outboundThroughOut){
				if (inboundThroughout > maxInboundThroughoutMount){
					maxInboundThroughoutMount = inboundThroughout;
				}
				
				if (outboundThroughOut > maxOutboundThroughoutMount){
					maxOutboundThroughoutMount = outboundThroughOut;
				}
				
				if (inboundThroughout < minInboundThroughoutMount){
					minInboundThroughoutMount = inboundThroughout;
				}
				
				if (outboundThroughOut < minOutboundThroughoutMount){
					minOutboundThroughoutMount = outboundThroughOut;
				}
			}
					
			public int getTotalRunningTime(){
				return this.totalRunningTime;
			}
			
			public int getMinTaskTime(){
				return this.minTaskTime;
			}
			
			public int getMaxTaskTime(){
				return this.maxTaskTime;
			}
			
			public int getMinConcurrency(){
				return this.minConcurrency;
			}
			
			public int getMaxConcurrency(){
				return this.maxConcurrency;
			}
			
			public double getMaxInboundThroughoutMount(){
				return this.maxInboundThroughoutMount;
			}
			
			public double getMinInboundThroughoutMount(){
				return this.minInboundThroughoutMount;
			}
			
			public double getMaxOutboundThroughoutMount(){
				return this.maxOutboundThroughoutMount;
			}
			
			public double getMinOutboundThroughoutMount(){
				return this.minOutboundThroughoutMount;
			}
			
			/*
			public void setMaxInboundThroughoutMount(double maxInboundThroughoutMount){
				this.maxInboundThroughoutMount = maxInboundThroughoutMount;
			}
			
			public void setMinInboundThroughoutMount(double minInboundThroughoutMount){
				this.minInboundThroughoutMount = minInboundThroughoutMount;
			}
			
			
			public void setMaxOutboundThroughoutMount(double maxOutboundThroughoutMount){
				this.maxOutboundThroughoutMount = maxOutboundThroughoutMount;
			}
			
			
			public void setMinOutboundThroughoutMount(double minOutboundThroughoutMount){
				this.minOutboundThroughoutMount = minOutboundThroughoutMount;
			}
			*/
			
		}
		
		private class RunInFixedRate implements Runnable{
			private Statistic st;
			public RunInFixedRate(Statistic st){
				this.st = st;
			}
			public void run(){
				ReportContent.getInstance().setTaskTime(st.getMaxTaskTime(),st.GetMinTaskTime());
				ReportContent.getInstance().setConcurrency(st.getTaskCounter());
				ReportContent.getInstance().setThroughoutMount(st.getInboundThroughoutMount(),st.getOutboundThroughoutMount());
				ReportContent.getInstance().totalThroughoutMount += (st.getInboundThroughoutMount() + st.getOutboundThroughoutMount());
				st.resetStatistic();
			}
		}
			
		public void executeFixedRate(int interval){
			executor.scheduleAtFixedRate(new RunInFixedRate(Statistic.getInstance()),interval,interval,TimeUnit.SECONDS);
		}
		
		public void stopExecution(){
			executor.execute(new RunInFixedRate(Statistic.getInstance()));
			executor.shutdown();
		}
		
		public ReportContent getReportContent(){
			return rc.getInstance();
		}
		
		public void buildReport(String filename) throws IOException{
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(filename))){
				String SeperateLine = "--------------------------------------------------------";
				bw.write("                   Stress Testing Report                 ");
				bw.newLine();
				bw.write(SeperateLine);
				bw.newLine();
				bw.write("Time(millisecond) :");
				bw.newLine();
				bw.write("-------------------");
				bw.newLine();
				bw.write("total running time : " + String.format("%-10d",ReportContent.getInstance().getTotalRunningTime()));
				bw.write("min task time : " + String.format("%-10d",ReportContent.getInstance().getMinTaskTime()));
				bw.write("max task time : " + String.format("%-10d",ReportContent.getInstance().getMaxTaskTime()));
				bw.newLine();
				bw.write("average task time : " + String.format("%10.2f",avgReport.getAveTaskTime()) );
				bw.newLine();
				bw.write(SeperateLine);
				bw.newLine();
				bw.write("ThroughoutMount(KiloByte)");
				bw.newLine();
				bw.write("-------------------------");
				bw.newLine();
				bw.write("total inbound throughout mount :  " + String.format("%-10.2f",Statistic.getInstance().getTotalInboundThroughoutMount()));
				bw.write("total outbound throughout mount : " + String.format("%-10.2f",Statistic.getInstance().getTotalOutboundThroughoutMount()));
				bw.newLine();
				bw.write("max inbound throughout mount :    " + String.format("%-10.2f",ReportContent.getInstance().getMaxInboundThroughoutMount()));
				bw.write("max outbound throughout mount :   " + String.format("%-10.2f", ReportContent.getInstance().getMaxOutboundThroughoutMount()));
				bw.newLine();
				bw.write("min inbound throughout mount :    " + String.format("%-10.2f", ReportContent.getInstance().getMinInboundThroughoutMount()));
				bw.write("min outbound throughout mount :   " + String.format("%-10.2f",ReportContent.getInstance().getMinOutboundThroughoutMount()));
				bw.newLine();
				bw.write("average throughout mount :        " + String.format("%-10.2f",avgReport.getAveThroughout()));
				bw.newLine();
				bw.write(SeperateLine);
				bw.newLine();
				bw.write("Concurrency ");
				bw.newLine();
				bw.write("------------");
				bw.newLine();
				bw.write("total task number : " + String.format("%-10d", Statistic.getInstance().getTotalTaskCounter()));
				bw.write("min concurrency : " + String.format("%-10d", ReportContent.getInstance().getMinConcurrency()));
				bw.write("max concurrency : " + String.format("%-10d",ReportContent.getInstance().getMaxConcurrency()));
				bw.newLine();
				bw.write("average concurrency : " + String.format("%-10.2f",avgReport.getAveConcurrency()));
			}
			
		}

		public void calculateAvg(int totalRunTime,int totalCount) {
			Statistic st = Statistic.getInstance();
			int totalTaskTime = st.totalTaskTime;
			double totalThroughout = st.getInboundThroughoutMount() + st.getOutboundThroughoutMount();
			avgReport = ReportContent.getInstance().getAverageReport(totalRunTime, totalCount, totalTaskTime, totalThroughout);
			
		}
	}
	
	private static class ThreadController{
		private int taskCount;
		private AnalysisTask[] threadPool;
		
		
		public ThreadController(int nThread,int count){
			this.taskCount = count;
			this.threadPool = new AnalysisTask[nThread];
			
			for (int i = 0; i < threadPool.length; i++){
				this.threadPool[i] = new AnalysisTask(); 
				this.threadPool[i].start();
			}
			
		}
		
		public void mainWait(){
			for (AnalysisTask at:threadPool){
				try{
					at.join();
				}catch(InterruptedException e){
					e.printStackTrace();
					throw new RuntimeException();
				}
			}
		}
		
		private class AnalysisTask extends Thread{
			private RequestOfCallTask req;
			
			public void run(){
				while(taskCount > 0){
					synchronized(AnalysisTask.class){
						taskCount--;
					}
					Statistic.getInstance().addTaskCounter();
					try{
						req = callFunction();
					}catch(Exception e){
						Statistic.getInstance().addErrorCounter();
					}
					Statistic.getInstance().addThroughoutMount(req.getInboundThroughoutCapacity(), req.getOutboundThroughoutCapacity());
					Statistic.getInstance().setTime(req.getRequestProcessTime());
				}
			}
			
			public RequestOfCallTask callFunction(){
				req = new RequestOfCallTask((Math.random() * 1000),(Math.random() * 2000));
				try{
					Thread.sleep(1200);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				req.setRequestProcessTime(System.currentTimeMillis());
				return req;
			}
		
		
			private class RequestOfCallTask{
				private double inboundThroughoutCapacity;
				private double outboundThroughoutCapacity;
				private long requestStartTime;
				private int requestProcessTime;
				
				/*
				public RequestOfCallTask(){
					this.inboundThroughoutCapacity = 0;
					this.outboundThroughoutCapacity = 0;
					this.requestStartTime = System.currentTimeMillis();
				}
				*/
				
				public RequestOfCallTask(double itc,double  otc){
					this.inboundThroughoutCapacity = itc;
					this.outboundThroughoutCapacity = otc;
					this.requestStartTime = System.currentTimeMillis();
				}
				
				public void setRequestProcessTime(long requestEndTime){
					this.requestProcessTime = (int)(requestEndTime - this.requestStartTime);
				}
				
				public double getInboundThroughoutCapacity(){
					return this.inboundThroughoutCapacity;
				}
				
				public double getOutboundThroughoutCapacity(){
					return this.outboundThroughoutCapacity;
				}
				
				public int getRequestProcessTime(){
					return this.requestProcessTime;
				}
			}
		}
	}
		

	
	public static void main(String[] args){
		String filename = "Stress_testing_report.txt";
		int nThread = 5,nRequest = 50;
		int interval = 3;
		long startRunTime = System.currentTimeMillis();
		Report report = new Report(interval);                           // begin the monitoring in interval 
		ThreadController tc = new ThreadController(nThread,nRequest);   // begin the simulating stress testing 
		tc.mainWait();                                                  // Hold the main thread to waiting for all threads finish their work
		int totalRunTime = (int)(System.currentTimeMillis() - startRunTime);
		report.stopExecution();                                         // Run the final try of monitoring and Stop the monitor
		report.calculateAvg(totalRunTime,nRequest);
		try{
			report.buildReport(filename);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
