package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _239_WontYouJoinUs extends QuestScript
{

	private static final int Athenia = 32643;

	private static final int WasteLandfillMachine = 18805;
	private static final int Suppressor = 22656;
	private static final int Exterminator = 22657;

	private static final int CertificateOfSupport = 14866;
	private static final int DestroyedMachinePiece = 14869;
	private static final int EnchantedGolemFragment = 14870;

	public _239_WontYouJoinUs()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Athenia);
		addKillId(WasteLandfillMachine, Suppressor, Exterminator);
		addQuestItem(DestroyedMachinePiece, EnchantedGolemFragment);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32643-03.htm"))
		{
			st.setCond(1);
		}
		if(event.equalsIgnoreCase("32643-07.htm"))
		{
			st.takeAllItems(DestroyedMachinePiece);
			st.setCond(3);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Athenia)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 82 || !st.getPlayer().isQuestCompleted(237))
					return "32643-00.htm";
				if(st.getQuestItemsCount(CertificateOfSupport) == 0)
					return "32643-12.htm";
				return "32643-01.htm";
			}
			else if(cond == 1)
				return "32643-04.htm";
			else if(cond == 2)
				return "32643-06.htm";
			else if(cond == 3)
				return "32643-08.htm";
			else if(cond == 4)
			{
				st.takeAllItems(CertificateOfSupport);
				st.takeAllItems(EnchantedGolemFragment);
				st.giveItems(ADENA_ID, 283346, true, true);
				st.addExpAndSp(1319736, 103553);
				st.finishQuest();
				return "32643-10.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == Athenia)
			htmltext = "32643-11.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1 && npc.getNpcId() == WasteLandfillMachine)
		{
			st.giveItems(DestroyedMachinePiece, 1, true, true);
			if(st.getQuestItemsCount(DestroyedMachinePiece) >= 10)
				st.setCond(2);
		}
		else if(cond == 3 && (npc.getNpcId() == Suppressor || npc.getNpcId() == Exterminator))
		{
			st.giveItems(EnchantedGolemFragment, 1, true, true);
			if(st.getQuestItemsCount(EnchantedGolemFragment) >= 20)
				st.setCond(4);
		}
		return null;
	}
}