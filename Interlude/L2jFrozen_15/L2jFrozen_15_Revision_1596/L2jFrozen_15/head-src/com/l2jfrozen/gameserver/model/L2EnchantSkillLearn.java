package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */

public final class L2EnchantSkillLearn
{
	// these two build the primary key
	private final int id;
	private final int level;
	// not needed, just for easier debug
	private final String name;
	private final int spCost;
	private final int baseLvl;
	private final int minSkillLevel;
	private final int exp;
	private final byte rate76;
	private final byte rate77;
	private final byte rate78;
	private final byte rate79;
	private final byte rate80;
	
	public L2EnchantSkillLearn(final int id, final int level, final int minSkillLevel, final int baseLvl, final String name, final int spCost, final int exp, final byte rate76, final byte rate77, final byte rate78, final byte rate79, final byte rate80)
	{
		this.id = id;
		this.level = level;
		this.baseLvl = baseLvl;
		this.minSkillLevel = minSkillLevel;
		this.name = name.intern();
		this.spCost = spCost;
		this.exp = exp;
		this.rate76 = rate76;
		this.rate77 = rate77;
		this.rate78 = rate78;
		this.rate79 = rate79;
		this.rate80 = rate80;
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return level;
	}
	
	/**
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return baseLvl;
	}
	
	/**
	 * @return Returns the minSkillLevel.
	 */
	public int getMinSkillLevel()
	{
		return minSkillLevel;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return Returns the spCost.
	 */
	public int getSpCost()
	{
		return spCost;
	}
	
	public int getExp()
	{
		return exp;
	}
	
	public byte getRate(final L2PcInstance ply)
	{
		byte result;
		switch (ply.getLevel())
		{
			case 76:
				result = rate76;
				break;
			case 77:
				result = rate77;
				break;
			case 78:
				result = rate78;
				break;
			case 79:
				result = rate79;
				break;
			case 80:
				result = rate80;
				break;
			default:
				result = rate80;
				break;
		}
		return result;
	}
}
