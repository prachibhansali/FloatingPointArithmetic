package FPAEvaluate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import javacalculus.core.CALC;
import javacalculus.core.CalcParser;
import javacalculus.struct.CalcObject;
import javacalculus.evaluator.CalcSUB;
import javacalculus.struct.CalcDouble;
import javacalculus.struct.CalcSymbol;

public class FPAEvaluate {

	String formula;
	String variableToChange;
	double initialValue;
	boolean increasing;
	double delta;
	//int power;
	ScriptEngine engine = null; 
	Object bsval=null;
	Object funcval=null;
	Set<String> keys = null;
	double exp;

	FPAEvaluate(String loc,String v) throws Exception
	{
		try{
			formula=new BufferedReader(new FileReader(loc+"formula.txt")).readLine();
			PrefixToInfix p=new PrefixToInfix(formula);
			formula=p.getFormula();
			engine = (new ScriptEngineManager()).getEngineByName("js"); 
			keys = new HashSet<String>();
			getKeyValues(loc+"initializers");
			delta=Double.MIN_VALUE;
			//power=0;
			exp=delta;
			variableToChange=v;//selectSuitableVariable();
			initialValue=(Double) engine.get(variableToChange);
		}
		catch(Exception e)
		{
			System.out.println();
		}
	}

	FPAEvaluate(String loc,String v,double delta) throws Exception
	{
		formula=new BufferedReader(new FileReader(loc+"formula.txt")).readLine();
		PrefixToInfix p=new PrefixToInfix(formula);
		formula=p.getFormula();
		engine = (new ScriptEngineManager()).getEngineByName("js"); 
		keys = new HashSet<String>();
		getKeyValues(loc+"initializers");
		this.delta=delta;
		//power=0;
		exp=delta;
		variableToChange=v;//selectSuitableVariable();
		initialValue=(Double) engine.get(variableToChange);
	}

	FPAEvaluate(String loc) throws Exception
	{
		formula=new BufferedReader(new FileReader(loc+"formula.txt")).readLine();
		PrefixToInfix p=new PrefixToInfix(formula);
		formula=p.getFormula();
		engine = (new ScriptEngineManager()).getEngineByName("js"); 
		keys = new HashSet<String>();
		getKeyValues(loc+"initializers");
		this.delta=Double.MIN_VALUE;
		//power=0;
		exp=delta;
		variableToChange=selectSuitableVariable();
		initialValue=(Double) engine.get(variableToChange);
	}

	private String selectSuitableVariable() throws Exception 
	{
		Iterator<String> keysitr= keys.iterator();
		String resvar="",minkey="";
		double min=Double.MAX_VALUE;
		double avgerror=0.0;
		HashMap<String,Double> keysderiv = new HashMap<String,Double>();
		while(keysitr.hasNext())
		{
			String var= (String) keysitr.next();
			double error = Math.abs(Math.abs(computeDerivateDirection(var))-1);
			System.out.println(error+" "+var);
			if(error >= 0) 
			{
				keysderiv.put(var, error);
				if(error<min) {
					min = error;
					minkey = var;
				}
				avgerror+=error;
			}
		}
		avgerror/=keys.size();
		resvar=minkey;
		System.out.println("The chosen one : "+resvar);
		return resvar;
	}

	public static void main (String args[]){
		//BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		//System.out.println("Enter variable to change : ");
		FPAEvaluate fpa=null;
		try {
			if(args.length==2) fpa = new FPAEvaluate(args[0],args[1]);
			else if(args.length==3) fpa = new FPAEvaluate(args[0],args[1],Double.parseDouble(args[2]));
			else if(args.length==1) fpa = new FPAEvaluate(args[0]);
			else throw new IOException();
		} catch (Exception e1) {
			System.out.println("Could not initialize FPA object.. Exiting.."+e1.toString());
			return;
		}
		if(fpa.evaluateFunction()>0) 
		{
			System.out.println("Function already positive");
			return;
		}
		// calculate derivative
		try{
			fpa.checkDerivativeForIncreasing();
		}
		catch(Exception e)
		{
			System.out.println("Could not find the derivative.. Exiting..");
			return;
		}
		try{
			fpa.insertVariableValues();
		}
		catch(Exception e)
		{
			System.out.println("Faced an error while trying values.. Exiting..");
			return;
		}
	}

	private void getKeyValues(String f) throws IOException {
		BufferedReader br=new BufferedReader(new FileReader(f));
		String s="";
		while((s=br.readLine())!=null)
		{
			String[] words = s.split(" ");
			engine.put(words[0], Double.parseDouble(words[1]));
			keys.add(words[0]);
		}
	}

	public double evaluateFunction()
	{
		double result = 0;
		try
		{
			CalcParser parser=new CalcParser();
			CalcObject parsed=parser.parse(formula);
			CalcObject obj = parsed.evaluate();
			Iterator<String> itr=keys.iterator();
			while(itr.hasNext())
			{
				String key=itr.next().toString();
				obj=subst(obj,key,(Double) engine.get(key));
			}
			result = Double.parseDouble(CALC.SYM_EVAL(obj).toString());
		}
		catch(Exception e){System.out.println("error " +e);}
		return result;
	}

	private void insertVariableValues() throws Exception {
		System.out.println(" *** Evaluating for distinct inputs for "+variableToChange+"*** ");
		double funcEval = 0;
		Double oldval=null;
		long count =0;
		do
		{
			double prevFuncEval = funcEval;
			double modVar = changeValue((Double) engine.get(variableToChange),getdelta());
			//System.out.println(modVar+" "+Double.MIN_VALUE+ " "+(modVar>Double.MAX_VALUE)+" "+(modVar<Double.MIN_VALUE));
			if(modVar>Double.MAX_VALUE || Math.abs(modVar)<Double.MIN_VALUE) {
				System.out.println("Exceeding valuation but no value found! Likely that it doesnt Exist.. Exiting..");
				return;
			}
			oldval=(Double)engine.get(variableToChange);
			engine.put(variableToChange, modVar);
			funcEval = evaluateFunction();
			System.out.println("Variable value = "+modVar);
			if(prevFuncEval!=funcEval && Math.abs(prevFuncEval-funcEval)<Double.MIN_VALUE) {
				System.out.println("Seen too many computations but no value found! Likely that it doesnt Exist.. Exiting..");
				return;
			}
			boolean newdir = computeDerivateDirection(variableToChange) >=0;
			if(newdir!=increasing) {
				System.out.println("Switched Direction of graph.. Exiting..");
				return;
			}
			System.out.println("Function Evaluated to  = "+funcEval+" "+ ++count);
		}
		while(funcEval<=0);
		System.out.println(" -> Function Evaluated to > 0 with " + (Double)engine.get(variableToChange));
		System.out.println(" -> delta value power to " + exp +"\n");

		long a = System.currentTimeMillis();

		System.out.println(" ** Starting Binary Search to obtain values between ** ");
		System.out.println(" -> from "+ oldval + " ------> " + (Double)engine.get(variableToChange) + "\n");
		BinarySearch(oldval,(Double)engine.get(variableToChange));
		long b = System.currentTimeMillis();
		System.out.println(" ** Ended Binary Search ** ");

		System.out.println("Time elapsed : "+ (b-a) +" milliseconds\n");		

		System.out.println("New Value of " + variableToChange + "  --->  " + bsval);
		System.out.println("Final Evaluation value : " + funcval);
	}

	public void BinarySearch(double low,double high)
	{
		System.out.println("low== " + low + "  " + high+" "+Double.MIN_EXPONENT);
		double two = 2.0f;
		double mid=(low+(high-low)/two);//new BigDecimal(low+(high-low)/2.0).setScale(20,BigDecimal.ROUND_HALF_DOWN).doubleValue();
		System.out.println("Evaluating for " + mid + " position");
		Double funcEval;
		engine.put(variableToChange, mid);
		System.out.println("New var value " + engine.get(variableToChange));
		funcEval = evaluateFunction();
		if(Math.abs(low-high) <= delta || mid==low || mid==high) {
			if(funcEval > 0) 
			{
				bsval=engine.get(variableToChange);
				funcval=funcEval;
				System.out.println("Function Evaluated to in mid= "+funcEval);
			}
			return;
		}
		funcEval = evaluateFunction();
		if(funcEval > 0) {
			bsval=engine.get(variableToChange);
			funcval=funcEval;
			System.out.println(" Evaluation > 0 for " + mid + " position ** search further left for smaller delta **");
			System.out.println("Evaluation value : " + funcEval);
			BinarySearch(low, (Double)bsval);
		}
		else {
			System.out.println(" Evaluation <= 0 for " + mid + " position ** search further right for higher delta **");
			System.out.println("Evaluation value : " + funcEval);
			BinarySearch((Double)engine.get(variableToChange),high);
		}
		return;
	}

	private double changeValue(double currvar,double delta) {
		return (increasing? currvar + delta : currvar - delta);
	}

	private double getdelta() {
		/*int p = power;
		double d=delta;
		while(p>1023)
		{
			d = (double)d*Math.pow(2, 1023);
			p-=1023;
		}
		d =  (double) (d * Math.pow(2, p));
		power++;
		return d;*/
		exp*=2;
		return exp;
	}

	static CalcObject subst(CalcObject input, String var, double number)
	{
		CalcSymbol symbol = new CalcSymbol(var);
		CalcDouble value = new CalcDouble(number);
		return CalcSUB.numericSubstitute(input, symbol, value);
	}

	private void checkDerivativeForIncreasing() throws Exception {
		System.out.println(" *** Fetching Derivative *** ");
		increasing = computeDerivateDirection(variableToChange) >=0;
		System.out.println("-> is variable increasing w.r.t function? " + increasing + "\n");	    
	}

	private double computeDerivateDirection(String v) throws Exception
	{
		String command ="DIFF("+ formula +"," + v + ")";
		CalcParser parser=new CalcParser();
		System.out.println(command);
		CalcObject parsed=parser.parse(command);
		CalcObject result = parsed.evaluate();
		Iterator<String> itr=keys.iterator();
		while(itr.hasNext())
		{
			String key=itr.next().toString();
			result=subst(result,key,(Double) engine.get(key));
		}
		double answer = Double.parseDouble(CALC.SYM_EVAL(result).toString());
		System.out.println("derv answer "+answer);
		return answer;
	}

}