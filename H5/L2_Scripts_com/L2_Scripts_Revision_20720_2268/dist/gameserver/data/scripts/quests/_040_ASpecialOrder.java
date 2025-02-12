package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _040_ASpecialOrder extends QuestScript
{

	// NPC
	static final int Helvetia = 30081;
	static final int OFulle = 31572;
	static final int Gesto = 30511;

	// Items
	static final int FatOrangeFish = 6452;
	static final int NimbleOrangeFish = 6450;
	static final int OrangeUglyFish = 6451;
	static final int GoldenCobol = 5079;
	static final int ThornCobol = 5082;
	static final int GreatCobol = 5084;

	// Quest items
	static final int FishChest = 12764;
	static final int SeedJar = 12765;
	static final int WondrousCubic = 10632;

	public _040_ASpecialOrder()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Helvetia);

		addQuestItem(FishChest);
		addQuestItem(SeedJar);

		addTalkId(OFulle);
		addTalkId(Gesto);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("take"))
		{
			int rand = Rnd.get(1, 2);
			if(rand == 1)
			{
				st.setCond(2);
				htmltext = "Helvetia-gave-ofulle.htm";
			}
			else
			{
				st.setCond(5);
				htmltext = "Helvetia-gave-gesto.htm";
			}
		}
		else if(event.equals("6"))
		{
			st.setCond(6);
			htmltext = "Gesto-3.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Helvetia)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 40)
					htmltext = "Helvetia-1.htm";
				else
					htmltext = "Helvetia-level.htm";
			}
			else if(cond == 2 || cond == 3 || cond == 5 || cond == 6)
				htmltext = "Helvetia-whereismyfish.htm";
			else if(cond == 4)
			{
				st.takeAllItems(FishChest);
				st.giveItems(WondrousCubic, 1, false, false);
				st.finishQuest();
				htmltext = "Helvetia-finish.htm";
			}
			else if(cond == 7)
			{
				st.takeAllItems(SeedJar);
				st.giveItems(WondrousCubic, 1, false, false);
				st.finishQuest();
				htmltext = "Helvetia-finish.htm";
			}
		}
		else if(npcId == OFulle)
		{
			if(cond == 2)
			{
				htmltext = "OFulle-1.htm";
				st.setCond(3);
			}
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(FatOrangeFish) >= 10 && st.getQuestItemsCount(NimbleOrangeFish) >= 10 && st.getQuestItemsCount(OrangeUglyFish) >= 10)
				{
					st.takeItems(FatOrangeFish, 10);
					st.takeItems(NimbleOrangeFish, 10);
					st.takeItems(OrangeUglyFish, 10);
					st.giveItems(FishChest, 1, false, false);
					st.setCond(4);
					htmltext = "OFulle-2.htm";
				}
				else
					htmltext = "OFulle-1.htm";
			}
			else if(cond == 5 || cond == 6)
				htmltext = "OFulle-3.htm";
		}
		else if(npcId == Gesto)
			if(cond == 5)
				htmltext = "Gesto-1.htm";
			else if(cond == 6)
			{
				if(st.getQuestItemsCount(GoldenCobol) >= 40 && st.getQuestItemsCount(ThornCobol) >= 40 && st.getQuestItemsCount(GreatCobol) >= 40)
				{
					st.takeItems(GoldenCobol, 40);
					st.takeItems(ThornCobol, 40);
					st.takeItems(GreatCobol, 40);
					st.giveItems(SeedJar, 1, false, false);
					st.setCond(7);
					htmltext = "Gesto-4.htm";
				}
				else
					htmltext = "Gesto-5.htm";
			}
			else if(cond == 7)
				htmltext = "Gesto-6.htm";
		return htmltext;
	}
}