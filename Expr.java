package FPAEvaluate;

import java.util.ArrayList;

class Expr {
	private	String op;
	private ArrayList<Object> loe = new ArrayList<Object>();
	
	public void addExpr(Expr e)
	{
		loe.add(e);
	}
	
	public ArrayList<Object> getexprs() {
		return loe;
	}
	
	public void addExpr(String e)
	{
		loe.add(e);
	}
	
	public void setOp(String op)
	{
		this.op=op;
	}
	
	public String getOp()
	{
		return op;
	}

}
