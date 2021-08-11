package com.epiinfo.interpreter.functions;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.epiinfo.interpreter.EnterRule;
import com.epiinfo.interpreter.Rule_Context;

public class Rule_CurrentUser extends EnterRule
{
    public Rule_CurrentUser(Rule_Context pContext, Reduction pToken)
	{
    	super(pContext);
	    // UserId
	}
	
	/// <summary>
	/// Executes the reduction.
	/// </summary>
	/// <returns>Returns the current system date.</returns>
    @Override
	public Object Execute()
	{
	    return "NOUSERNAME";
	}

}
