package carHypertextGraph;


import java.util.ArrayList;

public class SparseMatrix 
{
	private static final int MAX_ROW = 12;
	private static final int MAX_COL = 12;
	private static final int MAX_TERMS = MAX_ROW * MAX_COL;
	
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
		for(i = 1; i < list.size(); i++)
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
	
	public static Term[] transpose(Term[] a)
	{
		int[] rowTerms = new int[MAX_COL], startingPos = new int[MAX_COL];
		int i, j, numCols = a[0].getColumnIndex();
		double numTerms = a[0].getValue();
		Term[] b = new Term[a.length];
		/*Initialise b*/
		for(i = 0 ; i < b.length ; i++)
			b[i] = new Term();
		
		b[0].setRowIndex(numCols);
		b[0].setColumnIndex(a[0].getRowIndex());
		b[0].setValue(numTerms);
		
		if(numTerms > 0)
		{
			/*Non-zero matrix*/
			for(i = 0; i < numCols; i++)
				rowTerms[i] = 0;
			for( i = 1; i <= numTerms; i++)
				rowTerms[a[i].getColumnIndex()]++;
			startingPos[0] = 1;
			for(i = 1; i < numCols; i++)
				startingPos[i] = startingPos[i-1] + rowTerms[i-1];
			for(i = 1; i <= numTerms; i++)
			{
				j = startingPos[a[i].getColumnIndex()]++;
				b[j].setRowIndex(a[i].getColumnIndex());
				b[j].setColumnIndex(a[i].getRowIndex());
				b[j].setValue(a[i].getValue());
			}
		}
		return b;
	}
	public static Term[] multiply(Term[] a, Term[] b)
	{
		//===========================================================================================
		                                     
		                                    //Local Variables
		 
		int i, j; //Used to examine successively elements from a row of A and a column of B
		int row = a[1].getRowIndex(); //Row of A that we are currently multiplying with the columns in B
		int column; //Column of B that we are currently multiplying with a row in A
		int rowBegin = 1; //Position in A of the first element of the current row
		int totalD = 0; //Current number of elements in the product matrix D
		double totalA = a[0].getValue(), totalB = b[0].getValue();
		int rowsA = a[0].getRowIndex(), colsA = a[0].getColumnIndex(), colsB = b[0].getColumnIndex();
		int sum = 0;
		
		//============================================================================================
		
		Term[] d = new Term[MAX_TERMS];
		
		//Initialise d
		for(i = 0 ; i < d.length ; i++)
			d[i] = new Term();
		if(colsA != b[0].getRowIndex())
		{
			System.out.println("Incompatible matrices");
			System.exit(0);
		}
		Term[] newB = transpose(b); //Sparse Matrix that is the transpose of B
		//display(newB);
		
		//set boundary condition
		//a[totalA+1].setRowIndex(rowsA);
		//newB[totalB+1].setRowIndex(colsB);
		//newB[totalB+1].setColumnIndex(0);
		
		for(i = 1; i <= totalA; )
		{
			column = newB[1].getRowIndex();
			for(j = 1; j <= totalB+1; )
			{
				//multiply row of a by column of b
				if(a[i].getRowIndex() != row)
				{
					storeSum(d,totalD,row,column,sum);
					totalD++;
					sum = 0;
					i = rowBegin;
					for( ; newB[j].getRowIndex() == column; j++);
					column = newB[j].getRowIndex();
				}
				else if(newB[j].getRowIndex() != column)
				{
					storeSum(d,totalD,row,column,sum);
					totalD++;
					sum = 0;
					i = rowBegin;
					column = newB[j].getRowIndex();
				}
				else
				{
					switch(compare(a[i].getColumnIndex(),newB[j].getColumnIndex()))
					{
					case -1: //go to next term in a
						i++;
						break;
					case 0: //add terms, go to next term in a and b
						sum += (a[i++].getValue() * newB[j++].getValue());
						break;
					case 1: //advance to next term in b
						j++;
					}
				}
			}
			for( ; a[i].getRowIndex() == row; i++);
			rowBegin = i;
			row = a[i].getRowIndex();
		}
		d[0].setRowIndex(rowsA);
		d[0].setColumnIndex(colsB);
		d[0].setValue(totalD);
		
		return d;
	}
	
	private static void storeSum(Term[] d, int totalD, int row, int column, int sum)
	{
		/*if sum!=0, then it along with its row and column position is stored as the totalD+1 entry in d*/
		if(sum != 0)
		{
			if(totalD < MAX_TERMS)
			{
				totalD++;
				d[totalD].setRowIndex(row);
				d[totalD].setColumnIndex(column);
				d[totalD].setValue(sum);
			}
			else
			{
				System.out.println("Number of terms in product exceeds"+" "+MAX_TERMS);
				System.exit(0);
			}
		}
	}
	private static int compare(int x, int y)
	{
		if(x < y)
			return -1;
		else if( x == y)
			return 0;
		else
			return 1;
	}
	public static void display(Term[] a)
	{
		System.out.println("ROW\tCOLUMN\tVALUE");
		for(Term term : a)
			System.out.println(term.getRowIndex()+"\t"+term.getColumnIndex()+"\t"+term.getValue());
	}
	public static void display(ArrayList<Term> a)
	{
		System.out.println("ROW\tCOLUMN\tVALUE");
		for(Term term : a)
			System.out.println(term.getRowIndex()+"\t"+term.getColumnIndex()+"\t"+term.getValue());
	}
	public static ArrayList<Double> product(ArrayList<Double> vector,ArrayList<Term> matrix)
	{
		int numRows = matrix.get(0).getRowIndex();
		double numVals = matrix.get(0).getValue();
		int vectorSize = vector.size();
		double sum = 0;
		int col = 0;
		double vectorValue;
		double sparseValue;
		Term sparseTerm;
		ArrayList<Double> result = new ArrayList<Double>();
		if(vectorSize != numRows)
		{
			System.out.println("Incompatible. Cannot find product.");
			System.exit(0);
		}
		for(int i = 1; i <= vectorSize; i++)
		{
			sum=0;
			for(int j = 1; j <=numVals; j++)
			{
				sparseTerm = matrix.get(j);
				if(sparseTerm.getColumnIndex() == col)
				{
					vectorValue = vector.get(sparseTerm.getRowIndex());
					sparseValue = sparseTerm.getValue();
					sum += (vectorValue*sparseValue);
				}
			}
			result.add(sum);
			System.out.println("sum="+sum);
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
		
		ArrayList<Double> r = product(v,m);
		for(double d : r)
			System.out.print(d+" ");
		System.out.println();
		ArrayList<Term> t = transpose(m);
		System.out.println("A-->");
		display(m);
		System.out.println("Transpose-->");
		display(t);
		
	}
	
}
