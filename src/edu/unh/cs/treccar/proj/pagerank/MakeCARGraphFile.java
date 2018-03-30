package edu.unh.cs.treccar.proj.pagerank;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;

import edu.unh.cs.treccar.proj.qe.Index;


public class MakeCARGraphFile 
{
		private String index,filePath, paraRunFilePath;
		private ArrayList<Document> documents;
		
		public MakeCARGraphFile(String index, String filePath, String paraRunFilePath) throws IOException, ParseException 
		{
			this.index = index;
			this.filePath = filePath;
			this.paraRunFilePath = paraRunFilePath;
			documents = new ArrayList<Document>();
			new Index.Setup(this.index);
			getDocumentList();
		}
		private boolean isCommon(List<String> list1, List<String> list2)
		{
			if (list1.isEmpty() || list2.isEmpty())
				return false;
			boolean flag = false;
			for(String s1 : list1)
				if(list2.contains(s1))
				{
					flag = true;
					break;
				}
			return flag;
		}
		private void getDocumentList() throws IOException, ParseException
		{
			BufferedReader reader = new BufferedReader(new FileReader(paraRunFilePath));
			//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line, query;
			Document d;
			while((line = reader.readLine()) != null)	
			{
				query = line.split(" ")[2];
				d = Index.Search.searchIndex("paraid", query);
				System.out.println("QueryID="+query);
				System.out.println("Got="+d.getField("paraid").stringValue());
				//br.readLine();
				documents.add(d);
			}
			reader.close();
		}
		private static String[] clean(final String[] v) 
		{
		    List<String> list = new ArrayList<String>(Arrays.asList(v));
		    list.removeAll(Collections.singleton(null));
		    list.removeAll(Collections.singleton(""));
		    return list.toArray(new String[list.size()]);
		}
		public void makeGraphFile()throws IOException, ParseException
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File((filePath))));
			//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			for(Document d1 : documents)
			{
				String[] entity1 = d1.getField("paraentity").stringValue().split(" ");
				String[] nentity1 = clean(entity1);
				ArrayList<String> list1 = new ArrayList<String>(Arrays.asList(nentity1));
				String neighbour = d1.getField("paraid").stringValue()+" ";
				System.out.println("Checking para1="+d1.getField("paraid").stringValue());
				for(Document d2 : documents)
				{
					String[] entity2 = d2.getField("paraentity").stringValue().split(" ");
					String[] nentity2 = clean(entity2);
					ArrayList<String> list2 = new ArrayList<String>(Arrays.asList(nentity2));
					//System.out.println("para2="+d2.getField("paraid").stringValue());
					if(isCommon(list1,list2))
					{
						System.out.println("Found an edge");
						System.out.println("para2="+d2.getField("paraid").stringValue());
						//System.out.println("neighbour="+neighbour);
						neighbour = neighbour + " "+d2.getField("paraid").stringValue();
						System.out.println("neighbour="+neighbour);
						//br.readLine();
					}
				}
				writer.write(neighbour+"\n");
			}
			writer.close();
		}
		public static void main(String args[]) throws IOException, ParseException 
		{
			String s1 = args[0];
			String s2 = args[1];
			String s3 = args[2];
			MakeCARGraphFile ob = new MakeCARGraphFile(s1,s2,s3);
			ob.makeGraphFile();
		}

}

