package com.l2jfrozen.gameserver.ai.special;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2CommandChannel;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.Earthquake;
import com.l2jfrozen.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillCanceld;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.SpecialCamera;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

/**
 * Frintezza AI
 * @author Darki699
 * @author SANDMAN L2J_JP(modified)
 * @author JOJO Update by rocknow Updated by L2jOff team Updated by L2jFrozen team <BR>
 *         Warn: be careful with adding new spawns {@link #getXFix(int)}
 */
public class Frintezza extends Quest implements Runnable
{
	private static final int[][] invadeLoc =
	{
		{
			174102,
			-76039,
			-5105
		},
		{
			173235,
			-76884,
			-5105
		},
		{
			175003,
			-76933,
			-5105
		},
		{
			174196,
			-76190,
			-5105
		},
		{
			174013,
			-76120,
			-5105
		},
		{
			173263,
			-75161,
			-5105
		}
	};
	
	private static final int[][] skills =
	{
		{
			5015,
			1,
			5000
		},
		{
			5015,
			4,
			5000
		},
		{
			5015,
			2,
			5000
		},
		{
			5015,
			5,
			5000
		},
		{
			5018,
			1,
			10000
		},
		{
			5016,
			1,
			5000
		},
		{
			5015,
			3,
			5000
		},
		{
			5015,
			6,
			5000
		},
		{
			5018,
			2,
			10000
		},
		{
			5019,
			1,
			10000
		},
		{
			5016,
			1,
			5000
		}
	};
	
	private static final int[][] mobLoc =
	{
		{
			18328,
			172894,
			-76019,
			-5107,
			243
		},
		{
			18328,
			174095,
			-77279,
			-5107,
			16216
		},
		{
			18328,
			174111,
			-74833,
			-5107,
			49043
		},
		{
			18328,
			175344,
			-76042,
			-5107,
			32847
		},
		{
			18330,
			173489,
			-76227,
			-5134,
			63565
		},
		{
			18330,
			173498,
			-75724,
			-5107,
			58498
		},
		{
			18330,
			174365,
			-76745,
			-5107,
			22424
		},
		{
			18330,
			174570,
			-75584,
			-5107,
			31968
		},
		{
			18330,
			174613,
			-76179,
			-5107,
			31471
		},
		{
			18332,
			173620,
			-75981,
			-5107,
			4588
		},
		{
			18332,
			173630,
			-76340,
			-5107,
			62454
		},
		{
			18332,
			173755,
			-75613,
			-5107,
			57892
		},
		{
			18332,
			173823,
			-76688,
			-5107,
			2411
		},
		{
			18332,
			174000,
			-75411,
			-5107,
			54718
		},
		{
			18332,
			174487,
			-75555,
			-5107,
			33861
		},
		{
			18332,
			174517,
			-76471,
			-5107,
			21893
		},
		{
			18332,
			174576,
			-76122,
			-5107,
			31176
		},
		{
			18332,
			174600,
			-75841,
			-5134,
			35927
		},
		{
			18329,
			173481,
			-76043,
			-5107,
			61312
		},
		{
			18329,
			173539,
			-75678,
			-5107,
			59524
		},
		{
			18329,
			173584,
			-76386,
			-5107,
			3041
		},
		{
			18329,
			173773,
			-75420,
			-5107,
			51115
		},
		{
			18329,
			173777,
			-76650,
			-5107,
			12588
		},
		{
			18329,
			174585,
			-76510,
			-5107,
			21704
		},
		{
			18329,
			174623,
			-75571,
			-5107,
			40141
		},
		{
			18329,
			174744,
			-76240,
			-5107,
			29202
		},
		{
			18329,
			174769,
			-75895,
			-5107,
			29572
		},
		{
			18333,
			173861,
			-76011,
			-5107,
			383
		},
		{
			18333,
			173872,
			-76461,
			-5107,
			8041
		},
		{
			18333,
			173898,
			-75668,
			-5107,
			51856
		},
		{
			18333,
			174422,
			-75689,
			-5107,
			42878
		},
		{
			18333,
			174460,
			-76355,
			-5107,
			27311
		},
		{
			18333,
			174483,
			-76041,
			-5107,
			30947
		},
		{
			18331,
			173515,
			-76184,
			-5107,
			6971
		},
		{
			18331,
			173516,
			-75790,
			-5134,
			3142
		},
		{
			18331,
			173696,
			-76675,
			-5107,
			6757
		},
		{
			18331,
			173766,
			-75502,
			-5134,
			60827
		},
		{
			18331,
			174473,
			-75321,
			-5107,
			37147
		},
		{
			18331,
			174493,
			-76505,
			-5107,
			34503
		},
		{
			18331,
			174568,
			-75654,
			-5134,
			41661
		},
		{
			18331,
			174584,
			-76263,
			-5107,
			31729
		},
		{
			18339,
			173892,
			-81592,
			-5123,
			50849
		},
		{
			18339,
			173958,
			-81820,
			-5123,
			7459
		},
		{
			18339,
			174128,
			-81805,
			-5150,
			21495
		},
		{
			18339,
			174245,
			-81566,
			-5123,
			41760
		},
		{
			18334,
			173264,
			-81529,
			-5072,
			1646
		},
		{
			18334,
			173265,
			-81656,
			-5072,
			441
		},
		{
			18334,
			173267,
			-81889,
			-5072,
			0
		},
		{
			18334,
			173271,
			-82015,
			-5072,
			65382
		},
		{
			18334,
			174867,
			-81655,
			-5073,
			32537
		},
		{
			18334,
			174868,
			-81890,
			-5073,
			32768
		},
		{
			18334,
			174869,
			-81485,
			-5073,
			32315
		},
		{
			18334,
			174871,
			-82017,
			-5073,
			33007
		},
		{
			18335,
			173074,
			-80817,
			-5107,
			8353
		},
		{
			18335,
			173128,
			-82702,
			-5107,
			5345
		},
		{
			18335,
			173181,
			-82544,
			-5107,
			65135
		},
		{
			18335,
			173191,
			-80981,
			-5107,
			6947
		},
		{
			18335,
			174859,
			-80889,
			-5134,
			24103
		},
		{
			18335,
			174924,
			-82666,
			-5107,
			38710
		},
		{
			18335,
			174947,
			-80733,
			-5107,
			22449
		},
		{
			18335,
			175096,
			-82724,
			-5107,
			42205
		},
		{
			18336,
			173435,
			-80512,
			-5107,
			65215
		},
		{
			18336,
			173440,
			-82948,
			-5107,
			417
		},
		{
			18336,
			173443,
			-83120,
			-5107,
			1094
		},
		{
			18336,
			173463,
			-83064,
			-5107,
			286
		},
		{
			18336,
			173465,
			-80453,
			-5107,
			174
		},
		{
			18336,
			173465,
			-83006,
			-5107,
			2604
		},
		{
			18336,
			173468,
			-82889,
			-5107,
			316
		},
		{
			18336,
			173469,
			-80570,
			-5107,
			65353
		},
		{
			18336,
			173469,
			-80628,
			-5107,
			166
		},
		{
			18336,
			173492,
			-83121,
			-5107,
			394
		},
		{
			18336,
			173493,
			-80683,
			-5107,
			0
		},
		{
			18336,
			173497,
			-80510,
			-5134,
			417
		},
		{
			18336,
			173499,
			-82947,
			-5107,
			0
		},
		{
			18336,
			173521,
			-83063,
			-5107,
			316
		},
		{
			18336,
			173523,
			-82889,
			-5107,
			128
		},
		{
			18336,
			173524,
			-80627,
			-5134,
			65027
		},
		{
			18336,
			173524,
			-83007,
			-5107,
			0
		},
		{
			18336,
			173526,
			-80452,
			-5107,
			64735
		},
		{
			18336,
			173527,
			-80569,
			-5134,
			65062
		},
		{
			18336,
			174602,
			-83122,
			-5107,
			33104
		},
		{
			18336,
			174604,
			-82949,
			-5107,
			33184
		},
		{
			18336,
			174609,
			-80514,
			-5107,
			33234
		},
		{
			18336,
			174609,
			-80684,
			-5107,
			32851
		},
		{
			18336,
			174629,
			-80627,
			-5107,
			33346
		},
		{
			18336,
			174632,
			-80570,
			-5107,
			32896
		},
		{
			18336,
			174632,
			-83066,
			-5107,
			32768
		},
		{
			18336,
			174635,
			-82893,
			-5107,
			33594
		},
		{
			18336,
			174636,
			-80456,
			-5107,
			32065
		},
		{
			18336,
			174639,
			-83008,
			-5107,
			33057
		},
		{
			18336,
			174660,
			-80512,
			-5107,
			33057
		},
		{
			18336,
			174661,
			-83121,
			-5107,
			32768
		},
		{
			18336,
			174663,
			-82948,
			-5107,
			32768
		},
		{
			18336,
			174664,
			-80685,
			-5107,
			32676
		},
		{
			18336,
			174687,
			-83008,
			-5107,
			32520
		},
		{
			18336,
			174691,
			-83066,
			-5107,
			32961
		},
		{
			18336,
			174692,
			-80455,
			-5107,
			33202
		},
		{
			18336,
			174692,
			-80571,
			-5107,
			32768
		},
		{
			18336,
			174693,
			-80630,
			-5107,
			32994
		},
		{
			18336,
			174693,
			-82889,
			-5107,
			32622
		},
		{
			18337,
			172837,
			-82382,
			-5107,
			58363
		},
		{
			18337,
			172867,
			-81123,
			-5107,
			64055
		},
		{
			18337,
			172883,
			-82495,
			-5107,
			64764
		},
		{
			18337,
			172916,
			-81033,
			-5107,
			7099
		},
		{
			18337,
			172940,
			-82325,
			-5107,
			58998
		},
		{
			18337,
			172946,
			-82435,
			-5107,
			58038
		},
		{
			18337,
			172971,
			-81198,
			-5107,
			14768
		},
		{
			18337,
			172992,
			-81091,
			-5107,
			9438
		},
		{
			18337,
			173032,
			-82365,
			-5107,
			59041
		},
		{
			18337,
			173064,
			-81125,
			-5107,
			5827
		},
		{
			18337,
			175014,
			-81173,
			-5107,
			26398
		},
		{
			18337,
			175061,
			-82374,
			-5107,
			43290
		},
		{
			18337,
			175096,
			-81080,
			-5107,
			24719
		},
		{
			18337,
			175169,
			-82453,
			-5107,
			37672
		},
		{
			18337,
			175172,
			-80972,
			-5107,
			32315
		},
		{
			18337,
			175174,
			-82328,
			-5107,
			41760
		},
		{
			18337,
			175197,
			-81157,
			-5107,
			27617
		},
		{
			18337,
			175245,
			-82547,
			-5107,
			40275
		},
		{
			18337,
			175249,
			-81075,
			-5107,
			28435
		},
		{
			18337,
			175292,
			-82432,
			-5107,
			42225
		},
		{
			18338,
			173014,
			-82628,
			-5107,
			11874
		},
		{
			18338,
			173033,
			-80920,
			-5107,
			10425
		},
		{
			18338,
			173095,
			-82520,
			-5107,
			49152
		},
		{
			18338,
			173115,
			-80986,
			-5107,
			9611
		},
		{
			18338,
			173144,
			-80894,
			-5107,
			5345
		},
		{
			18338,
			173147,
			-82602,
			-5107,
			51316
		},
		{
			18338,
			174912,
			-80825,
			-5107,
			24270
		},
		{
			18338,
			174935,
			-80899,
			-5107,
			18061
		},
		{
			18338,
			175016,
			-82697,
			-5107,
			39533
		},
		{
			18338,
			175041,
			-80834,
			-5107,
			25420
		},
		{
			18338,
			175071,
			-82549,
			-5107,
			39163
		},
		{
			18338,
			175154,
			-82619,
			-5107,
			36345
		}
	};
	
	private static final int SCARLET1 = 29046;
	private static final int SCARLET2 = 29047;
	private static final int FRINTEZZA = 29045;
	private static final int GUIDE = 32011;
	private static final int CUBE = 29061;
	
	// Frintezza Status Tracking :
	private static final byte DORMANT = 0; // Frintezza is spawned and no one has entered yet. Entry is unlocked
	private static final byte WAITING = 1; // Frintezza is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	// before he unleashes his attack. Entry is unlocked
	private static final byte FIGHTING = 2; // Frintezza is engaged in battle, annihilating his foes. Entry is locked
	private static final byte DEAD = 3; // Frintezza has been killed. Entry is locked
	
	private static long lastAction = 0;
	private static int angle = 0;
	private static int heading = 0;
	private static int locCycle = 0;
	private static int bomber = 0;
	private static int checkDie = 0;
	private static int onCheck = 0;
	private static int onSong = 0;
	private static int abnormal = 0;
	private static int onMorph = 0;
	private static int scarlet_x = 0;
	private static int scarlet_y = 0;
	private static int scarlet_z = 0;
	private static int scarlet_h = 0;
	private static int secondMorph = 0;
	private static int thirdMorph = 0;
	private static int killHallAlarmDevice = 0;
	private static int killDarkChoirPlayer = 0;
	private static int killDarkChoirCaptain = 0;
	
	private static L2BossZone zone;
	@SuppressWarnings("unused")
	private L2GrandBossInstance frintezza, weakScarlet, strongScarlet, activeScarlet;
	private L2MonsterInstance demon1, demon2, demon3, demon4, portrait1, portrait2, portrait3, portrait4;
	private L2NpcInstance frintezzaDummy, overheadDummy, portraitDummy1, portraitDummy3, scarletDummy;
	private final List<L2PcInstance> playersInside = new ArrayList<>();
	private final List<L2NpcInstance> room1Mobs = new ArrayList<>();
	private final List<L2NpcInstance> room2Mobs = new ArrayList<>();
	private final List<L2Attackable> minions = new ArrayList<>();
	
	// Boss: Frintezza
	public Frintezza(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		final int[] mob =
		{
			SCARLET1,
			SCARLET2,
			FRINTEZZA,
			18328,
			18329,
			18330,
			18331,
			18332,
			18333,
			18334,
			18335,
			18336,
			18337,
			18338,
			18339,
			29048,
			29049,
			29050,
			29051
		};
		zone = GrandBossManager.getInstance().getZone(getXFix(174232), getYFix(-88020), getZFix(-5116));
		registerMobs(mob);
		addStartNpc(GUIDE);
		addTalkId(GUIDE);
		addStartNpc(CUBE);
		addTalkId(CUBE);
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(FRINTEZZA);
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		
		if (status == DEAD)
		{
			final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				startQuestTimer("frintezza_unlock", temp, null, null);
			}
			else
			{
				GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
			}
		}
		else if (status != DORMANT)
		{
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
		}
		
		// tempfix for messed door cords
		for (int i = 0; i < 8; i++)
		{
			DoorTable.getInstance().getDoor(25150051 + i).setRange(0, 0, 0, 0, 0, 0);
		}
	}
	
	@Override
	public String onAdvEvent(final String event, final L2NpcInstance npc, final L2PcInstance player)
	{
		long temp = 0;
		if (event.equalsIgnoreCase("waiting"))
		{
			startQuestTimer("close", 27000, npc, null);
			startQuestTimer("camera_1", 30000, npc, null);
			zone.broadcastPacket(new Earthquake(getXFix(174232), getYFix(-88020), getZFix(-5116), 45, 27));
		}
		else if (event.equalsIgnoreCase("room1_spawn"))
		{
			for (int i = 0; i <= 17; i++)
			{
				final L2NpcInstance mob = addSpawn(mobLoc[i][0], mobLoc[i][1], mobLoc[i][2], mobLoc[i][3], mobLoc[i][4], false, 0);
				synchronized (room1Mobs)
				{
					room1Mobs.add(mob);
				}
				
			}
		}
		else if (event.equalsIgnoreCase("room1_spawn2"))
		{
			for (int i = 18; i <= 26; i++)
			{
				final L2NpcInstance mob = addSpawn(mobLoc[i][0], mobLoc[i][1], mobLoc[i][2], mobLoc[i][3], mobLoc[i][4], false, 0);
				synchronized (room1Mobs)
				{
					room1Mobs.add(mob);
				}
				
			}
		}
		else if (event.equalsIgnoreCase("room1_spawn3"))
		{
			for (int i = 27; i <= 32; i++)
			{
				final L2NpcInstance mob = addSpawn(mobLoc[i][0], mobLoc[i][1], mobLoc[i][2], mobLoc[i][3], mobLoc[i][4], false, 0);
				synchronized (room1Mobs)
				{
					room1Mobs.add(mob);
				}
				
			}
		}
		else if (event.equalsIgnoreCase("room1_spawn4"))
		{
			for (int i = 33; i <= 40; i++)
			{
				final L2NpcInstance mob = addSpawn(mobLoc[i][0], mobLoc[i][1], mobLoc[i][2], mobLoc[i][3], mobLoc[i][4], false, 0);
				synchronized (room1Mobs)
				{
					room1Mobs.add(mob);
				}
				
			}
		}
		else if (event.equalsIgnoreCase("room2_spawn"))
		{
			for (int i = 41; i <= 44; i++)
			{
				final L2NpcInstance mob = addSpawn(mobLoc[i][0], mobLoc[i][1], mobLoc[i][2], mobLoc[i][3], mobLoc[i][4], false, 0);
				synchronized (room2Mobs)
				{
					room2Mobs.add(mob);
				}
				
			}
		}
		else if (event.equalsIgnoreCase("room2_spawn2"))
		{
			for (int i = 45; i <= 131; i++)
			{
				final L2NpcInstance mob = addSpawn(mobLoc[i][0], mobLoc[i][1], mobLoc[i][2], mobLoc[i][3], mobLoc[i][4], false, 0);
				synchronized (room2Mobs)
				{
					room2Mobs.add(mob);
				}
				
			}
		}
		else if (event.equalsIgnoreCase("room1_del"))
		{
			synchronized (room1Mobs)
			{
				for (final L2NpcInstance mob : room1Mobs)
				{
					if (mob != null)
					{
						mob.deleteMe();
					}
				}
				room1Mobs.clear();
			}
			
		}
		else if (event.equalsIgnoreCase("room2_del"))
		{
			synchronized (room2Mobs)
			{
				for (final L2NpcInstance mob : room2Mobs)
				{
					if (mob != null)
					{
						mob.deleteMe();
					}
				}
				room2Mobs.clear();
			}
			
		}
		else if (event.equalsIgnoreCase("room3_del"))
		{
			if (demon1 != null)
			{
				demon1.deleteMe();
			}
			if (demon2 != null)
			{
				demon2.deleteMe();
			}
			if (demon3 != null)
			{
				demon3.deleteMe();
			}
			if (demon4 != null)
			{
				demon4.deleteMe();
			}
			if (portrait1 != null)
			{
				portrait1.deleteMe();
			}
			if (portrait2 != null)
			{
				portrait2.deleteMe();
			}
			if (portrait3 != null)
			{
				portrait3.deleteMe();
			}
			if (portrait4 != null)
			{
				portrait4.deleteMe();
			}
			if (frintezza != null)
			{
				frintezza.deleteMe();
			}
			if (weakScarlet != null)
			{
				weakScarlet.deleteMe();
			}
			if (strongScarlet != null)
			{
				strongScarlet.deleteMe();
			}
			
			demon1 = null;
			demon2 = null;
			demon3 = null;
			demon4 = null;
			portrait1 = null;
			portrait2 = null;
			portrait3 = null;
			portrait4 = null;
			frintezza = null;
			weakScarlet = null;
			strongScarlet = null;
			activeScarlet = null;
		}
		else if (event.equalsIgnoreCase("clean"))
		{
			lastAction = 0;
			locCycle = 0;
			checkDie = 0;
			onCheck = 0;
			abnormal = 0;
			onMorph = 0;
			secondMorph = 0;
			thirdMorph = 0;
			killHallAlarmDevice = 0;
			killDarkChoirPlayer = 0;
			killDarkChoirCaptain = 0;
			playersInside.clear();
		}
		else if (event.equalsIgnoreCase("close"))
		{
			for (int i = 25150051; i <= 25150058; i++)
			{
				DoorTable.getInstance().getDoor(i).closeMe();
			}
			for (int i = 25150061; i <= 25150070; i++)
			{
				DoorTable.getInstance().getDoor(i).closeMe();
			}
			
			DoorTable.getInstance().getDoor(25150042).closeMe();
			DoorTable.getInstance().getDoor(25150043).closeMe();
			DoorTable.getInstance().getDoor(25150045).closeMe();
			DoorTable.getInstance().getDoor(25150046).closeMe();
		}
		/*
		 * else if (event.equalsIgnoreCase("loc_check")) { Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA); if (status == FIGHTING) { if (!_Zone.isInsideZone(npc)) npc.teleToLocation(getXFix(174232),getYFix(-88020),getZFix(-5116)); if (npc.getX() < getXFix(171932) || npc.getX() >
		 * getXFix(176532) || npc.getY() < getYFix(-90320) || npc.getY() > getYFix(-85720) || npc.getZ() < getZFix(-5130)) npc.teleToLocation(getXFix(174232),getYFix(-88020),getZFix(-5116)); } }
		 */
		else if (event.equalsIgnoreCase("camera_1"))
		{
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, FIGHTING);
			
			frintezzaDummy = addSpawn(29052, 174240, -89805, -5022, 16048, false, 0);
			frintezzaDummy.setIsInvul(true);
			frintezzaDummy.setIsImobilised(true);
			
			overheadDummy = addSpawn(29052, 174232, -88020, -5110, 16384, false, 0);
			overheadDummy.setIsInvul(true);
			overheadDummy.setIsImobilised(true);
			overheadDummy.setCollisionHeight(600);
			zone.broadcastPacket(new NpcInfo(overheadDummy, null));
			
			portraitDummy1 = addSpawn(29052, 172450, -87890, -5100, 16048, false, 0);
			portraitDummy1.setIsImobilised(true);
			portraitDummy1.setIsInvul(true);
			
			portraitDummy3 = addSpawn(29052, 176012, -87890, -5100, 16048, false, 0);
			portraitDummy3.setIsImobilised(true);
			portraitDummy3.setIsInvul(true);
			
			scarletDummy = addSpawn(29053, 174232, -88020, -5110, 16384, false, 0);
			scarletDummy.setIsInvul(true);
			scarletDummy.setIsImobilised(true);
			
			startQuestTimer("stop_pc", 0, npc, null);
			startQuestTimer("camera_2", 1000, overheadDummy, null);
		}
		else if (event.equalsIgnoreCase("camera_2"))
		{
			zone.broadcastPacket(new SpecialCamera(overheadDummy.getObjectId(), 0, 75, -89, 0, 100));
			startQuestTimer("camera_2b", 0, overheadDummy, null);
		}
		else if (event.equalsIgnoreCase("camera_2b"))
		{
			zone.broadcastPacket(new SpecialCamera(overheadDummy.getObjectId(), 0, 75, -89, 0, 100));
			startQuestTimer("camera_3", 0, overheadDummy, null);
		}
		else if (event.equalsIgnoreCase("camera_3"))
		{
			zone.broadcastPacket(new SpecialCamera(overheadDummy.getObjectId(), 300, 90, -10, 6500, 7000));
			
			frintezza = (L2GrandBossInstance) addSpawn(FRINTEZZA, 174240, -89805, -5022, 16048, false, 0);
			GrandBossManager.getInstance().addBoss(frintezza);
			frintezza.setIsImobilised(true);
			frintezza.setIsInvul(true);
			frintezza.disableAllSkills();
			zone.updateKnownList(frintezza);
			
			demon2 = (L2MonsterInstance) addSpawn(29051, 175876, -88713, -5100, 28205, false, 0);
			demon2.setIsImobilised(true);
			demon2.disableAllSkills();
			zone.updateKnownList(demon2);
			
			demon3 = (L2MonsterInstance) addSpawn(29051, 172608, -88702, -5100, 64817, false, 0);
			demon3.setIsImobilised(true);
			demon3.disableAllSkills();
			zone.updateKnownList(demon3);
			
			demon1 = (L2MonsterInstance) addSpawn(29050, 175833, -87165, -5100, 35048, false, 0);
			demon1.setIsImobilised(true);
			demon1.disableAllSkills();
			zone.updateKnownList(demon1);
			
			demon4 = (L2MonsterInstance) addSpawn(29050, 172634, -87165, -5100, 57730, false, 0);
			demon4.setIsImobilised(true);
			demon4.disableAllSkills();
			zone.updateKnownList(demon4);
			
			startQuestTimer("camera_4", 6500, overheadDummy, null);
		}
		else if (event.equalsIgnoreCase("camera_4"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezzaDummy.getObjectId(), 1800, 90, 8, 6500, 7000));
			startQuestTimer("camera_5", 900, frintezzaDummy, null);
		}
		else if (event.equalsIgnoreCase("camera_5"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezzaDummy.getObjectId(), 140, 90, 10, 2500, 4500));
			startQuestTimer("camera_5b", 4000, frintezzaDummy, null);
		}
		else if (event.equalsIgnoreCase("camera_5b"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 40, 75, -10, 0, 1000));
			startQuestTimer("camera_6", 0, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_6"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 40, 75, -10, 0, 12000));
			startQuestTimer("camera_7", 1350, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_7"))
		{
			zone.broadcastPacket(new SocialAction(frintezza.getObjectId(), 2));
			startQuestTimer("camera_8", 7000, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_8"))
		{
			startQuestTimer("camera_9", 1000, frintezza, null);
			frintezzaDummy.deleteMe();
			frintezzaDummy = null;
		}
		else if (event.equalsIgnoreCase("camera_9"))
		{
			zone.broadcastPacket(new SocialAction(demon2.getObjectId(), 1));
			zone.broadcastPacket(new SocialAction(demon3.getObjectId(), 1));
			startQuestTimer("camera_9b", 400, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_9b"))
		{
			zone.broadcastPacket(new SocialAction(demon1.getObjectId(), 1));
			zone.broadcastPacket(new SocialAction(demon4.getObjectId(), 1));
			
			for (final L2Character pc : zone.getCharactersInside().values())
			{
				if (pc instanceof L2PcInstance)
				{
					if (pc.getX() < getXFix(174232))
					{
						pc.broadcastPacket(new SpecialCamera(portraitDummy1.getObjectId(), 1000, 118, 0, 0, 1000));
					}
					else
					{
						pc.broadcastPacket(new SpecialCamera(portraitDummy3.getObjectId(), 1000, 62, 0, 0, 1000));
					}
				}
			}
			startQuestTimer("camera_9c", 0, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_9c"))
		{
			for (final L2Character pc : zone.getCharactersInside().values())
			{
				if (pc instanceof L2PcInstance)
				{
					if (pc.getX() < getXFix(174232))
					{
						pc.broadcastPacket(new SpecialCamera(portraitDummy1.getObjectId(), 1000, 118, 0, 0, 10000));
					}
					else
					{
						pc.broadcastPacket(new SpecialCamera(portraitDummy3.getObjectId(), 1000, 62, 0, 0, 10000));
					}
				}
			}
			startQuestTimer("camera_10", 2000, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_10"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 240, 90, 0, 0, 1000));
			startQuestTimer("camera_11", 0, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_11"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 240, 90, 25, 5500, 10000));
			zone.broadcastPacket(new SocialAction(frintezza.getObjectId(), 3));
			portraitDummy1.deleteMe();
			portraitDummy3.deleteMe();
			portraitDummy1 = null;
			portraitDummy3 = null;
			
			startQuestTimer("camera_12", 4500, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_12"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 100, 195, 35, 0, 10000));
			startQuestTimer("camera_13", 700, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_13"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 100, 195, 35, 0, 10000));
			startQuestTimer("camera_14", 1300, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_14"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 120, 180, 45, 1500, 10000));
			zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5006, 1, 34000, 0));
			startQuestTimer("camera_16", 1500, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_16"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 520, 135, 45, 8000, 10000));
			startQuestTimer("camera_17", 7500, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_17"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 1500, 110, 25, 10000, 13000));
			startQuestTimer("camera_18", 9500, frintezza, null);
		}
		else if (event.equalsIgnoreCase("camera_18"))
		{
			zone.broadcastPacket(new SpecialCamera(overheadDummy.getObjectId(), 930, 160, -20, 0, 1000));
			startQuestTimer("camera_18b", 0, overheadDummy, null);
		}
		else if (event.equalsIgnoreCase("camera_18b"))
		{
			zone.broadcastPacket(new SpecialCamera(overheadDummy.getObjectId(), 600, 180, -25, 0, 10000));
			zone.broadcastPacket(new MagicSkillUser(scarletDummy, overheadDummy, 5004, 1, 5800, 0));
			
			weakScarlet = (L2GrandBossInstance) addSpawn(SCARLET1, 174232, -88020, -5110, 16384, false, 0);
			weakScarlet.setIsInvul(true);
			weakScarlet.setIsImobilised(true);
			weakScarlet.disableAllSkills();
			zone.updateKnownList(weakScarlet);
			activeScarlet = weakScarlet;
			
			/*
			 * startQuestTimer("camera_19", 2400, scarletDummy, null); startQuestTimer("camera_19b", 5000, scarletDummy, null);
			 */
			startQuestTimer("camera_19", 5500, scarletDummy, null);
			startQuestTimer("camera_19b", 5400, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("camera_19"))
		{
			weakScarlet.teleToLocation(getXFix(174232), getYFix(-88020), getZFix(-5110));
		}
		else if (event.equalsIgnoreCase("camera_19b"))
		{
			zone.broadcastPacket(new SpecialCamera(scarletDummy.getObjectId(), 800, 180, 10, 1000, 10000));
			startQuestTimer("camera_20", 2100, scarletDummy, null);
		}
		else if (event.equalsIgnoreCase("camera_20"))
		{
			zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 300, 60, 8, 0, 10000));
			startQuestTimer("camera_21", 2000, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("camera_21"))
		{
			zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 500, 90, 10, 3000, 5000));
			startQuestTimer("camera_22", 3000, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("camera_22"))
		{
			portrait2 = (L2MonsterInstance) addSpawn(29049, 175876, -88713, -5000, 28205, false, 0);
			portrait2.setIsImobilised(true);
			portrait2.disableAllSkills();
			zone.updateKnownList(portrait2);
			
			portrait3 = (L2MonsterInstance) addSpawn(29049, 172608, -88702, -5000, 64817, false, 0);
			portrait3.setIsImobilised(true);
			portrait3.disableAllSkills();
			zone.updateKnownList(portrait3);
			
			portrait1 = (L2MonsterInstance) addSpawn(29048, 175833, -87165, -5000, 35048, false, 0);
			portrait1.setIsImobilised(true);
			portrait1.disableAllSkills();
			zone.updateKnownList(portrait1);
			
			portrait4 = (L2MonsterInstance) addSpawn(29048, 172634, -87165, -5000, 57730, false, 0);
			portrait4.setIsImobilised(true);
			portrait4.disableAllSkills();
			zone.updateKnownList(portrait4);
			
			overheadDummy.deleteMe();
			scarletDummy.deleteMe();
			overheadDummy = null;
			scarletDummy = null;
			
			startQuestTimer("camera_23", 2000, weakScarlet, null);
			startQuestTimer("start_pc", 2000, weakScarlet, null);
			// startQuestTimer("loc_check", 60000, weakScarlet, null, true);
			startQuestTimer("songs_play", 10000 + Rnd.get(10000), frintezza, null);
			startQuestTimer("skill01", 10000 + Rnd.get(10000), weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("camera_23"))
		{
			demon1.setIsImobilised(false);
			demon2.setIsImobilised(false);
			demon3.setIsImobilised(false);
			demon4.setIsImobilised(false);
			demon1.enableAllSkills();
			demon2.enableAllSkills();
			demon3.enableAllSkills();
			demon4.enableAllSkills();
			portrait1.setIsImobilised(false);
			portrait2.setIsImobilised(false);
			portrait3.setIsImobilised(false);
			portrait4.setIsImobilised(false);
			portrait1.enableAllSkills();
			portrait2.enableAllSkills();
			portrait3.enableAllSkills();
			portrait4.enableAllSkills();
			weakScarlet.setIsInvul(false);
			weakScarlet.setIsImobilised(false);
			weakScarlet.enableAllSkills();
			weakScarlet.setRunning();
			
			startQuestTimer("spawn_minion", 20000, portrait1, null);
			startQuestTimer("spawn_minion", 20000, portrait2, null);
			startQuestTimer("spawn_minion", 20000, portrait3, null);
			startQuestTimer("spawn_minion", 20000, portrait4, null);
		}
		else if (event.equalsIgnoreCase("stop_pc"))
		{
			for (final L2Character cha : zone.getCharactersInside().values())
			{
				cha.abortAttack();
				cha.abortCast();
				cha.disableAllSkills();
				cha.setTarget(null);
				cha.stopMove(null);
				cha.setIsImobilised(true);
				cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		else if (event.equalsIgnoreCase("stop_npc"))
		{
			heading = npc.getHeading();
			if (heading < 32768)
			{
				angle = Math.abs(180 - (int) (heading / 182.044444444));
			}
			else
			{
				angle = Math.abs(540 - (int) (heading / 182.044444444));
			}
		}
		else if (event.equalsIgnoreCase("start_pc"))
		{
			for (final L2Character cha : zone.getCharactersInside().values())
			{
				if (cha != frintezza)
				{
					cha.enableAllSkills();
					cha.setIsImobilised(false);
				}
			}
		}
		else if (event.equalsIgnoreCase("start_npc"))
		{
			npc.setRunning();
			npc.setIsInvul(false);
		}
		else if (event.equalsIgnoreCase("morph_end"))
		{
			onMorph = 0;
		}
		else if (event.equalsIgnoreCase("morph_01"))
		{
			zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 250, angle, 12, 2000, 15000));
			startQuestTimer("morph_02", 3000, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_02"))
		{
			zone.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 1));
			weakScarlet.setRHandId(7903);
			startQuestTimer("morph_03", 4000, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_03"))
		{
			startQuestTimer("morph_04", 1500, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_04"))
		{
			zone.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 4));
			final L2Skill skill = SkillTable.getInstance().getInfo(5017, 1);
			if (skill != null)
			{
				skill.getEffects(weakScarlet, weakScarlet, false, false, false);
			}
			
			startQuestTimer("morph_end", 6000, weakScarlet, null);
			startQuestTimer("start_pc", 3000, weakScarlet, null);
			startQuestTimer("start_npc", 3000, weakScarlet, null);
			startQuestTimer("songs_play", 10000 + Rnd.get(10000), frintezza, null);
			startQuestTimer("skill02", 10000 + Rnd.get(10000), weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_05a"))
		{
			zone.broadcastPacket(new SocialAction(frintezza.getObjectId(), 4));
		}
		else if (event.equalsIgnoreCase("morph_05"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 250, 120, 15, 0, 1000));
			startQuestTimer("morph_06", 0, frintezza, null);
		}
		else if (event.equalsIgnoreCase("morph_06"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 250, 120, 15, 0, 10000));
			
			// cancelQuestTimer("loc_check");
			
			scarlet_x = weakScarlet.getX();
			scarlet_y = weakScarlet.getY();
			scarlet_z = weakScarlet.getZ();
			scarlet_h = weakScarlet.getHeading();
			weakScarlet.deleteMe();
			weakScarlet = null;
			activeScarlet = null;
			weakScarlet = (L2GrandBossInstance) addSpawn(SCARLET1, scarlet_x, scarlet_y, scarlet_z, scarlet_h, false, 0);
			weakScarlet.setIsInvul(true);
			weakScarlet.setIsImobilised(true);
			weakScarlet.disableAllSkills();
			weakScarlet.setRHandId(7903);
			zone.updateKnownList(weakScarlet);
			
			startQuestTimer("morph_07", 7000, frintezza, null);
		}
		else if (event.equalsIgnoreCase("morph_07"))
		{
			zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5006, 1, 34000, 0));
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 500, 70, 15, 3000, 10000));
			startQuestTimer("morph_08", 3000, frintezza, null);
		}
		else if (event.equalsIgnoreCase("morph_08"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 2500, 90, 12, 6000, 10000));
			startQuestTimer("morph_09", 3000, frintezza, null);
		}
		else if (event.equalsIgnoreCase("morph_09"))
		{
			zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 250, angle, 12, 0, 1000));
			startQuestTimer("morph_10", 0, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_10"))
		{
			zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 250, angle, 12, 0, 10000));
			startQuestTimer("morph_11", 500, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_11"))
		{
			weakScarlet.doDie(weakScarlet);
			zone.broadcastPacket(new SpecialCamera(weakScarlet.getObjectId(), 450, angle, 14, 8000, 8000));
			
			startQuestTimer("morph_12", 6250, weakScarlet, null);
			startQuestTimer("morph_13", 7200, weakScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_12"))
		{
			weakScarlet.deleteMe();
			weakScarlet = null;
		}
		else if (event.equalsIgnoreCase("morph_13"))
		{
			strongScarlet = (L2GrandBossInstance) addSpawn(SCARLET2, reverseXFix(scarlet_x), reverseYFix(scarlet_y), reverseZFix(scarlet_z), scarlet_h, false, 0);
			strongScarlet.setIsInvul(true);
			strongScarlet.setIsImobilised(true);
			strongScarlet.disableAllSkills();
			zone.updateKnownList(strongScarlet);
			activeScarlet = strongScarlet;
			
			zone.broadcastPacket(new SpecialCamera(strongScarlet.getObjectId(), 450, angle, 12, 500, 14000));
			
			startQuestTimer("morph_14", 3000, strongScarlet, null);
			// startQuestTimer("loc_check", 60000, strongScarlet, null, true);
		}
		else if (event.equalsIgnoreCase("morph_14"))
		{
			startQuestTimer("morph_15", 5100, strongScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_15"))
		{
			zone.broadcastPacket(new SocialAction(strongScarlet.getObjectId(), 2));
			final L2Skill skill = SkillTable.getInstance().getInfo(5017, 1);
			if (skill != null)
			{
				skill.getEffects(strongScarlet, strongScarlet, false, false, false);
			}
			
			startQuestTimer("morph_end", 9000, strongScarlet, null);
			startQuestTimer("start_pc", 6000, strongScarlet, null);
			startQuestTimer("start_npc", 6000, strongScarlet, null);
			startQuestTimer("songs_play", 10000 + Rnd.get(10000), frintezza, null);
			startQuestTimer("skill03", 10000 + Rnd.get(10000), strongScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_16"))
		{
			zone.broadcastPacket(new SpecialCamera(strongScarlet.getObjectId(), 300, angle - 180, 5, 0, 7000));
			startQuestTimer("morph_17", 0, strongScarlet, null);
		}
		else if (event.equalsIgnoreCase("morph_17"))
		{
			zone.broadcastPacket(new SpecialCamera(strongScarlet.getObjectId(), 200, angle, 85, 4000, 10000));
			startQuestTimer("morph_17b", 7400, frintezza, null);
			startQuestTimer("morph_18", 7500, frintezza, null);
		}
		else if (event.equalsIgnoreCase("morph_17b"))
		{
			frintezza.doDie(frintezza);
		}
		else if (event.equalsIgnoreCase("morph_18"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 100, 120, 5, 0, 7000));
			startQuestTimer("morph_19", 0, frintezza, null);
		}
		else if (event.equalsIgnoreCase("morph_19"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 100, 90, 5, 5000, 15000));
			startQuestTimer("morph_20", 7000, frintezza, null);
			startQuestTimer("spawn_cubes", 7000, frintezza, null);
		}
		else if (event.equalsIgnoreCase("morph_20"))
		{
			zone.broadcastPacket(new SpecialCamera(frintezza.getObjectId(), 900, 90, 25, 7000, 10000));
			startQuestTimer("start_pc", 7000, frintezza, null);
		}
		else if (event.equalsIgnoreCase("songs_play"))
		{
			if (frintezza != null && !frintezza.isDead() && onMorph == 0)
			{
				onSong = Rnd.get(1, 5);
				if (onSong == 3)
				{ // to fix skill exception
					onSong = 2;
				}
				
				String SongName = "";
				
				// Name of the songs are custom, named with client side description.
				switch (onSong)
				{
					case 1:
						SongName = "Frintezza's Healing Rhapsody";
						break;
					case 2:
						SongName = "Frintezza's Rampaging Opus";
						break;
					case 3:
						SongName = "Frintezza's Power Concerto";
						break;
					case 4:
						SongName = "Frintezza's Plagued Concerto";
						break;
					case 5:
						SongName = "Frintezza's Psycho Symphony";
						break;
					default:
						SongName = "Frintezza's Song";
						break;
				}
				
				// Like L2OFF the skill name is printed on screen
				zone.broadcastPacket(new ExShowScreenMessage(SongName, 6000));
				
				if (onSong == 1 && thirdMorph == 1 && strongScarlet.getCurrentHp() < strongScarlet.getMaxHp() * 0.6 && Rnd.get(100) < 80)
				{
					zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5007, 1, 32000, 0));
					startQuestTimer("songs_effect", 5000, frintezza, null);
					startQuestTimer("songs_play", 32000 + Rnd.get(10000), frintezza, null);
				}
				else if (onSong == 2 || onSong == 3)
				{
					zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5007, onSong, 32000, 0));
					startQuestTimer("songs_effect", 5000, frintezza, null);
					startQuestTimer("songs_play", 32000 + Rnd.get(10000), frintezza, null);
				}
				else if (onSong == 4 && secondMorph == 1)
				{
					zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5007, 4, 31000, 0));
					startQuestTimer("songs_effect", 5000, frintezza, null);
					startQuestTimer("songs_play", 31000 + Rnd.get(10000), frintezza, null);
				}
				else if (onSong == 5 && thirdMorph == 1 && abnormal == 0)
				{
					abnormal = 1;
					zone.broadcastPacket(new MagicSkillUser(frintezza, frintezza, 5007, 5, 35000, 0));
					startQuestTimer("songs_effect", 5000, frintezza, null);
					startQuestTimer("songs_play", 35000 + Rnd.get(10000), frintezza, null);
				}
				else
				{
					startQuestTimer("songs_play", 5000 + Rnd.get(5000), frintezza, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("songs_effect"))
		{
			final L2Skill skill = SkillTable.getInstance().getInfo(5008, onSong);
			if (skill == null)
			{
				return null;
			}
			
			String SongName = "";
			
			// Name of the songs are custom, named with client side description.
			switch (onSong)
			{
				case 1:
					SongName = "Frintezza's Concert Hall Melody";
					break;
				case 2:
					SongName = "Frintezza's Rampaging Opus en masse";
					break;
				case 3:
					SongName = "Frintezza Power Encore";
					break;
				case 4:
					SongName = "Mournful Chorale Prelude";
					break;
				case 5:
					SongName = "Hypnotic Mazurka ";
					break;
				default:
					SongName = "Frintezza's Song";
					break;
			}
			
			// Like L2OFF the skill name is printed on screen
			zone.broadcastPacket(new ExShowScreenMessage(SongName, 6000));
			
			if (onSong == 1 || onSong == 2 || onSong == 3)
			{
				/*
				 * TODO: Frintezza songs to be fixed like retail if (frintezza != null && !frintezza.isDead() && activeScarlet != null && !activeScarlet.isDead()) skill.getEffects(frintezza, activeScarlet, false, false, false);
				 */
			}
			else if (onSong == 4)
			{
				for (final L2Character cha : zone.getCharactersInside().values())
				{
					if (cha instanceof L2PcInstance && Rnd.get(100) < 80)
					{
						skill.getEffects(frintezza, cha, false, false, false);
						cha.sendPacket(new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(5008, 4));
					}
				}
			}
			else if (onSong == 5)
			{
				for (final L2Character cha : zone.getCharactersInside().values())
				{
					if (cha instanceof L2PcInstance && Rnd.get(100) < 70)
					{
						cha.abortAttack();
						cha.abortCast();
						cha.disableAllSkills();
						cha.stopMove(null);
						cha.setIsImobilised(true);
						cha.setIsParalyzed(true);
						cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(frintezza, cha, false, false, false);
						cha.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_DANCE_STUNNED);
						cha.sendPacket(new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(5008, 5));
					}
				}
				startQuestTimer("stop_effect", 25000, frintezza, null);
			}
		}
		else if (event.equalsIgnoreCase("stop_effect"))
		{
			for (final L2Character cha : zone.getCharactersInside().values())
			{
				if (cha instanceof L2PcInstance)
				{
					cha.stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_DANCE_STUNNED);
					cha.stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLOATING_ROOT);
					cha.enableAllSkills();
					cha.setIsImobilised(false);
					cha.setIsParalyzed(false);
				}
			}
			abnormal = 0;
		}
		else if (event.equalsIgnoreCase("attack_stop"))
		{
			cancelQuestTimer("skill01", npc, null);
			cancelQuestTimer("skill02", npc, null);
			cancelQuestTimer("skill03", npc, null);
			cancelQuestTimer("songs_play", npc, null);
			cancelQuestTimer("songs_effect", npc, null);
			
			if (frintezza != null)
			{
				zone.broadcastPacket(new MagicSkillCanceld(frintezza.getObjectId()));
			}
		}
		else if (event.equalsIgnoreCase("check_hp"))
		{
			if (npc.isDead())
			{
				onMorph = 1;
				zone.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
				
				startQuestTimer("attack_stop", 0, frintezza, null);
				startQuestTimer("stop_pc", 0, npc, null);
				startQuestTimer("stop_npc", 0, npc, null);
				startQuestTimer("morph_16", 0, npc, null);
			}
			else
			{
				checkDie = checkDie + 10;
				if (checkDie < 3000)
				{
					startQuestTimer("check_hp", 10, npc, null);
				}
				else
				{
					onCheck = 0;
					checkDie = 0;
				}
			}
		}
		else if (event.equalsIgnoreCase("skill01"))
		{
			if (weakScarlet != null && !weakScarlet.isDead() && secondMorph == 0 && thirdMorph == 0 && onMorph == 0)
			{
				final int i = Rnd.get(0, 1);
				final L2Skill skill = SkillTable.getInstance().getInfo(skills[i][0], skills[i][1]);
				if (skill != null)
				{
					weakScarlet.stopMove(null);
					// weakScarlet.setIsCastingNow(true);
					weakScarlet.doCast(skill);
				}
				startQuestTimer("skill01", skills[i][2] + 5000 + Rnd.get(10000), npc, null);
			}
		}
		else if (event.equalsIgnoreCase("skill02"))
		{
			if (weakScarlet != null && !weakScarlet.isDead() && secondMorph == 1 && thirdMorph == 0 && onMorph == 0)
			{
				int i = 0;
				if (abnormal == 0)
				{
					i = Rnd.get(2, 5);
				}
				else
				{
					i = Rnd.get(2, 4);
				}
				
				final L2Skill skill = SkillTable.getInstance().getInfo(skills[i][0], skills[i][1]);
				if (skill != null)
				{
					weakScarlet.stopMove(null);
					// weakScarlet.setIsCastingNow(true);
					weakScarlet.doCast(skill);
				}
				startQuestTimer("skill02", skills[i][2] + 5000 + Rnd.get(10000), npc, null);
				
				if (i == 5)
				{
					abnormal = 1;
					startQuestTimer("float_effect", 4000, weakScarlet, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("skill03"))
		{
			if (strongScarlet != null && !strongScarlet.isDead() && secondMorph == 1 && thirdMorph == 1 && onMorph == 0)
			{
				int i = 0;
				if (abnormal == 0)
				{
					i = Rnd.get(6, 10);
				}
				else
				{
					i = Rnd.get(6, 9);
				}
				
				final L2Skill skill = SkillTable.getInstance().getInfo(skills[i][0], skills[i][1]);
				if (skill != null)
				{
					strongScarlet.stopMove(null);
					// strongScarlet.setIsCastingNow(true);
					strongScarlet.doCast(skill);
				}
				startQuestTimer("skill03", skills[i][2] + 5000 + Rnd.get(10000), npc, null);
				
				if (i == 10)
				{
					abnormal = 1;
					startQuestTimer("float_effect", 3000, npc, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("float_effect"))
		{
			if (npc.isCastingNow())
			{
				startQuestTimer("float_effect", 500, npc, null);
			}
			else
			{
				for (final L2Character cha : zone.getCharactersInside().values())
				{
					if (cha instanceof L2PcInstance)
					{
						if (cha.getFirstEffect(5016) != null)
						{
							cha.abortAttack();
							cha.abortCast();
							cha.disableAllSkills();
							cha.stopMove(null);
							cha.setIsImobilised(true);
							cha.setIsParalyzed(true);
							cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							cha.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLOATING_ROOT);
						}
					}
				}
				startQuestTimer("stop_effect", 25000, npc, null);
			}
		}
		else if (event.equalsIgnoreCase("action"))
		{
			zone.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
		}
		else if (event.equalsIgnoreCase("bomber"))
		{
			bomber = 0;
		}
		else if (event.equalsIgnoreCase("room_final"))
		{
			zone.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), "Exceeded his time limit, challenge failed!"));
			zone.oustAllPlayers();
			
			cancelQuestTimer("waiting", npc, null);
			cancelQuestTimer("frintezza_despawn", null, null);
			startQuestTimer("clean", 1000, npc, null);
			startQuestTimer("close", 1000, npc, null);
			startQuestTimer("room1_del", 1000, npc, null);
			startQuestTimer("room2_del", 1000, npc, null);
			
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
		}
		else if (event.equalsIgnoreCase("frintezza_despawn"))
		{
			temp = (System.currentTimeMillis() - lastAction);
			if (temp > 900000)
			{
				zone.oustAllPlayers();
				
				cancelQuestTimer("waiting", npc, null);
				// cancelQuestTimer("loc_check");
				cancelQuestTimer("room_final", null, null);
				cancelQuestTimer("spawn_minion", npc, null);
				startQuestTimer("clean", 1000, npc, null);
				startQuestTimer("close", 1000, npc, null);
				startQuestTimer("attack_stop", 1000, npc, null);
				startQuestTimer("room1_del", 1000, npc, null);
				startQuestTimer("room2_del", 1000, npc, null);
				startQuestTimer("room3_del", 1000, npc, null);
				startQuestTimer("minions_despawn", 1000, npc, null);
				
				GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
				
				cancelQuestTimer("frintezza_despawn", null, null);
			}
		}
		else if (event.equalsIgnoreCase("minions_despawn"))
		{
			synchronized (minions)
			{
				for (int i = 0; i < minions.size(); i++)
				{
					final L2Attackable mob = minions.get(i);
					if (mob != null)
					{
						mob.decayMe();
					}
				}
				minions.clear();
			}
			
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			if (npc != null && !npc.isDead() && frintezza != null && !frintezza.isDead())
			{
				final L2NpcInstance mob = addSpawn(npc.getNpcId() + 2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
				// mob.setIsRaidMinion(true);
				synchronized (minions)
				{
					minions.add((L2Attackable) mob);
				}
				
				startQuestTimer("action", 200, mob, null);
				startQuestTimer("spawn_minion", 18000, npc, null);
			}
		}
		else if (event.equalsIgnoreCase("spawn_cubes"))
		{
			addSpawn(CUBE, 174232, -88020, -5114, 16384, false, 900000);
		}
		else if (event.equalsIgnoreCase("frintezza_unlock"))
		{
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
		}
		else if (event.equalsIgnoreCase("remove_players"))
		{
			zone.oustAllPlayers();
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(final L2NpcInstance npc, final L2PcInstance player)
	{
		if (npc.getNpcId() == CUBE)
		{
			final int x = 150037 + Rnd.get(500);
			final int y = -57720 + Rnd.get(500);
			player.teleToLocation(x, y, -2976);
			return null;
		}
		
		String htmltext = "";
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		
		if (status == DEAD)
		{
			htmltext = "<html><body>There is nothing beyond the Magic Force Field. Come back later.<br>(You may not enter because Frintezza is not inside the Imperial Tomb.)</body></html>";
		}
		else if (status == DORMANT)
		{
			boolean party_check_success = true;
			
			if (!Config.BYPASS_FRINTEZZA_PARTIES_CHECK)
			{
				
				if ((!player.isInParty() || !player.getParty().isLeader(player)) || (player.getParty().getCommandChannel() == null) || (player.getParty().getCommandChannel().getChannelLeader() != player))
				{
					htmltext = "<html><body>No reaction. Contact must be initiated by the Command Channel Leader.</body></html>";
					party_check_success = false;
				}
				else if (player.getParty().getCommandChannel().getPartys().size() < Config.FRINTEZZA_MIN_PARTIES || player.getParty().getCommandChannel().getPartys().size() > Config.FRINTEZZA_MAX_PARTIES)
				{
					htmltext = "<html><body>Your command channel needs to have at least " + Config.FRINTEZZA_MIN_PARTIES + " parties and a maximum of " + Config.FRINTEZZA_MAX_PARTIES + ".</body></html>";
					party_check_success = false;
				}
				
			}
			
			if (party_check_success)
			{
				
				if (player.getInventory().getItemByItemId(8073) == null)
				{
					htmltext = "<html><body>You dont have required item.</body></html>";
				}
				else
				{
					player.destroyItemByItemId("Quest", 8073, 1, player, true);
					GrandBossManager.getInstance().setBossStatus(FRINTEZZA, WAITING);
					
					startQuestTimer("close", 0, npc, null);
					startQuestTimer("room1_spawn", 5000, npc, null);
					startQuestTimer("room_final", 2100000, null, null);
					startQuestTimer("frintezza_despawn", 60000, null, null, true);
					
					lastAction = System.currentTimeMillis();
					
					if (Config.BYPASS_FRINTEZZA_PARTIES_CHECK)
					{
						
						if (player.getParty() != null)
						{
							
							final L2CommandChannel CC = player.getParty().getCommandChannel();
							
							if (CC != null)
							{ // teleport all parties into CC
								
								for (final L2Party party : CC.getPartys())
								{
									if (party == null)
									{
										continue;
									}
									
									synchronized (playersInside)
									{
										
										for (final L2PcInstance member : party.getPartyMembers())
										{
											if (member == null || member.getLevel() < 74)
											{
												continue;
											}
											if (!member.isInsideRadius(npc, 700, false, false))
											{
												continue;
											}
											if (playersInside.size() > 45)
											{
												member.sendMessage("The number of challenges have been full, so can not enter.");
												break;
											}
											playersInside.add(member);
											zone.allowPlayerEntry(member, 300);
											member.teleToLocation(getXFix(invadeLoc[locCycle][0]) + Rnd.get(50), getYFix(invadeLoc[locCycle][1]) + Rnd.get(50), getZFix(invadeLoc[locCycle][2]));
										}
										if (playersInside.size() > 45)
										{
											break;
										}
										
									}
									
									locCycle++;
									if (locCycle >= 6)
									{
										locCycle = 1;
									}
								}
								
							}
							else
							{ // teleport just actual party
								
								final L2Party party = player.getParty();
								
								for (final L2PcInstance member : party.getPartyMembers())
								{
									if (member == null || member.getLevel() < 74)
									{
										continue;
									}
									if (!member.isInsideRadius(npc, 700, false, false))
									{
										continue;
									}
									
									synchronized (playersInside)
									{
										if (playersInside.size() > 45)
										{
											member.sendMessage("The number of challenges have been full, so can not enter.");
											break;
										}
										playersInside.add(member);
									}
									
									zone.allowPlayerEntry(member, 300);
									member.teleToLocation(getXFix(invadeLoc[locCycle][0]) + Rnd.get(50), getYFix(invadeLoc[locCycle][1]) + Rnd.get(50), getZFix(invadeLoc[locCycle][2]));
								}
								
								locCycle++;
								if (locCycle >= 6)
								{
									locCycle = 1;
								}
								
							}
							
						}
						else
						{ // teleport just player
							
							if (player.isInsideRadius(npc, 700, false, false))
							{
								
								synchronized (playersInside)
								{
									playersInside.add(player);
									
								}
								zone.allowPlayerEntry(player, 300);
								player.teleToLocation(getXFix(invadeLoc[locCycle][0]) + Rnd.get(50), getYFix(invadeLoc[locCycle][1]) + Rnd.get(50), getZFix(invadeLoc[locCycle][2]));
								
							}
							
						}
						
					}
					else
					{
						
						final L2CommandChannel CC = player.getParty().getCommandChannel();
						
						for (final L2Party party : CC.getPartys())
						{
							if (party == null)
							{
								continue;
							}
							
							synchronized (playersInside)
							{
								for (final L2PcInstance member : party.getPartyMembers())
								{
									if (member == null || member.getLevel() < 74)
									{
										continue;
									}
									if (!member.isInsideRadius(npc, 700, false, false))
									{
										continue;
									}
									if (playersInside.size() > 45)
									{
										member.sendMessage("The number of challenges have been full, so can not enter.");
										break;
									}
									playersInside.add(member);
									zone.allowPlayerEntry(member, 300);
									member.teleToLocation(getXFix(invadeLoc[locCycle][0]) + Rnd.get(50), getYFix(invadeLoc[locCycle][1]) + Rnd.get(50), getZFix(invadeLoc[locCycle][2]));
								}
								if (playersInside.size() > 45)
								{
									break;
								}
								
							}
							
							locCycle++;
							if (locCycle >= 6)
							{
								locCycle = 1;
							}
						}
						
					}
					
				}
				
			}
			
		}
		else
		{
			htmltext = "<html><body>Someone else is already inside the Magic Force Field. Try again later.</body></html>";
		}
		
		return htmltext;
	}
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		lastAction = System.currentTimeMillis();
		if (npc.getNpcId() == FRINTEZZA)
		{
			npc.setCurrentHpMp(npc.getMaxHp(), 0);
			return null;
		}
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		
		if (npc.getNpcId() == SCARLET1 && secondMorph == 0 && thirdMorph == 0 && onMorph == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.75 && status == FIGHTING)
		{
			startQuestTimer("attack_stop", 0, frintezza, null);
			
			secondMorph = 1;
			onMorph = 1;
			
			startQuestTimer("stop_pc", 1000, npc, null);
			startQuestTimer("stop_npc", 1000, npc, null);
			startQuestTimer("morph_01", 1100, npc, null);
		}
		else if (npc.getNpcId() == SCARLET1 && secondMorph == 1 && thirdMorph == 0 && onMorph == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.5 && status == FIGHTING)
		{
			startQuestTimer("attack_stop", 0, frintezza, null);
			
			thirdMorph = 1;
			onMorph = 1;
			
			startQuestTimer("stop_pc", 2000, npc, null);
			startQuestTimer("stop_npc", 2000, npc, null);
			startQuestTimer("morph_05a", 2000, npc, null);
			startQuestTimer("morph_05", 2100, npc, null);
		}
		else if (npc.getNpcId() == SCARLET2 && secondMorph == 1 && thirdMorph == 1 && onCheck == 0 && damage >= npc.getCurrentHp() && status == FIGHTING)
		{
			onCheck = 1;
			startQuestTimer("check_hp", 0, npc, null);
		}
		else if ((npc.getNpcId() == 29050 || npc.getNpcId() == 29051) && bomber == 0)
		{
			if (npc.getCurrentHp() < npc.getMaxHp() * 0.1)
			{
				if (Rnd.get(100) < 30)
				{
					bomber = 1;
					startQuestTimer("bomber", 3000, npc, null);
					
					final L2Skill skill = SkillTable.getInstance().getInfo(5011, 1);
					if (skill != null)
					{
						// npc.setIsCastingNow(true);
						npc.doCast(skill);
					}
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		final Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		
		if (npc.getNpcId() == FRINTEZZA)
		{
			return null;
		}
		else if (npc.getNpcId() == SCARLET2 && onCheck == 0 && status == FIGHTING)
		{
			onCheck = 1;
			startQuestTimer("stop_pc", 0, npc, null);
			startQuestTimer("stop_npc", 0, npc, null);
			startQuestTimer("morph_16", 0, npc, null);
		}
		else if (npc.getNpcId() == SCARLET2 && onCheck == 1 && status == FIGHTING)
		{
			if (!npc.getSpawn().isCustomRaidBoss())
			{
				
				// cancelQuestTimer("loc_check");
				cancelQuestTimer("spawn_minion", npc, null);
				cancelQuestTimer("frintezza_despawn", null, null);
				startQuestTimer("clean", 30000, npc, null);
				startQuestTimer("close", 30000, npc, null);
				startQuestTimer("room3_del", 60000, npc, null);
				startQuestTimer("minions_despawn", 60000, npc, null);
				startQuestTimer("remove_players", 900000, npc, null);
				
				GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DEAD);
				final long respawnTime = (long) (Config.FRINTEZZA_RESP_FIRST + Rnd.get(Config.FRINTEZZA_RESP_SECOND)) * 3600000;
				startQuestTimer("frintezza_unlock", respawnTime, npc, null);
				// also save the respawn time so that the info is maintained past reboots
				final StatsSet info = GrandBossManager.getInstance().getStatsSet(FRINTEZZA);
				info.set("respawn_time", System.currentTimeMillis() + respawnTime);
				GrandBossManager.getInstance().setStatsSet(FRINTEZZA, info);
				
			}
			
		}
		else if (npc.getNpcId() == 18328)
		{
			killHallAlarmDevice++;
			if (killHallAlarmDevice == 3) // open walls
			{
				for (int i = 25150051; i <= 25150058; i++)
				{
					DoorTable.getInstance().getDoor(i).openMe();
				}
			}
			if (killHallAlarmDevice == 4)
			{
				startQuestTimer("room1_del", 100, npc, null);
				startQuestTimer("room2_spawn", 100, npc, null);
				
				DoorTable.getInstance().getDoor(25150042).openMe();
				DoorTable.getInstance().getDoor(25150043).openMe();
			}
			/*
			 * killHallAlarmDevice++; if (_KillHallAlarmDevice == 4) { startQuestTimer("room1_del", 100, npc, null); startQuestTimer("room2_spawn", 100, npc, null); DoorTable.getInstance().getDoor(17130042).openMe(); DoorTable.getInstance().getDoor(17130043).openMe(); //
			 * DoorTable.getInstance().getDoor(17130045).openMe(); // DoorTable.getInstance().getDoor(17130046).openMe(); for (int i = 17130051; i <= 17130058; i++) DoorTable.getInstance().getDoor(i).openMe(); }
			 */
		}
		else if (npc.getNpcId() == 18339)
		{
			killDarkChoirPlayer++;
			if (killDarkChoirPlayer == 2)
			{
				DoorTable.getInstance().getDoor(25150042).closeMe();
				DoorTable.getInstance().getDoor(25150043).closeMe();
				DoorTable.getInstance().getDoor(25150045).closeMe();
				DoorTable.getInstance().getDoor(25150046).closeMe();
				int outside = 0;
				synchronized (playersInside)
				{
					for (final L2PcInstance room2_pc : playersInside)
					{
						if (zone.isInsideZone(room2_pc) && room2_pc.getY() > -86130)
						{
							outside++;
						}
					}
				}
				
				if (outside == 0)
				{
					startQuestTimer("room2_del", 100, npc, null);
					startQuestTimer("waiting", 180000, npc, null);
					cancelQuestTimer("room_final", null, null);
				}
				else
				{
					for (int i = 25150061; i <= 25150070; i++)
					{
						DoorTable.getInstance().getDoor(i).openMe();
					}
					
					startQuestTimer("room2_spawn2", 1000, npc, null);
				}
			}
			
		}
		else if (npc.getNpcId() == 18334)
		{
			killDarkChoirCaptain++;
			if (killDarkChoirCaptain == 8)
			{
				startQuestTimer("room2_del", 100, npc, null);
				
				DoorTable.getInstance().getDoor(25150045).openMe();
				DoorTable.getInstance().getDoor(25150046).openMe();
				
				startQuestTimer("waiting", 180000, npc, null);
				cancelQuestTimer("room_final", null, null);
			}
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	/**
	 * Override spawn method to correct spawn cords after lair move<BR>
	 * TODO: Update cords in script, im just lazy :)
	 */
	@Override
	public L2NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int heading, final boolean randomOffset, final int despawnDelay)
	{
		// cords fix:
		return super.addSpawn(npcId, getXFix(x), getYFix(y), getZFix(z), heading, randomOffset, despawnDelay);
	}
	
	public int getXFix(final int x)
	{
		return x/* - 262016 */;
	}
	
	public int getYFix(final int y)
	{
		return y/* - 65278 */;
	}
	
	public int getZFix(final int z)
	{
		return z/* - 4065 */;
	}
	
	public int reverseXFix(final int x)
	{
		return x/* + 262016 */;
	}
	
	public int reverseYFix(final int y)
	{
		return y/* + 65278 */;
	}
	
	public int reverseZFix(final int z)
	{
		return z/* + 4065 */;
	}
	
	@Override
	public void run()
	{
	}
}