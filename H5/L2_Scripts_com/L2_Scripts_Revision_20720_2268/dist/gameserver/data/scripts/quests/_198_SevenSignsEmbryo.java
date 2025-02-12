package quests;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.ExStartScenePlayer;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

public class _198_SevenSignsEmbryo extends QuestScript
{
	// NPCs
	private static int Wood = 32593;
	private static int Franz = 32597;
	private static int Jaina = 32582;
	private static int ShilensEvilThoughtsCapt = 27346;

	// ITEMS
	private static int PieceOfDoubt = 14355;
	private static int DawnsBracelet = 15312;
	private static int AncientAdena = 5575;

	private static final int izId = 113;

	Location setcloc = new Location(-23734, -9184, -5384, 0);

	public _198_SevenSignsEmbryo()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Wood);
		addTalkId(Wood, Franz, Jaina);
		addKillId(ShilensEvilThoughtsCapt);
		addQuestItem(PieceOfDoubt);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("wood_q198_2.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("wood_q198_3.htm"))
		{
			enterInstance(player);
			if(st.get("embryo") != null)
				st.unset("embryo");
		}
		else if(event.equalsIgnoreCase("franz_q198_3.htm"))
		{
			NpcInstance embryo = player.getReflection().addSpawnWithoutRespawn(ShilensEvilThoughtsCapt, setcloc, 0);
			st.set("embryo", 1);
			Functions.npcSay(npc, player.getName() + "! You should kill this monster! I'll try to help!");
			Functions.npcSay(embryo, "This is not yours.");
			embryo.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 500);
		}
		else if(event.equalsIgnoreCase("wood_q198_8.htm"))
			enterInstance(player);
		else if(event.equalsIgnoreCase("franz_q198_5.htm"))
		{
			Functions.npcSay(npc, "We will be with you always...");
			st.takeItems(PieceOfDoubt, -1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("jaina_q198_2.htm"))
			player.getReflection().collapse();
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		String htmltext = NO_QUEST_DIALOG;
		if(npcId == Wood)
		{
			if(cond == 0)
			{
				if(player.getLevel() >= 79 && player.isQuestCompleted(197))
					htmltext = "wood_q198_1.htm";
				else
					htmltext = "wood_q198_0.htm";
			}
			else if(cond == 1 || cond == 2)
				htmltext = "wood_q198_2a.htm";
			else if(cond == 3)
			{
				if(player.getBaseClassId() == player.getActiveClassId())
				{
					st.addExpAndSp(150000000, 15000000);
					st.giveItems(DawnsBracelet, 1, false, false);
					st.giveItems(AncientAdena, 1500000, true, true);
					st.finishQuest();
					htmltext = "wood_q198_4.htm";
				}
				else
					htmltext = "subclass_forbidden.htm";
			}
		}
		else if(npcId == Franz)
		{
			if(cond == 1)
			{
				if(st.get("embryo") == null || Integer.parseInt(st.get("embryo")) != 1)
					htmltext = "franz_q198_1.htm";
				else
					htmltext = "franz_q198_3a.htm";
			}
			else if(cond == 2)
				htmltext = "franz_q198_4.htm";
			else
				htmltext = "franz_q198_6.htm";
		}
		else if(npcId == Jaina)
			htmltext = "jaina_q198_1.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();
		if(player == null)
			return null;

		if(npcId == ShilensEvilThoughtsCapt && cond == 1)
		{
			Functions.npcSay(npc, player.getName() + ", I'm leaving now. But we shall meet again!");
			st.set("embryo", 2);
			st.setCond(2);
			st.giveItems(PieceOfDoubt, 1, false, false);
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_EMBRYO);
		}
		return null;
	}

	private void enterInstance(Player player)
	{
		Reflection r = player.getActiveReflection();
		if(r != null)
		{
			if(player.canReenterInstance(izId))
				player.teleToLocation(r.getTeleportLoc(), r);
		}
		else if(player.canEnterInstance(izId))
		{
			ReflectionUtils.enterReflection(player, izId);
		}
	}
}