package quests;

import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _369_CollectorOfJewels extends QuestScript
{
	// NPCs
	private static int NELL = 30376;
	// Mobs
	private static int Roxide = 20747;
	private static int Rowin_Undine = 20619;
	private static int Lakin_Undine = 20616;
	private static int Salamander_Rowin = 20612;
	private static int Lakin_Salamander = 20609;
	private static int Death_Fire = 20749;
	// Quest Items
	private static int FLARE_SHARD = 5882;
	private static int FREEZING_SHARD = 5883;

	private final Map<Integer, int[]> DROPLIST = new HashMap<Integer, int[]>();

	public _369_CollectorOfJewels()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(NELL);
		addKillId(Roxide);
		addKillId(Rowin_Undine);
		addKillId(Lakin_Undine);
		addKillId(Salamander_Rowin);
		addKillId(Lakin_Salamander);
		addKillId(Death_Fire);
		addQuestItem(FLARE_SHARD);
		addQuestItem(FREEZING_SHARD);

		DROPLIST.put(Roxide, new int[]{
				FREEZING_SHARD,
				85
		});
		DROPLIST.put(Rowin_Undine, new int[]{
				FREEZING_SHARD,
				73
		});
		DROPLIST.put(Lakin_Undine, new int[]{
				FREEZING_SHARD,
				60
		});
		DROPLIST.put(Salamander_Rowin, new int[]{
				FLARE_SHARD,
				77
		});
		DROPLIST.put(Lakin_Salamander, new int[]{
				FLARE_SHARD,
				77
		});
		DROPLIST.put(Death_Fire, new int[]{
				FLARE_SHARD,
				85
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("30376-03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("30376-08.htm"))
		{
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != NELL)
			return htmltext;

		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 25)
				return "30376-02.htm";
			return "30376-01.htm";
		}
		else if(cond == 1)
			htmltext = "30376-04.htm";
		else if(cond == 3)
			htmltext = "30376-09.htm";
		else if(cond == 2 || cond == 4)
		{
			int max_count = cond == 2 ? 50 : 200;
			long FLARE_SHARD_COUNT = st.getQuestItemsCount(FLARE_SHARD);
			long FREEZING_SHARD_COUNT = st.getQuestItemsCount(FREEZING_SHARD);
			if(FLARE_SHARD_COUNT < max_count || FREEZING_SHARD_COUNT < max_count)
			{
				st.setCond(Integer.valueOf(cond - 1));
				return onTalk(npc, st);
			}

			st.takeItems(FLARE_SHARD, -1);
			st.takeItems(FREEZING_SHARD, -1);
			if(cond == 2)
			{
				htmltext = "30376-05.htm";
				st.giveItems(ADENA_ID, 38810, true, true);
				st.setCond(3);
			}
			else
			{
				htmltext = "30376-10.htm";
				st.giveItems(ADENA_ID, 84415, true, true);
				st.finishQuest();
			}
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		int cond = qs.getCond();
		if(cond != 1 && cond != 3)
			return null;

		int[] drop = DROPLIST.get(npc.getNpcId());
		if(drop == null)
			return null;

		int max_count = cond == 1 ? 50 : 200;
		if(qs.getQuestItemsCount(drop[0]) < max_count && qs.rollAndGive(drop[0], 1, 1, max_count, drop[1]) && qs.getQuestItemsCount(FLARE_SHARD) >= max_count && qs.getQuestItemsCount(FREEZING_SHARD) >= max_count)
			qs.setCond(cond == 1 ? 2 : 4);

		return null;
	}
}