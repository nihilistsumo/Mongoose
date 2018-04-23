package edu.unh.cs.treccar.proj.sum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.StreamSupport;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.unh.cs.treccar.proj.util.DataUtilities;
import edu.unh.cs.treccar.proj.util.MapUtil;
import edu.unh.cs.treccar.proj.word2vec.Word2VecMapper;

public class SummaryMapper {
	
	private static final int RET_DOCS = 100;
	private HashMap<String, double[]> gloveVecs = null;
	private ILexicalDatabase db = null;
	private JiangConrath jc = null;
	private Path pat = null;
	private Lin lin = null;
	private WuPalmer wu = null;
	
	public SummaryMapper(Properties p){
		if(p.getProperty("sum-map-method").equalsIgnoreCase("w2v")){
			try {
				gloveVecs = DataUtilities.readGloveFile(p);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(p.getProperty("sum-map-method").equalsIgnoreCase("wn")){
			db = new NictWordNet();
			jc = new JiangConrath(db);
			pat = new Path(db);
			lin = new Lin(db);
			wu = new WuPalmer(db);
		}
	}
	
	public HashMap<String, Map<String, Double>> calcParaRankScores(String pageID, ArrayList<String> paraIDs, ArrayList<String> secIDs, 
			HashMap<String, ArrayList<String>> paraTokenMap, HashMap<String, ArrayList<String>> secTokenMap, Properties p){
		HashMap<String, Map<String, Double>> secParaRankScores = new HashMap<String, Map<String, Double>>();
		for(String sec:secIDs){
			ArrayList<String> secTokens = secTokenMap.get(sec);
			Map<String, Double> paraRankScores = new HashMap<String, Double>();
			for(String para:paraIDs){
				ArrayList<String> paraTokens = paraTokenMap.get(para);
				if(p.getProperty("sum-map-method").equalsIgnoreCase("w2v"))
					paraRankScores.put(para, this.w2vSimilarity(secTokens, paraTokens));
				else if(p.getProperty("sum-map-method").equalsIgnoreCase("wn"))
					paraRankScores.put(para, this.wnSimilarity(secTokens, paraTokens));
				else
					paraRankScores.put(para, this.w2vSimilarity(secTokens, paraTokens));
			}
			secParaRankScores.put(sec, paraRankScores);
		}
		return secParaRankScores;
	}
	
	public double w2vSimilarity(ArrayList<String> tokens1, ArrayList<String> tokens2){
		ArrayList<double[]> vecs1 = new ArrayList<double[]>();
		ArrayList<double[]> vecs2 = new ArrayList<double[]>();
		for(String t1:tokens1){
			if(this.gloveVecs.containsKey(t1) && !DataUtilities.stopwords.contains(t1))
				vecs1.add(this.gloveVecs.get(t1));
			else
				vecs1.add(new double[this.gloveVecs.get("the").length]);
		}
		for(String t2:tokens2){
			if(this.gloveVecs.containsKey(t2) && !DataUtilities.stopwords.contains(t2))
				vecs2.add(this.gloveVecs.get(t2));
			else
				vecs2.add(new double[this.gloveVecs.get("the").length]);
		}
		return getDotProduct(getAvgVec(vecs1), getAvgVec(vecs2));
	}
	
	public double wnSimilarity(ArrayList<String> tokens1, ArrayList<String> tokens2){
		double score = 0.0;
		String[] tarr1 = new String[tokens1.size()];
		String[] tarr2 = new String[tokens2.size()];
		for(int i=0; i<tokens1.size(); i++)
			tarr1[i] = tokens1.get(i);
		for(int i=0; i<tokens2.size(); i++)
			tarr2[i] = tokens2.get(i);
		int m=1;
		/*
		int m=0;
		for(String t2:tokens2){
			if(!tokens1.contains(t2)){
				tarr2[m] = t2;
				m++;
			}
		}
		*/
		double[][][] totalMat = new double[4][][];
		totalMat[0] = jc.getNormalizedSimilarityMatrix(tarr1, tarr2);
		totalMat[1] = pat.getNormalizedSimilarityMatrix(tarr1, tarr2);
		totalMat[2] = lin.getNormalizedSimilarityMatrix(tarr1, tarr2);
		totalMat[3] = wu.getNormalizedSimilarityMatrix(tarr1, tarr2);
		for(int i=0; i<totalMat.length; i++){
			for(int j=0; j<totalMat[0].length; j++){
				for(int k=0; k<totalMat[0][0].length; k++)
					score+=totalMat[i][j][k];
			}
		}
		score/=totalMat.length*totalMat[0].length*totalMat[0][0].length;
		return score;
	}
	
	private double[] getAvgVec(ArrayList<double[]> vecs){
		double[] avg = new double[vecs.get(0).length];
		for(double[] v:vecs){
			for(int i=0; i<v.length; i++)
				avg[i]+=v[i];
		}
		for(int i=0; i<vecs.get(0).length; i++)
			avg[i]/=vecs.size();
		return avg;
	}
	
	private double getDotProduct(double[] a, double[] b){
		double val = 0;
		for(int i=0; i<a.length; i++)
			val+=a[i]*b[i];
		return val;
	}
	
	public void map(Properties p, String candSetFilePath, String outputRunfilePath){
		try {
			HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getPageParaMapFromRunfile(candSetFilePath);
			/*
			HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getGTMapQrels(
					p.getProperty("data-dir")+"/"+p.getProperty("art-qrels"));
			*/
			
			HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(
					p.getProperty("data-dir")+"/"+p.getProperty("outline"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputRunfilePath)));
			//ArrayList<String> results = new ArrayList<String>();
			StreamSupport.stream(pageSecMap.keySet().spliterator(), true).forEach(page -> { 
				try {
					ArrayList<String> paraIDsInPage = pageParaMap.get(page);
					ArrayList<String> secIDsInPage = pageSecMap.get(page);
					HashMap<String, ArrayList<String>> paraTokenMap = DataUtilities.getParaSummaryMap(p, paraIDsInPage);
					HashMap<String, ArrayList<String>> secTokenMap = DataUtilities.getSecTokenMap(p, secIDsInPage);
					//System.out.println("Going to calculate scores for "+page);
					HashMap<String, Map<String, Double>> secParaRankScores = this.calcParaRankScores(page, paraIDsInPage, secIDsInPage,
							paraTokenMap, secTokenMap, p);
					for(String secid:secParaRankScores.keySet()){
						Map<String, Double> paraRanks = secParaRankScores.get(secid);
						int count = 0;
						for(Map.Entry<String, Double> entry : paraRanks.entrySet()){
							//results.add(secid+" Q0 "+entry.getKey()+" 0 "+entry.getValue()+" TOP\n");
							bw.write(secid+" Q0 "+entry.getKey()+" 0 "+entry.getValue()+" TOP\n");
							//System.out.println(secid+" Q0 "+entry.getKey()+" 0 "+entry.getValue()+" TOP");
							count++;
							if(count>=SummaryMapper.RET_DOCS)
								break;
						}
					}
					System.out.println(page+" done");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			/*
			for(String s:results)
				bw.write(s);
				*/
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			SummaryMapper sm = new SummaryMapper(p);
			//sm.map(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
