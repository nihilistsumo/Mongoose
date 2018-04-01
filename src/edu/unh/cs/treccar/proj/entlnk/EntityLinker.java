package edu.unh.cs.treccar.proj.entlnk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.CborFileTypeException;
import edu.unh.cs.treccar_v2.read_data.CborRuntimeException;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;


public class EntityLinker 
{
	private String tokenize;
	private String nameFinder;
	private String locationFinder;
	private String orgFinder;
	private String file1; // path to allButBenchmark file
	private String file2; // path to leading paragraphs file
	private String cbor_file; // path to leading paragraphs cbor file
	private HTreeMap<String,Double> paraToScoreMap;
	private DB db;
	private AnnotateText annotate;
	
	public EntityLinker(String tokenize, String nameFinder, String locationFinder, String orgFinder, String file1, String file2, String cbor_file) throws FileNotFoundException, IOException
	{
		this.tokenize = tokenize;
		this.nameFinder = nameFinder;
		this.locationFinder = locationFinder;
		this.orgFinder = orgFinder;
		this.file1 = file1;
		this.file2 = file2;
		this.cbor_file = cbor_file;
		db = DBMaker.fileDB("EntityLinker.db").fileMmapEnable().transactionEnable().make();
		paraToScoreMap = db.hashMap("paraToScoreMap", Serializer.STRING, Serializer.DOUBLE).counterEnable().create();
		annotate = new AnnotateText(this.tokenize,this.nameFinder,this.locationFinder,this.orgFinder);
	}
	
	public void link() throws IOException, ParseException
	{
		// Candidate Entity Generation Step
		CandidateEntityGeneration ceg = new CandidateEntityGeneration(file1);
		
		//Build the dictionary 
		ceg.buildDictionary();
		
		BufferedReader br = new BufferedReader(new FileReader(file2));
		String line, text,topEntity;
		Data.Paragraph paragraph;
		ArrayList<String> entityMentions = new ArrayList<String>();
		ArrayList<String> candidateEntitySet = new ArrayList<String>();
		List<String> trueEntities;
		List<String> topEntities;
		double f1Measure;
		while ((line = br.readLine()) != null) 
		{
			paragraph = getParagraph(line);
			if(paragraph == null )
			{
				System.out.println("Error! Could not find paragraph!");
				System.exit(0);
			}
			text = paragraph.getTextOnly();
			trueEntities = paragraph.getEntitiesOnly();
			topEntities = new ArrayList<String>();
			entityMentions = annotate.getEntityMentions(text);
			for(String e : entityMentions)
			{
				candidateEntitySet = ceg.getCandidateEntities(e);
				topEntity = CandidateEntityRank.rank(e,candidateEntitySet);
				topEntities.add(topEntity);
			}	
			f1Measure = getF1Score( trueEntities, topEntities);
			paraToScoreMap.put(line, f1Measure);
		}
		br.close();
		displayScores();
	}
	/*public void link() throws FileNotFoundException
	{
		// Candidate Entity Generation Step
		CandidateEntityGeneration ceg = new CandidateEntityGeneration(file1);
				
		//Build the dictionary 
		ceg.buildDictionary();
		HTreeMap<String,String> dictionary = CandidateEntityGeneration.getDictionary();
		System.out.println("Anchor\t\t\tPage");
		for(Object s:dictionary.keySet())
		{
			String key = (String)s;
			String value = dictionary.get(s);
			System.out.println(key+"\t\t\t"+value);
		}
			
	}*/
	private void displayScores()
	{
		double sum = 0.0d, mean = 0.0d, score, deviation, standardDeviation, standardError;
		int n = paraToScoreMap.size();
		System.out.println("ParaID\t\t\tF1 Score");
		for(Object s : paraToScoreMap.keySet())
		{
			score = paraToScoreMap.get(s);
			System.out.println((String)s+"\t\t\t"+score);
			sum += score;
		}
		mean = sum / n;
		System.out.println("Mean F1 Score for all paragraphs = "+mean);
		sum = 0.0d;
		for( Object s : paraToScoreMap.keySet())
		{
			score = paraToScoreMap.get(s);
			deviation = mean - score;
			deviation = deviation * deviation;
			sum = sum + deviation;
		}
		standardDeviation = Math.sqrt((sum / (n - 1)));
		standardError = standardDeviation / Math.sqrt(n);
		System.out.println("Standard Error = "+standardError);
	}
	private Data.Paragraph getParagraph(String paraID) throws CborRuntimeException, CborFileTypeException, FileNotFoundException
	{
		Data.Paragraph para = null;
		for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(cbor_file))))
			if(p.getParaId().equalsIgnoreCase(paraID))
			{
				para = p;
				break;
			}
		return para;
	}
	private double getF1Score(List<String> relevant, List<String> retrieved)
	{
		int common = 0;
		double precision, recall, F;
		
		for(String s : relevant)
			if(retrieved.contains(s))
				common++;
		precision = common / retrieved.size();
		recall = common / relevant.size();
		F = (2 * precision * recall) / (precision + recall);
		
		return F;
	}

}
