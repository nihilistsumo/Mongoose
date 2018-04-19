package edu.unh.cs.treccar.proj.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.unh.cs.treccar.proj.cluster.ClusteringMetrics;
import edu.unh.cs.treccar.proj.cluster.CustomHACSimilarity;
import edu.unh.cs.treccar.proj.cluster.CustomHACWord2Vec;
import edu.unh.cs.treccar.proj.cluster.CustomKMeansWord2Vec;
import edu.unh.cs.treccar.proj.cluster.ParaMapper;
import edu.unh.cs.treccar.proj.prmat.PageRankClusters;
import edu.unh.cs.treccar.proj.qe.Query;
import edu.unh.cs.treccar.proj.similarities.HerstStOngeSimilarity;
import edu.unh.cs.treccar.proj.similarities.JiangConrathSimilarity;
import edu.unh.cs.treccar.proj.similarities.LeacockChodorowSimilarity;
import edu.unh.cs.treccar.proj.similarities.LeskSimilarity;
import edu.unh.cs.treccar.proj.similarities.LinSimilarity;
import edu.unh.cs.treccar.proj.similarities.PathSimilarity;
import edu.unh.cs.treccar.proj.similarities.ResnikSimilarity;
import edu.unh.cs.treccar.proj.similarities.SimilarityCalculator;
import edu.unh.cs.treccar.proj.similarities.SimilarityCalculatorThread;
import edu.unh.cs.treccar.proj.similarities.SimilarityFunction;
import edu.unh.cs.treccar.proj.similarities.WuPalmerSimilarity;
import edu.unh.cs.treccar_v2.Data;

public class MongooseHelper {
	Properties p;
	HashMap<String, Data.Paragraph> parasMap;
	HashMap<String, ArrayList<String>> preprocessedParasMap;
	//HashMap<String, ArrayList<String>> reducedParasMap;
	SimilarityCalculator sc;
	int nThreads;
	
	public MongooseHelper(Properties pr, String mode) {
		// TODO Auto-generated constructor stub
		this.p = pr;
		//this.parasMap = DataUtilities.getParaMapFromPath(pr.getProperty("data-dir")+"/"+pr.getProperty("parafile"));
		//this.preprocessedParasMap = DataUtilities.getPreprocessedParaMap(parasMap);
		//this.reducedParasMap = DataUtilities.getReducedParaMap(preprocessedParasMap);
		if(mode.equalsIgnoreCase("-p") || mode.equalsIgnoreCase("-l"))
			this.sc = new SimilarityCalculator();
		if(this.p.getProperty("use-default-poolsize").equalsIgnoreCase("yes")||
				this.p.getProperty("use-default-poolsize").equalsIgnoreCase("y"))
			this.nThreads = Runtime.getRuntime().availableProcessors()+1;
		else
			this.nThreads = Integer.parseInt(this.p.getProperty("threads"));
		System.out.println("Thread pool size "+this.nThreads);
	}
	
	public void runPRC(String clFilePath, String indexDir, String curlScriptPath, String runfilePath){
		PageRankClusters prc = new PageRankClusters();
		try {
			prc.prWithClusters(clFilePath, indexDir, curlScriptPath, runfilePath);
		} catch (IOException | org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	public void runQueryExpand(String dir, String out_dir, String outline_file, String out_file, String stopFilePath, 
			String word2vecFile, int topSearch, int topFeedback, int topTerms, String qe_method, String cs_method, Analyzer a, Similarity s){
	*/
	public void runQueryExpand(Properties p, Analyzer a, Similarity s){
		Query.Search ob = new Query.Search(p.getProperty("index-dir"), p.getProperty("out-dir"), p.getProperty("data-dir")+"/"+p.getProperty("outline"), p.getProperty("trec-runfile"), p.getProperty("stopfile"), 
				p.getProperty("glove-dir")+"/"+p.getProperty("glove-file"), Integer.parseInt(p.getProperty("no-ret")), 100, 10, p.getProperty("qe-method"), p.getProperty("cs-method"), a, s);
		ob.searchPageTitles();
	}
	
	public void combineRunfilesForRLib(){
		CombineRunFilesToRLibFetFile rlib = new CombineRunFilesToRLibFetFile();
		try {
			rlib.writeFetFile(p);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void runPlainTextExtractor(){
		PlainTextExtractorForGlove pt = new PlainTextExtractorForGlove();
		pt.plainTextExtractor(p.getProperty("data-dir")+"/"+p.getProperty("parafile"),
				p.getProperty("out-dir")+"/"+p.getProperty("plaintextpara"));
	}
	
	public void convertClusterDataToText() throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(p.getProperty("out-dir")+"/"+p.getProperty("cluster-out"))));
		HashMap<String, ArrayList<ArrayList<String>>> resultPageClusters = (HashMap<String, ArrayList<ArrayList<String>>>) ois.readObject();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(p.getProperty("out-dir")+"/"+p.getProperty("cluster-out-txt"))));
		for(String page:resultPageClusters.keySet()){
			bw.write("PAGE:"+page+"\n");
			ArrayList<ArrayList<String>> clusters = resultPageClusters.get(page);
			for(ArrayList<String> cl:clusters){
				for(String p:cl){
					bw.write(p+" ");
				}
				bw.write("\n");
			}
		}
		ois.close();
		bw.close();
	}
	
	public void runKMeansW2VClustering() throws IOException, ParseException{
		HashMap<String, ArrayList<ArrayList<String>>> resultPageClusters = new HashMap<String, ArrayList<ArrayList<String>>>();
		HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(
				p.getProperty("data-dir")+"/"+p.getProperty("outline"));
		
		HashMap<String, ArrayList<String>> pageParaMapRunFile = DataUtilities.getPageParaMapFromRunfile(
				p.getProperty("out-dir")+"/"+p.getProperty("trec-runfile"));
		
		/*
		HashMap<String, ArrayList<String>> pageParaMapRunFile = DataUtilities.getGTMapQrels(
				p.getProperty("data-dir")+"/"+p.getProperty("art-qrels"));
		*/
		HashMap<String, double[]> gloveVecs = DataUtilities.readGloveFile(p);
		int vecSize = gloveVecs.entrySet().iterator().next().getValue().length;
		
		StreamSupport.stream(pageSecMap.keySet().spliterator(), true).forEach(page -> { 
			try {
				ArrayList<String> paraIDsInPage = pageParaMapRunFile.get(page);
				//ArrayList<String> paraIDsInPage = pageParaMapArtQrels.get(page);
				ArrayList<String> secIDsInPage = pageSecMap.get(page);
				//ArrayList<ParaPairData> ppdList = similarityData.get(page);
				HashMap<String, double[]> paraVecMap = DataUtilities.getParaVecMap(p, paraIDsInPage, gloveVecs, vecSize);
				CustomKMeansWord2Vec kmeans = new CustomKMeansWord2Vec(p, page, secIDsInPage, paraVecMap);
				if(paraIDsInPage.size()<secIDsInPage.size())
					resultPageClusters.put(page, kmeans.cluster(paraIDsInPage.size(), false));
				else
					resultPageClusters.put(page, kmeans.cluster(secIDsInPage.size(), false));
				System.out.println("Clustering done for "+page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(p.getProperty("out-dir")+"/"+p.getProperty("cluster-out"))));
		oos.writeObject(resultPageClusters);
		oos.close();
	}
	
	public void runHACSimClustering() throws IOException, ParseException, ClassNotFoundException{
		HashMap<String, ArrayList<ArrayList<String>>> resultPageClusters = new HashMap<String, ArrayList<ArrayList<String>>>();
		//HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(p.getProperty("data-dir")+"/"+p.getProperty("outline"));
		HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(p.getProperty("data-dir")+"/"+p.getProperty("outline"));
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File(p.getProperty("out-dir")+"/"+p.getProperty("sim-data-out"))));
		HashMap<String, ArrayList<ParaPairData>> similarityData = (HashMap<String, ArrayList<ParaPairData>>) ois.readObject();
		ois.close();
		/*
		 * Properties p, String pID, double[] w, 
			ArrayList<String> sectionIDs, ArrayList<Data.Paragraph> paras, ArrayList<ParaPairData> ppdList
		 * 
		 */
		double[] w = this.getWeightVecFromRlibModel(p.getProperty("out-dir")+"/"+p.getProperty("rlib-model"));
		HashMap<String, ArrayList<String>> pageParaMapRunFile = DataUtilities.getPageParaMapFromRunfile(
				p.getProperty("out-dir")+"/"+p.getProperty("trec-runfile"));
		/* To cluster with true page-para map
		HashMap<String, ArrayList<String>> pageParaMapRunFile = DataUtilities.getGTMapQrels(
				p.getProperty("data-dir")+"/"+p.getProperty("art-qrels"));
		*/
		for(String page:pageSecMap.keySet()){
			ArrayList<String> paraIDsInPage = pageParaMapRunFile.get(page);
			//ArrayList<String> paraIDsInPage = pageParaMapArtQrels.get(page);
			ArrayList<String> secIDsInPage = pageSecMap.get(page);
			ArrayList<ParaPairData> ppdList = similarityData.get(page);
			CustomHACSimilarity hac = new CustomHACSimilarity(p, page, w, secIDsInPage, paraIDsInPage, ppdList);
			resultPageClusters.put(page, hac.cluster());
			System.out.println("Clustering done for "+page);
		}
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(p.getProperty("out-dir")+"/"+p.getProperty("cluster-out"))));
		oos.writeObject(resultPageClusters);
		oos.close();
	}
	
	public void runHACW2VClustering() throws IOException, ParseException, ClassNotFoundException{
		HashMap<String, ArrayList<ArrayList<String>>> resultPageClusters = new HashMap<String, ArrayList<ArrayList<String>>>();
		//HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(p.getProperty("data-dir")+"/"+p.getProperty("outline"));
		HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(p.getProperty("data-dir")+"/"+p.getProperty("outline"));
		/*
		HashMap<String, ArrayList<String>> pageParaMapRunFile = DataUtilities.getPageParaMapFromRunfile(
				p.getProperty("out-dir")+"/"+p.getProperty("trec-runfile"));
		*/
		HashMap<String, double[]> gloveVecs = DataUtilities.readGloveFile(p);
		int vecSize = gloveVecs.get("the").length;
		/* To cluster with true page-para map */
		HashMap<String, ArrayList<String>> pageParaMapRunFile = DataUtilities.getGTMapQrels(
				p.getProperty("data-dir")+"/"+p.getProperty("art-qrels"));
		
		StreamSupport.stream(pageSecMap.keySet().spliterator(), true).forEach(page -> { 
			try {
				ArrayList<String> paraIDsInPage = pageParaMapRunFile.get(page);
				//ArrayList<String> paraIDsInPage = pageParaMapArtQrels.get(page);
				ArrayList<String> secIDsInPage = pageSecMap.get(page);
				//ArrayList<ParaPairData> ppdList = similarityData.get(page);
				HashMap<String, double[]> paraVecMap = DataUtilities.getParaVecMap(p, paraIDsInPage, gloveVecs, vecSize);
				CustomHACWord2Vec hac = new CustomHACWord2Vec(p, page, paraIDsInPage, secIDsInPage, paraVecMap);
				resultPageClusters.put(page, hac.cluster());
				System.out.println("Clustering done for "+page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(p.getProperty("out-dir")+"/"+p.getProperty("cluster-out"))));
		oos.writeObject(resultPageClusters);
		oos.close();
	}
	
	public double[] getWeightVecFromRlibModel(String modelFilePath) throws IOException{
		double[] weightVec;
		BufferedReader br = new BufferedReader(new FileReader(new File(modelFilePath)));
		String line = br.readLine();
		while(line!=null && line.startsWith("#"))
			line = br.readLine();
		String[] values = line.split(" ");
		weightVec = new double[values.length];
		for(int i=0; i<values.length; i++)
			weightVec[i] = Double.parseDouble(values[i].split(":")[1]);
		return weightVec;
	}
	
	public HashMap<String, ArrayList<ParaPairData>> processParaPairData(
			HashMap<String, ArrayList<String>> pageParasMap) throws IOException, ParseException{
		HashMap<String, ArrayList<ParaPairData>> allPagesData = new HashMap<String, ArrayList<ParaPairData>>();
		ILexicalDatabase db = new NictWordNet();
		//int i=0;
		//int n=pageParasMap.keySet().size();
		//File logOut = new File(this.p.getProperty("out-dir")+"/"+this.p.getProperty("log-file"));
		IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(this.p.getProperty("index-dir")).toPath()))));
		Analyzer analyzer = new StandardAnalyzer();
		StreamSupport.stream(pageParasMap.keySet().spliterator(), true).forEach(pageID ->{
		//for(String pageID:pageParasMap.keySet()){
			try {
				QueryParser qp = new QueryParser("paraid", analyzer);
				ArrayList<String> paraIDs = pageParasMap.get(pageID);
				//ArrayList<String> secIDs = this.pageSecMap.get(pageID);
				//ArrayList<Data.Paragraph> paras = new ArrayList<Data.Paragraph>();
				System.out.println("Page ID: "+pageID+", "+paraIDs.size()+" paras");
				//BufferedWriter bw = new BufferedWriter(new FileWriter(logOut));
				//bw.append(pageID+" has started with "+paraIDs.size()+" paras, "+(n-i-1)+" to go after this\n");
				//bw.close();
				/*
				for(String paraID:paraIDs)
					paras.add(this.parasMap.get(paraID));
				*/
				
				//Expensive op
				ArrayList<ParaPairData> data = this.getParaPairData(paraIDs, db, is, qp, analyzer);
				//
				
				allPagesData.put(pageID, data);
				//i++;
				System.out.println(pageID+" is done");
				//bw.append(" is done, "+(n-i)+" to go");
				//bw.close();
				//System.out.println(data.size());
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return allPagesData;
	}
	
	public ArrayList<ParaPairData> getParaPairData(ArrayList<String> paraIDList, ILexicalDatabase db, 
			IndexSearcher is, QueryParser qp, Analyzer a) throws IOException, ParseException{
		Vector<ParaPairData> pairData = new Vector<ParaPairData>();
		ExecutorService exec = Executors.newFixedThreadPool(this.nThreads);
		Document paradoc1, paradoc2;
		ArrayList<String> para1tokens, para2tokens;
		for(int i=0; i<paraIDList.size()-1; i++){
			for(int j=i+1; j<paraIDList.size(); j++){ 
				String pid1 = paraIDList.get(i);
				String pid2 = paraIDList.get(j);
				paradoc1 = is.doc(is.search(qp.parse(pid1), 1).scoreDocs[0].doc);
				paradoc2 = is.doc(is.search(qp.parse(pid2), 1).scoreDocs[0].doc);
				para1tokens = tokenizeString(a, paradoc1.get("parabody"));
				para2tokens = tokenizeString(a, paradoc2.get("parabody"));
				//ParaPair pp = new ParaPair(pid1, pid2, this.preprocessedParasMap.get(pid1), this.preprocessedParasMap.get(pid2));
				ParaPair pp = new ParaPair(pid1, pid2, para1tokens, para2tokens);
				//ArrayList<Double> scores = this.computeScores(3);
				boolean showMsg = false;
				if(this.p.getProperty("show-msg").equalsIgnoreCase("yes")||
						this.p.getProperty("show-msg").equalsIgnoreCase("y"))
					showMsg = true;
				Runnable sct = new SimilarityCalculatorThread(pp, this.p.getProperty("func"), this.sc, pairData, showMsg);
				exec.execute(sct);
			}
		}
		exec.shutdown();
        while (!exec.isTerminated()) {
        }
		ArrayList<ParaPairData> ppdList = new ArrayList<ParaPairData>(pairData);
		return ppdList;
	}
	
	public static ArrayList<String> tokenizeString(Analyzer analyzer, String string){
	    ArrayList<String> result = new ArrayList<String>();
	    Map<String, Integer> termFreq = new HashMap<String, Integer>();
	    String token;
	    try {
	    	TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
	    	stream.reset();
	    	while (stream.incrementToken()){
	    		token = stream.getAttribute(CharTermAttribute.class).toString();
	    		if(termFreq.keySet().contains(token))
	    			termFreq.put(token, termFreq.get(token)+1);
	    		else
	    			termFreq.put(token, 1);
	    	}
	    	stream.close();
	    } catch (IOException e) {
	    	throw new RuntimeException(e);
	    }
	    Map<String, Integer> sortedTermFreq = MapUtil.sortByValue(termFreq);
	    int count = 0;
	    for(Map.Entry<String, Integer> entry : sortedTermFreq.entrySet()){
	    	result.add(entry.getKey());
	    	count++;
	    	if(count>9)
	    		break;
	    }
	    return result;
	}
	
	public void runClusteringMeasure() throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(
				this.p.getProperty("out-dir")+"/"+this.p.getProperty("cluster-out"))));
		HashMap<String, ArrayList<ArrayList<String>>> candClusters = (HashMap<String, ArrayList<ArrayList<String>>>) ois.readObject();
		double rand, fmeasure, meanRand = 0, meanF = 0;
		int count = 0;
		for(String pageid:candClusters.keySet()){
			ClusteringMetrics cm = new ClusteringMetrics(DataUtilities.getGTClusters(
					pageid, this.p.getProperty("data-dir")+"/"+this.p.getProperty("top-qrels")), candClusters.get(pageid), false);
			rand = cm.getAdjRAND();
			fmeasure = cm.fMeasure();
			meanRand+=rand;
			meanF+=fmeasure;
			count++;
			System.out.println(pageid+": Adj RAND = "+rand+", fmeasure = "+fmeasure);
		}
		meanRand/=count;
		meanF/=count;
		System.out.println("Mean Adj RAND = "+meanRand+", mean fmeasure = "+meanF);
	}
	
	public void runParaMapper(){
		HashMap<String, ArrayList<ArrayList<String>>> dataCl;
		try {
			this.p.load(new FileInputStream(new File("project.properties")));
			HashMap<String, ArrayList<String>> trainSec = DataUtilities.getArticleSecMap(this.p.getProperty("data-dir")+"/"+this.p.getProperty("outline"));
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(new File(this.p.getProperty("out-dir")+"/"+this.p.getProperty("cluster-out"))));
			dataCl = (HashMap<String, ArrayList<ArrayList<String>>>) ois.readObject();
			ois.close();
			for(String page:dataCl.keySet()){
				System.out.println(page+" started");
				ParaMapper pm = new ParaMapper(this.p, dataCl.get(page), trainSec.get(page), DataUtilities.readGloveFile(p));
				pm.map();
				System.out.println(page+" done");
			}
			//ParaMapper pm = new ParaMapper(p, trainCl, trainSec);
		} catch (IOException | ClassNotFoundException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveParaSimilarityData(HashMap<String, ArrayList<ParaPairData>> allPagesData, String filePath){
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(allPagesData);
			fos.close();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}