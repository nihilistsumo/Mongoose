package edu.unh.cs.treccar.proj.rlib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.lucene.queryparser.classic.ParseException;

import edu.unh.cs.treccar.proj.util.DataUtilities;
import edu.unh.cs.treccar.proj.util.MongooseHelper;
import edu.unh.cs.treccar.proj.util.ParaPairData;

public class RLibFileWriterForCluster {
	
	public static final String PROP_FILE = "project.properties";
	public Properties pr;
	public MongooseHelper mh;
	//public HashMap<String, ArrayList<ParaPairData>> similarityData;
	
	public RLibFileWriterForCluster(){
		this.pr = new Properties();
		try {
			this.pr.load(new FileInputStream(new File(RLibFileWriterForCluster.PROP_FILE)));
			this.mh = new MongooseHelper(this.pr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RLibFileWriterForCluster(MongooseHelper m){
		this.mh = m;
		this.pr = new Properties();
		try {
			this.pr.load(new FileInputStream(new File(RLibFileWriterForCluster.PROP_FILE)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void processParaForRlib() throws IOException, ParseException{
		this.mh.saveParaSimilarityData(this.mh.processParaPairData(DataUtilities.getGTMapQrels(
				this.pr.getProperty("data-dir")+"/"+this.pr.getProperty("art-qrels"))),
				this.pr.getProperty("out-dir")+"/"+this.pr.getProperty("sim-data-out"));
	}
	
	public void writeFeatureFile(){
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File(this.pr.getProperty("out-dir")+"/"+this.pr.getProperty("sim-data-out"))));
			HashMap<String, ArrayList<ParaPairData>> similarityData = (HashMap<String, ArrayList<ParaPairData>>) ois.readObject();
			ois.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new File(this.pr.getProperty("out-dir")+"/"+this.pr.getProperty("rlib-out"))));
			ArrayList<ParaPairData> ppdList;
			ArrayList<ArrayList<String>> trueClusters;
			for(String page:similarityData.keySet()){
				ppdList = similarityData.get(page);
				trueClusters = DataUtilities.getGTClusters(page, this.pr.getProperty("data-dir")+"/"+this.pr.getProperty("top-qrels"));
				String para1, para2, rel, fetLine;
				ArrayList<Double> scores;
				for(ParaPairData ppd:ppdList){
					rel = "";
					fetLine = "";
					para1 = ppd.getParaPair().getPara1();
					para2 = ppd.getParaPair().getPara2();
					for(ArrayList<String> cluster:trueClusters){
						if(cluster.contains(para1)){
							if(cluster.contains(para2))
								rel = "1";
							else
								rel = "0";
							break;
						}
					}
					if(rel.equals("")){
						for(ArrayList<String> cluster:trueClusters){
							if(cluster.contains(para2)){
								rel = "0";
								break;
							}
						}
					}
					if(rel.equals(""))
						continue;
					fetLine+=rel+" qid:"+para1;
					scores = ppd.getSimScoreList();
					int fetCount = 1;
					for(double score:scores){
						fetLine+=" "+fetCount+":"+score;
						fetCount++;
					}
					fetLine+=" #"+para2+"\n";
					System.out.print(fetLine);
					bw.write(fetLine);
				}
			}
			bw.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RLibFileWriterForCluster rlib = new RLibFileWriterForCluster();
		try {
			rlib.processParaForRlib();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rlib.writeFeatureFile();
	}

}
