package main;

import java.io.IOException;
import java.util.ArrayList;

import carHypertextGraph.MakeCARGraphFile;
import carHypertextGraph.PageRank;
import pageRank.PersonalisedPageRank;

/**
 * 
 * @author Shubham Chatterjee
 * Main class to handle calls to other clases.
 *
 */
public class ProjectMain 
{
	
	public static void main(String[] args)throws IOException
	{
		if(args[0].equalsIgnoreCase("-u"))
			use();
		else if(args[0].equalsIgnoreCase("-b"))
		{
			System.out.println("Building index");
			String indexDir = args[1];
			String cborDir = args[2];
			new BuildIndex(indexDir,cborDir);
			BuildIndex.createIndex();
		}
		else if(args[0].equalsIgnoreCase("-sp"))
		{
			System.out.println("Searching index for pages");
			String indexDir = args[1];
			String outDir = args[2];
			String cborOutline = args[3];
			String outFile = args[4];
			int top = Integer.parseInt(args[5]);
			new SearchIndex(indexDir,outDir,cborOutline,outFile,top);
			SearchIndex.searchPages();
		}
		else if(args[0].equalsIgnoreCase("-ss"))
		{
			System.out.println("Searching index for sections");
			String indexDir = args[1];
			String outDir = args[2];
			String cborOutline = args[3];
			String outFile= args[4];
			int top = Integer.parseInt(args[5]);
			new SearchIndex(indexDir,outDir,cborOutline,outFile,top);
			SearchIndex.searchSections();
		}
		else if(args[0].equalsIgnoreCase("-pr"))
		{
			System.out.println("PageRank Algorithm on CAR Graph of top paragraphs of each page as nodes");
			
			String indexDir = args[1];
			String outDir = args[2];
			String cborOutline = args[3];
			String outFile= args[4];
			String outParaFile= args[5];
			int top = Integer.parseInt(args[6]);
			double alpha = Double.parseDouble(args[7]);
			new PageRank(indexDir,outDir,cborOutline,outFile,outParaFile,top,alpha);
		}
		else if(args[0].equalsIgnoreCase("-ppr"))
		{
			System.out.println("Doing PersonalisedPageRank...");
			String file = args[1];
			double alpha = Double.parseDouble(args[2]);
			int n = Integer.parseInt(args[3]);
			int c=4;
			ArrayList<String> seed = new ArrayList<String>();
			for(int i = 1 ; i <= n ; i++)
				seed.add(args[c++]);
			PersonalisedPageRank p = new PersonalisedPageRank(file,alpha,seed);
			p.calculate();
		}
		else if(args[0].equalsIgnoreCase("-make"))
		{
			System.out.println("Making CAR Hypertext Graph File");
			String cborFile = args[1];
			String file = args[2];
			String paraRunFile = args[3];
			MakeCARGraphFile ob = new MakeCARGraphFile(cborFile,file,paraRunFile);
			ob.makeGraphFile();
		}
		else
		{
			System.out.println("Wrong usage.To see usage, run with option -u.");
		}
		
	}
	private static void use()
	{
		System.out.println("************************************************************************************************************************************************************");
		System.out.println("             										USAGE OPTIONS                   																			");
		System.out.println("************************************************************************************************************************************************************");
		System.out.println("-u: Display usage");
		System.out.println("-b: Build Index");
		System.out.println("-sp: Search Index for Page queries");
		System.out.println("-ss: Search Index for Section queries");
		System.out.println("-pr: Run PageRank Algorithm on a Graph");
		System.out.println("-ppr: Run PersonalisedPageRank Algorithm on a Graph");
		System.out.println("-make: Make a CAR Hypertext Graph of paragraphs as nodes and entities as edges");
		System.out.println();
		System.out.println("************************************************************************************************************************************************************");
		System.out.println("             										USAGE SYNTAX                   																			");
		System.out.println("************************************************************************************************************************************************************");
		
		
		
		System.out.println("java -jar $jar file$ -b $path to index directory$ $path to directory containing paragrapgh cbor file$");
		
		System.out.println("java -jar $jar file$ -sp $path to index directory$ $path to output directory$"
				+" "+"$path to cbor outline file$ $name of paragragh run file$ $top how many results$");
		
		System.out.println("java -jar $jar file$ -ss $path to index directory$ $path to output directory$"
				+" "+"$path to cbor outline file$ $name of section run file$ $top how many results$");
		
		System.out.println("java -jar $jar file$ -pr  $value of random jump (alpha)$");
		
		System.out.println("java -jar $jar file$ -ppr $path to graph file$ $value of random jump (alpha)$ $size of seed set$ $seed values$");
		
		System.out.println("java -jar $jar file$ -make $path to paragraph corpus cbor file$ $path to graph file$");
	}
}