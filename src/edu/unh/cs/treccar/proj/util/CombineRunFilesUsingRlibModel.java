package edu.unh.cs.treccar.proj.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

public class CombineRunFilesUsingRlibModel {
	
	public void writeRunFile(Properties p) throws IOException{
		String[] runfiles = p.getProperty("runfile-list").split(" ");
		MongooseHelper mh = new MongooseHelper(p);
		double[] optW = mh.getWeightVecFromRlibModel(p.getProperty("out-dir")+"/"+p.getProperty("rlib-model"));
		ArrayList<HashMap<String, HashMap<String, Double>>> runfileObjList = new ArrayList<HashMap<String, HashMap<String, Double>>>();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(p.getProperty("out-dir")+"/"+p.getProperty("comb-run-out"))));
		for(String rf:runfiles){
			try {
				runfileObjList.add(this.getRunfileObj(p.getProperty("out-dir")+"/"+rf));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Read all runfiles");
		HashSet<String> intersection = new HashSet<String>(runfileObjList.get(0).keySet());
		/*
		for(HashMap<String, HashMap<String, Double>> rf:runfileObjList){
			if(!rf.keySet().equals(runfileObjList.get(0).keySet()))
				throw new Exception("Query list of the run files are different!");
		}
		*/
		for(int i=1; i<runfileObjList.size(); i++){
			intersection.retainAll(runfileObjList.get(i).keySet());
		}
		for(String q:intersection){
			HashSet<String> paras = new HashSet<String>();
			paras.addAll(runfileObjList.get(0).get(q).keySet());
			for(int i=1; i<runfileObjList.size(); i++)
				paras.addAll(runfileObjList.get(i).get(q).keySet());
			for(String para:paras){
				String runfileLine = "";
				double combinedScore = 0;
				assert(optW.length==runfileObjList.size());
				for(int r=0; r<runfileObjList.size(); r++){
					if(runfileObjList.get(r).get(q).containsKey(para))
						combinedScore+=runfileObjList.get(r).get(q).get(para)*optW[r];
				}
				bw.write(q+" Q0 "+para+" 0 "+combinedScore+" COMBINED\n");
			}
		}
		bw.close();
	}
	
	public HashMap<String, HashMap<String, Double>> getRunfileObj(String runfilePath) throws IOException{
		HashMap<String, HashMap<String, Double>> rfObj = new HashMap<String, HashMap<String, Double>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(runfilePath)));
		String line = br.readLine();
		String q,p,s;
		while(line!=null){
			q = line.split(" ")[0];
			if(!q.contains("/")){
				line = br.readLine();
			}
			else{
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
		}
		br.close();
		return rfObj;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			CombineRunFilesUsingRlibModel comb = new CombineRunFilesUsingRlibModel();
			comb.writeRunFile(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
