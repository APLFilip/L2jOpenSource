package l2s.gameserver.model.entity.events.impl.fightclub;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.fightclubmanager.FightClubPlayer;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub.EVENT_STATE;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

public class LuckyCreaturesEvent extends AbstractFightClub
{
	private class RespawnThread extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(getState() == AbstractFightClub.EVENT_STATE.OVER || getState() == AbstractFightClub.EVENT_STATE.NOT_ACTIVE)
				return;

			long current = System.currentTimeMillis();
			List<Long> toRemove = new ArrayList<Long>();
			for(Long deathTime : _deathTimes)
			{
				if(deathTime.longValue() < current)
				{
					LuckyCreaturesEvent.this.spawnMonster();
					toRemove.add(deathTime);
				}
			}

			for(Long l : toRemove)
				_deathTimes.remove(l);

			ThreadPoolManager.getInstance().schedule(this, 10000L);
		}
	}

	private final int _monstersCount;
	private final int[] _monsterTemplates;
	private final int _respawnSeconds;
	private List<NpcInstance> _monsters = new CopyOnWriteArrayList<NpcInstance>();
	private List<Long> _deathTimes = new CopyOnWriteArrayList<Long>();

	public LuckyCreaturesEvent(MultiValueSet<String> set)
	{
		super(set);
		_monstersCount = set.getInteger("monstersCount", 1);
		_respawnSeconds = set.getInteger("monstersRespawn", 60);
		_monsterTemplates = parseExcludedSkills(set.getString("monsterTemplates", "14200"));
	}

	public void onKilled(Creature actor, Creature victim)
	{
		if(victim.isMonster() && actor != null && actor.isPlayable())
		{
			FightClubPlayer fActor = getFightClubPlayer(actor.getPlayer());
			fActor.increaseKills(true);
			updatePlayerScore(fActor);
			actor.getPlayer().sendUserInfo();

			_deathTimes.add(Long.valueOf(System.currentTimeMillis() + _respawnSeconds * 1000));
			_monsters.remove(victim);
		}

		super.onKilled(actor, victim);
	}

	public void startEvent()
	{
		super.startEvent();

		ThreadPoolManager.getInstance().schedule(new RespawnThread(), 30000L);

		for(Zone zone : getReflection().getZones())
			zone.setType(Zone.ZoneType.peace_zone);
	}

	public void startRound()
	{
		super.startRound();

		System.out.println("spawning " + _monstersCount + " monsters");
		for(int i = 0; i < _monstersCount; i++)
			spawnMonster();
	}

	@Override
	public void stopEvent(boolean force)
	{
		super.stopEvent(force);

		for(NpcInstance npc : _monsters)
		{
			if(npc != null)
				npc.doDecay();
		}
		_monsters.clear();
	}

	private void spawnMonster()
	{
		Zone zone = getReflection().getZones().iterator().next();
		Location loc = zone.getTerritory().getRandomLoc(getReflection().getGeoIndex(), false);

		int template = Rnd.get(_monsterTemplates);
		SimpleSpawner spawn = new SimpleSpawner(template);
		spawn.setLoc(loc);
		spawn.setAmount(1);
		spawn.setRespawnDelay(0);
		spawn.setReflection(getReflection());
		NpcInstance monster = spawn.spawnOne();
		spawn.stopRespawn();

		_monsters.add(monster);
	}

	protected boolean inScreenShowBeScoreNotKills()
	{
		return false;
	}

	public boolean isFriend(Creature c1, Creature c2)
	{
		return !c1.isMonster() && !c2.isMonster();
	}

	public String getVisibleTitle(Player player, String currentTitle, boolean toMe)
	{
		FightClubPlayer fPlayer = getFightClubPlayer(player);

		if(fPlayer == null)
			return currentTitle;

		return "Kills: " + fPlayer.getKills(true);
	}
}