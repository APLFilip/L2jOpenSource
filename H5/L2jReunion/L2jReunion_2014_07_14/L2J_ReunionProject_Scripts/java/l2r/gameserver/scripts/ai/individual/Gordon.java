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
package l2r.gameserver.scripts.ai.individual;

import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

/**
 * Gordon AI
 * @author TOFIZ, malyelfik
 */
public class Gordon extends AbstractNpcAI
{
	private static final int GORDON = 29095;
	
	private Gordon(String name, String descr)
	{
		super(name, descr);
		addSpawnId(GORDON);
		addSeeCreatureId(GORDON);
		
		final L2Npc gordon = SpawnTable.getInstance().getFirstSpawn(GORDON).getLastSpawn();
		if (gordon != null)
		{
			onSpawn(gordon);
		}
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayer() && ((L2PcInstance) creature).isCursedWeaponEquipped())
		{
			attackPlayer((L2Attackable) npc, (L2PcInstance) creature);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		((L2Attackable) npc).setCanReturnToSpawnPoint(false);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Gordon(Gordon.class.getSimpleName(), "ai");
	}
}