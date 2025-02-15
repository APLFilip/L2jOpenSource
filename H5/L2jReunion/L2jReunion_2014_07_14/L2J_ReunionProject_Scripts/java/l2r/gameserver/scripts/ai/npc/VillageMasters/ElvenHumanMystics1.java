package l2r.gameserver.scripts.ai.npc.VillageMasters;

import l2r.gameserver.enums.PcRace;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Util;

public class ElvenHumanMystics1 extends Quest
{
	private static final String qn = "ElvenHumanMystics1";
	
	// NPCs
	private static int[] NPCS =
	{
		30070,
		30289,
		30037,
		32153,
		32147
	};
	
	// Items
	private static int MARK_OF_FAITH = 1201;
	private static int ETERNITY_DIAMOND = 1230;
	private static int LEAF_OF_ORACLE = 1235;
	private static int BEAD_OF_SEASON = 1292;
	
	// Rewards
	private static int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	
	private static int[][] CLASSES =
	{
		{
			26,
			25,
			15,
			16,
			17,
			18,
			ETERNITY_DIAMOND
		},
		{
			29,
			25,
			19,
			20,
			21,
			22,
			LEAF_OF_ORACLE
		},
		{
			11,
			10,
			23,
			24,
			25,
			26,
			BEAD_OF_SEASON
		},
		{
			15,
			10,
			27,
			28,
			29,
			30,
			MARK_OF_FAITH
		}
	};
	
	public ElvenHumanMystics1(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(NPCS);
		addTalkId(NPCS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		if (Util.isDigit(event))
		{
			int i = Integer.valueOf(event);
			final ClassId cid = player.getClassId();
			if (((cid.getRace() == PcRace.Elf) || (cid.getRace() == PcRace.Human)) && (cid.getId() == CLASSES[i][1]))
			{
				int suffix;
				final boolean item = st.hasQuestItems(CLASSES[i][6]);
				if (player.getLevel() < 20)
				{
					suffix = (!item) ? CLASSES[i][2] : CLASSES[i][3];
				}
				else
				{
					if (!item)
					{
						suffix = CLASSES[i][4];
					}
					else
					{
						suffix = CLASSES[i][5];
						st.giveItems(SHADOW_WEAPON_COUPON_DGRADE, 15);
						st.takeItems(CLASSES[i][6], -1);
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						st.playSound("ItemSound.quest_fanfare_2");
						player.broadcastUserInfo();
						st.exitQuest(false);
					}
				}
				event = npc.getId() + "-" + suffix + ".htm";
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (player.isSubClassActive())
		{
			return htmltext;
		}
		
		final ClassId cid = player.getClassId();
		
		if ((cid.getRace() == PcRace.Elf) || (cid.getRace() == PcRace.Human))
		{
			switch (cid)
			{
				case elvenMage:
				{
					htmltext = npc.getId() + "-01.htm";
					break;
				}
				case mage:
				{
					htmltext = npc.getId() + "-08.htm";
					break;
				}
				default:
				{
					if (cid.level() == 1)
					{
						return npc.getId() + "-31.htm";
					}
					else if (cid.level() >= 2)
					{
						return npc.getId() + "-32.htm";
					}
				}
			}
		}
		else
		{
			htmltext = npc.getId() + "-33.htm";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new ElvenHumanMystics1(-1, qn, "ai/npc/VillageMasters");
	}
}