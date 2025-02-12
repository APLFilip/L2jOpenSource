package com.l2jfrozen.gameserver.model.actor.stat;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Calculator;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.Stats;

import main.EngineModsManager;

public class CharStat
{
	private final Logger LOGGER = Logger.getLogger(CharStat.class);
	private final L2Character activeChar;
	private long exp = 0;
	private int sp = 0;
	private int level = 1;
	
	/**
	 * Instantiates a new char stat.
	 * @param activeChar the active char
	 */
	public CharStat(final L2Character activeChar)
	{
		this.activeChar = activeChar;
	}
	
	/**
	 * Calculate the new value of the state with modifiers that will be applied on the targeted L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...) : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>. Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in the
	 * value property of an Env class instance.<BR>
	 * <BR>
	 * @param  stat   The stat to calculate the new value with modifiers
	 * @param  init   The initial value of the stat before applying modifiers
	 * @param  target The L2Charcater whose properties will be used in the calculation (ex : CON, INT...)
	 * @param  skill  The L2Skill whose properties will be used in the calculation (ex : Level...)
	 * @return        the double
	 */
	public final double calcStat(final Stats stat, final double init, final L2Character target, final L2Skill skill)
	{
		if (activeChar == null)
		{
			return init;
		}
		
		final int id = stat.ordinal();
		
		Calculator c = activeChar.getCalculators()[id];
		
		// If no Func object found, no modifier is applied
		if (c == null || c.size() == 0)
		{
			return init;
		}
		
		// Create and init an Env object to pass parameters to the Calculator
		final Env env = new Env();
		env.player = activeChar;
		env.target = target;
		env.skill = skill;
		env.value = init;
		env.baseValue = init;
		
		// Launch the calculation
		c.calc(env);
		// avoid some troubles with negative stats (some stats should never be
		// negative)
		if (env.value <= 0)
		{
			switch (stat)
			{
				case MAX_HP:
				case MAX_MP:
				case MAX_CP:
				case MAGIC_DEFENCE:
				case POWER_DEFENCE:
				case POWER_ATTACK:
				case MAGIC_ATTACK:
				case POWER_ATTACK_SPEED:
				case MAGIC_ATTACK_SPEED:
				case SHIELD_DEFENCE:
				case STAT_CON:
				case STAT_DEX:
				case STAT_INT:
				case STAT_MEN:
				case STAT_STR:
				case STAT_WIT:
					env.value = 1;
			}
			
		}
		
		c = null;
		
		return EngineModsManager.onStats(stat, activeChar, env.value);
	}
	
	/**
	 * Return the Accuracy (base+modifier) of the L2Character in function of the Weapon Expertise Penalty.
	 * @return the accuracy
	 */
	public int getAccuracy()
	{
		if (activeChar == null)
		{
			return 0;
		}
		
		return (int) (calcStat(Stats.ACCURACY_COMBAT, 0, null, null) / activeChar.getWeaponExpertisePenalty());
	}
	
	/**
	 * Gets the active char.
	 * @return the active char
	 */
	public L2Character getActiveChar()
	{
		return activeChar;
	}
	
	/**
	 * Return the Attack Speed multiplier (base+modifier) of the L2Character to get proper animations.
	 * @return the attack speed multiplier
	 */
	public final float getAttackSpeedMultiplier()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (float) (1.1 * getPAtkSpd() / activeChar.getTemplate().basePAtkSpd);
	}
	
	/**
	 * Return the CON of the L2Character (base+modifier).
	 * @return the cON
	 */
	public final int getCON()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_CON, activeChar.getTemplate().baseCON, null, null);
	}
	
	/**
	 * Return the Critical Damage rate (base+modifier) of the L2Character.
	 * @param  target the target
	 * @param  init   the init
	 * @return        the critical dmg
	 */
	public final double getCriticalDmg(final L2Character target, final double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
	}
	
	/**
	 * Return the Critical Hit rate (base+modifier) of the L2Character.
	 * @param  target the target
	 * @param  skill  the skill
	 * @return        the critical hit
	 */
	public int getCriticalHit(final L2Character target, final L2Skill skill)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		int criticalHit = (int) (calcStat(Stats.CRITICAL_RATE, activeChar.getTemplate().baseCritRate, target, skill) * 10.0 + 0.5);
		
		criticalHit /= 10;
		
		// Set a cap of Critical Hit at 500
		if (criticalHit > Config.MAX_PCRIT_RATE)
		{
			criticalHit = Config.MAX_PCRIT_RATE;
		}
		
		return criticalHit;
	}
	
	/**
	 * Return the DEX of the L2Character (base+modifier).
	 * @return the dEX
	 */
	public final int getDEX()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_DEX, activeChar.getTemplate().baseDEX, null, null);
	}
	
	/**
	 * Return the Attack Evasion rate (base+modifier) of the L2Character.
	 * @param  target the target
	 * @return        the evasion rate
	 */
	public int getEvasionRate(final L2Character target)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) (calcStat(Stats.EVASION_RATE, 0, target, null) / activeChar.getArmourExpertisePenalty());
	}
	
	/**
	 * Gets the exp.
	 * @return the exp
	 */
	public long getExp()
	{
		return exp;
	}
	
	/**
	 * Sets the exp.
	 * @param value the new exp
	 */
	public void setExp(final long value)
	{
		exp = value;
	}
	
	/**
	 * Return the INT of the L2Character (base+modifier).
	 * @return the iNT
	 */
	public int getINT()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_INT, activeChar.getTemplate().baseINT, null, null);
	}
	
	/**
	 * Gets the level.
	 * @return the level
	 */
	public int getLevel()
	{
		return level;
	}
	
	/**
	 * Sets the level.
	 * @param value the new level
	 */
	public void setLevel(final int value)
	{
		level = value;
	}
	
	/**
	 * Return the Magical Attack range (base+modifier) of the L2Character.
	 * @param  skill the skill
	 * @return       the magical attack range
	 */
	public final int getMagicalAttackRange(final L2Skill skill)
	{
		if (skill != null)
		{
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		}
		
		if (activeChar == null)
		{
			return 1;
		}
		
		return activeChar.getTemplate().baseAtkRange;
	}
	
	/**
	 * Gets the max cp.
	 * @return the max cp
	 */
	public int getMaxCp()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_CP, activeChar.getTemplate().baseCpMax, null, null);
	}
	
	/**
	 * Gets the max hp.
	 * @return the max hp
	 */
	public int getMaxHp()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_HP, activeChar.getTemplate().baseHpMax, null, null);
	}
	
	/**
	 * Gets the max mp.
	 * @return the max mp
	 */
	public int getMaxMp()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_MP, activeChar.getTemplate().baseMpMax, null, null);
	}
	
	/**
	 * Return the MAtk (base+modifier) of the L2Character for a skill used in function of abnormal effects in progress.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Calculate Magic damage</li> <BR>
	 * <BR>
	 * @param  target The L2Character targeted by the skill
	 * @param  skill  The L2Skill used against the target
	 * @return        the m atk
	 */
	public int getMAtk(final L2Character target, final L2Skill skill)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && activeChar.isChampion())
		{
			bonusAtk = Config.L2JMOD_CHAMPION_ATK;
		}
		
		double attack = activeChar.getTemplate().baseMAtk * bonusAtk;
		
		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		Stats stat = skill == null ? null : skill.getStat();
		
		if (stat != null)
		{
			switch (stat)
			{
				case AGGRESSION:
					attack += activeChar.getTemplate().baseAggression;
					break;
				case BLEED:
					attack += activeChar.getTemplate().baseBleed;
					break;
				case POISON:
					attack += activeChar.getTemplate().basePoison;
					break;
				case STUN:
					attack += activeChar.getTemplate().baseStun;
					break;
				case ROOT:
					attack += activeChar.getTemplate().baseRoot;
					break;
				case MOVEMENT:
					attack += activeChar.getTemplate().baseMovement;
					break;
				case CONFUSION:
					attack += activeChar.getTemplate().baseConfusion;
					break;
				case SLEEP:
					attack += activeChar.getTemplate().baseSleep;
					break;
				case FIRE:
					attack += activeChar.getTemplate().baseFire;
					break;
				case WIND:
					attack += activeChar.getTemplate().baseWind;
					break;
				case WATER:
					attack += activeChar.getTemplate().baseWater;
					break;
				case EARTH:
					attack += activeChar.getTemplate().baseEarth;
					break;
				case HOLY:
					attack += activeChar.getTemplate().baseHoly;
					break;
				case DARK:
					attack += activeChar.getTemplate().baseDark;
					break;
			}
		}
		
		// Add the power of the skill to the attack effect
		if (skill != null)
		{
			attack += skill.getPower();
		}
		
		stat = null;
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	/**
	 * Return the MAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 * @return the m atk spd
	 */
	public int getMAtkSpd()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		float bonusSpdAtk = 1;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && activeChar.isChampion())
		{
			bonusSpdAtk = Config.L2JMOD_CHAMPION_SPD_ATK;
		}
		
		double val = calcStat(Stats.MAGIC_ATTACK_SPEED, activeChar.getTemplate().baseMAtkSpd * bonusSpdAtk, null, null);
		
		val /= activeChar.getArmourExpertisePenalty();
		
		if (val > Config.MAX_MATK_SPEED && activeChar instanceof L2PcInstance)
		{
			val = Config.MAX_MATK_SPEED;
		}
		
		return (int) val;
	}
	
	/**
	 * Return the Magic Critical Hit rate (base+modifier) of the L2Character.
	 * @param  target the target
	 * @param  skill  the skill
	 * @return        the m critical hit
	 */
	public final int getMCriticalHit(final L2Character target, final L2Skill skill)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		double mrate = calcStat(Stats.MCRITICAL_RATE, activeChar.getTemplate().baseMCritRate * Config.MCRIT_RATE_MUL, target, skill);
		
		if (mrate > Config.MAX_MCRIT_RATE)
		{
			mrate = Config.MAX_MCRIT_RATE;
		}
		
		return (int) mrate;
	}
	
	/**
	 * Return the MDef (base+modifier) of the L2Character against a skill in function of abnormal effects in progress.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Calculate Magic damage</li> <BR>
	 * @param  target The L2Character targeted by the skill
	 * @param  skill  The L2Skill used against the target
	 * @return        the m def
	 */
	public int getMDef(final L2Character target, final L2Skill skill)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		// Get the base MDef of the L2Character
		double defence = activeChar.getTemplate().baseMDef;
		
		// Calculate modifier for Raid Bosses
		if (activeChar.isRaid())
		{
			defence *= Config.RAID_M_DEFENCE_MULTIPLIER;
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}
	
	/**
	 * Return the MEN of the L2Character (base+modifier).
	 * @return the mEN
	 */
	public final int getMEN()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_MEN, activeChar.getTemplate().baseMEN, null, null);
	}
	
	/**
	 * Gets the movement speed multiplier.
	 * @return the movement speed multiplier
	 */
	public final float getMovementSpeedMultiplier()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return getRunSpeed() * 1f / activeChar.getTemplate().baseRunSpd;
	}
	
	/**
	 * Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the L2Character in function of the movement type.
	 * @return the move speed
	 */
	public final float getMoveSpeed()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		if (activeChar.isRunning())
		{
			return getRunSpeed();
		}
		
		return getWalkSpeed();
	}
	
	/**
	 * Return the MReuse rate (base+modifier) of the L2Character.
	 * @param  skill the skill
	 * @return       the m reuse rate
	 */
	public final double getMReuseRate(final L2Skill skill)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return calcStat(Stats.MAGIC_REUSE_RATE, activeChar.getTemplate().baseMReuseRate, null, skill);
	}
	
	/**
	 * Return the PReuse rate (base+modifier) of the L2Character.
	 * @param  skill the skill
	 * @return       the p reuse rate
	 */
	public final double getPReuseRate(final L2Skill skill)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return calcStat(Stats.P_REUSE, activeChar.getTemplate().baseMReuseRate, null, skill);
	}
	
	/**
	 * Return the PAtk (base+modifier) of the L2Character.
	 * @param  target the target
	 * @return        the p atk
	 */
	public int getPAtk(final L2Character target)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && activeChar.isChampion())
		{
			bonusAtk = Config.L2JMOD_CHAMPION_ATK;
		}
		
		return (int) calcStat(Stats.POWER_ATTACK, activeChar.getTemplate().basePAtk * bonusAtk, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against animals.
	 * @param  target the target
	 * @return        the p atk animals
	 */
	public final double getPAtkAnimals(final L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against dragons.
	 * @param  target the target
	 * @return        the p atk dragons
	 */
	public final double getPAtkDragons(final L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against angels.
	 * @param  target the target
	 * @return        the p atk angels
	 */
	public final double getPAtkAngels(final L2Character target)
	{
		return calcStat(Stats.PATK_ANGELS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against insects.
	 * @param  target the target
	 * @return        the p atk insects
	 */
	public final double getPAtkInsects(final L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against monsters.
	 * @param  target the target
	 * @return        the p atk monsters
	 */
	public final double getPAtkMonsters(final L2Character target)
	{
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against plants.
	 * @param  target the target
	 * @return        the p atk plants
	 */
	public final double getPAtkPlants(final L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 * @return the p atk spd
	 */
	public int getPAtkSpd()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && activeChar.isChampion())
		{
			bonusAtk = Config.L2JMOD_CHAMPION_SPD_ATK;
		}
		
		double val = calcStat(Stats.POWER_ATTACK_SPEED, activeChar.getTemplate().basePAtkSpd * bonusAtk, null, null);
		
		val /= activeChar.getArmourExpertisePenalty();
		
		if (val > Config.MAX_PATK_SPEED && activeChar instanceof L2PcInstance)
		{
			val = Config.MAX_PATK_SPEED;
		}
		
		return (int) val;
	}
	
	/**
	 * Return the PAtk Modifier against undead.
	 * @param  target the target
	 * @return        the p atk undead
	 */
	public final double getPAtkUndead(final L2Character target)
	{
		return calcStat(Stats.PATK_UNDEAD, 1, target, null);
	}
	
	/**
	 * Gets the p def undead.
	 * @param  target the target
	 * @return        the p def undead
	 */
	public final double getPDefUndead(final L2Character target)
	{
		return calcStat(Stats.PDEF_UNDEAD, 1, target, null);
	}
	
	/**
	 * Gets the p def plants.
	 * @param  target the target
	 * @return        the p def plants
	 */
	public final double getPDefPlants(final L2Character target)
	{
		return calcStat(Stats.PDEF_PLANTS, 1, target, null);
	}
	
	/**
	 * Gets the p def insects.
	 * @param  target the target
	 * @return        the p def insects
	 */
	public final double getPDefInsects(final L2Character target)
	{
		return calcStat(Stats.PDEF_INSECTS, 1, target, null);
	}
	
	/**
	 * Gets the p def animals.
	 * @param  target the target
	 * @return        the p def animals
	 */
	public final double getPDefAnimals(final L2Character target)
	{
		return calcStat(Stats.PDEF_ANIMALS, 1, target, null);
	}
	
	/**
	 * Gets the p def monsters.
	 * @param  target the target
	 * @return        the p def monsters
	 */
	public final double getPDefMonsters(final L2Character target)
	{
		return calcStat(Stats.PDEF_MONSTERS, 1, target, null);
	}
	
	/**
	 * Gets the p def dragons.
	 * @param  target the target
	 * @return        the p def dragons
	 */
	public final double getPDefDragons(final L2Character target)
	{
		return calcStat(Stats.PDEF_DRAGONS, 1, target, null);
	}
	
	/**
	 * Gets the p def angels.
	 * @param  target the target
	 * @return        the p def angels
	 */
	public final double getPDefAngels(final L2Character target)
	{
		return calcStat(Stats.PDEF_ANGELS, 1, target, null);
	}
	
	/**
	 * Return the PDef (base+modifier) of the L2Character.
	 * @param  target the target
	 * @return        the p def
	 */
	public int getPDef(final L2Character target)
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		// Get the base PDef of the L2Character
		double defence = activeChar.getTemplate().basePDef;
		
		// Calculate modifier for Raid Bosses
		if (activeChar.isRaid())
		{
			defence *= Config.RAID_P_DEFENCE_MULTIPLIER;
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.POWER_DEFENCE, defence, target, null);
	}
	
	/**
	 * Return the Physical Attack range (base+modifier) of the L2Character.
	 * @return the physical attack range
	 */
	public final int getPhysicalAttackRange()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		final int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, activeChar.getTemplate().baseAtkRange, null, null);
		
		if (Config.DEBUG && activeChar instanceof L2PcInstance)
		{
			LOGGER.debug("	Player 	- " + activeChar.getName() + " - PhysicalAttackRange is - " + range + " -");
		}
		
		return range;
	}
	
	/**
	 * Return the Skill/Spell reuse modifier.
	 * @param  target the target
	 * @return        the reuse modifier
	 */
	public final double getReuseModifier(final L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	/**
	 * Return the RunSpeed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 * @return the run speed
	 */
	public int getRunSpeed()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		// err we should be adding TO the persons run speed
		// not making it a constant
		int val = (int) calcStat(Stats.RUN_SPEED, activeChar.getTemplate().baseRunSpd, null, null) + Config.RUN_SPD_BOOST;
		
		if (activeChar.isInsideZone(L2Character.ZONE_WATER))
		{
			val /= 2;
		}
		
		if (activeChar.isFlying())
		{
			val += Config.WYVERN_SPEED;
			return val;
		}
		
		if (activeChar.isRiding())
		{
			val += Config.STRIDER_SPEED;
			return val;
		}
		
		val /= activeChar.getArmourExpertisePenalty();
		
		if (val > Config.MAX_RUN_SPEED && !activeChar.charIsGM())
		{
			val = Config.MAX_RUN_SPEED;
		}
		
		return val;
	}
	
	/**
	 * Return the ShieldDef rate (base+modifier) of the L2Character.
	 * @return the shld def
	 */
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}
	
	/**
	 * Gets the sp.
	 * @return the sp
	 */
	public int getSp()
	{
		return sp;
	}
	
	/**
	 * Sets the sp.
	 * @param value the new sp
	 */
	public void setSp(final int value)
	{
		sp = value;
	}
	
	/**
	 * Return the STR of the L2Character (base+modifier).
	 * @return the sTR
	 */
	public final int getSTR()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_STR, activeChar.getTemplate().baseSTR, null, null);
	}
	
	/**
	 * Return the WalkSpeed (base+modifier) of the L2Character.
	 * @return the walk speed
	 */
	public final int getWalkSpeed()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		if (activeChar instanceof L2PcInstance)
		{
			return getRunSpeed() * 70 / 100;
		}
		return (int) calcStat(Stats.WALK_SPEED, activeChar.getTemplate().baseWalkSpd, null, null);
	}
	
	/**
	 * Return the WIT of the L2Character (base+modifier).
	 * @return the wIT
	 */
	public final int getWIT()
	{
		if (activeChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_WIT, activeChar.getTemplate().baseWIT, null, null);
	}
	
	/**
	 * Return the mpConsume.
	 * @param  skill the skill
	 * @return       the mp consume
	 */
	public final int getMpConsume(final L2Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		
		int mpconsume = skill.getMpConsume();
		
		if (skill.isDance() && activeChar != null && activeChar.getDanceCount() > 0)
		{
			mpconsume += activeChar.getDanceCount() * skill.getNextDanceMpCost();
		}
		
		return (int) calcStat(Stats.MP_CONSUME, mpconsume, null, skill);
	}
	
	/**
	 * Return the mpInitialConsume.
	 * @param  skill the skill
	 * @return       the mp initial consume
	 */
	public final int getMpInitialConsume(final L2Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
	}
	
	/**
	 * Return the PDef Modifier against giants.
	 * @param  target the target
	 * @return        the p def giants
	 */
	public final double getPDefGiants(final L2Character target)
	{
		return calcStat(Stats.PDEF_GIANTS, 1, target, null);
	}
	
	/**
	 * Return the PDef Modifier against giants.
	 * @param  target the target
	 * @return        the p def magic creatures
	 */
	public final double getPDefMagicCreatures(final L2Character target)
	{
		return calcStat(Stats.PDEF_MCREATURES, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against giants.
	 * @param  target the target
	 * @return        the p atk giants
	 */
	public final double getPAtkGiants(final L2Character target)
	{
		return calcStat(Stats.PATK_GIANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against magic creatures.
	 * @param  target the target
	 * @return        the p atk magic creatures
	 */
	public final double getPAtkMagicCreatures(final L2Character target)
	{
		return calcStat(Stats.PATK_MCREATURES, 1, target, null);
	}
}
