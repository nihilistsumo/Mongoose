package edu.unh.cs.treccar.proj.entlnk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page.SectionPathParagraphs;
import edu.unh.cs.treccar_v2.Data.ParaBody;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
public class CandidateEntityGeneration 
{
	private static HTreeMap<String,String> dictionary;
	private String file; // path to allButBenchmark file
	private DB db;
	//private  static ArrayList<Data.Page> pagelist;
	
	public CandidateEntityGeneration(String file)
	{
		this.file = file;
		db = DBMaker.fileDB("CandidateEntityGeneration.db").fileMmapEnable().transactionEnable().make();
		dictionary = db.hashMap("dictionary", Serializer.STRING, Serializer.STRING).counterEnable().create();
		//pagelist = getPageListFromPath(this.file);
	}
	public void buildDictionary() throws FileNotFoundException
	{
		System.out.println("Building dictionary...");
		/*for(Data.Page page : pagelist)
		{
			buildDictionary(page);
			System.out.println("Done page:"+page.getPageName());
		}*/
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(file)));
		Iterable<Data.Page> ip = DeserializeData.iterableAnnotations(bis);

		StreamSupport.stream(ip.spliterator(), true)
			.forEach(page -> 
			{
				List<SectionPathParagraphs> list = page.flatSectionPathsParagraphs();
				String anchorText = null,pageName = null;
				for(SectionPathParagraphs section : list )
				{
					Data.Paragraph paragraph = section.getParagraph();
					List<ParaBody> bodies = paragraph.getBodies();
					for(Data.ParaBody body:bodies)
					{
						if(body instanceof Data.ParaLink) 
				        {
							anchorText = ((Data.ParaLink)body).getAnchorText();
				        	pageName = ((Data.ParaLink)body).getPage();
				        	dictionary.merge(anchorText, pageName+" ", String::concat);
				        }
						//dictionary.merge(anchorText, pageName+" ", String::concat);
					}
				}
				System.out.println("Got page:"+page.getPageName());
			});
		
	}
	/*private ArrayList<Data.Page> getPageListFromPath(String path)
	{
		System.out.println("Getting the pages from cbor file...");
		ArrayList<Data.Page> pageList = new ArrayList<Data.Page>();
		try 
		{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(path)));
			for(Data.Page page: DeserializeData.iterableAnnotations(bis))
			{
				pageList.add(page);
				System.out.println("Got page:"+page.getPageName());
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		System.out.println("Finished getting pages...");
		return pageList;
	}
	private void buildDictionary(Data.Page page)
	{
		List<SectionPathParagraphs> list = page.flatSectionPathsParagraphs();
		String anchorText = null,pageName = null;
		for(SectionPathParagraphs section : list )
		{
			Data.Paragraph paragraph = section.getParagraph();
			List<ParaBody> bodies = paragraph.getBodies();
			for(Data.ParaBody body:bodies)
			{
				if(body instanceof Data.ParaLink) 
		        {
					anchorText = ((Data.ParaLink)body).getAnchorText();
		        	pageName = ((Data.ParaLink)body).getPage();
		        }
				dictionary.merge(anchorText, pageName+" ", String::concat);
			}
		}
		System.out.println("Got page:"+page.getPageName());
	}*/
	public static HTreeMap<String,String> getDictionary()
	{
		return dictionary;
	}
	public ArrayList<String> getCandidateEntities(String str)
	{
		ArrayList<String> candidates = new ArrayList<String>();
		String s = null;
		String[] cand;
		for(Object anchor : dictionary.keySet())
		{
			if(isMatch((String) anchor, str))
			{
				s = dictionary.get(anchor);
				cand = s.split("\\s+");
				for(String nstr : cand)
					candidates.add(nstr);
			}
		}
		return candidates;
	}
	private boolean isMatch(String s1, String s2)
	{
		if(s1.compareTo(s2) == 0)
			return true;
		else if((s1.toLowerCase().contains(s2.toLowerCase())) || (s2.toLowerCase().contains(s1.toLowerCase())))
			return true;
		else 
			return false;
	}
}
