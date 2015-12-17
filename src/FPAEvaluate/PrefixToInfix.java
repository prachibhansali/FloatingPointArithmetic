package FPAEvaluate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class PrefixToInfix {
	String expression;
	int index=0;
	/**
	 * @param args
	 * @throws IOException 
	 */
	PrefixToInfix(String formula) {
		System.out.println(" *** Converting "+ formula + " from Prefix to Infix ***");
		formula=formula.substring(1,formula.length()-1);
		StringTokenizer st=new StringTokenizer(formula," ");
		ArrayList<String> arr = new ArrayList<String>();
		while(st.hasMoreElements())
			arr.add(st.nextToken());
		Expr e= convertToInfix(arr);
		expression = print(e);
		System.out.println(" -> Output Expression : " + expression + "\n");
	}

	public String getFormula()
	{
		return expression;
	}

	private String print(Expr e) {
		String infix = "";
		ArrayList<Object> exprs = e.getexprs();
		for(int i=0;i<exprs.size();i++)
		{
			String op = e.getOp();
			if(exprs.get(i) instanceof Expr)
			{
				if((exprs.size()>1 && i==0) || op==null)
					infix += print((Expr) exprs.get(i));
				else infix += op + print((Expr) exprs.get(i));
			}
			else 
			{
				if((exprs.size()>1 && i==0) || op==null)
					infix += exprs.get(i);
				else infix += op + exprs.get(i);
			}
		}
		return "(" + infix + ")";
	}

	private Expr convertToInfix(ArrayList<String> arr) {
		Expr e = new Expr();
		int index=0;
		while(index<arr.size())
		{
			String str=arr.get(index);
			//System.out.println(str);
			if(str.equals("+") || str.equals("*") || str.equals("-") || str.equals("/"))
				e.setOp(str);
			else if(str.equals("expt"))
				e.setOp("^");
			else if(str.equals("log"))
				e.setOp("LN");
			else if(str.equals("("))
			{
				int closebrace=index+1+findClosestBrace(arr.subList(index+1,arr.size()));
				e.addExpr(convertToInfix(new ArrayList<String>(arr.subList(index+1, closebrace))));
				index=closebrace;
			}
			else 
				e.addExpr(str);
			
			index+=1;
		}
		return e;
	}

	private int findClosestBrace(List<String> list) {
		int open=1;
		for(int i=0;i<list.size();i++)
		{
			if(list.get(i).equals(")")) open--;
			if(open==0) return i;
			if(list.get(i).equals("(")) open++;
		}
		return -1;
	}
}