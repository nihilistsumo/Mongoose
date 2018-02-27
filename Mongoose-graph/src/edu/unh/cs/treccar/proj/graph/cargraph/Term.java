package edu.unh.cs.treccar.proj.graph.cargraph;

public class Term 
{
	private int column;
	private int row;
	private double value;
	
	public Term(int row, int column, double value)
	{
		this.row = row;
		this.column = column;
		this. value = value;
	}
	public Term()
	{
		this.row = 0;
		this.column = 0;
		this. value = 0.0d;
	}
	public int getRowIndex()
	{
		return this.row;
	}
	public void setRowIndex(int index)
	{
		this.row = index;
	}
	public int getColumnIndex()
	{
		return this.column;
	}
	public void setColumnIndex(int index)
	{
		this.column = index;
	}
	public double getValue()
	{
		return this.value;
	}
	public void setValue(double val)
	{
		this.value = val;
	}
	

}
