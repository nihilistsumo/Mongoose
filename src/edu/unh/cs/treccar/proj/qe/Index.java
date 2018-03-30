package edu.unh.cs.treccar.proj.qe;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class Index 
{
	private  static String INDEX_DIR ;
	private  static String CBOR_FILE ;
	private static int COUNT;
	private  static IndexSearcher is = null;
	private  static QueryParser qp = null;
	public  final static class Build
	{
		private static HashMap<String,Float> pageRankScore;
		private String PAGE_RANK_FILE;
		public Build(String INDEX_DIR,String CBOR_FILE,String page_rank_file) throws IOException
		{
			Index.INDEX_DIR = INDEX_DIR;
			Index.CBOR_FILE = CBOR_FILE;
			PAGE_RANK_FILE = page_rank_file;
			COUNT = 0;
			pageRankScore = new HashMap<String,Float>();
			getPageRankScores();
		}
		private void getPageRankScores() throws IOException
		{
			BufferedReader reader = new BufferedReader(new FileReader(new File(PAGE_RANK_FILE)));
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			System.out.println("Reading pagerank socres");
			while((line = reader.readLine()) != null)
			{
				line = line.trim();
				String paraId = line.split(" ")[1];
				float score = Float.parseFloat(line.split(" ")[2]);
				pageRankScore.put(paraId, score);
			}
			System.out.println("done");
			br.readLine();
			reader.close();
		}
		private static IndexWriter createWriter(Analyzer analyzer)throws IOException
		{
			Directory indexdir = FSDirectory.open((new File(INDEX_DIR)).toPath());
			IndexWriterConfig conf = new IndexWriterConfig(analyzer);
			conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			IndexWriter iw = new IndexWriter(indexdir, conf);
			return iw;
		}
		private static Document createDocument2(String entity, String text, String id)
		{
			
			COUNT++;
			Document doc = new Document();
			doc.add(new StringField("paraentity", entity, Field.Store.YES));
			doc.add(new TextField("parabody", text, Field.Store.YES));
			doc.add(new StringField("paraid", id, Field.Store.YES));
			System.out.println(id);
			return doc;
		}
		private static Document createDocument(Data.Paragraph para)
		{
			COUNT++;
			Document paradoc = new Document();
			/*List<String> entity = para.getEntitiesOnly();
			String entityString = "";
			for(String s : entity)
			{
				s = s.replaceAll("\\s+", "_").toLowerCase();
				entityString += " "+s;
			}
			entity = null;*/
			paradoc.add(new StringField("paraid", para.getParaId(), Field.Store.YES));
			//paradoc.add(new StringField("paraentity", entityString, Field.Store.YES));
			paradoc.add(new TextField("parabody", para.getTextOnly(), Field.Store.YES));
			if(pageRankScore.containsKey(para.getParaId()))
			{
				paradoc.add(new FloatDocValuesField("paraboost", pageRankScore.get(para.getParaId())));
			}
			System.out.println(para.getParaId());
			return paradoc;
		}
		public static void createIndex(Analyzer analyzer)throws IOException
		{
			IndexWriter writer = createWriter(analyzer);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(CBOR_FILE)));
			
			for(Data.Paragraph paragraph : DeserializeData.iterableParagraphs(bis))
			{
				try 
				{
					writer.addDocument(createDocument(paragraph));
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			writer.close();
		}
		public static void createIndex2(Analyzer analyzer)throws IOException
		{
			IndexWriter writer = createWriter(analyzer);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(CBOR_FILE)));
			
			for(Data.Paragraph paragraph : DeserializeData.iterableParagraphs(bis))
			{
				String t = paragraph.getTextOnly();
				String id = paragraph.getParaId();
				for(Data.ParaBody body: paragraph.getBodies())
				{
					if(body instanceof Data.ParaLink) 
					{
						String s = ((Data.ParaLink) body).getPageId();
						try 
						{
							writer.addDocument(createDocument2(s,t,id));
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
				}
			}
			writer.close();
		}
	}
	public final static class Setup
	{
		public Setup(String INDEX_DIR)
		{
			Index.INDEX_DIR = INDEX_DIR;
			try 
			{
				is = createSearcher();
				qp = createParser();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		public Setup(String INDEX_DIR, String field, Analyzer analyzer, Similarity sim)
		{
			Index.INDEX_DIR = INDEX_DIR;
			try 
			{
				is = createSearcher(sim);
				qp = createParser(field, analyzer);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		private IndexSearcher createSearcher(Similarity sim)throws IOException
		{
			Directory dir = FSDirectory.open((new File(INDEX_DIR).toPath()));
	        IndexReader reader = DirectoryReader.open(dir);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        searcher.setSimilarity(sim);
	        return searcher;
		}
		private IndexSearcher createSearcher()throws IOException
		{
			Directory dir = FSDirectory.open((new File(INDEX_DIR).toPath()));
	        IndexReader reader = DirectoryReader.open(dir);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        searcher.setSimilarity(new BM25Similarity());
	        return searcher;
		}
		private QueryParser createParser()throws IOException
		{
			QueryParser parser = new QueryParser("parabody", new StandardAnalyzer());
			return parser;
		}
		private QueryParser createParser(String field, Analyzer analyzer)throws IOException
		{
			QueryParser parser = new QueryParser(field, analyzer);
			return parser;
		}
		public IndexSearcher getSearcher()
		{
			return is;
		}
		public QueryParser getParser()
		{
			return qp;
		}
	}
	public final static class Search
	{
		public static TopDocs searchIndex(String query,int n)throws IOException,ParseException
		{
			Query q = qp.parse(query);
			TopDocs tds = is.search(q, n);
			return tds;
		}
		public static TopDocs searchIndex(BooleanQuery query,int n)throws IOException,ParseException
		{
			TopDocs tds = is.search(query, n);
			return tds;
		}
		public static Document searchIndex(String field,String query)throws IOException,ParseException
		{
			Term term = new Term(field,query);
			Query q = new TermQuery(term);
			TopDocs tds = is.search(q,1);
			ScoreDoc[] retDocs = tds.scoreDocs;
			Document d = is.doc(retDocs[0].doc);
			return d;
		}
		public static HashMap<Document, Float> searchIndex(String field,String query,int n)throws IOException,ParseException
		{
			HashMap<Document,Float> results = new HashMap<Document,Float>();
			Term term = new Term(field,query);
			Query q = new TermQuery(term);
			TopDocs tds = is.search(q,n);
			ScoreDoc[] retDocs = tds.scoreDocs;
			for (int i = 0; i < retDocs.length; i++) 
				results.put(is.doc(retDocs[i].doc),tds.scoreDocs[i].score);
			return results;
		}
	}
	public static int getIndexSize()
	{
		return COUNT;
	}
	public static void main(String[] args) throws IOException
	{
		System.out.println("Building index.....");
		String indexDir = args[0];
		String cborDir = args[1];
		String pageRankFile = args[2];
		new Index.Build(indexDir,cborDir, pageRankFile);
		Index.Build.createIndex(new StandardAnalyzer());
		System.out.println("Number of paragraphs indexed = "+Index.getIndexSize());
	}
}
