/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.util.Rnd;

/**
 * Confusion effect implementation.
 * @author littlecrow
 */
public class Confusion extends L2Effect
{
	public Confusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startConfused();
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (!getEffected().isPlayer())
		{
			try
			{
				getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
			}
			catch (Exception e)
			{
				_log.warn("Logger: notifyEvent failed (Confusion) Report this to team. ");
			}
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		final List<L2Character> targetList = new ArrayList<>();
		// Getting the possible targets
		for (L2Object obj : getEffected().getKnownList().getKnownObjects().values())
		{
			if (((getEffected().isMonster() && obj.isAttackable()) || (obj instanceof L2Character)) && (obj != getEffected()))
			{
				targetList.add((L2Character) obj);
			}
		}
		// if there is no target, exit function
		if (!targetList.isEmpty())
		{
			// Choosing randomly a new target
			final L2Character target = targetList.get(Rnd.nextInt(targetList.size()));
			// Attacking the target
			getEffected().setTarget(target);
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.CONFUSED.getMask();
	}
}
