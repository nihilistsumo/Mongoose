package edu.unh.cs.treccar.proj.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class RunFileWriterFromParaMapper {
	
	Properties pr;
	ArrayList<String> clusterLabels;
	ArrayList<String> titleList;
	HashMap<String, ArrayList<String>> labeledClusters;
	double[][] dotMatrix;
	
	public RunFileWriterFromParaMapper(Properties p, double[][] matrix, ArrayList<String> clabels, 
			ArrayList<String> tlabels, HashMap<String, ArrayList<String>> cl){
		this.pr = p;
		this.clusterLabels = clabels;
		this.titleList = tlabels;
		this.labeledClusters = cl;
		this.dotMatrix = matrix;
	}
	
	public void writeRunFile(){
		
	}

}
