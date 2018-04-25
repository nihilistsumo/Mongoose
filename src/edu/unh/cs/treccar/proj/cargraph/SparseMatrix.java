package edu.unh.cs.treccar.proj.cargraph;

import java.util.ArrayList;

public class SparseMatrix 
{
	public static ArrayList<Term> transpose2(ArrayList<Term> a)
	{
		ArrayList<Term> b = new ArrayList<Term>();
		int i,j;
		double n = a.get(0).getValue(); //total number of elements
		
		b.add(new Term(a.get(0).getColumnIndex(),a.get(0).getRowIndex(),n ));
		
		if(n > 0)
		{
			//non zero matrix
			for(i = 0; i < a.get(0).getColumnIndex(); i++)
				//transpose by the columns in a
				for(j = 1; j <= n; j++)
					//find elements from the current column
					if(a.get(j).getColumnIndex() == i)
					{
						//element is in current column so add it to b
						Term t = new Term(a.get(j).getColumnIndex(),a.get(j).getRowIndex(),a.get(j).getValue());
						b.add(t);
					}
		}
		return b;
	}
	public static ArrayList<Double> add(ArrayList<Double> list, double val)
	{
		ArrayList<Double> res = new ArrayList<Double>();

		for(double d : list)
		{
			d += val;
			res.add(d);
		}
		return res;
	}
	public static ArrayList<Term> product(ArrayList<Term> list, double val)
	{
		ArrayList<Term> res = new ArrayList<Term>();
		double d;
		Term t;
		int i;
		for(i = 1; i <= list.size(); i++)
		{
			t = list.get(i);
			d = t.getValue();
			d *= val;
			res.add(new Term(t.getRowIndex(), t.getColumnIndex(), d));
		}
		return res;
	}
	public static ArrayList<Term> transpose(ArrayList<Term> a)
	{
		ArrayList<Integer>rowTerms = new ArrayList<Integer>();
		ArrayList<Integer> startingPos = new ArrayList<Integer>();
		int i, j, numCols = a.get(0).getColumnIndex();
		double numTerms = a.get(0).getValue();
		ArrayList<Term> b = new ArrayList<Term>();
		
		b.add(new Term(numCols,a.get(0).getRowIndex(),numTerms));
		
		if(numTerms > 0)
		{
			//Non-zero matrix
			for(i = 0; i < numCols; i++)
				rowTerms.add(0);
			for( i = 1; i <= numTerms; i++)
			{
				int v = rowTerms.get(a.get(i).getColumnIndex());
				v++;
				rowTerms.add(i, v);
			}
			startingPos.add(0,1);
			for(i = 1; i < numCols; i++)
			{
				int v1 = startingPos.get(i-1);
				int v2 = rowTerms.get(i-1);
				startingPos.add(i, (v1+v2));
			}
			for(i = 1; i <= numTerms; i++)
			{
				int v = startingPos.get(a.get(i).getColumnIndex());
				j = v;
				v++;
				startingPos.add(a.get(i).getColumnIndex(), v);
				Term t = new Term(a.get(i).getColumnIndex(),a.get(i).getRowIndex(), a.get(i).getValue() );
				b.add(j, t);
			}
		}
		return b;
	}
	public static void display(ArrayList<Term> a)
	{
		System.out.println("ROW\tCOLUMN\tVALUE");
		for(Term term : a)
			System.out.println(term.getRowIndex()+"\t"+term.getColumnIndex()+"\t"+term.getValue());
	}
	public static ArrayList<Double> product(double[][] vector,ArrayList<Term> matrix)
	{
		int numRows = matrix.get(0).getRowIndex();
		double numVals = matrix.get(0).getValue();
		int rows = vector.length;
		double sum = 0;
		int col = 0;
		double vectorValue;
		double sparseValue;
		Term sparseTerm;
		ArrayList<Double> result = new ArrayList<Double>();
		if(rows != numRows)
		{
			System.out.println("Incompatible. Cannot find product.");
			System.exit(0);
		}
		for(int i = 1; i <= rows; i++)
		{
			sum=0;
			for(int j = 1; j <=numVals; j++)
			{
				sparseTerm = matrix.get(j);
				if(sparseTerm.getColumnIndex() == col)
				{
					vectorValue = vector[sparseTerm.getRowIndex()][1];
					sparseValue = sparseTerm.getValue();
					sum += (vectorValue*sparseValue);
				}
			}
			result.add(sum);
			col++;
		}
		return result;
	}
	public static void main(String[] args)
	{
		
		ArrayList<Double> v = new ArrayList<Double>();
		v.add(0.5);
		v.add(0.2);
		v.add(0.6);
		v.add(0.8);
		ArrayList<Term> m = new ArrayList<Term>();
		m.add(new Term(4,4,5));
		m.add(new Term(0,0,1));
		m.add(new Term(3,2,2));
		m.add(new Term(1,2,3));
		m.add(new Term(3,1,4));
		m.add(new Term(3,0,6));
		
		//ArrayList<Double> r = product(v,m);
		//for(double d : r)
			//System.out.print(d+" ");
		System.out.println();
		ArrayList<Term> t = transpose(m);
		System.out.println("A-->");
		display(m);
		System.out.println("Transpose-->");
		display(t);
		
	}
	
}
