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
package l2r.gameserver.scripts.instances;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.scripts.quests.Q10284_AcquisitionOfDivineSword;

/**
 * Mithril Mine instance zone.
 * @author Adry_85
 */
public final class MithrilMine extends Quest
{
	protected class MMWorld extends InstanceWorld
	{
		long storeTime = 0;
	}
	
	private static final int TEMPLATE_ID = 138;
	// NPCs
	private static final int KEGOR = 18846;
	private static final int MITHRIL_MILLIPEDE = 22766;
	private static final int KRUN = 32653;
	private static final int TARUN = 32654;
	// Item
	private static final int COLD_RESISTANCE_POTION = 15514;
	// Skill
	private static SkillHolder BLESS_OF_SWORD = new SkillHolder(6286, 1);
	// Location
	private static final Location START_LOC = new Location(186852, -173492, -3763, 0, 0);
	private static final Location EXIT_LOC = new Location(178823, -184303, -347, 0, 0);
	private static final Location[] MOB_SPAWNS = new Location[]
	{
		new Location(185216, -184112, -3308, -15396),
		new Location(185456, -184240, -3308, -19668),
		new Location(185712, -184384, -3308, -26696),
		new Location(185920, -184544, -3308, -32544),
		new Location(185664, -184720, -3308, 27892)
	};
	// Misc
	private int _count = 0;
	private L2PcInstance _player = null;
	
	private MithrilMine()
	{
		super(-1, MithrilMine.class.getSimpleName(), "instances");
		addFirstTalkId(KEGOR);
		addKillId(KEGOR, MITHRIL_MILLIPEDE);
		addSpawnId(KEGOR);
		addStartNpc(TARUN, KRUN);
		addTalkId(TARUN, KRUN, KEGOR);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "BUFF":
			{
				if ((player != null) && npc.isInsideRadius(player, 1000, true, false) && npc.isScriptValue(1) && !player.isDead())
				{
					npc.setTarget(player);
					npc.doCast(BLESS_OF_SWORD.getSkill());
				}
				startQuestTimer("BUFF", 30000, npc, player);
				break;
			}
			case "TIMER":
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
				if (tmpworld instanceof MMWorld)
				{
					for (Location loc : MOB_SPAWNS)
					{
						final L2Attackable spawnedMob = (L2Attackable) addSpawn(MITHRIL_MILLIPEDE, loc, false, 0, false, tmpworld.getInstanceId());
						spawnedMob.setScriptValue(1);
						spawnedMob.setIsRunning(true);
						spawnedMob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc);
						spawnedMob.addDamageHate(npc, 0, 999999);
					}
				}
				break;
			}
			case "FINISH":
			{
				if (_count >= 5)
				{
					npc.setScriptValue(2);
					npc.setTarget(player);
					npc.setWalking();
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.I_CAN_FINALLY_TAKE_A_BREATHER_BY_THE_WAY_WHO_ARE_YOU_HMM_I_THINK_I_KNOW_WHO_SENT_YOU));
					InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
					InstanceManager.getInstance().getInstance(world.getInstanceId()).setDuration(3000);
					cancelQuestTimers("FINISH");
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());
		if ((qs != null))
		{
			if (qs.isMemoState(2))
			{
				return npc.isScriptValue(0) ? "18846.html" : "18846-01.html";
			}
			else if (qs.isMemoState(3))
			{
				final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				world.removeAllowed(player.getObjectId());
				player.setInstanceId(0);
				player.teleToLocation(EXIT_LOC, 0);
				qs.giveAdena(296425, true);
				qs.addExpAndSp(921805, 82230);
				qs.exitQuest(false, true);
				return "18846-03.html";
			}
		}
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (npc.getId() == KEGOR)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.HOW_COULD_I_FALL_IN_A_PLACE_LIKE_THIS));
			InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			InstanceManager.getInstance().getInstance(world.getInstanceId()).setDuration(1000);
		}
		else
		{
			if (npc.isScriptValue(1))
			{
				_count++;
			}
			
			if (_count >= 5)
			{
				final QuestState qs = player.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());
				if ((qs != null) && qs.isMemoState(2))
				{
					cancelQuestTimers("BUFF");
					qs.setMemoState(3);
					qs.setCond(6, true);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setRHandId(15280);
		startQuestTimer("FINISH", 3000, npc, _player, true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		switch (npc.getId())
		{
			case TARUN:
			case KRUN:
			{
				final QuestState qs = talker.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());
				if ((qs != null) && qs.isMemoState(2))
				{
					if (!qs.hasQuestItems(COLD_RESISTANCE_POTION))
					{
						qs.giveItems(COLD_RESISTANCE_POTION, 1);
					}
					qs.setCond(4, true);
					_player = talker;
					enterInstance(talker, "MithrilMine.xml", START_LOC);
				}
				break;
			}
			case KEGOR:
			{
				final QuestState qs = talker.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());
				if ((qs != null) && qs.isMemoState(2) && qs.hasQuestItems(COLD_RESISTANCE_POTION) && npc.isScriptValue(0))
				{
					qs.takeItems(COLD_RESISTANCE_POTION, -1);
					qs.setCond(5, true);
					npc.setScriptValue(1);
					startQuestTimer("TIMER", 3000, npc, talker);
					startQuestTimer("BUFF", 3500, npc, talker);
					return "18846-02.html";
				}
				break;
			}
		}
		return super.onTalk(npc, talker);
	}
	
	protected int enterInstance(L2PcInstance player, String template, Location loc)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof MMWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}
			teleportPlayer(player, loc, world.getInstanceId(), false);
			return 0;
		}
		// New instance
		world = new MMWorld();
		world.setInstanceId(InstanceManager.getInstance().createDynamicInstance(template));
		world.setTemplateId(TEMPLATE_ID);
		world.setStatus(0);
		((MMWorld) world).storeTime = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("Mithril Mine started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
		// teleport players
		teleportPlayer(player, loc, world.getInstanceId(), false);
		world.addAllowed(player.getObjectId());
		return world.getInstanceId();
	}
	
	public static void main(String[] args)
	{
		new MithrilMine();
	}
}