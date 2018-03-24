package edu.unh.cs.treccar.proj.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.CborFileTypeException;
import edu.unh.cs.treccar_v2.read_data.CborRuntimeException;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class PlainTextExtractorForGlove {
	
	public void plainTextExtractor(String paraFilePath, String outFilePath){
		try {
			ArrayList<String> paraTexts = new ArrayList<String>();
			Analyzer analyzer = new StandardAnalyzer();
			Iterable<Data.Paragraph> paraIter = DeserializeData.iterableParagraphs(
					new BufferedInputStream(new FileInputStream(new File(paraFilePath))));
			StreamSupport.stream(paraIter.spliterator(), false).forEach(para -> {
				/*
				(Arrays.asList(para.getTextOnly().split(" "))).forEach(token -> {
					if(!DataUtilities.stopwords.contains(token.toLowerCase()))
						paraTexts.add(token.toLowerCase());
				});
				*/
				try {
					TokenStream stream  = analyzer.tokenStream(null, new StringReader(para.getTextOnly()));
					stream.reset();
					String token;
					while (stream.incrementToken()){
						token = stream.getAttribute(CharTermAttribute.class).toString().toLowerCase();
						if(DataUtilities.stopwords.contains(token))
							continue;
						paraTexts.add(token);
					}
					stream.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			for(int i=0; i<1000; i++)
				System.out.println(paraTexts.get(i));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFilePath)));
			for(String t:paraTexts)
				bw.write(t+"\n");
			bw.close();
		} catch (CborRuntimeException | CborFileTypeException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void plainTextExtractor(IndexSearcher is, String outFilePath){
		try {
			IndexReader ir = is.getIndexReader();
			Analyzer analyzer = new StandardAnalyzer();
			ArrayList<String> paraTexts = new ArrayList<String>();
			for(int i=0; i<ir.maxDoc(); i++){
				Document doc = ir.document(i);
				String paraText = doc.get("parabody");
				TokenStream stream  = analyzer.tokenStream(null, new StringReader(paraText));
		    	stream.reset();
		    	String token;
		    	while (stream.incrementToken()){
		    		token = stream.getAttribute(CharTermAttribute.class).toString().toLowerCase();
		    		if(DataUtilities.stopwords.contains(token))
		    			continue;
		    		paraTexts.add(token);
		    	}
		    	stream.close();
			}
			for(int i=0; i<1000; i++)
				System.out.println(paraTexts.get(i));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFilePath)));
			for(String t:paraTexts)
				bw.write(t+"\n");
			analyzer.close();
			bw.close();
		} catch (CborRuntimeException | CborFileTypeException
				| IOException e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new BufferedInputStream(new FileInputStream(new File("project.properties"))));
			/*
			IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(p.getProperty("index-dir")).toPath()))));
			(new PlainTextExtractorForGlove()).plainTextExtractor(is,
					p.getProperty("out-dir")+"/"+p.getProperty("plaintextpara"));
					*/
			(new PlainTextExtractorForGlove()).plainTextExtractor(p.getProperty("data-dir")+"/"+p.getProperty("parafile"),
					p.getProperty("out-dir")+"/"+p.getProperty("plaintextpara"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
