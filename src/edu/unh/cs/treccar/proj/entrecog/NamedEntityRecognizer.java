package edu.unh.cs.treccar.proj.entrecog;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.ParaBody;
import edu.unh.cs.treccar_v2.Data.ParaLink;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;
import org.omg.Messaging.SyncScopeHelper;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.apache.http.message.BasicNameValuePair;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/*
 * @author Ajesh Vijayaragavan
 */

public class NamedEntityRecognizer {

    public static void main(String[] args) throws IOException {
        System.setProperty("file.encoding", "UTF-8");

        if (args.length <3) {
        	System.out.println("Format: ParagraphFile OutFile CandidateParagraphs");
        	System.exit(-1);
        }        		
        
        final String ParagraphsFile = args[0];
        PrintWriter out = new PrintWriter(new FileWriter(args[1]));
        Scanner scanner = new Scanner(new FileReader(args[2]));
        Scanner scanner1 = new Scanner(new FileReader(args[2]));
        final FileInputStream fileInputStream = new FileInputStream(new File(ParagraphsFile));
               
        List<String> paragraphs1 = new ArrayList<String>();

        Map<String, List<String>> TrecEntitylinks = new HashMap<String, List<String>>();
        
        List<String> Clines = new ArrayList<String>();
        List<String> Clines1 = new ArrayList<String>();
        
        //determine number of paragraphs for each query

        int count = 0;
        int count1 = 0;
        while (scanner.hasNextLine())
        {
        	String[] entries = scanner.nextLine().split(" ");
        	count++;
        	if(!Clines.contains(entries[0]))
        	{
        		Clines.add(entries[0]);
        		count1++;
        		if (count1 == 2)
        		{
        			break;
        		}
        		
        		//break;
        	}
        }

        int count3 = 0;
        while (scanner.hasNextLine()) {
        	String[] entries = scanner.nextLine().split(" ");
        	//ArrayList<String> list = new ArrayList<String>();
        	for (int i = 0; i < entries.length; i++) {
        		
        		if (i!=0 && i % 3 == 0)
        		{
        			if (0 < Integer.parseInt(entries[i]) && Integer.parseInt(entries[i]) < 6)
        			{
        				count3++;
        			}
        		}
        		
        	
        		if (i != 0 && i % 2 == 0)
        		{
		        	if (0 < Integer.parseInt(entries[3]) && Integer.parseInt(entries[3]) < 3)
		        	{
		        			Clines.add(entries[2]);
		        			count3++;
		        	}
        		
        		Clines.add(entries[2]);

        	}
        }
        for(Data.Paragraph paragraph: DeserializeData.iterableParagraphs(fileInputStream)) {
        	
        	if(Clines.contains(paragraph.getParaId()))
        	{
	        	paragraphs1.add(paragraph.getTextOnly());

	        	TrecEntitylinks.put(paragraph.getParaId(), paragraph.getEntitiesOnly());
	        	        	
	        	for (Data.ParaBody ParaBody: paragraph.getBodies()) {
	        		if (ParaBody instanceof Data.ParaLink)
	        		{
	        			//System.out.println(((Data.ParaLink) ParaBody).getAnchorText());
	        		} 
	        }
        }        
        	
    }   
        String url1 = null;
        
        List<String> url2_1 = new ArrayList<String>();
        List<String> NamedEntities = new ArrayList<String>();
        Map<String, Integer> Entitycounts = new HashMap<String, Integer>();
        int count5 = 0;
        
        for (String item : paragraphs1)
        {
	        url1 = "https://api.dandelion.eu/datatxt/nex/v1/? text=" + URLEncoder.encode(item, "UTF-8") + "&token=fbebf37590184e668b2784ccc13482e7";
	        url2_1.add(url1);
        }
        
        StringBuilder sb1 = null;
        
        for (String item2 : url2_1)
        {
        	
       	 try {
                 HttpClient client = HttpClientBuilder.create().build();
                 HttpGet request = new HttpGet(item2);
                 HttpResponse response = client.execute(request);
      
                 int responseCode = response.getStatusLine().getStatusCode();
                 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                 String line = "";
                 sb1 = new StringBuilder();
                 while ((line = rd.readLine()) != null) {
                     sb1.append(line.trim());
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
       	 
       	  int count4 = sb1.length();
       	  String temp = paragraphs1.get(count5);
          Entitycounts.put(temp, count4);
      }
                
        String url = null;
        List<String> url2 = new ArrayList<String>();
        List<String> DbpediaLinks = new ArrayList<String>();
        
        for (String item : paragraphs1)
        {
	        url = "http://model.dbpedia-spotlight.org/en/annotate?text=" + URLEncoder.encode(item, "UTF-8");
	        url2.add(url);
        }
        
        for (String item2 : url2)
        {
        	
       	 try {
                 HttpClient client = HttpClientBuilder.create().build();
                 HttpGet request = new HttpGet(item2);
                 HttpResponse response = client.execute(request);
      
                 int responseCode = response.getStatusLine().getStatusCode();
                 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                 String line = "";
                 StringBuilder sb = new StringBuilder();
                 while ((line = rd.readLine()) != null) {
                     sb.append(line.trim());
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
 	
         }

      }

    }
}