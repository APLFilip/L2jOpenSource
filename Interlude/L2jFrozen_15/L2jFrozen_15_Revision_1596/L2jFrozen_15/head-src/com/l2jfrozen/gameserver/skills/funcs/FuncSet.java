package com.l2jfrozen.gameserver.skills.funcs;

import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.Stats;

public class FuncSet extends Func
{
	private final Lambda lambda;
	
	public FuncSet(final Stats pStat, final int pOrder, final Object owner, final Lambda lambda)
	{
		super(pStat, pOrder, owner);
		this.lambda = lambda;
	}
	
	@Override
	public void calc(final Env env)
	{
		if (cond == null || cond.test(env))
		{
			env.value = lambda.calc(env);
		}
	}
}
