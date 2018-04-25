package edu.unh.cs.treccar.proj.entrecog;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.Messaging.SyncScopeHelper;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.ArrayList;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;

/*
 * @author Ajesh Vijayaragavan
 *
 */

public class QueryProcessor {

    static class MyQueryBuilder {

        private final StandardAnalyzer analyzer;
        private List<String> tokens;

        public MyQueryBuilder(StandardAnalyzer standardAnalyzer){
            analyzer = standardAnalyzer;
            tokens = new ArrayList<String>(128);
        }

        public BooleanQuery toQuery(String queryStr) throws IOException {

            TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(queryStr));
            tokenStream.reset();
            tokens.clear();
            while (tokenStream.incrementToken()) {
                final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
                tokens.add(token);
            }
            tokenStream.end();
            tokenStream.close();
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            for (String token : tokens) {
                booleanQuery.add(new TermQuery(new Term("text", token)), BooleanClause.Occur.SHOULD);
            }
            return booleanQuery.build();
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("file.encoding", "UTF-8");

        String indexPath = args[1];
        if (args.length < 3) {
        	System.out.println("Format: Outlinescborfile IndexPath Hierarchicaloutfile");
        	System.exit(-1);
        }
        
        	//1) Similarity:BM25
	        JSONObject obj1;
	        JSONObject obj2;
	        JSONArray jarr1;
	        JSONArray jarr2;
        	String url = null;
        	String url2 = null;
        	String str1 = null;
        	String str3 = null;
        	String str4 = null;
        	
        	String searchstring = null;
        	String searchstring2 = null;
        	String searchstring3 = null;
        	
        	List<String> list1 = new ArrayList<String>();
            IndexSearcher searcher = setupIndexSearcher(indexPath, "paragraph.lucene");
            searcher.setSimilarity(new BM25Similarity());
            final MyQueryBuilder queryBuilder = new MyQueryBuilder(new StandardAnalyzer());
            final String pagesFile = args[0];
            List<String> paragraphids1 = new ArrayList<String>();
            
            List<String> queryId3 = new ArrayList<String>();
                 
            PrintWriter out1 = new PrintWriter(new FileWriter(args[2]));
            final FileInputStream fileInputStream1 = new FileInputStream(new File(pagesFile));
            int count = 0;
            
            //For Pagename/Heading/Heading1.1
            for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream1)) {
            	for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
                //final String queryId = page.getPageId();
            	String queryId2 = null;
                
                queryId2 = Data.sectionPathId(page.getPageId(), sectionPath);
                
                
                if (!queryId3.contains(queryId2))  {
                //queryStr2 = buildSectionQueryStrtls(page, sectionPath);
                String queryStr2 = buildSectionQueryStrtls(page, sectionPath);

                String[] arr1 = queryId2.split("/");
                //System.out.println(arr1[arr1.length-1]);                
                
                searchstring = arr1[arr1.length-1];
                
                searchstring2 = searchstring.replaceAll("%20", "+");
                searchstring2 = searchstring2.replaceAll("-", "+");
                
                //2) Datamuse - Adding grammatically related terms
	            url = "https://api.datamuse.com/words?ml=" + searchstring2;
          	
	           	try {
	                     HttpClient client = HttpClientBuilder.create().build();
	                     HttpGet request = new HttpGet(url);
	                     HttpResponse response = client.execute(request);
	                     //System.out.println(response);
	          
	                     int responseCode = response.getStatusLine().getStatusCode();

	                     BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

	                     String line = "";
	                     StringBuilder sb = new StringBuilder();
	                     while ((line = rd.readLine()) != null) {

	                    	 sb.append(line);
	                     }
	                                         
						try 
						{
	
							jarr1 = new JSONArray(sb.toString());
							//System.out.println(jarr1);
							
							for(int i=0; i<jarr1.length(); i++)
					        {
								str1 = jarr1.getJSONObject(i).getString("word");
								list1.add(str1);
								
					        }
												
						}
						catch (JSONException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							          
	                 }
	                 
	           			catch (ClientProtocolException e) {
	                     e.printStackTrace();
	                    } 
	           			catch (UnsupportedOperationException e) {
	                     e.printStackTrace();
	           			} 
	           			catch (IOException e) {
	                     e.printStackTrace();
	           			}
	           	
	           	//3) Adding terms with ConceptNet's Subject and Object
	           		           	
	           	searchstring3 = searchstring.replaceAll("%20", "_").toLowerCase();
	           	searchstring3 = searchstring3.replaceAll("-", "_").toLowerCase();
	           	
	           	url2 = "http://api.conceptnet.io/c/en/" + searchstring3;
	           		          	
	           	try {
	                     HttpClient client = HttpClientBuilder.create().build();
	                     HttpGet request = new HttpGet(url2);
	                     HttpResponse response = client.execute(request);
	                     //System.out.println(response);
	          
	                     int responseCode = response.getStatusLine().getStatusCode();

	                     BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

	                     String line = "";
	                     StringBuilder sb = new StringBuilder();
	                     while ((line = rd.readLine()) != null) {

	                    	 sb.append(line);
	                     }
	                     //System.out.println(sb);
	                    	                    	                    
						try 
						{
							obj1 = new JSONObject(sb.toString());
							//obj2 = obj1.getJSONArray("edges").getJSONObject(0);
							jarr2 = obj1.getJSONArray("edges");
							
							for(int i=0; i<jarr2.length(); i++)
					        {
								obj2 = jarr2.getJSONObject(i);
								str3 = obj2.getJSONObject("start").getString("label");
								str4 = obj2.getJSONObject("end").getString("label");
								
								list1.add(str3);
								list1.add(str4);
								//queryStr2 = queryStr2 + str3 +" " + str4 +" ";
					        }
							searchstring = searchstring3.replaceAll("_", " ").toLowerCase() + list1;
							//System.out.println(queryBuilder.toQuery(searchstring));
							
						}
						catch (JSONException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							          
	                 } 
	           			catch (ClientProtocolException e) {
	                     e.printStackTrace();
	                    } 
	           			catch (UnsupportedOperationException e) {
	                     e.printStackTrace();
	           			} 
	           			catch (IOException e) {
	                     e.printStackTrace();
	           			}
	           	
	           	//Hierarchical sections and ranking of 200 paragraphs
                TopDocs tops = searcher.search(queryBuilder.toQuery(searchstring), 200);
                ScoreDoc[] scoreDoc = tops.scoreDocs;
                //String paragraphid2 = null;
                
                for (int i = 0; i < scoreDoc.length; i++) {
                    ScoreDoc score = scoreDoc[i];
                    final Document doc = searcher.doc(score.doc);
                    final String paragraphid = doc.getField("paragraphid").stringValue();
                    final float searchScore = score.score;
                    final int searchRank = i+1;
                    
                    if (!paragraphids1.contains(paragraphid))  {
                    	out1.println(queryId2+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" JOINED");
                    	//System.out.println(queryId+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" JOINED");
                    	paragraphids1.add(paragraphid);
                    }
                    
                	}
                queryId3.add(queryId2);
                searchstring = "";
            	searchstring2 = "";
            	searchstring3 = "";
            	paragraphids1.clear();
                list1.clear();                
            	}
            }           	
                count++;
                //System.out.println(count);
            }
            
        out1.close();
        	
        	//Analyzing section headers
	        PrintWriter out2 = new PrintWriter(new FileWriter(args[3]));
	        String queryId2 = null;
	        String queryStr2 = null;
	        final FileInputStream fileInputStream2 = new FileInputStream(new File(pagesFile));
	        for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream2)) {
	            for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
	                queryId2 = Data.sectionPathId(page.getPageId(), sectionPath);   
	                queryStr2 = buildSectionQueryStrlh(page, sectionPath);
	            }
	                TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr2), 100);
	                ScoreDoc[] scoreDoc = tops.scoreDocs;
	                String paragraphid2 = null;
	                for (int i = 0; i < scoreDoc.length; i++) {
	                    ScoreDoc score = scoreDoc[i];
	                    final Document doc = searcher.doc(score.doc);
	                    final String paragraphid = doc.getField("paragraphid").stringValue();
	                    final float searchScore = score.score;
	                    final int searchRank = i+1;
	                    if (paragraphid != paragraphid2)
	                    	//out2.println(queryId2+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" Lucene-BM25");
	                    paragraphid2 = paragraphid;
	                }
	            //}
	        }
	        out2.close();
 
            PrintWriter out3 = new PrintWriter(new FileWriter(args[4]));
            List<String> queryStr4 = new ArrayList<String>();
            final FileInputStream fileInputStream3 = new FileInputStream(new File(pagesFile));
            for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream3)) {
                for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
                    final String queryId4 = Data.sectionPathId(page.getPageId(), sectionPath);
                    String queryStr3 = buildSectionQueryStrihc(page, sectionPath);
                    if(!queryStr4.contains(queryStr3)) {
                    TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr3), 100);
                    ScoreDoc[] scoreDoc = tops.scoreDocs;
                    List<String> paragraphid2 = new ArrayList<String>();
                    for (int i = 0; i < scoreDoc.length; i++) {
                        ScoreDoc score = scoreDoc[i];
                        final Document doc = searcher.doc(score.doc);
                        final String paragraphid = doc.getField("paragraphid").stringValue();
                        final float searchScore = score.score;
                        final int searchRank = i+1;                        
                        if (paragraphid2 != null) {
                        	if(!paragraphid2.contains(paragraphid)) {
                        		//out3.println(queryId3+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" Lucene-BM25");
                        		paragraphid2.add(paragraphid);
                        	}
                        	else {
                        		//System.out.println(queryId3+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" Lucene-BM25");
                        	}
                        }
                    }
                    queryStr4.add(queryStr3.toString());
                    }
                }
            }
            out3.close();
    }
    
    @NotNull
    private static IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
        Directory indexDir = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(indexDir);
        return new IndexSearcher(reader);
    }
    
    @NotNull
    private static String buildSectionQueryStrpn(Data.Page page, List<Data.Section> sectionPath) {
        StringBuilder queryStr1 = new StringBuilder();
        queryStr1.append(page.getPageName());
        return queryStr1.toString();
    }
    
    @NotNull   
    private static String buildSectionQueryStrihc(Data.Page page, List<Data.Section> sectionPath) {
        StringBuilder queryStr3 = new StringBuilder();
        for (Data.Section section: sectionPath) {
            queryStr3.append(" ").append(section.getHeading());
        }
        return queryStr3.toString();
    }

    @NotNull
    private static String buildSectionQueryStrlh(Data.Page page, List<Data.Section> sectionPath) {
    	Section queryStr2;
        queryStr2 = sectionPath.get(sectionPath.size() - 1);
        return queryStr2.toString();
    }
    
    @NotNull
    private static String buildSectionQueryStrtls(Data.Page page, List<Data.Section> sectionPath) {
    	Section queryStr2;
        //queryStr2 = sectionPath.get(sectionPath.size() - 1);
        queryStr2 = sectionPath.get(0);
        return queryStr2.toString();
    }
    
}