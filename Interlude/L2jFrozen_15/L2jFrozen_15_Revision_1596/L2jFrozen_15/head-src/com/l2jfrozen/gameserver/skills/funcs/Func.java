package com.l2jfrozen.gameserver.skills.funcs;

import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.conditions.Condition;

/**
 * A Func object is a component of a Calculator created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematics function : <BR>
 * <BR>
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
 * <BR>
 * When the calc method of a calculator is launched, each mathematics function is called according to its priority <B>_order</B>. Indeed, Func with lowest priority order is executed first and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in the
 * value property of an Env class instance.<BR>
 * <BR>
 */
public abstract class Func
{
	
	/** Statistics, that is affected by this function (See L2Character.CALCULATOR_XXX constants) */
	public final Stats stat;
	
	/**
	 * Order of functions calculation. Functions with lower order are executed first. Functions with the same order are executed in unspecified order. Usually add/subtract functions has lowest order, then bonus/penalty functions (Multiply/divide) are applied, then functions that do more complex
	 * calculations (non-linear functions).
	 */
	public final int order;
	
	/**
	 * Owner can be an armor, weapon, skill, system event, quest, etc Used to remove all functions added by this owner.
	 */
	public final Object funcOwner;
	
	/** Function may be disabled by attached condition. */
	public Condition cond;
	
	/**
	 * Constructor of Func.<BR>
	 * <BR>
	 * @param pStat
	 * @param pOrder
	 * @param owner
	 */
	public Func(final Stats pStat, final int pOrder, final Object owner)
	{
		stat = pStat;
		order = pOrder;
		funcOwner = owner;
	}
	
	/**
	 * Add a condition to the Func.<BR>
	 * <BR>
	 * @param pCond
	 */
	public void setCondition(final Condition pCond)
	{
		cond = pCond;
	}
	
	/**
	 * Run the mathematics function of the Func.<BR>
	 * <BR>
	 * @param env
	 */
	public abstract void calc(Env env);
	
}
