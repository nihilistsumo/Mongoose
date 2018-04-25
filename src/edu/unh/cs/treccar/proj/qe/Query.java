package edu.unh.cs.treccar.proj.qe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
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

/**
 * Main class to query a lucene index
 * @author Shubham Chatterjee
 *
 */
public class Query 
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
	 * Path to the file containing the stopwords
	 */
	private static String STOP_FILE;
	/**
	 * Path to the word vector file
	 */
	private static String WORD_2_VEC_FILE;
	/**
	 * Method for query expansion
	 * Can be: KNN or RM3 
	 */
	private static String QE_METHOD;
	/**
	 * Method for candidate set generation
	 * Can be: BM25, LM-DS, LM-JM
	 */
	private static String CANDIDATE_SET_METHOD;
	/**
	 * Top search results for a query
	 */
	private static int TOP_SEARCH;
	/**
	 * Number of top documents to use for PRF
	 */
	private static int TOP_FEEDBACK;
	/**
	 * Number of top terms to consider for expansion
	 */
	private static int TOP_TERMS;
	/**
	 * List of pages in the corpus
	 */
	private static ArrayList<Data.Page> pagelist;
	/**
	 * List of paragraph IDs already seen
	 * Used to prevent duplicate documents in the run file
	 * Paragraph is added to the run file only if it is not already in this list
	 */
	private static ArrayList<String> paraID;
	/**
	 * HashMap of (word,vector)
	 */
	private static HashMap<String, ArrayList<Double>> word2vec;
	private static IndexSearcher is;
	private static Analyzer analyzer;
	private static Similarity similarity;
	private static List<String> tokens;
	/**
	 * List of stopwords
	 * To be read from the stopwords file specified
	 */
	private static ArrayList<String> stopWords;
	
	/**
	 * Inner class to search the lucene index for a query
	 * @author Shubham Chatterjee
	 *
	 */
	public final static class Search
	{
		/**
		 * Search a query 
		 * @param dir
		 * @param out_dir
		 * @param outline_file
		 * @param out_file
		 * @param stopFilePath
		 * @param word2vecFile
		 * @param topSearch
		 * @param topFeedback
		 * @param topTerms
		 * @param qe_method
		 * @param cs_method
		 * @param a
		 * @param s
		 */
		public Search(String dir, String out_dir, String outline_file, String out_file, String stopFilePath, String word2vecFile ,int topSearch, int topFeedback, int topTerms, String qe_method, String cs_method, Analyzer a, Similarity s)
		{
			Query.INDEX_DIR = dir;
			Query.OUTPUT_DIR = out_dir;
			Query.CBOR_OUTLINE_FILE = outline_file;
			Query.OUT_FILE = out_file;
			Query.STOP_FILE = stopFilePath;
			Query.WORD_2_VEC_FILE = word2vecFile;
			Query.QE_METHOD = qe_method;
			Query.CANDIDATE_SET_METHOD = cs_method;
			Query.TOP_SEARCH = topSearch;
			Query.TOP_FEEDBACK = topFeedback;
			Query.TOP_TERMS = topTerms;
			Query.pagelist = getPageListFromPath(CBOR_OUTLINE_FILE);
			Query.paraID = new ArrayList<String>();
			Query.analyzer = a;
			Query.similarity = s;
			Query.is = new Index.Setup(INDEX_DIR, "parabody",a ,s).getSearcher();
			Query.tokens = new ArrayList<>(128);
			Query.stopWords = new ArrayList<String>();
			Query.word2vec = new HashMap<String, ArrayList<Double>>();
			System.out.println("Fetching top-"+TOP_SEARCH+" "+"passages");
			if(QE_METHOD.equalsIgnoreCase("KNN-PRF"))
				System.out.println("Using KNN with PRF for QE");
			else if(QE_METHOD.equalsIgnoreCase("KNN-INC"))
				System.out.println("Using Incremental KNN with PRF for QE");
			else if(QE_METHOD.equalsIgnoreCase("KNN-EXT"))
				System.out.println("Using Extended set KNN with PRF for QE");
			else
				System.out.println("Using RM3 for QE");
			
			try 
			{
				getStopWords();
				if(QE_METHOD.equalsIgnoreCase("KNN-PRF") || QE_METHOD.equalsIgnoreCase("KNN-INC") || QE_METHOD.equalsIgnoreCase("KNN-EXT"))
				{
					getWordToVec();
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		/**
		 * Get the word vectors from the file 
		 */
		private void getWordToVec() 
		{
			System.out.println("Getting word vectors");
			BufferedReader reader = null;
			String line;
			int i;
			try 
			{
				reader = new BufferedReader(new FileReader(new File(WORD_2_VEC_FILE)));
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			
			try 
			{
				while((line = reader.readLine()) != null)
				{
					String[] words = line.split(" ");
					ArrayList<Double> vector = new ArrayList<Double>();
					for(i = 1; i < words.length; i++)
					{
						vector.add(Double.parseDouble(words[i]));
					}
					word2vec.put(words[0], vector);
				}
				reader.close();
			} 
			catch (NumberFormatException | IOException e) 
			{
				e.printStackTrace();
			}
			System.out.println("Done");
		}
		/**
		 * Get the stop words from the file
		 * @throws IOException
		 */
		public void getStopWords() throws IOException
		{
			String line;
			BufferedReader reader = new BufferedReader(new FileReader(new File(STOP_FILE)));
			while((line = reader.readLine()) != null )
				stopWords.add(line);
			reader.close();
		}
		/**
		 * Rank the paragraphs for the specified query string and create a run file
		 * @param qString BooleanQuery Query to search in index
		 * @param qID String ID of the Query
		 * @param n Integer Top hits for the query
		 * @throws IOException
		 * @throws ParseException
		 */
		public void rankParas(BooleanQuery qString, String qID, int n) throws IOException, ParseException 
		{
			TopDocs tds = Index.Search.searchIndex(qString,n);
			ScoreDoc[] retDocs = tds.scoreDocs;
			String topTerms = "";
			String originalQuery = qString.toString("parabody");
			if(QE_METHOD.equalsIgnoreCase("RM3"))
			{
				Query.RM3Expand ob = new Query.RM3Expand(tds, TOP_TERMS);
				topTerms = ob.expand();
			}
			else if(QE_METHOD.equalsIgnoreCase("KNN-PRF"))
			{
				Query.KNNExpand ob = new Query.KNNExpand(tds,TOP_TERMS);
				topTerms = ob.PRFExpand(originalQuery);
			}
			else if(QE_METHOD.equalsIgnoreCase("KNN-INC"))
			{
				Query.KNNExpand ob = new Query.KNNExpand(tds,TOP_TERMS);
				topTerms = ob.incrementalExpand(originalQuery);
			}
			else if(QE_METHOD.equalsIgnoreCase("KNN-EXT"))
			{
				Query.KNNExpand ob = new Query.KNNExpand(tds,TOP_TERMS);
				topTerms = ob.bigramExpand(originalQuery);
			}
			if(topTerms != null)
			{
				BooleanQuery query = toQuery(originalQuery,topTerms);
				TopDocs tds1 = Index.Search.searchIndex(query,n);
				ScoreDoc[] retDocs1 = tds1.scoreDocs;
				createRunFile(qID,tds1,retDocs1);
			}
			else
				createRunFile(qID,tds,retDocs);
		}
		/**
		 * Convert a query along with the top expansion terms to a boolean query
		 * @param originalQuery String original query
		 * @param topTerms String Top expansion terms
		 * @return BooleanQuery A boolean query representing the terms in the original query as well as the expansion terms 
		 * @throws IOException
		 */
		private BooleanQuery toQuery(String originalQuery, String topTerms) throws IOException
		{
			String newQuery = "";
			String[] terms = topTerms.split(" ");
			for(String s : terms)
				if(!originalQuery.contains(s))
					newQuery += " " + s;
			
			ArrayList<TermQuery> originalTerms = getQueryTerms(originalQuery);
			ArrayList<TermQuery> newTerms = getQueryTerms(newQuery);
			BooleanQuery.Builder query = new BooleanQuery.Builder();
			for(TermQuery tq : originalTerms)
				query.add(tq,BooleanClause.Occur.MUST);
			
			for(TermQuery tq : newTerms)
				query.add(tq,BooleanClause.Occur.SHOULD);
			
			return query.build();
		}
		/**
		 * Get the terms from a query in the form of term queries
		 * @param queryStr String Query whose terms are required
		 * @return ArrayList<TermQuery> A list which containes the terms in the query as term queries
		 * @throws IOException
		 */
		private ArrayList<TermQuery> getQueryTerms(String queryStr) throws IOException
		{
			ArrayList<TermQuery> terms = new ArrayList<TermQuery>();
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
	        
	        for (String token : tokens) 
	        {
	            terms.add(new TermQuery(new Term("parabody", token)));
	        }
	        return terms;
		}
		/**
		 * Convert a query along  to a boolean query
		 * @param queryStr String  query
		 * @return BooleanQuery A boolean query representing the terms in the original query
		 * @throws IOException
		 */
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
		/**
		 * Create a run file 
		 * Run file string format: $queryId Q0 $paragraphId $rank $score $name
		 * @param queryID String ID of the query
		 * @param tds TopDocs Top hits for the query
		 * @param retDocs ScoreDoc[] Scores of the top hits
		 * @throws IOException
		 */
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
				
				runFileString = queryID+" Q0 "+pID+" "+i+" "+tds.scoreDocs[i].score+" "+CANDIDATE_SET_METHOD+"+"+QE_METHOD;
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
		/**
		 * Get the list of pages 
		 * @param path String Path to the outlines file
		 * @return ArrayList<Data.page> List containing the Pages
		 */
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
						//System.out.println("\n" + StringUtils.repeat("=", 128) + "\n");
		        
		            }
					System.out.println("Done page:"+page.getPageName());
				}
			} 
			catch ( IOException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
	}
	/**
	 * Inner class for Query Expansion
	 * This is a superclass representning general Query Expansion techniques
	 * Subclasses must implement specific query expansion methods and define their own methods
	 * @author Shubham Chatterjee
	 *
	 */
	public static class Expand
	{
		/**
		 * Top documents to be used to PRF
		 */
		protected TopDocs topDocs;
		/**
		 * Score of the top documents
		 */
		protected ScoreDoc[] scores;
		/**
		 * All words in the top documents
		 */
		protected ArrayList<String> words;
		/**
		 * Preprocessed words from the top documents
		 * Preprocess means: lowercase and remove stop words
		 */
		protected ArrayList<String> processedWords;
		
		/**
		 * Expand the query using these top documents for PRF
		 * @param tds TopDocs Top documents for PRF
		 * @throws IOException
		 */
		public Expand(TopDocs tds) throws IOException
		{
			topDocs = tds;
			scores = tds.scoreDocs;
			words = getAllWords();
			Preprocess p = new Preprocess(words);
			processedWords = p.getProcessedText();
		}
		/**
		 * Expand the query without PRF
		 * @throws IOException
		 */
		public Expand() throws IOException
		{
			words = getAllWords();
			Preprocess p = new Preprocess(words);
			processedWords = p.getProcessedText();
		}
		/**
		 * Get all the words in the vocabulary of these top documents
		 * @return ArrayList<String> List of words
		 * @throws IOException
		 */
		private ArrayList<String> getAllWords() throws IOException
		{
			ArrayList<String> words = new ArrayList<String>();
			for (int i = 0; i < TOP_FEEDBACK && i < topDocs.scoreDocs.length; i++) 
			{
				Document d = is.doc(scores[i].doc);
				String text = d.getField("parabody").stringValue();
				String[] w = text.split(" ");
				for(String s : w)
				words.add(s);
			}
			return words;
		}
		/**
		 * Get the top k words in the vocabulary for expansion
		 * @param map Map of (word,score) where score is the score of the word for expansion
		 * @param k Top number of words for expansion
		 * @return String Top words concatenated into a string
		 * @throws IOException
		 */
		protected String getTopWords(Map<String, Float> map, int k) throws IOException
		{
			Map<String, Float>  sortedMap = new LinkedHashMap<String,Float>();
			String topTerms = "";
			int count = 0;
			List<Map.Entry<String, Float>> entries = new ArrayList<Map.Entry<String, Float>>((Collection<? extends Entry<String, Float>>) map.entrySet());
		    Collections.sort(entries,new CustomizedHashMap());
		    
		    for (Map.Entry<String, Float> entry : entries) 
		    	sortedMap.put(entry.getKey(), entry.getValue());
		    
		    for(String s : sortedMap.keySet())
		    {
		    	topTerms += " "+s;
		    	count++;
		    	if(count == k)
		    		break;
		    }
		    return topTerms;
			
		}
		protected String getTopWords1(Map<Query.KNNExpand.BigramTerm, Float> map, int k) throws IOException
		{
			Map<Query.KNNExpand.BigramTerm, Float>  sortedMap = new LinkedHashMap<Query.KNNExpand.BigramTerm,Float>();
			String topTerms = "";
			int count = 0;
			List<Map.Entry<Query.KNNExpand.BigramTerm, Float>> entries = new ArrayList<Map.Entry<Query.KNNExpand.BigramTerm, Float>>((Collection<? extends Entry<Query.KNNExpand.BigramTerm, Float>>) map.entrySet());
		    Collections.sort(entries,new CustomizedHashMap1());
		    
		    for (Map.Entry<Query.KNNExpand.BigramTerm, Float> entry : entries) 
		    	sortedMap.put(entry.getKey(), entry.getValue());
		    
		    for(Query.KNNExpand.BigramTerm s : sortedMap.keySet())
		    {
		    	String w1 = s.getFirstWord();
		    	String w2 = s.getSecondWord();
		    	topTerms += " "+w1+" "+w2;
		    	count++;
		    	if(count == k)
		    		break;
		    }
		    return topTerms;
			
		}
		/**
		 * Class to sort a Map based on the values in descending order
		 * @author Shubham Chatterjee
		 *
		 */
		protected class CustomizedHashMap implements Comparator<Map.Entry<String, Float>> 
		{

			@Override
			public int compare(Entry<String, Float> o1, Entry<String, Float> o2) 
			{
				return -o1.getValue().compareTo(o2.getValue());
			}

		}
		protected class CustomizedHashMap1 implements Comparator<Map.Entry<Query.KNNExpand.BigramTerm, Float>> 
		{

			@Override
			public int compare(Entry<Query.KNNExpand.BigramTerm, Float> o1, Entry<Query.KNNExpand.BigramTerm, Float> o2) 
			{
				return -o1.getValue().compareTo(o2.getValue());
			}

		}
	}
	/**
	 * Inner class to implement Query Expansion using RM3
	 * This is a subclass of Expansion
	 * @author Shubham Chatterjee
	 *
	 */
	public final static class RM3Expand extends Expand
	{
		/**
		 * Top expansion terms
		 */
		private int TOP;
		/**
		 * Expand the query using RM3
		 * @param tds TopDocs Top documents for PRF
		 * @param top Integer Top terms for expansion
		 * @throws IOException
		 */
		public RM3Expand(TopDocs tds, int top) throws IOException
		{
			super(tds);
			TOP = top;
		}
		/**
		 * Get the sum of scores of the documents
		 * This method uses the log-exp trick to prevent underflow or overflow
		 * @return Float Sum of scores of documents
		 */
		private float getSumOfScores()
		{
			float max = getMaxScore();
			float s = 0;
			for (int i = 0; i < TOP_FEEDBACK && i < topDocs.scoreDocs.length; i++) 
				s += Math.exp(topDocs.scoreDocs[i].score - max);
			s = (float) (max + Math.log(s));
			return s;
		}
		/**
		 * Get the maximum score among all documents
		 * @return Float Maximum document score
		 */
		private float getMaxScore()
		{
			float max = topDocs.scoreDocs[0].score;
			for(int i = 1; i < TOP_FEEDBACK && i < topDocs.scoreDocs.length; i++) 
			{
				if( topDocs.scoreDocs[i].score > max )
					max = topDocs.scoreDocs[i].score;
			}
			return max;
		}
		/**
		 * Probabilty of document given query
		 * @param doc Integer Document number
		 * @return Float 
		 */
		private float prob_D_given_Q(int doc)
		{
			float sum = getSumOfScores();;
			return ( topDocs.scoreDocs[doc].score / sum );
		}
		/**
		 * probability of word given the document
		 * @param word String
		 * @param text String Document text
		 * @return Float
		 */
		private float prob_W_given_D(String word, String text)
		{
			int numOfWords = findNumOfWords(text);
			int freqOfWord = findFreqOfWord(word, text);
			
			return (float)freqOfWord / numOfWords;
		}
		/**
		 * Get number of words in the document
		 * @param text String Document text
		 * @return Integer Number of words in the document
		 */
		private int findNumOfWords(String text)
		{
			return text.split(" ").length;
		}
		/**
		 * Find the ferquency of the word in the document
		 * @param word String
		 * @param text String 
		 * @return Integer
		 */
		private int findFreqOfWord(String word, String text)
		{
			String[] words = text.split(" ");
			int count = 0;
			for(String s : words)
				if(s.equalsIgnoreCase(word))
					count++;
			return count;
		}
		/**
		 * Exapnd the query using RM3
		 * @return String Top expansion terms concateneted into a string
		 * @throws IOException
		 */
		public String expand() throws IOException
		{
			Document d;
			String text, topTerms;
			float p_W_given_Q;
			float p_W_given_D;
			float p_D_given_Q;
			
			Map<String, Float> wordToProbMap = new LinkedHashMap<String, Float>();

			for(String word: processedWords)
			{
				p_W_given_Q = 0;
				for (int i = 0; i < TOP_FEEDBACK && i < topDocs.scoreDocs.length; i++) 
				{
					d = is.doc(scores[i].doc);
					text = d.getField("parabody").stringValue();
					p_D_given_Q = prob_D_given_Q(i);	
					p_W_given_D = prob_W_given_D(word, text);
					p_W_given_Q += p_W_given_D * p_D_given_Q;
				}
				wordToProbMap.put(word, p_W_given_Q);
			}
			topTerms = getTopWords(wordToProbMap, TOP);
			return topTerms;
		}
	}
	/**
	 * Inner class for Query Expansion using KNN
	 * Subclass of Expansion
	 * @author Shubham Chatterjee
	 *
	 */
	public final static class KNNExpand extends Expand
	{
		/**
		 * Value of K in KNN
		 */
		private int K;
		/**
		 * Expand the query using KNN and PRF
		 * @param tds TopDocs Top documents for PRF
		 * @param k Integer Value of K in KNN
		 * @throws IOException
		 */
		public KNNExpand(TopDocs tds,int k) throws IOException
		{
			super(tds);
			K = k;
		}
		/**
		 * Expand the query using KNN without PRF
		 * @param k Integer Value of K in KNN
		 * @throws IOException
		 */
		public KNNExpand(int k) throws IOException
		{
			super();
			K = k;
		}
		/**
		 * Expand the query
		 * @param query String Query to expand
		 * @return String Top expansion terms concateneted into a string
		 * @throws IOException
		 */
		public String PRFExpand(String query) throws IOException
		{
			String[] qTerms = query.split(" ");
			LinkedHashMap<String,ArrayList<Double>> queryVectorMap = getQueryVector(qTerms);
			LinkedHashMap<String,ArrayList<Double>> candidateMap = getCandidateSet2(qTerms);
			HashMap<String, Float> scores = new HashMap<String, Float>();
			float sum = 0.0f;
			String topTerms;
			
			if(candidateMap != null) 
			{
				for(String t : candidateMap.keySet())
				{
					ArrayList<Double> vector = candidateMap.get(t);
					for(String q : queryVectorMap.keySet())
					{
						if(vector != null && queryVectorMap.get(q) != null)
							sum += findSimilarity(vector, queryVectorMap.get(q));
					}
					scores.put(t, (sum / qTerms.length));
				}
				topTerms = getTopWords(scores, K);
				return topTerms;
			}
			else
				return null;
		}
		public String bigramExpand(String query) throws IOException
		{
			String[] qTerms = query.split(" ");
			LinkedHashMap<BigramTerm,ArrayList<Double>> bigramMap = getBigrams(qTerms);
			LinkedHashMap<String,ArrayList<Double>> queryVectorMap = getQueryVector(qTerms);
			LinkedHashMap<String,ArrayList<Double>> candidateMap = getCandidateSet2(qTerms);
			if(candidateMap != null && bigramMap != null)
			{
				int size = bigramMap.size() + candidateMap.size();
				
				String topTerms1 = getCandidateTopTerms(candidateMap,queryVectorMap, size);
				String topTerms2 = getBigramTopTerms(bigramMap, queryVectorMap, size);
				String topTerms = topTerms1+" "+topTerms2;
				return topTerms;
			}
			return null;
		}
		private String getBigramTopTerms(LinkedHashMap<BigramTerm,ArrayList<Double>> bigramMap, LinkedHashMap<String,ArrayList<Double>> queryVectorMap, int size) throws IOException
		{
			String topTerms="";
			float sum = 0.0f;
			HashMap<BigramTerm, Float> scores = new HashMap<BigramTerm, Float>();
			if(bigramMap != null) 
			{
				for(BigramTerm t : bigramMap.keySet())
				{
					ArrayList<Double> vector = bigramMap.get(t);
					for(String q : queryVectorMap.keySet())
					{
						if(vector != null && queryVectorMap.get(q) != null)
							sum += findSimilarity(vector, queryVectorMap.get(q));
					}
					scores.put(t, (sum / size));
				}
				topTerms = getTopWords1(scores, K);
			}
			return topTerms;
			
			
		}
		private String getCandidateTopTerms(LinkedHashMap<String,ArrayList<Double>> candidateMap, LinkedHashMap<String,ArrayList<Double>> queryVectorMap, int size) throws IOException
		{
			String topTerms = "";
			float sum = 0.0f;
			HashMap<String, Float> scores = new HashMap<String, Float>();
			if(candidateMap != null) 
			{
				for(String t : candidateMap.keySet())
				{
					ArrayList<Double> vector = candidateMap.get(t);
					for(String q : queryVectorMap.keySet())
					{
						if(vector != null && queryVectorMap.get(q) != null)
							sum += findSimilarity(vector, queryVectorMap.get(q));
					}
					scores.put(t, (sum / size));
				}
				topTerms = getTopWords(scores, K);
			}
			return topTerms;
		}
		
		private LinkedHashMap<Query.KNNExpand.BigramTerm,ArrayList<Double>> getBigrams(String[] qTerms)
		{
			LinkedHashMap<BigramTerm,ArrayList<Double>> bigramMap = new LinkedHashMap<BigramTerm,ArrayList<Double>>();
			for(int i = 0; i < qTerms.length - 1; i++)
			{
				String w1 = qTerms[i];
				String w2 = qTerms[i+1];
				ArrayList<Double> v1 = word2vec.get(w1);
				ArrayList<Double> v2 = word2vec.get(w2); 
				if( v1 != null && v2 != null )
				{
					BigramTerm b = new BigramTerm(w1,v1,w2,v2);
					ArrayList<Double> vector = b.getBigramEmbedding();
					bigramMap.put(b, vector);
				}
			}
			return bigramMap;
		}
		
		private static class BigramTerm
		{
			private String word1;
			private String word2;
			private ArrayList<Double> vector1;
			private ArrayList<Double> vector2;
			
			public BigramTerm(String w1, ArrayList<Double> v1, String w2, ArrayList<Double> v2)
			{
				word1 = w1;
				word2 = w2;
				vector1 = new ArrayList<Double>(v1);
				vector2 = new ArrayList<Double>(v2);
			}
			
			public String getFirstWord()
			{
				return word1;
			}
			
			public String getSecondWord()
			{
				return word2;
			}
			
			public ArrayList<Double> getFirstVector()
			{
				return vector1;
			}
			
			public ArrayList<Double> getSecondVector()
			{
				return vector2;
			}
			
			public ArrayList<Double> getBigramEmbedding()
			{
				ArrayList<Double> vector = new ArrayList<Double>();
				if(vector1.size() != vector2.size())
				{
					System.out.println("Vectors of different length! Cannot add them!");
					System.exit(0);
				}
				for(int i = 0; i < vector1.size(); i++)
					vector.add(vector1.get(i) + vector2.get(i));
				return vector;
			}
		}
		/**
		 * Expand the query using incremental expansion
		 * @param query String
		 * @return String
		 * @throws IOException
		 */
		public String incrementalExpand(String query) throws IOException
		{
			String[] qTerms = query.split(" ");
			LinkedHashMap<String,ArrayList<Double>> queryVectorMap = getQueryVector(qTerms);
			LinkedHashMap<String,ArrayList<Double>> candidateMap = getCandidateSet1(qTerms);
			HashMap<String, Float> scores = new HashMap<String, Float>();
			float sum = 0.0f;
			String topTerms;
			
			if(candidateMap != null) 
			{
				for(String t : candidateMap.keySet())
				{
					ArrayList<Double> vector = candidateMap.get(t);
					for(String q : queryVectorMap.keySet())
					{
						if(vector != null && queryVectorMap.get(q) != null)
							sum += findSimilarity(vector, queryVectorMap.get(q));
					}
					scores.put(t, (sum / qTerms.length));
				}
				topTerms = getTopWords(scores, K);
				return topTerms;
			}
			else
				return null;
		}
		/**
		 * Find the similarity between two vectors
		 * @param vec1 ArrayList<Double> First vector
		 * @param vec2 ArrayList<Double> Second vector
		 * @return Double cosine similarity between the two vectors
		 */
		private float findSimilarity(ArrayList<Double> vec1, ArrayList<Double> vec2)
		{
			if(vec1.size() != vec2.size())
			{
				System.out.println("Vectors of different length! Cannot find similarity!");
				System.exit(0);
			}
			int i, size = vec1.size();
			float score, numerator = 0.0f, denominator = 0.0f;
			for(i = 0; i < size; i++)
			{
				numerator += vec1.get(i) * vec2.get(i);
			}
			denominator = getNorm(vec1) * getNorm(vec2);
			score = numerator / denominator;
			return score;
		}
		/**
		 * Find the L1 norm of the given vector
		 * @param vector ArrayList<Double> Vector to find norm
		 * @return Double L1 norm of the vector
		 */
		private float getNorm(ArrayList<Double> vector)
		{
			float norm = 0.0f;
			for(int i = 0; i < vector.size(); i++)
			{
				norm += vector.get(i) * vector.get(i);
			}
			norm = (float) Math.sqrt(norm);
			return norm;
		}
		/**
		 * Get the word vectors for every word in the query
		 * @param terms String[] Query terms
		 * @return LinkedHashMap<String,ArrayList<Double>> HashMap of (word,vector)
		 */
		private LinkedHashMap<String,ArrayList<Double>> getQueryVector(String[] terms)
		{
			LinkedHashMap<String,ArrayList<Double>> queryVectorMap = new LinkedHashMap<String,ArrayList<Double>>();
			for(String s : terms)
				queryVectorMap.put(s, word2vec.get(s));
			return queryVectorMap;
		}
		/**
		 * Get the candidate set of words in the query to consider for expansion
		 * @param terms String[] Query terms
		 * @return LinkedHashMap<String,ArrayList<Double>> HashMap of (word,vector)
		 */
		private LinkedHashMap<String,ArrayList<Double>> getCandidateSet2(String[] terms)
		{
			LinkedHashMap<String,ArrayList<Double>> candidateMap = new LinkedHashMap<String,ArrayList<Double>>();
			for(String s : terms)
			{
				LinkedHashMap<String,ArrayList<Double>> neighbours = getNearestNeighbours(s);
				if(neighbours != null)
				{
					for( String s1 : neighbours.keySet() )
						candidateMap.put(s1, neighbours.get(s1));
				}
			}
			if(!candidateMap.isEmpty())
				return candidateMap;
			return null;
		}
		/**
		 * Get the candidate set of words in the query to consider for expansion
		 * @param terms String[] Query terms
		 * @return LinkedHashMap<String,ArrayList<Double>> HashMap of (word,vector)
		 */
		private LinkedHashMap<String,ArrayList<Double>> getCandidateSet1(String[] terms)
		{
			LinkedHashMap<String,ArrayList<Double>> candidateMap = new LinkedHashMap<String,ArrayList<Double>>();
			for(String s : terms)
			{
				LinkedHashMap<String,ArrayList<Double>> neighbours = getNearestNeighbours(s);
				
				if(neighbours != null)
				{
					LinkedHashMap<String,ArrayList<Double>> prunedNeighbours = prune(neighbours);
					if(prunedNeighbours != null)
					{
						for( String s1 : prunedNeighbours.keySet() )
							candidateMap.put(s1, prunedNeighbours.get(s1));
					}
				}
			}
			if(!candidateMap.isEmpty())
				return candidateMap;
			return null;
		}
		/**
		 * Method to prune the nearest neighbours
		 * @param neighbours LinkedHashMap<String,ArrayList<Double>>
		 * @return LinkedHashMap<String,ArrayList<Double>>
		 */
		private LinkedHashMap<String,ArrayList<Double>> prune(LinkedHashMap<String,ArrayList<Double>> neighbours) 
		{
			LinkedHashMap<String,ArrayList<Double>> prunedNeighbours = new LinkedHashMap<String,ArrayList<Double>>();
			LinkedHashMap<String,ArrayList<Double>> tempNeighbours = new LinkedHashMap<String,ArrayList<Double>>(neighbours);
			
			for(int i = 1; i <= 5; i++)
			{
				int count = 0;
				LinkedHashMap<String,ArrayList<Double>> temp = new LinkedHashMap<String,ArrayList<Double>>();
				if(tempNeighbours != null)
				{
					for( String s1 : tempNeighbours.keySet() )
					{
						temp.put(s1, tempNeighbours.get(s1));
						count++;
						if(count == 20)
							break;
					}
					Iterator<Entry<String, ArrayList<Double>>> it = temp.entrySet().iterator();
					if(it.hasNext())
					{
						Map.Entry<String,ArrayList<Double>> entry = it.next();
						String key = entry.getKey();
						ArrayList<Double> value = entry.getValue();
						prunedNeighbours.put(key,value);
						temp.remove(key);
						tempNeighbours = reorder(value,temp);
					}
					else
						return null;
				}
			}
			return prunedNeighbours;
		}
		/**
		 * Method to reorder the terms relative to a given term according to similarity
		 * @param value ArrayList<Double>
		 * @param neighbours LinkedHashMap<String,ArrayList<Double>>
		 * @return temp LinkedHashMap<String,ArrayList<Double>>
		 */
		private LinkedHashMap<String,ArrayList<Double>> reorder(ArrayList<Double> value, LinkedHashMap<String,ArrayList<Double>> neighbours)
		{
			LinkedHashMap<String, ArrayList<Double>>  temp = new LinkedHashMap<String,ArrayList<Double>>();
			LinkedHashMap<String, Float>  sortedMap = new LinkedHashMap<String,Float>();
			HashMap<String, Float> scores = new HashMap<String, Float>();
			for(String t : neighbours.keySet())
			{
				ArrayList<Double> vector = neighbours.get(t);
				if(vector != null)
					scores.put(t, findSimilarity(vector, value));
			}
			List<Map.Entry<String, Float>> entries = new ArrayList<Map.Entry<String, Float>>((Collection<? extends Entry<String, Float>>)scores.entrySet());
			Collections.sort(entries,new Expand.CustomizedHashMap());
		    
		    for (Map.Entry<String, Float> entry : entries) 
		    	sortedMap.put(entry.getKey(), entry.getValue());
		    
		    for(String str : sortedMap.keySet())
		    	temp.put(str,word2vec.get(str));
			return temp;
		}
		/**
		 * Get the nearest neighbours of a term in embedding space
		 * @param s String Term to get nearest neighbour
		 * @return LinkedHashMap<String,ArrayList<Double>> HashMap of (word,vector) which are nearest to given term
		 */
		private LinkedHashMap<String,ArrayList<Double>> getNearestNeighbours(String s)
		{
			HashMap<String, Double> distanceMap = new HashMap<String, Double>();
			HashMap<String, Double> sortedDistanceMap = new HashMap<String, Double>();
			LinkedHashMap<String, ArrayList<Double>> neighbours = new LinkedHashMap<String, ArrayList<Double>>();
			ArrayList<Double> queryVector = word2vec.get(s);
			if(queryVector != null)
			{
				int count = 0;
				
				for(String str : processedWords)
				{
					if(!str.equals(s) && word2vec.containsKey(str))
					{
						ArrayList<Double> wordVector = word2vec.get(str);
						double distance = getDistance(queryVector, wordVector);
						distanceMap.put(str, distance);
					}
				}
				List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>((Collection<? extends Entry<String, Double>>) distanceMap.entrySet());
			    Collections.sort(entries,new CustomizedHashMap());
			    
			    for (Map.Entry<String, Double> entry : entries) 
			    	sortedDistanceMap.put(entry.getKey(), entry.getValue());
			    
			    for(String str : sortedDistanceMap.keySet())
			    {
			    	neighbours.put(str,word2vec.get(str));
			    	count++;
			    	if(count == K)
			    		break;
			    }
			    return neighbours;
			}
			else
			return null;
		}
		/**
		 * Get distance between two vectors
		 * @param vec1 ArrayList<Double> First vector
		 * @param vec2 ArrayList<Double> Second vector
		 * @return Double distance between the two vectors
		 */
		private double getDistance(ArrayList<Double> vec1, ArrayList<Double> vec2)
		{
			if(vec1.size() != vec2.size())
			{
				System.out.println("Vectors of different length! Cannot find distance!");
				System.exit(0);
			}
			int i , size = vec1.size();
			double sum = 0.0d;
			for(i =0; i < size; i++)
			{
				sum += Math.pow((vec1.get(i) - vec2.get(i)), 2);
			}
			return Math.sqrt(sum);
		}
		private class CustomizedHashMap implements Comparator<Map.Entry<String, Double>> 
		{

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) 
			{
				return o1.getValue().compareTo(o2.getValue());
			}

		}
	}
	/**
	 * Inner class to pre-process the data 
	 * @author Shubham Chatterjee
	 *
	 */
	public final static class Preprocess
	{
		/**
		 * Hold the preprocessed words
		 */
		private ArrayList<String> processedWords;
		/**
		 * Holds the original words
		 */
		private ArrayList<String> words;
		
		public Preprocess(ArrayList<String> text)
		{
			words = lowerCaseWords(text);
			processedWords = removeStopWords(words);
		}
		/**
		 * Lowercase the words
		 * @param words ArrayList<String> Words to lowercase
		 * @return ArrayList<String> Lowercased words
		 */
		private ArrayList<String> lowerCaseWords(ArrayList<String> words)
		{
			ArrayList<String> newWords = new ArrayList<String>();
			for(String s : words)
				newWords.add(s.replaceAll("[^a-zA-Z ]", "").toLowerCase());
			return newWords;
		}
		/**
		 * Remove stop words from the list
		 * @param words ArrayList<String> Words from which stopwords are to be removed
		 * @return ArrayList<String>
		 */
		private ArrayList<String> removeStopWords(ArrayList<String> words)
		{
			ArrayList<String> newWords = new ArrayList<String>();
			for( String s : words )
				if(!stopWords.contains(s))
					newWords.add(s);
			return newWords;
		}
		/**
		 * Get the preprocessed text
		 * @return ArrayList<String>
		 */
		public ArrayList<String> getProcessedText()
		{
			return processedWords;
		}
	}
	public static void main(String[]  args)
	{
		Similarity sim = null;

		String dir = args[0];
		String out_dir = args[1];
		String outline_file = args[2];
		String out_file = args[3];
		String stopFilePath = args[4];
		String word2vecFile = args[5];
		int topSearch = Integer.parseInt(args[6]);
		int topFeedback = Integer.parseInt(args[7]);
		int topTerms = Integer.parseInt(args[8]);
		String mode = args[9];
		String qe_method = args[10];
		String cs_method = args[11];

		
		if(cs_method.equals("BM25"))
		{
			System.out.println("Using BM25 for candidate set generation");
			sim = new BM25Similarity();
		}
		else if(cs_method.equals("LM-DS"))
		{
			System.out.println("Using LM-DS for candidate set generation");
			sim = new LMDirichletSimilarity();
		}
		else if(cs_method.equals("LM-JM"))
		{
			System.out.println("Using LM-JM for candidate set generation");
			float lambda = Float.parseFloat(args[12]);
			sim = new LMJelinekMercerSimilarity(lambda);
		}
		else
		{
			System.out.println("Wrong similarity metric");
			System.exit(0);
		}
		Query.Search ob = new Query.Search(dir, out_dir, outline_file, out_file, stopFilePath, word2vecFile, topSearch, topFeedback, topTerms, qe_method, cs_method, new StandardAnalyzer(), sim);
		ob.search(mode);
	}
}

