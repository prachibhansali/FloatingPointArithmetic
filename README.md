## FloatingPointArithmeticRepository
Find the smallest variable whose floating point value can be tweaked such that the expression stays valid 

#Flow of the code

#Create an initial object:
      1. expressions, read from the formula file
      2. PrefixToInfix object, converts the formula from prefix to infix
      3. JavaScript ScriptEngine object, evaluates String expressions
      4. key-value pairs. read the initial values of variable from the initializers file
      5. delta, initial value of delta by which the variable value will increment at every iteration (set to 
         Double.MIN_VALUE by default)
      6. variableToChange, the most suitable variable that should be modified
              If the variable has not been given as an input, take derivative w.r.t each variable and the one which                        generates a curve closest to a straight line is selected as the variable whose value woudl be changed since                  it implies the variable would affect the expression the least.

Once the values are set, the program runs in 2 phases:


##Phase 1: Exponential search

Once the variable to change has been decided, take derivative w.r.t variable and parameter "increasing" is set to true or false based on whether the expression increases or decreases w.r.t the function. 

      while evalFunc not positive i.e. expression does not evaluate to value greater than zero
            delta = 2*delta
            store oldVariableValue
            changedVariableValue = if increasing? oldVariableValue+delta else oldVariableValue-delta
            evalFunc = evaluate formula with initializers and changedVariableValue using engine
            if the change from previous evalFunc to new evalFunc is zero
            return Seen too many computations but no value found! Likely that it doesnt Exist.. Exiting..
            if variable changes direction from increasing to decreasing
            return Switched Direction of graph.. Exiting..
        

#Phase 2: Binary Seach between the range oldVariableValue and changedVariableValue 

Range from oldVariableValue and changedVariableValue is the range where the function correctly evaluates but we try and corner to the best and smallest possible variable value

      binary_search(low=oldVariableValue,high=changedVariableValue)
      { 
            mid = low+high /2
            if high-low < delta and function evaluated to positive for mid
                  return mid as the new value of the variable
            else if function evaluates to positive with mid
            *since the function already evaluates to positive, we should search on the left because we COULD probably find a smaller value which would still evaluate the function to positive and that would be a better value to be chosen for the variable*
                  return binary_search(oldVariable,mid)
            else 
            *since the function already evaluated to negative we need to move it in a direction such that it would increment the function evaluation just to make it positive*
                  return binary_search(mid,changedVariableValue)

}
