package lucene.searchandIndexing;

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


import java.util.*;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.ArrayList;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
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
 */

public class LMJelinekMercer {

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
                //System.out.println(token);
                tokens.add(token);
            }
            tokenStream.end();
            tokenStream.close();
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            for (String token : tokens) {
            	//System.out.println(token);
                booleanQuery.add(new TermQuery(new Term("text", token)), BooleanClause.Occur.SHOULD);
            }
            return booleanQuery.build();
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("file.encoding", "UTF-8");

        String indexPath = args[1];
        if (args.length != 3) {
        	System.out.println("Format: Outlinescborfile IndexPath toplevelsectionsout");
        	System.exit(-1);
        }
        
            IndexSearcher searcher = setupIndexSearcher(indexPath, "paragraph.lucene");
            searcher.setSimilarity(new LMJelinekMercerSimilarity(0.1f));
            final MyQueryBuilder queryBuilder = new MyQueryBuilder(new StandardAnalyzer());
            final String pagesFile = args[0];
                       
	      	//LMJelinekMercer
	        PrintWriter out1 = new PrintWriter(new FileWriter(args[2]));
	        String queryId2 = null;
	        String queryStr2 = null;
	        String url = null;
	        List<String> url2 = new ArrayList<String>();
	        final FileInputStream fileInputStream2 = new FileInputStream(new File(pagesFile));
	        for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream2)) {
	            for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
	                queryId2 = Data.sectionPathId(page.getPageId(), sectionPath);
	                queryStr2 = buildSectionQueryStrtls(page, sectionPath);
	                //System.out.println(queryStr2);
	                break;
	            }
	            /*
	            String[] arr = queryStr2.split("\'");
	            String arr2 = arr[1].toLowerCase();
	            //System.out.println(arr[1]);
	            
	            //System.exit(-1);          
		        //url = "http://api.conceptnet.io/c/en/" + URLEncoder.encode(queryBuilder.toQuery(queryStr2), "UTF-8");
	            //url = "http://api.conceptnet.io/c/en/" + arr2;
          	
	           	try {
	                     HttpClient client = HttpClientBuilder.create().build();
	                     HttpGet request = new HttpGet(url);
	                     HttpResponse response = client.execute(request);
	          
	                     int responseCode = response.getStatusLine().getStatusCode();
	                     BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	                     String line = "";
	                     StringBuilder sb = new StringBuilder();
	                     while ((line = rd.readLine()) != null) {
	                         sb.append(line.trim());
	                     }
	                    System.out.println(sb);
	                     System.exit(-1);
	          
	                 } catch (ClientProtocolException e) {
	                     e.printStackTrace();
	                 } catch (UnsupportedOperationException e) {
	                     e.printStackTrace();
	                 } catch (IOException e) {
	                     e.printStackTrace();
	                 }        
	
	            	 System.exit(-1); 
	            }
	        */
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
	                    	out1.println(queryId2+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" LM-JelinekMercer");
	                    paragraphid2 = paragraphid;
	                }
    }	        
	        out1.close();	        
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
