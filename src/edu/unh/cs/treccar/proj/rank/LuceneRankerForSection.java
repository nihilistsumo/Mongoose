package edu.unh.cs.treccar.proj.rank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar.proj.util.DataUtilities;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class LuceneRankerForSection {
	
	public void rank(String indexDirPath, String outlinePath, String outRunPath, String level, String method) throws IOException {
		HashMap<String, ArrayList<String>> pageSecMap = new HashMap<String, ArrayList<String>>();
		if(level.equalsIgnoreCase("top"))
			pageSecMap = DataUtilities.getArticleToplevelSecMap(outlinePath);
		else
			pageSecMap = DataUtilities.getArticleSecMap(outlinePath);
		
		for(String page:pageSecMap.keySet()) {
			IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(indexDirPath).toPath()))));
			QueryParser qp = new QueryParser("parabody", new StandardAnalyzer());
			ArrayList<String> secIDsinPage = pageSecMap.get(page);
			
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
