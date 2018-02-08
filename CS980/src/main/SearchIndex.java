package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class SearchIndex 
{
	private static IndexSearcher is = null;
	private static QueryParser qp = null;
	private  static String INDEX_DIR ;
	private  static String OUTPUT_DIR ;
	private  static String CBOR_OUTLINE_FILE ;
	private  static String OUT_FILE ;
	private  static int TOP_SEARCH;
	private  static ArrayList<Data.Page> pagelist;
	
	public SearchIndex(String INDEX_DIR, String OUTPUT_DIR, String CBOR_OUTLINE_FILE, String OUT_FILE, int TOP_SEARCH)throws IOException
	{
		SearchIndex.INDEX_DIR = INDEX_DIR;
		SearchIndex.OUTPUT_DIR = OUTPUT_DIR;
		SearchIndex.CBOR_OUTLINE_FILE = CBOR_OUTLINE_FILE;
		SearchIndex.OUT_FILE = OUT_FILE;
		SearchIndex.TOP_SEARCH = TOP_SEARCH;
		is = createSearcher();
		qp = createParser();
		pagelist = getPageListFromPath(SearchIndex.CBOR_OUTLINE_FILE);
	}
	private static IndexSearcher createSearcher()throws IOException
	{
		Directory dir = FSDirectory.open((new File(INDEX_DIR).toPath()));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity());
        return searcher;
	}
	private static QueryParser createParser()throws IOException
	{
		QueryParser parser = new QueryParser("parabody", new StandardAnalyzer());
		return parser;
	}
	private static TopDocs searchIndex(String query,int n)throws IOException,ParseException
	{
		Query q = qp.parse(query);
		TopDocs tds = is.search(q, n);
		return tds;
	}
	private static void rankParas(Data.Page page, int n) throws IOException, ParseException 
	{
		String qString = page.getPageName();
		System.out.println("Query: " + qString);
		TopDocs tds = searchIndex(qString,n);
		ScoreDoc[] retDocs = tds.scoreDocs;
		createRunFile(qString,tds,retDocs);
	}
	private static void rankParas(Data.Page page, Data.Section section, String parentId, int n) throws IOException, ParseException 
	{
		String qString = parentId+"/"+section.getHeadingId();
		System.out.println("Query: " + qString);
		String query = qString.replaceAll("\\W", " ");
		TopDocs tds = searchIndex(query,n);
		ScoreDoc[] retDocs = tds.scoreDocs;
		createRunFile(qString,tds,retDocs);
	}
	private static void createRunFile(String query, TopDocs tds, ScoreDoc[] retDocs)throws IOException
	{
		ArrayList<String> runStrings = new ArrayList<String>();
		Document d;
		String runFileString;
		String outFilePath = OUTPUT_DIR+"/"+OUT_FILE;
		for (int i = 0; i < retDocs.length; i++) 
		{
			d = is.doc(retDocs[i].doc);
			System.out.println("Doc " + i);
			System.out.println("Score " + tds.scoreDocs[i].score);
			System.out.println(d.getField("paraid").stringValue());
			System.out.println(d.getField("parabody").stringValue() + "\n");
			
			/*runFile string format: $queryId Q0 $paragraphId $rank $score $name*/
			
			runFileString = query+" Q0 "+d.getField("paraid").stringValue()+" "+i+" "+tds.scoreDocs[i].score+" "+"shubham";
			runStrings.add(runFileString);
		}
		
		FileWriter fw = new FileWriter(outFilePath, true);
		for(String runString:runStrings)
			fw.write(runString+"\n");
		fw.close();
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
	public  static void searchPages()
	{
		try
		{
			for(Data.Page page:pagelist)
			{
				rankParas(page, TOP_SEARCH);
				System.out.println("\n" + StringUtils.repeat("=", 128) + "\n");
			}
		}
		catch (IOException | ParseException e) 
		{
			e.printStackTrace();
		}
	}
	public  static void searchSections()
	{
		String parentId = "";
		try
		{
			for(Data.Page page:pagelist)
			{
				parentId = page.getPageId();
				for(Data.Section secL1:page.getChildSections())
				{
					rankParas(page, secL1, parentId, TOP_SEARCH);
					parentId = parentId+"/"+secL1.getHeadingId();
					for(Data.Section secL2:secL1.getChildSections())
					{
						rankParas(page, secL2, parentId, TOP_SEARCH);
					}
				}
			}
		} 
		catch ( IOException | ParseException e) 
		{
			e.printStackTrace();
		}
	}
}
