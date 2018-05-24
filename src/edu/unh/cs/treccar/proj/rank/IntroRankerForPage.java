package edu.unh.cs.treccar.proj.rank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class IntroRankerForPage {
	
	public void getIntroParas(String indexDirPath, String outlinePath, String method, String outRunPath, int retNo) throws IOException {
		HashMap<String, ArrayList<String>> pageIntroMap = new HashMap<String, ArrayList<String>>();
		FileInputStream fis = new FileInputStream(new File(outlinePath));
		final Iterator<Data.Page> pageIt = DeserializeData.iterAnnotations(fis); 
		Iterable<Data.Page> pageIterable = ()->pageIt;
		IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(indexDirPath).toPath()))));
		if(method.equalsIgnoreCase("bm25"))
			is.setSimilarity(new BM25Similarity());
		else if(method.equalsIgnoreCase("bool"))
			is.setSimilarity(new BooleanSimilarity());
		else if(method.equalsIgnoreCase("classic"))
			is.setSimilarity(new ClassicSimilarity());
		else if(method.equalsIgnoreCase("lmds"))
			is.setSimilarity(new LMDirichletSimilarity());
		QueryParser qp = new QueryParser("parabody", new StandardAnalyzer());
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outRunPath)));
		StreamSupport.stream(pageIterable.spliterator(), true).forEach(page -> {
			try {
				ArrayList<String> introParaList = new ArrayList<String>();
				pageIntroMap.put(page.getPageId(), introParaList);
				String queryString = page.getPageId();
				queryString = queryString.replaceAll("[/,%20]", " ").replaceAll("enwiki:", "");
				Query q = qp.parse(queryString);
				TopDocs tds = is.search(q, retNo);
				ScoreDoc[] retDocs = tds.scoreDocs;
				int count = 0;
				for (int i = 0; i < retDocs.length; i++) {
					Document d = is.doc(retDocs[i].doc);
					String para = d.getField("paraid").stringValue();
					String firstLine = d.getField("parabody").stringValue().split("[.]")[0];
					String regex = "^((the\\s)*(a\\s)*(an\\s)*)"+queryString.toLowerCase().replaceAll(" +", " ")+"(s*|es*)(,*)\\s(or\\s(\\w+\\s)*)*(((\\()(.*)(\\))\\s)*)"
							+ "(is\\s|are\\s|was\\s|were\\s|(\\w+\\s){0,1}refers|(\\w+\\s){0,1}called|(\\w+\\s){0,1}known)";
					//String regex = queryString.toLowerCase();
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(firstLine);
					if(m.find()) {
						pageIntroMap.get(page.getPageId()).add(d.getField("paraid").stringValue());
						count++;
						bw.write(page.getPageId()+" Q0 "+para+" 0 "+(retDocs[0].score+1)+" "+method.toUpperCase()+"-INTRO-PARA\n");
						//System.out.println(page.getPageId()+" "+d.getField("paraid").stringValue()+" "+firstLine);
					}
					else {
						bw.write(page.getPageId()+" Q0 "+para+" 0 "+retDocs[i].score+" "+method.toUpperCase()+"-INTRO-PARA\n");
					}
					//retrievedResult.put(d.getField("paraid").stringValue(), tds.scoreDocs[i].score);
				}
				//if(count==0)
				System.out.println(page.getPageId()+" "+count);
				
				//System.out.println(page.getPageId()+" is done");
			} catch (ParseException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		bw.close();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			IntroRankerForPage irp = new IntroRankerForPage();
			irp.getIntroParas(p.getProperty("index-dir"), 
					"/home/sumanta/Documents/Mongoose-data/trec-data/benchmarkY1-train/train.pages.cbor-outlines.cbor", "bm25", 
					"/home/sumanta/Documents/Mongoose-data/Mongoose-results/page-runs-basic-sim-and-fixed/lucene-basic/train/intro-para-bm25-rerank-train-page-run", 1000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
