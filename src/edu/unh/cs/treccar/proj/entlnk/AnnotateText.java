package edu.unh.cs.treccar.proj.entlnk;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import opennlp.tools.namefind.NameFinderME; 
import opennlp.tools.namefind.TokenNameFinderModel; 
import opennlp.tools.tokenize.TokenizerME; 
import opennlp.tools.tokenize.TokenizerModel; 
import opennlp.tools.util.Span;  

public class AnnotateText 
{  
	private String tokenize;
	private String nameFinder;
	private String locationFinder;
	private String orgFinder;
	private TokenizerModel tokenModel = null;
	private  TokenizerME tokenizer = null;
	private  ArrayList<String> entity;
	
	public AnnotateText(String tokenize, String nameFinder, String locationFinder, String orgFinder) throws FileNotFoundException, IOException
	{
		this.tokenize = tokenize;
		this.nameFinder = nameFinder;
		this.locationFinder = locationFinder;
		this.orgFinder = orgFinder;
		//Loading the tokenizer model 
		tokenModel = new TokenizerModel(new FileInputStream(this.tokenize)); 
		//Instantiating the TokenizerME class 
		tokenizer = new TokenizerME(tokenModel);
		entity = new ArrayList<String>();
	}
	private void findNames(String[] tokens) throws FileNotFoundException, IOException
	{
	      //Loading the NER-person model      
	      TokenNameFinderModel model = new TokenNameFinderModel(new FileInputStream(nameFinder));
	      
	      // feed the model to name finder class
	      NameFinderME nameFinder = new NameFinderME(model); 
	      
	      Span nameSpans[] = nameFinder.find(tokens); 
	      for(Span s: nameSpans) 
	      {
	    	  String str = "";
	    	  for(int index=s.getStart();index<s.getEnd();index++)
	    		  str = str + " "+tokens[index];
	    	  System.out.println("name="+str);
	    	  entity.add(str);
	      }
		
	}
	private void  findLocation(String[] tokens) throws FileNotFoundException, IOException
	{
	      
	      //Loading the NER-person model      
	      TokenNameFinderModel model = new TokenNameFinderModel(new FileInputStream(locationFinder));
	      
	      // feed the model to name finder class
	      NameFinderME nameFinder = new NameFinderME(model); 
	      
	      Span nameSpans[] = nameFinder.find(tokens); 
	      for(Span s: nameSpans) 
	      {
	    	  String str = "";
	    	  for(int index=s.getStart();index<s.getEnd();index++)
	    		  str = str + " "+tokens[index];
	    	  System.out.println("place="+str);
	    	  entity.add(str);
	      }
		
	}
	private void  findOrganization(String[] tokens) throws FileNotFoundException, IOException
	{
	      
	      //Loading the NER-person model      
	      TokenNameFinderModel model = new TokenNameFinderModel(new FileInputStream(orgFinder));
	      
	      // feed the model to name finder class
	      NameFinderME nameFinder = new NameFinderME(model); 
	      
	      Span nameSpans[] = nameFinder.find(tokens); 
	      for(Span s: nameSpans) 
	      {
	    	  String str = "";
	    	  for(int index=s.getStart();index<s.getEnd();index++)
	    		  str = str + " "+tokens[index];
	    	  System.out.println("org="+str);
	    	  entity.add(str);
	      }
		
	}
	public ArrayList<String> getEntityMentions(String text) throws IOException
	{
		//Tokenizing the sentence in to a string array 
	    String[] tokens = tokenizer.tokenize(text); 
	    
	    findNames(tokens);
	    findLocation(tokens);
	    findOrganization(tokens);
	    return entity;
	       
		
	}
	public static void main(String[] args) throws IOException
	{
		String dir = args[0];
		String s1 = dir+"/"+args[1];
		String s2 = dir+"/"+args[2];
		String s3 = dir+"/"+args[3];
		String s4 = dir+"/"+args[4];
		String text = "Mike is senior programming manager and George Martin is a clerk both are working at Google Inc  at New York and California"; 
		
		AnnotateText ob = new AnnotateText(s1,s2,s3,s4);
		ArrayList<String> list = ob.getEntityMentions(text);
		System.out.println("Found entites-->");
		for(String s : list)
			System.out.print(s+" ");
	}

}