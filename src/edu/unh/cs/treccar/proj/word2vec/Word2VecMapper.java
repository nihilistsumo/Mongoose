package edu.unh.cs.treccar.proj.word2vec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.StreamSupport;

import edu.unh.cs.treccar.proj.cluster.CustomKMeansWord2Vec;
import edu.unh.cs.treccar.proj.util.DataUtilities;
import edu.unh.cs.treccar.proj.util.MapUtil;

public class Word2VecMapper {
	
	private static final int RET_DOCS = 100;
	
	public HashMap<String, Map<String, Double>> calcParaRankScores(String pageID, ArrayList<String> paraIDs, ArrayList<String> secIDs, 
			HashMap<String, double[]> paraVecMap, HashMap<String, double[]> secVecMap){
		HashMap<String, Map<String, Double>> secParaRankScores = new HashMap<String, Map<String, Double>>();
		for(String sec:secIDs){
			Map<String, Double> paraRankScores = new HashMap<String, Double>();
			for(String para:paraIDs)
				paraRankScores.put(para, this.getDotProduct(secVecMap.get(sec), paraVecMap.get(para)));
			secParaRankScores.put(sec, MapUtil.sortByValue(paraRankScores));
		}
		return secParaRankScores;
	}
	
	public void map(Properties p){
		try {
			/*
			HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getPageParaMapFromRunfile(
					p.getProperty("out-dir")+"/"+p.getProperty("trec-runfile"));
			*/
			
			HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getGTMapQrels(
					p.getProperty("data-dir")+"/"+p.getProperty("art-qrels"));
			
			HashMap<String, double[]> tokenVecMap = DataUtilities.readGloveFile(p);
			HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(
					p.getProperty("data-dir")+"/"+p.getProperty("outline"));
			int vecSize = tokenVecMap.entrySet().iterator().next().getValue().length;
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(p.getProperty("out-dir")+"/"+p.getProperty("paramap-run"))));
			
			StreamSupport.stream(pageSecMap.keySet().spliterator(), true).forEach(page -> { 
				try {
					ArrayList<String> paraIDsInPage = pageParaMap.get(page);
					ArrayList<String> secIDsInPage = pageSecMap.get(page);
					HashMap<String, double[]> paraVecMap = DataUtilities.getParaVecMap(p, paraIDsInPage, tokenVecMap, vecSize);
					HashMap<String, double[]> secVecMap = DataUtilities.getSecVecMap(p, secIDsInPage, tokenVecMap, vecSize);
					HashMap<String, Map<String, Double>> secParaRankScores = this.calcParaRankScores(page, paraIDsInPage, secIDsInPage, paraVecMap, secVecMap);
					for(String secid:secParaRankScores.keySet()){
						Map<String, Double> paraRanks = secParaRankScores.get(secid);
						int count = 0;
						for(Map.Entry<String, Double> entry : paraRanks.entrySet()){
							bw.write(secid+" Q0 "+entry.getKey()+" 0 "+entry.getValue()+" TOP\n");
							count++;
							if(count>=Word2VecMapper.RET_DOCS)
								break;
						}
							//System.out.println(secid+" Q0 "+paraid+" 0 "+paraRanks.get(paraid)+" TOP");
					}
					System.out.println(page+" done");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private double getDotProduct(double[] a, double[] b){
		double val = 0;
		for(int i=0; i<a.length; i++)
			val+=a[i]*b[i];
		return val;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			(new Word2VecMapper()).map(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
