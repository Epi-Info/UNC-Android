package com.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_DefineVariables_Statement extends EnterRule 
{
	private EnterRule define_Statements_Group = null;

    public Rule_DefineVariables_Statement(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);

        //<DefineVariables_Statement> ::= DefineVariables <Define_Statement_Group> End-DefineVariables
        if (pToken.size() > 2)
        {
            //define_Statements_Group = new Rule_Define_Statement_Group(pContext, (NonterminalToken)pToken.Tokens[1]);
            define_Statements_Group = EnterRule.BuildStatements(pContext, (Reduction) pToken.get(1).getData());
        }
    }

    
   /// <summary>
    /// performs execute command
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {

        //if (define_Statements_Group != null && define_Statements_Group.Define_Statement_Type != null && this.Context.EnterCheckCodeInterface.IsExecutionEnabled)
        //if (define_Statements_Group != null && this.Context.EnterCheckCodeInterface.IsExecutionEnabled)
    	if (define_Statements_Group != null)
        {
            this.Context.DefineVariablesCheckcode = this;
            return define_Statements_Group.Execute();
        }
        else
        {
            this.Context.DefineVariablesCheckcode = null;
            return null;
        }
    }


    @Override
    public boolean IsNull() { return this.define_Statements_Group == null; } 
}
