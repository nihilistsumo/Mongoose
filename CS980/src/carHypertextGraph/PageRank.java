package carHypertextGraph;

import java.util.ArrayList;
import java.util.HashMap;

public class PageRank 
{
	protected final double alpha;
	protected Graph g;
	protected ArrayList<Node> nodeSet;
	protected ArrayList<Term> adj;
	protected ArrayList<Term> transition;
	protected HashMap<String,Double> nodeScore; 
	protected double numOfNodes;
	protected double numOfEdges;
	protected double initialRank ;
	
	public PageRank(String cborParaFilePath, String paraRunFilePath,double a)
	{
		g = new Graph(cborParaFilePath,paraRunFilePath);
		nodeSet = g.getNodeSet();
		adj = g.getAdjacencySparseMatrix();
		transition = g.getTransitionSparseMatrix();
		alpha = a;
		numOfNodes = g.getNumberOfNodes();
		numOfEdges = g.getNumberOfEdges();
		initialRank = 1/numOfNodes ;
		nodeScore = new HashMap<String,Double>();
		calculate();
	}
	
	private void calculate()
	{
		ArrayList<Double> vector = new ArrayList<Double>();
		ArrayList<Double> newVector = new ArrayList<Double>();
		int i;
		for(i = 1; i <= numOfNodes; i++)
			vector.add(Math.random());
		newVector = vector;
		
		ArrayList<Term> transpose = SparseMatrix.transpose(transition);
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
		for(String s : nodeScore.keySet())
			System.out.println(s+" "+nodeScore.get(s));
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
