package FPAEvaluate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javacalculus.core.CALC;
import javacalculus.core.CalcParser;
import javacalculus.struct.CalcObject;
import javacalculus.evaluator.CalcSUB;
import javacalculus.exception.CalcSyntaxException;
import javacalculus.struct.CalcDouble;
import javacalculus.struct.CalcSymbol;
public class FPAEvaluateBinarySearch {
	
	String formula;
	String variableToChange;
	double initialValue;
	boolean increasing;
	double delta;
	int power;
	ScriptEngine engine = null; 
	Object bsval=null;
	
	FPAEvaluateBinarySearch(String v) throws IOException
	{
		formula=new BufferedReader(new FileReader("C:\\Users\\Bhansali\\Desktop\\prachi\\NEU\\Racket\\FPA\\formula.txt")).readLine();
        engine = (new ScriptEngineManager()).getEngineByName("js"); 
		getKeyValues("C:\\Users\\Bhansali\\Desktop\\prachi\\NEU\\Racket\\FPA\\initializers1.txt");
		delta=0.001;
		power=0;
		variableToChange=v;
		initialValue=(Double) engine.get(variableToChange);
	}
	
	private void getKeyValues(String f) throws IOException {
		BufferedReader br=new BufferedReader(new FileReader(f));
		String s="";
		while((s=br.readLine())!=null)
		{
			String[] words = s.split(" ");
			engine.put(words[0], Double.parseDouble(words[1]));
		}
	}

	public double evaluateFunction()
	{
	double result = 0;
    try
    {
    	result = (Double) engine.eval(formula);
    }
    catch(Exception e){System.out.println("error " +e);}
    return result;
	}
	
	public static void main (String args[]) throws Exception{
	BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
	System.out.println("Enter variable to change : ");
	FPAEvaluateBinarySearch fpa=new FPAEvaluateBinarySearch(br.readLine());
	// calculate derivative
	fpa.checkDerivativeForIncreasing();
	fpa.insertVariableValues();
	}

	private void insertVariableValues() {
		System.out.println("Trying different input values for "+variableToChange+"\n");
		double funcEval;
		double oldval;
		do
		{
			double modVar = changeValue((Double) engine.get(variableToChange),getdelta());
			oldval=(Double)engine.get(variableToChange);
			engine.put(variableToChange, modVar);
			funcEval = evaluateFunction();
			System.out.println("Old value= "+oldval+ "\n Evaluated value for= "+ modVar+ "\n Evaluation= " + funcEval);
		}
		while(funcEval<=0);
		long a = System.currentTimeMillis();
		System.out.println(oldval+ " "+(Double)engine.get(variableToChange));
		BinarySearch(oldval,(Double)engine.get(variableToChange));
		long b = System.currentTimeMillis();
		System.out.println("Time elapsed : "+(b-a));		
		System.out.println("Answer : "+bsval);
	}

	private void evaluateFunctionBackwards() {
		System.out.println("Start Evaluating Backwards\n");
		double funcEval;
		double currvar = 0.0f;
		do
		{
			currvar = (Double) changeValueBackwards((Double) engine.get(variableToChange),delta);
			engine.put(variableToChange, currvar);
			funcEval = evaluateFunction();
			//System.out.println(currvar+ " " + funcEval);
		}
		while(funcEval>0);
		engine.put(variableToChange, changeValue(currvar,delta));
	}

	private Object changeValueBackwards(double currvar,double delta) {
		return (increasing? currvar - delta : currvar + delta);
	}

	private double changeValue(double currvar,double delta) {
		return (increasing? currvar + delta : currvar - delta);
	}

	private double getdelta() {
		return (double) (delta * Math.pow(2, power++));
	}

	static CalcObject subst(CalcObject input, String var, double number)
    {
        CalcSymbol symbol = new CalcSymbol(var);
        CalcDouble value = new CalcDouble(number);
        return CalcSUB.numericSubstitute(input, symbol, value);
    }
	
	private void checkDerivativeForIncreasing() throws Exception {
		System.out.println("Fetching Derivative");
		String command ="DIFF("+ formula +"," + variableToChange + ")";
		System.out.println(command);
		CalcParser parser=new CalcParser();
		CalcObject parsed=parser.parse(command);
		CalcObject result = parsed.evaluate();
		Iterator<String> itr=engine.getBindings(ScriptContext.GLOBAL_SCOPE).keySet().iterator();
		while(itr.hasNext())
	    {
	    	String key=itr.next().toString();
	    	result=subst(result,key,(Double) engine.get(key));
	    }
	    double answer = Double.parseDouble(CALC.SYM_EVAL(result).toString());
	    increasing = answer>=0;	
	    System.out.println("-> is-increasing w.r.t function? " + increasing);	    
	}
	
	
	
	public void BinarySearch(double low,double high)
	{
		long mid =(long) (((high-low)*0.5)/delta);
		mid=mid>=0? mid : -mid;
		Double funcEval;
		System.out.println("low ==  " + low+ " " + mid+" " + high);
		if(mid<1) {
			engine.put(variableToChange, changeValue(low,mid*delta));
			funcEval = evaluateFunction();
			System.out.println("in mid  "+low+ " " + funcEval);
			if(funcEval > 0) {
			bsval=engine.get(variableToChange);
			System.out.println("Binary search > 0 =" + engine.get(variableToChange));
		}
			return;
		}
		engine.put(variableToChange, changeValue(low,mid*delta));
		funcEval = evaluateFunction();
		//System.out.println("Binary search ==  "+mid*delta+ " " + funcEval);
		if(funcEval > 0) {
			bsval=engine.get(variableToChange);
			System.out.println("Binary search > 0 =" + engine.get(variableToChange));
			BinarySearch(low, (Double)engine.get(variableToChange));
		}
		else {
			System.out.println("Binary search <= 0 =" + engine.get(variableToChange));
			BinarySearch((Double)engine.get(variableToChange),high);
		}
			//LinearSearch(mid, high);
		return;
	}

	private void LinearSearch(double low, double high) {
		double funcEval;
		do
		{
			double modVar = changeValue((Double) engine.get(variableToChange),delta);
			engine.put(variableToChange, modVar);
			funcEval = evaluateFunction();
			//System.out.println(modVar+ " " + funcEval);
		}
		while(funcEval<=0);
	}
}