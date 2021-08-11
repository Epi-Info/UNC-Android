package com.epiinfo.interpreter.functions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.epiinfo.interpreter.EnterRule;
import com.epiinfo.interpreter.Rule_Context;

public class Rule_Day extends EnterRule 
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_Day(Rule_Context pContext, Reduction pToken)
    {
    	super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }

    /// <summary>
    /// Executes the reduction.
    /// </summary>
    /// <returns>Returns the date difference in years between two dates.</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        Object p1 = this.ParameterList.get(0).Execute();

        if (p1 instanceof Date)
        {

            Date param1 = (Date)p1;
            result = param1.getDay();
        }
         
        return result;
    }
}
