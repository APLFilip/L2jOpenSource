package ai.hellbound;

import l2f.commons.threading.RunnableImpl;
import l2f.commons.util.Rnd;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.ai.Fighter;
import l2f.gameserver.data.xml.holder.NpcHolder;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.SimpleSpawner;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.utils.Location;
import l2f.gameserver.utils.NpcUtils;

/**
 * Darion Faithful Servant 8го этажа Tully Workshop
 * @автор pchayka, доработка VAVAN
 */
public class DarionFaithfulServant8Floor extends Fighter
{
	private static final int MysteriousAgent = 32372;

	public DarionFaithfulServant8Floor(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		
		if(Rnd.chance(15) && actor.isInZone("[tully8_room1]"))
			NpcUtils.spawnSingle(MysteriousAgent, new Location(-13312, 279172, -10492, -20300), 600000L);
		if(Rnd.chance(15) && actor.isInZone("[tully8_room2]"))
			NpcUtils.spawnSingle(MysteriousAgent, new Location(-11696, 280208, -10492, 13244), 600000L);
		if(Rnd.chance(15) && actor.isInZone("[tully8_room3]"))
			NpcUtils.spawnSingle(MysteriousAgent, new Location(-13008, 280496, -10492, 27480), 600000L);
		if(Rnd.chance(15) && actor.isInZone("[tully8_room4]"))
			NpcUtils.spawnSingle(MysteriousAgent, new Location(-11984, 278880, -10492, -4472), 600000L);
		super.onEvtDead(killer);
	}
}