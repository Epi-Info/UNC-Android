package com.epiinfo.interpreter.functions;

import java.util.ArrayList;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.epiinfo.interpreter.EnterRule;
import com.epiinfo.interpreter.Rule_Context;

public class Rule_GetCoordinates extends EnterRule
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_GetCoordinates(Rule_Context pContext, Reduction pToken)
        
    {
    	super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }

    /// <summary>
    /// Executes the reduction.
    /// </summary>
    /// <returns>Returns the absolute value of two numbers.</returns>
    @Override
    public  Object Execute()
    {

            return null;
        
    }
}
