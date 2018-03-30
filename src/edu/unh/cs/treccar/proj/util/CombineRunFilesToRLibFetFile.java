package edu.unh.cs.treccar.proj.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Set;

/*
 * This class combines run files to RankLib feature file
 * 
 */

public class CombineRunFilesToRLibFetFile {
	
	class ParaScore{
		String paraID;
		double score;
		
		public ParaScore(String p, double s){
			this.paraID = p;
			this.score = s;
		}
		
		public String getParaID() {
			return paraID;
		}
		public void setParaID(String paraID) {
			this.paraID = paraID;
		}
		public double getScore() {
			return score;
		}
		public void setScore(double score) {
			this.score = score;
		}
	}
	
	public void writeFetFile(Properties p) throws Exception{
		String[] runfiles = p.getProperty("runfile-list").split(" ");
		HashMap<String, ArrayList<String>> qrels = DataUtilities.getGTMapQrels(p.getProperty("data-dir")+"/"+p.getProperty("top-qrels"));
		ArrayList<HashMap<String, HashMap<String, Double>>> runfileObjList = new ArrayList<HashMap<String, HashMap<String, Double>>>();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(p.getProperty("out-dir")+"/"+p.getProperty("rlib-out"))));
		for(String rf:runfiles){
			try {
				runfileObjList.add(this.getRunfileObj(p.getProperty("out-dir")+"/"+rf));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(HashMap<String, HashMap<String, Double>> rf:runfileObjList){
			if(!rf.keySet().equals(runfileObjList.get(0).keySet()))
				throw new Exception("Query list of the run files are different!");
		}
		for(String q:runfileObjList.get(0).keySet()){
			if(qrels.get(q)==null){
				System.out.println("No query in qrels as "+q);
				continue;
			}
			HashSet<String> paras = new HashSet<String>();
			paras.addAll(runfileObjList.get(0).get(q).keySet());
			for(int i=1; i<runfileObjList.size(); i++)
				paras.addAll(runfileObjList.get(i).get(q).keySet());
			for(String para:paras){
				String fetFileLine = "";
				ArrayList<String> trueParas = qrels.get(q);
				if(trueParas.contains(para))
					fetFileLine = "1 qid:"+q;
				else
					fetFileLine = "0 qid:"+q;
				int j = 1;
				for(HashMap<String, HashMap<String, Double>> rf:runfileObjList){
					if(rf.get(q).containsKey(para))
						fetFileLine+= " "+j+":"+rf.get(q).get(para);
					else
						fetFileLine+= " "+j+":0";
					j++;
				}
				fetFileLine+=" #"+para;
				//System.out.println(fetFileLine);
				bw.write(fetFileLine+"\n");
			}
		}
		System.out.println("done");
		bw.close();
	}
	
	public HashMap<String, HashMap<String, Double>> getRunfileObj(String runfilePath) throws IOException{
		HashMap<String, HashMap<String, Double>> rfObj = new HashMap<String, HashMap<String, Double>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(runfilePath)));
		String line = br.readLine();
		String q,p,s;
		while(line!=null){
			q = line.split(" ")[0];
			p = line.split(" ")[2];
			s = line.split(" ")[4];
			if(rfObj.keySet().contains(q)){
				rfObj.get(q).put(p, Double.parseDouble(s));
			}
			else{
				HashMap<String, Double> psmap = new HashMap<String, Double>();
				psmap.put(p, Double.parseDouble(s));
				rfObj.put(q, psmap);
			}
			line = br.readLine();
		}
		br.close();
		return rfObj;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			CombineRunFilesToRLibFetFile rlib = new CombineRunFilesToRLibFetFile();
			rlib.writeFetFile(p);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
