package edu.unh.cs.treccar.proj.sum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;

import edu.unh.cs.treccar.proj.util.DataUtilities;
import edu.unh.cs.treccar.proj.util.MapUtil;

public class Summarizer {
	
	public static final int MAX_SUMMARY_SIZE = 5;
	
	public ArrayList<String> summarize(Properties p, IndexSearcher is, Analyzer analyzer, QueryParser qp, String paraID) throws IOException, ParseException{
		Document para = is.doc(is.search(qp.parse(paraID), 1).scoreDocs[0].doc);
		Map<String, Double> tfidfMap = MapUtil.sortByValue(DataUtilities.tokenTfidfMap(is, analyzer, para.get("parabody")));
		if(p.getProperty("sum-method").equalsIgnoreCase("tfidf"))
			return this.tfidfSummary(tfidfMap);
		else
			return this.tfidfSummary(tfidfMap);
	}
	
	private ArrayList<String> tfidfSummary(Map<String, Double> tfidfMap){
		ArrayList<String> summary = new ArrayList<String>();
		int count = 0;
	    for(Map.Entry<String, Double> entry : tfidfMap.entrySet()){
	    	summary.add(entry.getKey());
	    	count++;
	    	if(count>=Summarizer.MAX_SUMMARY_SIZE)
	    		break;
	    }
		return summary;
	}
}
