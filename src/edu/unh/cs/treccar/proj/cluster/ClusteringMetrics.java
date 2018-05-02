package edu.unh.cs.treccar.proj.cluster;

import java.util.ArrayList;
import java.util.HashMap;

public class ClusteringMetrics {
	/*
	ArrayList<ArrayList<String>> trueClusters;
	ArrayList<ArrayList<String>> candidateClusters;
	ArrayList<String> relParaIDs;
	HashMap<String, String> trueParaClMap;
	HashMap<String, String> candidateParaClMap;
	boolean detailed;

	public ClusteringMetrics(ArrayList<ArrayList<String>> trueC, ArrayList<ArrayList<String>> candC, boolean printDetails){
		this.trueClusters = trueC;
		this.candidateClusters = candC;
		this.relParaIDs = new ArrayList<String>();
		for(ArrayList<String> c:trueC){
			for(String p:c){
				if(!relParaIDs.contains(p))
					relParaIDs.add(p);
			}
		}
		this.trueParaClMap = new HashMap<String, String>();
		this.candidateParaClMap = new HashMap<String, String>();
		for(String pid:this.relParaIDs){
			for(int i=0;i<trueClusters.size();i++){
				if(trueClusters.get(i).contains(pid))
					trueParaClMap.put(pid, "t"+i);
			}
			for(int i=0;i<candidateClusters.size();i++){
				if(candidateClusters.get(i).contains(pid))
					candidateParaClMap.put(pid, "c"+i);
			}
		}
		this.detailed = printDetails;
	}
	*/
	
	public double getAdjRAND(ArrayList<ArrayList<String>> trueC, ArrayList<ArrayList<String>> candC, boolean printDetails){
		double rand = this.calculateRandIndex(candC, trueC, printDetails);
		return rand;
	}
	
	public double bCubedPrecision(ArrayList<ArrayList<String>> trueC, ArrayList<ArrayList<String>> candC, boolean printDetails){
		double bCubedPrec = 0.0;
		double w = 0.0;
		HashMap<String, String> trueParaClMap = new HashMap<String, String>();
		HashMap<String, String> candidateParaClMap = new HashMap<String, String>();
		ArrayList<String> relParaIDs = new ArrayList<String>();
		for(ArrayList<String> c:trueC){
			for(String p:c){
				if(!relParaIDs.contains(p))
					relParaIDs.add(p);
			}
		}
		ArrayList<String> candParaIDs = new ArrayList<String>();
		for(ArrayList<String> c:candC){
			for(String p:c){
				if(!candParaIDs.contains(p))
					candParaIDs.add(p);
			}
		}
		for(String pid:relParaIDs){
			for(int i=0;i<trueC.size();i++){
				if(trueC.get(i).contains(pid))
					trueParaClMap.put(pid, "t"+i);
			}
		}
		for(String pid:candParaIDs){
			for(int i=0;i<candC.size();i++){
				if(candC.get(i).contains(pid))
					candidateParaClMap.put(pid, "c"+i);
			}
		}
		w = 1.0/candParaIDs.size();
		for(String pid:candParaIDs)
			bCubedPrec+=w*(double)getCorrectsInCluster(pid, trueParaClMap, candidateParaClMap)/getClusterSize(pid, trueParaClMap, candidateParaClMap);
		return bCubedPrec;
	}
	
	public double bCubedRecall(ArrayList<ArrayList<String>> trueC, ArrayList<ArrayList<String>> candC, boolean printDetails){
		double bCubedRec = 0.0;
		double w = 0.0;
		HashMap<String, String> trueParaClMap = new HashMap<String, String>();
		HashMap<String, String> candidateParaClMap = new HashMap<String, String>();
		ArrayList<String> relParaIDs = new ArrayList<String>();
		for(ArrayList<String> c:trueC){
			for(String p:c){
				if(!relParaIDs.contains(p))
					relParaIDs.add(p);
			}
		}
		ArrayList<String> candParaIDs = new ArrayList<String>();
		for(ArrayList<String> c:candC){
			for(String p:c){
				if(!candParaIDs.contains(p))
					candParaIDs.add(p);
			}
		}
		for(String pid:relParaIDs){
			for(int i=0;i<trueC.size();i++){
				if(trueC.get(i).contains(pid))
					trueParaClMap.put(pid, "t"+i);
			}
		}
		for(String pid:candParaIDs){
			for(int i=0;i<candC.size();i++){
				if(candC.get(i).contains(pid))
					candidateParaClMap.put(pid, "c"+i);
			}
		}
		w = 1.0/candParaIDs.size();
		for(String pid:candParaIDs) {
			int corrects = getCorrectsInCluster(pid, trueParaClMap, candidateParaClMap);
			int ret = getNumRet(pid, trueParaClMap, candParaIDs);
			/*
			if(corrects!=ret)
				System.out.println(pid+" -> corrects = "+corrects+", ret = "+ret);
				*/
			double recFori = (double)corrects/ret;
			bCubedRec+=w*recFori;
		}
		return bCubedRec;
	}
	
	private int getNumRet(String i, HashMap<String, String> trueParaClMap, ArrayList<String> candParaIDs) {
		int result = 0;
		if(!trueParaClMap.keySet().contains(i))
			return 1;
		String tLabel = trueParaClMap.get(i);
		ArrayList<String> trueCluster = new ArrayList<String>();
		for(String pid:trueParaClMap.keySet()) {
			if(trueParaClMap.get(pid).equalsIgnoreCase(tLabel))
				trueCluster.add(pid);
		}
		for(String pid:trueCluster) {
			if(candParaIDs.contains(pid))
				result++;
		}
		return result;
	}
	
	private int getCorrectsInCluster(String i, HashMap<String, String> trueParaClMap, HashMap<String, String> candidateParaClMap) {
		int result = 0;
		if(!trueParaClMap.keySet().contains(i))
			return 1;
		String cLabel = candidateParaClMap.get(i);
		String tLabel = trueParaClMap.get(i);
		ArrayList<String> candCluster = new ArrayList<String>();
		ArrayList<String> trueCluster = new ArrayList<String>();
		for(String pid:trueParaClMap.keySet()) {
			if(trueParaClMap.get(pid).equalsIgnoreCase(tLabel))
				trueCluster.add(pid);
		}
		for(String pid:candidateParaClMap.keySet()) {
			if(candidateParaClMap.get(pid).equalsIgnoreCase(cLabel))
				candCluster.add(pid);
		}
		for(String pid:candCluster) {
			if(trueCluster.contains(pid))
				result++;
		}
		return result;
	}
	
	private int getClusterSize(String i, HashMap<String, String> trueParaClMap, HashMap<String, String> candidateParaClMap) {
		String cLabel = candidateParaClMap.get(i);
		ArrayList<String> candCluster = new ArrayList<String>();
		for(String pid:candidateParaClMap.keySet()) {
			if(candidateParaClMap.get(pid).equalsIgnoreCase(cLabel))
				candCluster.add(pid);
		}
		return candCluster.size();
	}
	
	/*
	public double fMeasure(){
		double f = 0.0;
		double[] precRec = pairPrecisionRecall();
		if(!(precRec[0]==0 && precRec[1]==0))
			f = 2*precRec[0]*precRec[1]/(precRec[0]+precRec[1]);
		return f;
	}
	
	public double[] pairPrecisionRecall(){
		//pr = {prec, rec}
		double[] pr = {0.0, 0.0};
		int[] stats = getStats();
		if(!(stats[3]==0 && stats[0]==0))
			pr[0] = (double)stats[3]/((double)(stats[3]+stats[0]));
		if(!(stats[3]==0 && stats[1]==0))
			pr[1] = (double)stats[3]/((double)(stats[3]+stats[1]));
		return pr;
	}
	
	public int[] getStats(){
		// stats[4] = {tp, tn, fp, fn}
		int[] stats = {0,0,0,0};
		String p1, p2;
		if(trueParaClMap.keySet().containsAll(relParaIDs) && 
				relParaIDs.containsAll(trueParaClMap.keySet()) &&
				candidateParaClMap.keySet().containsAll(relParaIDs) &&
				relParaIDs.containsAll(candidateParaClMap.keySet())){
			for(int i=0; i<this.relParaIDs.size()-1; i++){
				for(int j=i+1; j<this.relParaIDs.size(); j++){
					p1 = this.relParaIDs.get(i);
					p2 = this.relParaIDs.get(j);
					if(this.trueParaClMap.get(p1).equalsIgnoreCase(this.trueParaClMap.get(p2))){
						//same cluster in true
						if(this.candidateParaClMap.get(p1).equalsIgnoreCase(this.candidateParaClMap.get(p2))){
							//same cluster in cand
							stats[3]++;
						}
						else{
							//diff cluster in cand  
							stats[0]++;
						}
					}
					else{
						//diff cluster in true
						if(this.candidateParaClMap.get(p1).equalsIgnoreCase(this.candidateParaClMap.get(p2))){
							//same cluster in cand
							stats[1]++;
						}
						else{
							//diff cluster in cand
							stats[2]++;
						}
					}
				}
			}
		}
		else{
			//System.out.println("Page does not contain all paras, ignoring it with output 0 for F1");
		}
		return stats;
	}
	*/
	
	public double calculateRandIndex(ArrayList<ArrayList<String>> candidateClusters, ArrayList<ArrayList<String>> gtClusters, boolean detailed){
		//candidateClusters will be map between cluster labels and cluster of para IDs
		//String resultString = "";
		//HashMap<String, ArrayList<String>> correct = this.groundTruth;
		//HashMap<AssignParagraphs.SectionPathID, ArrayList<Data.Paragraph>> candidate = this.candidateAssign;
		//String[] correctSections = new String[correct.size()];
		//String[] candLabels = new String[candidateClusters.size()];
		//correct.keySet().toArray(correctSections);
		//candidateClusters.keySet().toArray(candLabels);
		
		int[][] contingencyMatrix = new int[gtClusters.size()][candidateClusters.size()];
		double randIndex = 0.0;
		ArrayList<String> correctParas = new ArrayList<String>();
		ArrayList<String> candParas = new ArrayList<String>();
		for(int i=0; i<gtClusters.size(); i++){
			for(int j=0; j<candidateClusters.size(); j++){
				int matchCount = 0;
				correctParas = gtClusters.get(i);
				candParas = candidateClusters.get(j);
				if(correctParas == null){
					System.out.println("We have null in correctParas!");
				} else if(candParas != null){
					for(String candPara : candParas){
						if(correctParas.contains(candPara)){
							matchCount++;
						}
					}
				}
				contingencyMatrix[i][j] = matchCount;
			}
		}
		if(detailed)
			printContingencyMatrix(contingencyMatrix);
		if((new Double(this.computeRand(contingencyMatrix, detailed))).isNaN()) {
			//System.out.println("Adjusted Rand index could not be computed!");
			return 0.0;
		}
		else {
			randIndex = this.computeRand(contingencyMatrix, detailed);
			//System.out.println("Calculated RAND index: "+randIndex);
			return randIndex;
		}
	}
	
	private double computeRand(int[][] contMat, boolean detailed){
		double score = 0.0;
		int sumnij=0, sumni=0, sumnj=0, n=0, nC2=0, nrow=0, ncol=0;		
		ncol = contMat[0].length;
		nrow = contMat.length;
		int[] njvals = new int[ncol];
		for(int i=0; i<nrow; i++){
			int ni=0;
			for(int j=0; j<ncol; j++){
				sumnij+=this.nC2(contMat[i][j]);
				ni+=contMat[i][j];
				njvals[j]+=contMat[i][j];
				n+=contMat[i][j];
			}
			sumni+=this.nC2(ni);
		}
		for(int j=0; j<njvals.length; j++){
			sumnj+=this.nC2(njvals[j]);
		}
		nC2 = this.nC2(n);
		
		/* ################### 
		 * This code is for simple Rand index
		
		int a=0, b=0, c=0, d=0;
		a = sumnij;
		b = sumni - sumnij;
		c = sumnj - sumnij;
		d = nC2 - (a+b+c);
		score = ((double)(a+d))/(a+b+c+d);
		
		#################### */
		
		double denom = ((double)(sumni+sumnj))/2-((double)sumni*sumnj/nC2);
		double nom = (sumnij-((double)(sumni*sumnj))/nC2);
		if(detailed)
			System.out.println("sumnij: "+sumnij+", sumni: "+sumni+", sumnj: "+sumnj+", nC2: "+nC2+", nom: "+nom+", denom: "+denom);
		score = nom/denom;
		return score;
	}
	
	private int nC2(int n){
		if(n<2) return 0;
		else if(n==2) return 1;
		else{
			return n*(n-1)/2;
		}
	}
	
	private void printContingencyMatrix(int[][] contingency){
		int colNum = contingency[0].length;
		int rowNum = contingency.length;
		for(int i=0; i<rowNum; i++){
			for(int j=0; j<colNum; j++){
				System.out.print(contingency[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		ArrayList<String> c1 = new ArrayList<String>();
		ArrayList<String> c2 = new ArrayList<String>();
		ArrayList<String> t1 = new ArrayList<String>();
		ArrayList<String> t2 = new ArrayList<String>();
		c1.add("1");c1.add("5");c1.add("6");c1.add("7");c1.add("2");
		c2.add("3");c2.add("4");
		t1.add("1");t1.add("2");t1.add("3");t1.add("4");
		t2.add("5");t2.add("6");t2.add("7");
		ArrayList<ArrayList<String>> c = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> t = new ArrayList<ArrayList<String>>();
		c.add(c1);c.add(c2);t.add(t1);t.add(t2);
		ClusteringMetrics cm = new ClusteringMetrics();
		System.out.println("bcubed Precision = "+cm.bCubedPrecision(t, c, false));
		System.out.println("bcubed Recall = "+cm.bCubedRecall(t, c, false));
	}
}