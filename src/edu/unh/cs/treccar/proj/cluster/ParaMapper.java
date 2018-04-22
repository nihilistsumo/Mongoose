package edu.unh.cs.treccar.proj.cluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar.proj.util.DataUtilities;
import edu.unh.cs.treccar.proj.util.MongooseHelper;

public class ParaMapper {
	Properties pr;
	ArrayList<ArrayList<String>> cl;
	ArrayList<String> titleIDsToMap;
	int word2vecSize;
	HashMap<String, double[]> tokenVecMap;
	
	public ParaMapper(Properties p, ArrayList<ArrayList<String>> clusters, ArrayList<String> titles, HashMap<String, double[]> gloveVecs){
		this.pr = p;
		this.cl = clusters;
		this.titleIDsToMap = new ArrayList<String>();
		for(String t:titles) {
			if(!this.titleIDsToMap.contains(t))
				this.titleIDsToMap.add(t);
		}
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(this.pr.getProperty("glove-dir")+"/"+this.pr.getProperty("glove-file"))));
			String line = br.readLine();
			this.word2vecSize = line.split(" ").length-1;
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.tokenVecMap = gloveVecs;
	}
	
	public void map(String runfileOut) throws IOException, ParseException{
		HashMap<String, ArrayList<String>> labeledClusters = new HashMap<String, ArrayList<String>>();
		HashMap<String, double[]> clusterVecMap = new HashMap<String, double[]>();
		HashMap<String, double[]> titleVecMap = new HashMap<String, double[]>();
		ArrayList<String> clusterLabels = new ArrayList<String>();
		double[][] dotProductMatrix;
		IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(this.pr.getProperty("index-dir")).toPath()))));
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser qp = new QueryParser("paraid", analyzer);
		int label = 1;
		for(ArrayList<String> cl:this.cl){
			labeledClusters.put("c"+label, cl);
			label++;
		}
		for(String clabel:labeledClusters.keySet()){
			ArrayList<String> parasInCluster = labeledClusters.get(clabel);
			ArrayList<double[]> allVecs = new ArrayList<double[]>();
			double[] clusterVec = new double[this.word2vecSize];
			/*
			Document paradoc;
			ArrayList<String> paraTokens;
			double val = 0;
			for(String paraid:parasInCluster){
				paradoc = is.doc(is.search(qp.parse(paraid), 1).scoreDocs[0].doc);
				paraTokens = MongooseHelper.tokenizeString(analyzer, paradoc.get("parabody"));
				//allVecs.add(this.getAvgWord2VecFromTokenList(paraTokens));
			}
			*/
			allVecs = new ArrayList<double[]>(DataUtilities.getParaVecMap(pr, parasInCluster, tokenVecMap, word2vecSize).values());
			clusterVec = allVecs.get(0);
			for(int i=1; i<allVecs.size(); i++){
				for(int j=0; j<clusterVec.length; j++){
					clusterVec[j]+=allVecs.get(i)[j];
				}
			}
			for(int i=0; i<clusterVec.length; i++)
				clusterVec[i] = clusterVec[i]/allVecs.size();
			clusterVecMap.put(clabel, clusterVec);
			clusterLabels.add(clabel);
		}
		for(String title:this.titleIDsToMap){
			ArrayList<String> tokens = new ArrayList<String>();
			String trimmedTitle = title.toLowerCase().split(":")[1].replaceAll("%20", " ").replaceAll("/", " ");
			for(String token:trimmedTitle.split(" "))
				tokens.add(token);
			titleVecMap.put(title, this.getAvgWord2VecFromTokenList(tokens));
			//System.out.println(title+" "+titleVecMap.size());
		}
		ArrayList<double[]> titleVecs = new ArrayList<double[]>();
		ArrayList<double[]> clusterVecs = new ArrayList<double[]>();
		for(String t:titleVecMap.keySet())
			titleVecs.add(titleVecMap.get(t));
		for(String c:clusterVecMap.keySet())
			clusterVecs.add(clusterVecMap.get(c));
		dotProductMatrix = this.getDotProductMatrix(titleVecs, clusterVecs);
		this.writeRunFile(runfileOut, dotProductMatrix, clusterLabels, this.titleIDsToMap, labeledClusters);
	}
	
	private void writeRunFile(String outputRunfilePath, double[][] mat, ArrayList<String> clabels, ArrayList<String> tids, HashMap<String, ArrayList<String>> cl){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputRunfilePath), true));
			int topRankedClusters = 3;
			for(int i=0; i<tids.size(); i++){
				double[] clScores = new double[mat[0].length];
				for(int j=0; j<mat[i].length; j++)
					clScores[j] = mat[i][j];
				for(int loop=0; loop<topRankedClusters; loop++){
					double max = -99;
					int clIndex = 0;
					for(int k=0; k<clScores.length; k++){
						if(clScores[k]>max){
							max = clScores[k];
							clIndex = k;
						}
					}
					for(String paraid:cl.get(clabels.get(clIndex))){
						//System.out.print(tids.get(i)+" Q0 "+paraid+" 0 "+max+" TOP\n");
						bw.append(tids.get(i)+" Q0 "+paraid+" 0 "+max+" TOP\n");
					}
					clScores[clIndex] = -99;
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private double[] getAvgWord2VecFromTokenList(ArrayList<String> tokens) throws IOException{
		double[] avgVec = new double[this.word2vecSize];
		ArrayList<double[]> allVecs = new ArrayList<double[]>();
		//BufferedReader br = new BufferedReader(new FileReader(new File(this.pr.getProperty("glove-dir")+"/"+this.pr.getProperty("glove-file"))));
		for(String token:tokens){
			if(this.tokenVecMap.keySet().contains(token))
				allVecs.add(this.tokenVecMap.get(token));
		}
		if(allVecs.size()==0)
			return avgVec;
		//System.out.println(avgVec.length);
		for(int i=0; i<allVecs.size(); i++){
			for(int j=0; j<avgVec.length; j++){
				avgVec[j]+=allVecs.get(i)[j];
			}
		}
		for(int i=0; i<avgVec.length; i++)
			avgVec[i] = avgVec[i]/allVecs.size();
		return avgVec;
	}
	
	private double[][] getDotProductMatrix(ArrayList<double[]> titleVecs, ArrayList<double[]> clusterVecs){
		double[][] matrix = new double[titleVecs.size()][clusterVecs.size()];
		double[] titleVec, clusterVec;
		for(int i=0; i<titleVecs.size(); i++){
			for(int j=0; j<clusterVecs.size(); j++){
				titleVec = titleVecs.get(i);
				clusterVec = clusterVecs.get(j);
				matrix[i][j] = this.getDotProduct(titleVec, clusterVec);
			}
		}
		return matrix;
	}
	
	private double getDotProduct(double[] a, double[] b){
		double val = 0;
		for(int i=0; i<a.length; i++)
			val+=a[i]*b[i];
		val = val/a.length;
		return val;
	}
	
	public static void main(String[] args){
		ArrayList<ArrayList<String>> trainCl;
		HashMap<String, ArrayList<ArrayList<String>>> dataCl;
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(new File("project.properties")));
			HashMap<String, ArrayList<String>> trainSec = DataUtilities.getArticleToplevelSecMap(p.getProperty("data-dir")+"/"+p.getProperty("outline"));
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(new File(p.getProperty("out-dir")+"/"+p.getProperty("cluster-out"))));
			dataCl = (HashMap<String, ArrayList<ArrayList<String>>>) ois.readObject();
			ois.close();
			for(String page:dataCl.keySet()){
				System.out.println(page+" started");
				//ParaMapper pm = new ParaMapper(p, dataCl.get(page), trainSec.get(page));
				//pm.map();
				System.out.println(page+" done");
			}
			//ParaMapper pm = new ParaMapper(p, trainCl, trainSec);
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
