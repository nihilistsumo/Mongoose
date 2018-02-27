package edu.unh.cs.treccar.proj.graph.cargraph;

/**
 * @author Shubham Chatterjee
 * Class to implement the Pagerank Algorithm on the graph.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//import org.apache.lucene.document.Document;
import org.mapdb.*;

import edu.unh.cs.treccar.proj.graph.main.SearchIndex;



//import main.SearchIndex;
public class PageRank 
{
	protected final double alpha;
	protected Graph g;
	protected ArrayList<Node> nodeSet;
	protected ArrayList<Term> adj;
	protected ArrayList<Term> transition;
	//private ArrayList<Document> documents;
	protected HTreeMap<String,Double> nodeScore; 
	protected double numOfNodes;
	protected double numOfEdges;
	protected double initialRank ;
	private DB db;
	
	public PageRank(String indexDir,String outDir,String cborOutline,String outFile,String outParaFile,int top, double a) throws IOException
	{
		//documents = SearchIndex.getTopDocumentList();
		new SearchIndex(indexDir,outDir,cborOutline,outFile,top);
		SearchIndex.searchPages();
		g = SearchIndex.getGraph();
		g.makeAdjacencySparseMatrix();
		g.makeNumToIdMap();
		db = DBMaker.fileDB("pageRank.db").fileMmapEnable().transactionEnable().make();
		nodeSet = g.getNodeSet();
		adj = g.getAdjacencySparseMatrix();
		transition = g.getTransitionSparseMatrix();
		alpha = a;
		numOfNodes = g.getNumberOfNodes();
		numOfEdges = g.getNumberOfEdges();
		initialRank = 1/numOfNodes ;
		nodeScore = db.hashMap("nodeScore", Serializer.STRING, Serializer.DOUBLE).counterEnable().create();
		calculate(outParaFile);
	}
	
	private void calculate(String outParaFile) throws IOException
	{
		ArrayList<Double> vector = new ArrayList<Double>();
		ArrayList<Double> newVector = new ArrayList<Double>();
		int i;
		for(i = 1; i <= numOfNodes; i++)
			vector.add(Math.random());
		newVector = vector;
		
		ArrayList<Term> transpose = SparseMatrix.transpose(adj);
		ArrayList<Term> list = SparseMatrix.product(adj, (1 - alpha));
		i=0;
		do
		{
			vector = newVector;
			newVector = SparseMatrix.product(vector, list);
			newVector = SparseMatrix.add(newVector, (alpha/numOfNodes));
			i++;
			System.out.println(i);
			
		}
		while(!isConverged(vector,newVector));
		System.out.println("converged after="+i);
		for(i = 0; i < vector.size(); i++)
		{
			String nodeId = g.getNodeId(i);
			double score = vector.get(i);
			nodeScore.put(nodeId, score);
		}
		for(Object s : nodeScore.keySet())
		{
			System.out.println((String)s+" "+nodeScore.get(s)+"\n");
		}
		
		FileWriter fw = new FileWriter(outParaFile, true);
		for(Object s : nodeScore.keySet())
		{
			fw.write((String)s+" "+nodeScore.get(s)+"\n");
		}
		fw.close();
	}
	public double getNodeScore(String id)
	{
		return nodeScore.get(id);
	}
	private boolean isConverged(ArrayList<Double> l1, ArrayList<Double> l2)
	{
		if(norm(l1,l2) > 0.00001)
			return false;
		return true;
	}
	private double norm(ArrayList<Double> l1, ArrayList<Double> l2)
	{
		double s = 0.0d;;
		for(int i = 0; i < l1.size(); i++)
			s = s + Math.pow((l2.get(i) - l1.get(i)), 2);
		s = Math.sqrt(s);
		return s;
	}
	
	

}
