package main;

import java.io.IOException;

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
		else if(args[0].equalsIgnoreCase("-s") && args[1].equalsIgnoreCase("-p"))
		{
			System.out.println("Searching index for pages");
			String indexDir = args[2];
			String outDir = args[3];
			String cborOutline = args[4];
			String outFile = args[5];
			int top = Integer.parseInt(args[6]);
			new SearchIndex(indexDir,outDir,cborOutline,outFile,top);
			SearchIndex.searchPages();
		}
		else if(args[0].equalsIgnoreCase("-s") && args[1].equalsIgnoreCase("-s"))
		{
			System.out.println("Searching index for sections");
			String indexDir = args[2];
			String outDir = args[3];
			String cborOutline = args[4];
			String outFile= args[5];
			int top = Integer.parseInt(args[6]);
			new SearchIndex(indexDir,outDir,cborOutline,outFile,top);
			SearchIndex.searchSections();
		}
		else
		{
			System.out.println("Wrong usage.To see usage, run with option -u.");
		}
		
	}
	private static void use()
	{
		System.out.println("There are two options with which you can run this software. Using option -b builds"
				+ "the index whereas using option -s searches the index. When using option -s, there are two modes "
				+ "in which the search can be done. Use option -p after -s to search the index for page queries and"
				+ "use option -s after -s to search the index for section queries.");
		System.out.println("When using the -b option:");
		System.out.println("java -jar $jar file$ -b $path to index directory$ $path to directory containing paragrapgh cbor file$");
		System.out.println("When using -s option:");
		System.out.println("java -jar $jar file$ -s -p $path to index directory$ $path to output directory$"
				+" "+"$path to cbor outline file$ $name of paragragh run file$ $top how many results$");
		System.out.println("java -jar $jar file$ -s -s $path to index directory$ $path to output directory$"
				+" "+"$path to cbor outline file$ $name of section run file$ $top how many results$");
	}
}