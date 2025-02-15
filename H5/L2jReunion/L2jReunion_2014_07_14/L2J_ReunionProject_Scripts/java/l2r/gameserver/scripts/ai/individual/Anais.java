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

import java.util.Map;

import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.quest.QuestTimer;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

/**
 * Anais AI.
 * @author nonom
 */
public final class Anais extends AbstractNpcAI
{
	// NPCs
	private static final int ANAIS = 25701;
	private static final int DIVINE_BURNER = 18915;
	private static final int GRAIL_WARD = 18929;
	// Skill
	private static SkillHolder DIVINE_NOVA = new SkillHolder(6326, 1);
	// Instances
	private final L2Npc[] _divineBurners = new L2Npc[4];
	private L2Npc _anais = null;
	private L2PcInstance _nextTarget = null;
	private L2Npc _current = null;
	private int _pot = 0;
	
	private Anais(String name, String descr)
	{
		super(name, descr);
		addAttackId(ANAIS);
		addKillId(GRAIL_WARD);
		
		int i = 0;
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawns(DIVINE_BURNER))
		{
			_divineBurners[i++] = spawn.getLastSpawn();
		}
		_anais = SpawnTable.getInstance().getFirstSpawn(ANAIS).getLastSpawn();
	}
	
	private void burnerOnAttack(int pot)
	{
		L2Npc npc = _divineBurners[pot];
		npc.setDisplayEffect(1);
		npc.setIsRunning(false);
		if (pot < 4)
		{
			_current = npc;
			QuestTimer checkAround = getQuestTimer("CHECK", _anais, null);
			if (checkAround == null) // || !checkAround.getIsActive()
			{
				startQuestTimer("CHECK", 3000, _anais, null);
			}
		}
		else
		{
			cancelQuestTimer("CHECK", _anais, null);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "CHECK":
				if (!npc.isAttackingNow())
				{
					cancelQuestTimer("CHECK", npc, null);
				}
				if ((_current != null) || (_pot < 4))
				{
					Map<Integer, L2PcInstance> players = _anais.getKnownList().getKnownPlayers();
					L2PcInstance target = players.get(getRandom(players.size() - 1));
					_nextTarget = target;
					if (_nextTarget == null)
					{
						_nextTarget = (L2PcInstance) _anais.getTarget();
					}
					L2Npc b = _divineBurners[_pot];
					_pot = _pot + 1;
					b.setDisplayEffect(1);
					b.setIsRunning(false);
					L2Npc ward = addSpawn(GRAIL_WARD, new Location(b.getX(), b.getY(), b.getZ()), true, 0);
					((L2Attackable) ward).addDamageHate(_nextTarget, 0, 999);
					ward.setIsRunning(true);
					ward.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _nextTarget, null);
					startQuestTimer("GUARD_ATTACK", 1000, ward, _nextTarget, true);
					startQuestTimer("SUICIDE", 20000, ward, null);
					ward.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _nextTarget);
				}
				break;
			case "GUARD_ATTACK":
				if (_nextTarget != null)
				{
					final double distance = Math.sqrt(npc.getPlanDistanceSq(_nextTarget.getX(), _nextTarget.getY()));
					if (distance < 100)
					{
						npc.doCast(DIVINE_NOVA.getSkill());
					}
					else if (distance > 2000)
					{
						npc.doDie(null);
						cancelQuestTimer("GUARD_ATTACK", npc, player);
					}
				}
				break;
			case "SUICIDE":
				npc.doCast(DIVINE_NOVA.getSkill());
				cancelQuestTimer("GUARD_ATTACK", npc, _nextTarget);
				if (_current != null)
				{
					_current.setDisplayEffect(2);
					_current.setIsRunning(false);
					_current = null;
				}
				npc.doDie(null);
				break;
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (_pot == 0)
		{
			burnerOnAttack(0);
		}
		else if ((npc.getCurrentHp() <= (npc.getMaxRecoverableHp() * 0.75)) && (_pot == 1))
		{
			burnerOnAttack(1);
		}
		else if ((npc.getCurrentHp() <= (npc.getMaxRecoverableHp() * 0.5)) && (_pot == 2))
		{
			burnerOnAttack(2);
		}
		else if ((npc.getCurrentHp() <= (npc.getMaxRecoverableHp() * 0.25)) && (_pot == 3))
		{
			burnerOnAttack(3);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		npc.doCast(DIVINE_NOVA.getSkill());
		cancelQuestTimer("GUARD_ATTACK", npc, _nextTarget);
		if (_current != null)
		{
			_current.setDisplayEffect(2);
			_current.setIsRunning(false);
			_current = null;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Anais(Anais.class.getSimpleName(), "ai");
	}
}
