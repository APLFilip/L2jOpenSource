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

import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * Servitor Share effect.<br>
 * Synchronizing effects on player and servitor if one of them gets removed for some reason the same will happen to another.
 * @author UnAfraid
 */
public class ServitorShare extends L2Effect
{
	public ServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean canBeStolen()
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.SERVITOR_SHARE.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public void onExit()
	{
		L2Effect[] effects = null;
		if (getEffected().isPlayer())
		{
			final L2Summon summon = getEffected().getSummon();
			if ((summon != null) && summon.isServitor())
			{
				effects = summon.getAllEffects();
			}
		}
		else if (getEffected().isServitor())
		{
			final L2PcInstance owner = getEffected().getActingPlayer();
			if (owner != null)
			{
				effects = owner.getAllEffects();
			}
		}
		
		if (effects != null)
		{
			for (L2Effect eff : effects)
			{
				if (eff.getSkill().getId() == getSkill().getId())
				{
					eff.exit();
					break;
				}
			}
		}
		super.onExit();
	}
}
