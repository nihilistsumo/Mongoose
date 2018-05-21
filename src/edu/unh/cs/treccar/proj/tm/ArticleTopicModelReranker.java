package edu.unh.cs.treccar.proj.tm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.unh.cs.treccar.proj.util.DataUtilities;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class ArticleTopicModelReranker {
	
	private final static int ITERATIONS_INFERENCER = 100;
	private final static int THINNING_INFERENCER = 1;
	private final static int BURNIN_INFERENCER = 5;
	
	public void rerank(Properties prop, String tmPath, String candSetFilePath, String outlinePath, String runfileOutPath, int parallel) {
		try {
			ParallelTopicModel tm = ParallelTopicModel.read(new File(tmPath));
			TopicInferencer inf = tm.getInferencer();
			FileInputStream fis = new FileInputStream(new File(outlinePath));
			final Iterator<Data.Page> pageIt = DeserializeData.iterAnnotations(fis); 
			Iterable<Data.Page> pageIterable = ()->pageIt;
			HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getPageParaMapFromRunfile(candSetFilePath);
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(runfileOutPath)));
			IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(prop.getProperty("index-dir")).toPath()))));
			Analyzer analyzer = new StandardAnalyzer();
			boolean isParallel = false;
			if(parallel>0)
				isParallel = true;
			StreamSupport.stream(pageIterable.spliterator(), isParallel).forEach(page -> { 
				try {
					HashSet<String> secIDsinPage = new HashSet<String>();
					secIDsinPage = this.getAllSectionIDs(page);
					QueryParser qp = new QueryParser("paraid", analyzer);
					ArrayList<String> paraIDsInPage = pageParaMap.get(page.getPageId());
					HashMap<String, String> paraIDTextMap = new HashMap<String, String>();
					for(String paraID:paraIDsInPage) {
						String paraText = is.doc(is.search(qp.parse(paraID), 1).scoreDocs[0].doc).get("parabody");
						paraIDTextMap.put(paraID, paraText);
					}
					InstanceList paraIList = this.convertParasToIList(paraIDTextMap);
					Instance secIns = this.getPageQueryInstance(page.getPageId(), secIDsinPage);
					double[] secTopicDist = inf.getSampledDistribution(secIns, ITERATIONS_INFERENCER, THINNING_INFERENCER, BURNIN_INFERENCER);
					for(Instance paraIns:paraIList) {
						double[] paraTopicDist = inf.getSampledDistribution(paraIns, ITERATIONS_INFERENCER, THINNING_INFERENCER, BURNIN_INFERENCER);
						bw.write(page.getPageId()+" Q0 "+paraIns.getName()+" 0 "+this.getKLdiv(secTopicDist, paraTopicDist)+"TMPAGE-RERANK\n");
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
			
			for(String sec:rankResult.keySet()) {
				for(String para:rankResult.get(sec).keySet()) {
					//System.out.println(q+" "+para+" "+rankResult.get(q).get(para));
					bw.write(sec+" Q0 "+para+" 0 "+rankResult.get(sec).get(para)+" TOPIC-MODEL\n");
				}
			}
			*/
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Instance getPageQueryInstance(String pageID, HashSet<String> secIDs) {
		String queryString = pageID;
		for(String s:secIDs)
			queryString+=" "+s;
		queryString = queryString.replaceAll("[/,%20]", " ").replaceAll("enwiki:", "");
		Instance qIns = new Instance(queryString, null, pageID, queryString);
		return qIns;
	}
	
	private InstanceList convertParasToIList(HashMap<String, String> paraTexts) {
		InstanceList iListPara = new InstanceList(TopicModelMapper.buildPipeForLDA());
		for(String paraID:paraTexts.keySet()) {
			Instance paraIns = new Instance(paraTexts.get(paraID), null, paraID, paraTexts.get(paraID));
			iListPara.addThruPipe(paraIns);
		}
		return iListPara;
	}
	
	private double getKLdiv(double[] p, double[] q){
		double result = 0;
		for(int i=0; i<p.length; i++){
			if(q[i]<0.0000001 || p[i]<0.0000001){
				continue; 
			}
			result+=p[i]*Math.log(p[i]/q[i]);
		}
		return result;
	}
	
	private HashSet<String> getAllSectionIDs(Data.Page page){
		HashSet<String> secIDList = new HashSet<String>();
		String parent = page.getPageId();
		for(Data.Section sec:page.getChildSections())
			addSectionIDToList(sec, secIDList, parent);
		return secIDList;
	}
	
	private void addSectionIDToList(Data.Section sec, HashSet<String> idlist, String parent){
		if(sec.getChildSections() == null || sec.getChildSections().size() == 0){
			idlist.add(parent+"/"+sec.getHeadingId());
		}
		else{
			idlist.add(parent+"/"+sec.getHeadingId());
			parent = parent+"/"+sec.getHeadingId();
			for(Data.Section child:sec.getChildSections())
				addSectionIDToList(child, idlist, parent);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
