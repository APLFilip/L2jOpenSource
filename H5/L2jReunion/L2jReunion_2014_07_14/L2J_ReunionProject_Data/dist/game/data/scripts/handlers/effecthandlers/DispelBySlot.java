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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * @author Gnacik
 */
public class DispelBySlot extends L2Effect
{
	public DispelBySlot(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NEGATE;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if ((target == null) || target.isDead())
		{
			return false;
		}
		
		String stackType = getAbnormalType();
		float stackOrder = getAbnormalLvl();
		int skillCast = getSkill().getId();
		
		// If order is 0 don't remove effect
		if (stackOrder == 0)
		{
			return true;
		}
		
		final L2Effect[] effects = target.getAllEffects();
		
		for (L2Effect e : effects)
		{
			if (!e.getSkill().canBeDispeled())
			{
				continue;
			}
			
			// Fist check for stacktype
			if (stackType.equalsIgnoreCase(e.getAbnormalType()) && (e.getSkill().getId() != skillCast))
			{
				if (stackOrder == -1)
				{
					e.exit();
				}
				else if (stackOrder >= e.getAbnormalLvl())
				{
					e.exit();
				}
			}
		}
		return true;
	}
}
