package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.CborFileTypeException;
import edu.unh.cs.treccar_v2.read_data.CborRuntimeException;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class FindParas 
{
	
	private String filePath,outlineFilePath,cborParaFilePath;
	private ArrayList<Data.Page> pageList = new ArrayList<Data.Page>();
	private LinkedHashMap<String,ArrayList<String>> rankings = new LinkedHashMap<String,ArrayList<String>>();
	public FindParas(String filePath,String outlineFilePath, String cborParaFilePath) 
	{
		this.filePath = filePath;
		this.outlineFilePath = outlineFilePath;
		this.cborParaFilePath = cborParaFilePath;
		getPageListFromPath();
		getRankings();
	}
	private void getPageListFromPath()
	{
		try 
		{
			FileInputStream fis = new FileInputStream(new File(outlineFilePath));
			for(Data.Page page: DeserializeData.iterableAnnotations(fis))
				pageList.add(page);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
	}
	private void getRankings()
	{
		BufferedReader br = null;
		String line = "",s1 = "",s2 = "",s3 = "";
		ArrayList<String> list = new ArrayList<String>();
		try 
		{
            br = new BufferedReader(new FileReader(filePath));

            while((line = br.readLine()) != null) 
            {
            	s3 = s1;
            	s1 = line.split(" ")[0];
                s2 = line.split(" ")[2];
            	if(!s1.equals(s3))
            	{
            		if(!s3.equals(""))
            			rankings.put(s3, list);
            		list = new ArrayList<String>(); 
            	}
                list.add(s2);
            }
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
	}
	private void mapParaToEntity() 
	{
		LinkedHashMap<String,String> paraToEntityMap = new LinkedHashMap<String,String>();
        for(String s1 : rankings.keySet())
        {
        	ArrayList<String> entityRank = new ArrayList<String>(rankings.get(s1));
     
        	for(String s2 : entityRank)
        	{
        		String text = getRelevantPara(s2);
        		paraToEntityMap.put(s2, text);
        	}
        }
        for(String s1 :paraToEntityMap.keySet() )
        {
        	System.out.println("String="+s1);
        	System.out.println(paraToEntityMap.get(s1));
        }
	}
	
	private String getRelevantPara(String str) 
	{
		String text = ""; int f = 0;
		try 
		{
			for (Data.Paragraph paragraph : DeserializeData.iterableParagraphs(new FileInputStream(new File(cborParaFilePath))))
			{
				for(Data.ParaBody body: paragraph.getBodies())
				{
					if(body instanceof Data.ParaLink) 
					{
						String s = ((Data.ParaLink) body).getPageId();
						
						if(s.equalsIgnoreCase(str))
						{
							System.out.println("Found a relevant para-->");
							text = paragraph.getTextOnly();
							System.out.println("Entity="+str);
							System.out.println(text);
							f=1;
							break;
						}
					}
				}
				if(f==1)
					break;
			}
		} 
		catch (CborRuntimeException e) 
		{
			e.printStackTrace();
		} 
		catch (CborFileTypeException e) 
		{
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		return text;
	}

	public static void main(String args[]) 
	{
		String s1 = args[0];
		String s2 = args[1];
		String s3 = args[2];
		FindParas fp = new FindParas(s1,s2,s3);
		fp.mapParaToEntity();
	}

}
