package edu.unh.cs.treccar.proj.entlinkqe;

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
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.omg.Messaging.SyncScopeHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

/*
 * @author Ajesh Vijayaragavan
 * Sends top paragraph ids with page name for entity linking
 * to form graph 
 */

public class EntityLinking1 {

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
            for (String token : tokens) 
            {
                booleanQuery.add(new TermQuery(new Term("text", token)), BooleanClause.Occur.SHOULD);
            }
            return booleanQuery.build();
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("file.encoding", "UTF-8");

        String indexPath = args[1];
        if (args.length < 5) {
        	System.out.println("Format: Outlinescborfile IndexPath toplevelsectionsout ParagraphsFile EntityLinkingout");
        	System.exit(-1);
        }
            IndexSearcher searcher = setupIndexSearcher(indexPath, "paragraph.lucene");
            searcher.setSimilarity(new LMJelinekMercerSimilarity(0.1f));
            final MyQueryBuilder queryBuilder = new MyQueryBuilder(new StandardAnalyzer());
            final String pagesFile = args[0];

	        PrintWriter out1 = new PrintWriter(new FileWriter(args[2]));
	        final String ParagraphsFile = args[3];

	        String out2 = args[4];
	        String queryId2 = null;
	        String queryStr2 = null;
	        String url = null;
	        String str1 = null;
	        String str2 = null;
	        JSONObject obj1;
	        JSONObject obj2;
	        JSONArray jarr1;
	        List<String> paragraphids1 = new ArrayList<String>();
	        List<String> paragraphids2 = new ArrayList<String>();
	        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
	        final FileInputStream fileInputStream2 = new FileInputStream(new File(pagesFile));
	        for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream2)) {
	            for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
	                queryId2 = Data.sectionPathId(page.getPageId(), sectionPath);
	                queryStr2 = buildSectionQueryStrtls(page, sectionPath);

	                break;
	            }
	            
	            String[] arr = queryStr2.split("\'");
	            String arr2 = arr[1].replaceAll(" ", "_").toLowerCase();
	            
	            url = "http://api.conceptnet.io/c/en/" + arr2;
          	
	           	try {
	                     HttpClient client = HttpClientBuilder.create().build();
	                     HttpGet request = new HttpGet(url);
	                     HttpResponse response = client.execute(request);

	          
	                     int responseCode = response.getStatusLine().getStatusCode();

	                     BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

	                     String line = "";
	                     StringBuilder sb = new StringBuilder();
	                     while ((line = rd.readLine()) != null) {

	                    	 sb.append(line);
	                     }
	                                         	                    	                    
						try 
						{
							obj1 = new JSONObject(sb.toString());

							jarr1 = obj1.getJSONArray("edges");
							
							for(int i=0; i<jarr1.length(); i++)
					        {
								obj2 = jarr1.getJSONObject(i);
								str1 = obj2.getJSONObject("start").getString("label");
								str2 = obj2.getJSONObject("end").getString("label");

								queryStr2 = queryStr2 + str1 +" " + str2 +" ";
					        }														
						}
						catch (JSONException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
	          
	                 } catch (ClientProtocolException e) {
	                     e.printStackTrace();
	                    } 
	           			catch (UnsupportedOperationException e) {
	                     e.printStackTrace();
	           			} 
	           			catch (IOException e) {
	                     e.printStackTrace();
	           			}
	           	
	                TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr2), 5);
	                ScoreDoc[] scoreDoc = tops.scoreDocs;
	                String paragraphid2 = null;
	                for (int i = 0; i < scoreDoc.length; i++)
	                {
	                    ScoreDoc score = scoreDoc[i];
	                    final Document doc = searcher.doc(score.doc);
	                    final String paragraphid = doc.getField("paragraphid").stringValue();
	                    final float searchScore = score.score;
	                    final int searchRank = i+1;
                                       
	                    if (!paragraphids1.contains(paragraphid))  {
	                    	out1.println(queryId2+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" LM-JelinekMercer");                    	
	                    	paragraphids1.add(paragraphid);
	                    }
	                    
	                    if (!paragraphids2.contains(paragraphid)) {
                    	 	paragraphids2.add(paragraphid);
                    	}
	                    	                    
	                }
	                hashMap.put(page.getPageName(), paragraphids1);

	                paragraphids1.clear();
	        }
	        out1.close();
	        EntityLinking2.main(hashMap, paragraphids2, ParagraphsFile, out2);
    }	                

	@NotNull
    private static IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
        Directory indexDir = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(indexDir);
        return new IndexSearcher(reader);
    }
    
    @NotNull
    private static String buildSectionQueryStrtls(Data.Page page, List<Data.Section> sectionPath) {
    	Section queryStr2;
        //queryStr2 = sectionPath.get(sectionPath.size() - 1);
        queryStr2 = sectionPath.get(0);
        return queryStr2.toString();
    }
}