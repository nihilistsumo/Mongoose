package edu.unh.cs.treccar.proj.qe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class QueryIndex 
{
	/**
	 * Path to the index directory
	 */
	private static String INDEX_DIR ;
	/**
	 * Path to the output directory
	 */
	private static String OUTPUT_DIR ;
	/**
	 * Path to cbor outline file
	 */
	private static String CBOR_OUTLINE_FILE ;
	/**
	 * Name of the output file
	 */
	private static String OUT_FILE ;
	/**
	 * List of pages in the corpus
	 */
	/**
	 * Top search results for a query
	 */
	private static int TOP_SEARCH;
	/**
	 * Method to use for querying index
	 */
	private static String METHOD;
	private static ArrayList<Data.Page> pagelist;
	/**
	 * List of paragraph IDs already seen
	 * Used to prevent duplicate documents in the run file
	 * Paragraph is added to the run file only if it is not already in this list
	 */
	private static ArrayList<String> paraID;
	private static IndexSearcher is;
	private static Analyzer analyzer;
	private static Similarity similarity;
	private static List<String> tokens;
	
	public QueryIndex(String dir, String out_dir, String outline_file, String out_file, int topSearch, String method, Analyzer a, Similarity s)
	{
		QueryIndex.INDEX_DIR = dir;
		QueryIndex.OUTPUT_DIR = out_dir;
		QueryIndex.CBOR_OUTLINE_FILE = outline_file;
		QueryIndex.OUT_FILE = out_file;
		QueryIndex.TOP_SEARCH = topSearch;
		QueryIndex.METHOD = method;
		QueryIndex.pagelist = getPageListFromPath(CBOR_OUTLINE_FILE);
		QueryIndex.tokens = new ArrayList<>(128);
		QueryIndex.paraID = new ArrayList<String>();
		QueryIndex.analyzer = a;
		QueryIndex.similarity = s;
		QueryIndex.is = new Index.Setup(INDEX_DIR, "parabody",a ,s).getSearcher();
	}
	
	public BooleanQuery toQuery(String queryStr) throws IOException 
	{

        TokenStream tokenStream = analyzer.tokenStream("parabody", new StringReader(queryStr));
        tokenStream.reset();
        tokens.clear();
        while (tokenStream.incrementToken()) 
        {
            final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
            tokens.add(token);
        }
        tokenStream.end();
        tokenStream.close();
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        for (String token : tokens) 
        {
            booleanQuery.add(new TermQuery(new Term("parabody", token)), BooleanClause.Occur.SHOULD);
        }
        return booleanQuery.build();
    }
	private ArrayList<Data.Page> getPageListFromPath(String path)
	{
		ArrayList<Data.Page> pageList = new ArrayList<Data.Page>();
		try 
		{
			FileInputStream fis = new FileInputStream(new File(path));
			for(Data.Page page: DeserializeData.iterableAnnotations(fis))
				pageList.add(page);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		return pageList;
	}
	private void createRunFile(String queryID, TopDocs tds, ScoreDoc[] retDocs)throws IOException
	{
		ArrayList<String> runStrings = new ArrayList<String>();
		Document d;
		String runFileString;
		String outFilePath = OUTPUT_DIR+"/"+OUT_FILE;
		
		for (int i = 0; i < retDocs.length; i++) 
		{
			d = is.doc(retDocs[i].doc);
			String pID = d.getField("paraid").stringValue();
			
			runFileString = queryID+" Q0 "+pID+" "+i+" "+tds.scoreDocs[i].score+" "+METHOD;
			if(!paraID.contains(pID))
			{
				paraID.add(pID);
				runStrings.add(runFileString);
			}
		}
		
		FileWriter fw = new FileWriter(outFilePath, true);
		for(String runString:runStrings)
			fw.write(runString+"\n");
		fw.close();
	}	
	private String buildSectionQueryStr(Data.Page page, List<Data.Section> sectionPath) 
	{
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(page.getPageName());
        for (Data.Section section: sectionPath)
        {
        	if(!section.getHeading().contains("/"))
        		queryStr.append(" ").append(section.getHeading());
        }
        return queryStr.toString();
    }
	public void rankParas(BooleanQuery qString, String qID, int n) throws IOException, ParseException 
	{
		TopDocs tds = Index.Search.searchIndex(qString,n);
		ScoreDoc[] retDocs = tds.scoreDocs;
		createRunFile(qID,tds,retDocs);
	}
	public void search(String mode)
	{
		switch(mode)
		{
		case "page-title" : searchPageTitles();
		break;
		
		case "section-path" : searchSectionHeadings();
		break;
		
		case "top-level-sections" : searchTopLevelSections();
		break;
		
		default: System.out.println("*******************************Wrong mode! See usage!****************************");
		System.exit(0);
		}
	}
	/**
	 * Search the top level section names as queries
	 */
	private void searchTopLevelSections()
	{
		String qString,qID;
		BooleanQuery query;
		try
		{
			for(Data.Page page:pagelist)
			{	
				qString = page.getPageName();
				query = toQuery(qString);
				qID = page.getPageId();
				rankParas(query,qID, TOP_SEARCH);
				List<Data.Section> childSections = page.getChildSections();
				for(Data.Section child : childSections)
				{
	                qString = child.getHeading();
	                query = toQuery(qString);
	                qID = page.getPageId()+"/"+child.getHeadingId();
	                rankParas(query,qID, TOP_SEARCH);
				}
				System.out.println("Done page:"+page.getPageName());
			}
		} 
		catch ( IOException | ParseException e) 
		{
			e.printStackTrace();
		}
	}
	/**
	 * Search the page titles as queries
	 */
	private void searchPageTitles()
	{
		try
		{
			for(Data.Page page:pagelist)
			{
				String qString = buildSectionQueryStr(page, Collections.<Data.Section>emptyList());
				BooleanQuery query = toQuery(qString);
				String qID = page.getPageId();
				rankParas(query,qID, TOP_SEARCH);
				System.out.println("Done page:"+page.getPageName());
			}
		}
		catch (IOException | ParseException e) 
		{
			e.printStackTrace();
		}
	}
	/**
	 * Search the section headings as queries
	 */
	private void searchSectionHeadings()
	{
		try
		{
			for(Data.Page page:pagelist)
			{	
				for (List<Data.Section> sectionPath : page.flatSectionPaths()) 
				{
	                String qString = buildSectionQueryStr(page, sectionPath);
	                BooleanQuery query = toQuery(qString);
	                String qID = Data.sectionPathId(page.getPageId(), sectionPath);
	                rankParas(query,qID, TOP_SEARCH);
	            }
				System.out.println("Done page:"+page.getPageName());
			}
		} 
		catch ( IOException | ParseException e) 
		{
			e.printStackTrace();
		}
	}
	public static void main(String[]  args)
	{
		Similarity sim = null;
		String dir = args[0];
		String out_dir = args[1];
		String outline_file = args[2];
		String out_file = args[3];
		int topSearch = Integer.parseInt(args[4]);
		String mode = args[5];
		String method = args[6];
		
		if(method.equals("BM25"))
		{
			System.out.println("Using BM25 for candidate set generation");
			sim = new BM25Similarity();
		}
		else if(method.equals("LM-DS"))
		{
			System.out.println("Using LM-DS for candidate set generation");
			sim = new LMDirichletSimilarity();
		}
		else if(method.equals("LM-JM"))
		{
			System.out.println("Using LM-JM for candidate set generation");
			float lambda = Float.parseFloat(args[7]);
			sim = new LMJelinekMercerSimilarity(lambda);
		}
		else
		{
			System.out.println("Wrong similarity metric");
			System.exit(0);
		}
		QueryIndex ob = new QueryIndex(dir, out_dir, outline_file, out_file, topSearch,method,new StandardAnalyzer(), sim);
		ob.search(mode);
	}

}
