package edu.unh.cs.treccar.proj.cargraph;

import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.mapdb.*;

import main.SearchIndex;
public class PageRank 
{
	protected final double alpha;
	protected Graph g;
	protected ArrayList<Node> nodeSet;
	protected ArrayList<Term> adj;
	protected ArrayList<Term> transition;
	private ArrayList<Document> documents;
	protected HTreeMap<String,Double> nodeScore; 
	protected int numOfNodes;
	protected int numOfEdges;
	protected double initialRank ;
	private DB db;
	
	public PageRank(double a)
	{
		documents = SearchIndex.getTopDocumentList();
		g = new Graph(documents);
		db = DBMaker.fileDB("pageRank.db").fileMmapEnable().transactionEnable().make();
		nodeSet = g.getNodeSet();
		adj = g.getAdjacencySparseMatrix();
		transition = g.getTransitionSparseMatrix();
		alpha = a;
		numOfNodes = g.getNumberOfNodes();
		numOfEdges = g.getNumberOfEdges();
		initialRank = 1/numOfNodes ;
		nodeScore = db.hashMap("nodeScore", Serializer.STRING, Serializer.DOUBLE).counterEnable().create();
		calculate();
	}
	
	private void calculate()
	{
		double[][] vector = new double[numOfNodes][1];
		double[][] newVector = new double[numOfNodes][1];
		int i,j;
		for(i = 1; i <= numOfNodes; i++)
			vector[i][1] = initialRank;
		newVector = vector;
		
		ArrayList<Term> transpose = SparseMatrix.transpose(adj);
		ArrayList<Term> list = SparseMatrix.product(transpose, (1 - alpha));
		
		do
		{
			vector = newVector;
			newVector = SparseMatrix.product(vector, list);
			newVector = SparseMatrix.add(newVector, (alpha/numOfNodes));
			
		}
		while(!isConverged(vector,newVector));
		
		for(i = 0; i < vector.size(); i++)
		{
			String nodeId = g.getNodeId(i);
			double score = vector.get(i);
			nodeScore.put(nodeId, score);
		}
		for(Object s : nodeScore.keySet())
			System.out.println((String)s+" "+nodeScore.get(s));
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
