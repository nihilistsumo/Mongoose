package edu.unh.cs.treccar.proj.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import edu.unh.cs.treccar.proj.util.ParaPairData;

public class CustomKMeansSimilarity {
	
	public HashMap<String, ArrayList<String>> clusterData;
	public HashMap<HashSet<String>, ArrayList<Double>> clusterPairData;
	public Properties prop;
	public String pageID;
	public int secNo;
	ArrayList<String> paraIDList;
	ArrayList<ParaPairData> ppds;
	ArrayList<String> secids;
	double[] wVec;
	
	public CustomKMeansSimilarity(Properties p, String pID, double[] w, ArrayList<String> sectionIDs, ArrayList<String> paraIDs,
			ArrayList<ParaPairData> ppdList){
		this.prop = p;
		this.pageID = pID;
		this.secNo = sectionIDs.size();
		this.paraIDList = paraIDs;
		this.ppds = ppdList;
		this.wVec = w;
		this.secids = sectionIDs;
	}

}
