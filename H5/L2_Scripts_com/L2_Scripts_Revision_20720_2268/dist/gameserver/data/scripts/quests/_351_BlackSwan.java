package quests;

import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

// Рейты учтены
public class _351_BlackSwan extends QuestScript
{
	//NPC
	private static final int Gosta = 30916;
	private static final int Heine = 30969;
	private static final int Ferris = 30847;
	private static final int ORDER_OF_GOSTA = 4296;
	private static final int LIZARD_FANG = 4297;
	private static final int BARREL_OF_LEAGUE = 4298;
	private static final int BILL_OF_IASON_HEINE = 4310;
	private static final int CHANCE = 100;
	private static final int CHANCE2 = 5;

	public _351_BlackSwan()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Gosta);
		addTalkId(Heine);
		addTalkId(Ferris);
		addKillId(20784, 20785, 21639, 21640, 21642, 21643);
		addQuestItem(ORDER_OF_GOSTA, LIZARD_FANG, BARREL_OF_LEAGUE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		long amount = st.getQuestItemsCount(LIZARD_FANG);
		long amount2 = st.getQuestItemsCount(BARREL_OF_LEAGUE);
		if(event.equalsIgnoreCase("30916-03.htm"))
		{
			st.setCond(1);
			st.giveItems(ORDER_OF_GOSTA, 1, false, false);
		}
		else if(event.equalsIgnoreCase("30969-02a.htm") && amount > 0)
		{
			htmltext = "30969-02.htm";
			st.giveItems(ADENA_ID, amount * 30, true, true);
			st.takeItems(LIZARD_FANG, -1);
		}
		else if(event.equalsIgnoreCase("30969-03a.htm") && amount2 > 0)
		{
			htmltext = "30969-03.htm";
			st.setCond(2);
			st.giveItems(ADENA_ID, amount2 * 500, true, true);
			st.giveItems(BILL_OF_IASON_HEINE, amount2, false, false);
			st.takeItems(BARREL_OF_LEAGUE, -1);
		}
		else if(event.equalsIgnoreCase("30969-01.htm") && st.getCond() == 2)
			htmltext = "30969-04.htm";
		else if(event.equalsIgnoreCase("5"))
		{
			st.finishQuest();
			htmltext = "";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == Gosta)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 32)
					htmltext = "30916-01.htm";
				else
					htmltext = "30916-00.htm";
			}
			else if(cond >= 1)
				htmltext = "30916-04.htm";
		if(npcId == Heine)
		{
			if(cond == 1)
				htmltext = "30969-01.htm";
			if(cond == 2)
				htmltext = "30969-04.htm";
		}
		if(npcId == Ferris)
			htmltext = "30847.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		double mod = Experience.penaltyModifier(st.calculateLevelDiffForDrop(npc.getLevel(), st.getPlayer().getLevel()), 9);
		st.rollAndGive(LIZARD_FANG, 1, CHANCE * mod);
		st.rollAndGive(BARREL_OF_LEAGUE, 1, CHANCE2 * mod);
		return null;
	}
}