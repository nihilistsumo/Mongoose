package carHypertextGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.CborFileTypeException;
import edu.unh.cs.treccar_v2.read_data.CborRuntimeException;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
public class MakeCARGraphFile 
{
		private String cborParaFilePath,filePath, paraRunFilePath;
		private BufferedReader br;
		private HashMap<String, Data.Paragraph> paraToIDMap;
		
		public MakeCARGraphFile(String cborParaFilePath, String filePath, String paraRunFilePath) 
		{
			this.cborParaFilePath = cborParaFilePath;
			this.filePath = filePath;
			this.paraRunFilePath = paraRunFilePath;
			makeParaToIDMap();
		}
		private void makeParaToIDMap()
		{
			try 
			{
				for(Data.Paragraph para : DeserializeData.iterableParagraphs(new FileInputStream(new File(cborParaFilePath))))
					paraToIDMap.put(para.getParaId(),para);
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
		}
		private boolean isCommon(List<String> list1, List<String> list2)
		{
			boolean flag = false;
			for(String s1 : list1)
				for(String s2 : list2)
					if(s1.equalsIgnoreCase(s2))
					{
						flag = true;
						break;
					}
			return flag;
		}
		public void makeGraphFile()throws IOException
		{
			PrintWriter writer = new PrintWriter(filePath, "UTF-8");
			br = new BufferedReader(new FileReader(paraRunFilePath));
			ArrayList<Data.Paragraph> paras = new ArrayList<Data.Paragraph>();
			String line;
			while((line = br.readLine()) != null)
			{
				String s = line.split(" ")[2];
				Data.Paragraph p = paraToIDMap.get(s);
				System.out.println(s+" "+p.getParaId());
				paras.add(p);
			}
			for(Data.Paragraph para1 : paras)
			{
				List<String> list1 = para1.getEntitiesOnly();
				String neighbour = para1.getParaId()+" ";
				System.out.println("para1="+para1.getParaId());
				for(Data.Paragraph para2 : paras)
				{
					List<String> list2 = para2.getEntitiesOnly();
					System.out.println("para2="+para2.getParaId());
					if(isCommon(list1,list2))
					{
						System.out.println("neighbour="+neighbour);
						neighbour = neighbour + " "+para2.getParaId();
					}
				}
				writer.println(neighbour+"\n");
			}
			writer.close();
			
		}
		private Data.Paragraph getParaObject(String str) throws CborRuntimeException, CborFileTypeException, FileNotFoundException
		{
			Data.Paragraph p = null;
			for(Data.Paragraph para : DeserializeData.iterableParagraphs(new FileInputStream(new File(cborParaFilePath))))
			{
				if(para.getParaId().equalsIgnoreCase(str))
					p = para;
			}
			return p;
		}
		public void makeGraphFile2() throws FileNotFoundException, UnsupportedEncodingException
		{
			PrintWriter writer = new PrintWriter(filePath, "UTF-8");
			for(Data.Paragraph para1 : DeserializeData.iterableParagraphs(new FileInputStream(new File(cborParaFilePath))))
			{
				List<String> list1 = para1.getEntitiesOnly();
				String neighbour = para1.getParaId()+" ";
				System.out.println("para1="+para1.getParaId());
				for(Data.Paragraph para2 : DeserializeData.iterableParagraphs(new FileInputStream(new File(cborParaFilePath))))
				{
					List<String> list2 = para2.getEntitiesOnly();
					System.out.println("para2="+para2.getParaId());
					if(isCommon(list1,list2))
					{
						System.out.println("neighbour="+neighbour);
						neighbour = neighbour + " "+para2.getParaId();
					}
				}
				writer.println(neighbour+"\n");
			}
			writer.close();
		}
		public static void main(String args[]) throws IOException 
		{
			String s1 = args[0];
			String s2 = args[1];
			String s3 = args[3];
			MakeCARGraphFile ob = new MakeCARGraphFile(s1,s2,s3);
			ob.makeGraphFile();
		}

}

