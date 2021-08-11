package com.epiinfo.interpreter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Token;
import com.epiinfo.interpreter.CSymbol.VariableScope;



public class Rule_Assign extends EnterRule
{
    public String QualifiedId;
    private String Namespace = null;
    //private string functionCall;
    //private string Identifier;
    EnterRule value = null;
    //Epi.View View = null;

    //object ReturnResult = null;

    public Rule_Assign(Rule_Context pContext, Reduction pTokens)
    {
    	super(pContext);
        //ASSIGN <Qualified ID> '=' <Expression>
        //<Let_Statement> ::= LET Identifier '=' <Expression> 
        //<Simple_Assign_Statement> ::= Identifier '=' <Expression>
        //<Assign_DLL_Statement> ::= ASSIGN <Qualified ID> '=' identifier'!'<FunctionCall>
        String[] temp;
        Token T;
        
        switch(Rule_Enum.Convert(pTokens.getParent().getHead().getName()))
        {
              
            case Assign_Statement:
                T = pTokens.get(1);
                if (T.getName().equalsIgnoreCase("<Fully_Qualified_Id>"))
                {
                    //temp = this.ExtractTokens(T.Tokens).Split(' ');
                	temp = this.ExtractIdentifier(pTokens.get(1)).split("\\.");
                    this.Namespace = temp[0];
                    this.QualifiedId = temp[1];
                }
                else
                {
                    this.QualifiedId = this.ExtractIdentifier(pTokens.get(1));
                }
                this.value = EnterRule.BuildStatements(pContext, (Reduction) pTokens.get(3).getData());
                
                if (!this.Context.AssignVariableCheck.containsKey(this.QualifiedId.toLowerCase()))
                {
                    this.Context.AssignVariableCheck.put(this.QualifiedId.toLowerCase(), this.QualifiedId.toLowerCase());
                }
                break;
            case Let_Statement:
                T = pTokens.get(1);
                if (T.getName().equalsIgnoreCase("<Fully_Qualified_Id>"))
                {
                    //temp = this.ExtractTokens(T.Tokens).Split(' ');
                	temp = this.ExtractIdentifier(T).split("\\.");
                    this.Namespace = pTokens.get(0).getData().toString();
                    this.QualifiedId = pTokens.get(2).getData().toString();
                }
                else
                {
                    this.QualifiedId = this.ExtractIdentifier(pTokens.get(0));
                }

                
                this.value = EnterRule.BuildStatements(pContext, (Reduction)pTokens.get(3).getData());

                if (!this.Context.AssignVariableCheck.containsKey(this.QualifiedId.toLowerCase()))
                {
                    this.Context.AssignVariableCheck.put(this.QualifiedId.toLowerCase(), this.QualifiedId.toLowerCase());
                }
                break;
            case Simple_Assign_Statement:
                //Identifier '=' <Expression>
                //T = pTokens.get(1);
            	
                T = pTokens.get(0);
                if (T.getName().equalsIgnoreCase("Fully_Qualified_Id"))
                {
                    //temp = this.ExtractTokens(T.Tokens).Split(' ');
                    this.Namespace = this.ExtractIdentifier(pTokens.get(0));
                    this.QualifiedId = this.ExtractIdentifier(pTokens.get(2));
                }
                else
                {
                    this.QualifiedId = this.ExtractIdentifier(pTokens.get(0));
                }

                
                this.value = EnterRule.BuildStatements(pContext, (Reduction)pTokens.get(2).getData());
                if (!this.Context.AssignVariableCheck.containsKey(this.QualifiedId.toLowerCase()))
                {
                    this.Context.AssignVariableCheck.put(this.QualifiedId.toLowerCase(), this.QualifiedId.toLowerCase());
                }
                break;
		default:
			break;

        }
    }
    /// <summary>
    /// peforms an assign rule by assigning an expression to a variable.  return the variable that was assigned
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        Object result = this.value.Execute();
        
        CSymbol var;
        String dataValue = "";
        var =  this.Context.GetCurrentScope().resolve(this.QualifiedId, this.Namespace);

        if (var != null)
        {
            if (var.VariableScope == VariableScope.DataSource)
            {
                var.Value = result;
            }
            else
            {
                if (!isNullOrEmpty(this.Namespace))
                {
                    result = var.Value;
                }
                else if (result != null)
                {
                    var.Value = result;
                }
                else
                {
                    var.Value = null;
                }

                if (var.VariableScope == VariableScope.Permanent)
                {
                    //Rule_Context.UpdatePermanentVariable(var);
                }
            }
        }
        else
        {
            this.Context.CheckCodeInterface.Assign(this.QualifiedId, CheckSystemVariables(result));
        }
        
        return result;
    }
    
    private Object CheckSystemVariables(Object result)
    {
    	if (result.toString().equals("SYSTEMTIME"))
    	{
    		DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
    		Date date = new Date();
    		return dateFormat.format(date);
    	}
    	else if (result.toString().equals("SYSTEMDATE"))
    	{
    		DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
    		Date date = new Date();
    		return dateFormat.format(date);
    	}
    	return result;
    }

    /*

    private Epi.DataType GuessDataTypeFromExpression(string expression)
    {
        double d = 0.0;
        DateTime dt;
        if (double.TryParse(expression, out d))
        {
            return DataType.Number;
        }
        if (System.Text.RegularExpressions.Regex.IsMatch(expression, "([+,-])"))
        {
            return DataType.Boolean;
        }
        if (DateTime.TryParse(expression, out dt))
        {
            return DataType.Date;
        }
        return DataType.Unknown;
    }*/
}
