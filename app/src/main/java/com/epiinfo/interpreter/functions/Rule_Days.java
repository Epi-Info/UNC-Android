package com.epiinfo.interpreter.functions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.creativewidgetworks.goldparser.engine.Reduction;
import com.epiinfo.interpreter.EnterRule;
import com.epiinfo.interpreter.Rule_Context;

public class Rule_Days extends EnterRule
{
    private List<EnterRule> ParameterList = new ArrayList<EnterRule>();

    public Rule_Days(Rule_Context pContext, Reduction pToken)

    {
        super(pContext);
        this.ParameterList = EnterRule.GetFunctionParameters(pContext, pToken);
    }

    /// <summary>
    /// Executes the reduction
    /// </summary>
    /// <returns>returns the date difference in minutes between two dates.</returns>
    @Override
    public Object Execute()
    {
        Object result = null;

        Object p1 = this.ParameterList.get(0).Execute();
        Object p2 = this.ParameterList.get(1).Execute();

        if (p1 instanceof Date && p2 instanceof Date)
        {
            Date param1 = (Date)p1;
            Date param2 = (Date)p2;

            //TimeSpan timeSpan = param2 - param1;
            //result = timeSpan.Days;

        }

        return result;
    }
}
