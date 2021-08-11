package com.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;

public class Rule_Enable extends EnterRule
{
    boolean IsExceptList = false;
    String[] IdentifierList = null;

    public Rule_Enable(Rule_Context pContext, Reduction pToken) 
    {
    	super(pContext);
        //<IdentifierList> ::= <IdentifierList> Identifier | Identifier

        if (pToken.size() > 2)
        {
            //<Hide_Except_Statement> ::= HIDE '*' EXCEPT <IdentifierList>
            this.IsExceptList = true;
            this.IdentifierList = this.ExtractIdentifier(pToken.get(3)).toString().split(" ");
        }
        else
        {
            //<Hide_Some_Statement> ::= HIDE <IdentifierList>
            this.IdentifierList = this.ExtractIdentifier(pToken.get(1)).toString().split(" ");
        }
    }


    /// <summary>
    /// performs execution of the HIDE command via the EnterCheckCodeInterface.Hide method
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        this.Context.CheckCodeInterface.Enable(this.IdentifierList, this.IsExceptList);
        return null;
    }
}
