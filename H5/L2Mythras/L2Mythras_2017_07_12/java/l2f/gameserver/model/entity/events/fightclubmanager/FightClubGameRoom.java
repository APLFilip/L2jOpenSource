package l2f.gameserver.model.entity.events.fightclubmanager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2f.commons.util.Rnd;
import l2f.gameserver.data.xml.holder.FightClubMapHolder;
import l2f.gameserver.listener.actor.player.OnPlayerExitListener;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.base.ClassId;
import l2f.gameserver.model.entity.events.fightclubmanager.FightClubEventManager.CLASSES;
import l2f.gameserver.model.entity.events.impl.AbstractFightClub;
import l2f.gameserver.utils.Util;

public class FightClubGameRoom
{

	private class LeaveListener implements OnPlayerExitListener
	{
		@Override
		public void onPlayerExit(Player player)
		{
			leaveRoom(player);
		}
	}

	private final FightClubMap _map;
	private final AbstractFightClub _event;
	private final int _roomMaxPlayers;
	private final int _teamsCount;

	//Players in FFA:
	private final Map<Integer, Player> _players = new ConcurrentHashMap<>();

	//Leave listener
	private final LeaveListener _leaveListener = new LeaveListener();

	public FightClubGameRoom(AbstractFightClub event)
	{
		_event = event;

		String eventName = Util.getChangedEventName(event);
		_map = Rnd.get(FightClubMapHolder.getInstance().getMapsForEvent(eventName));
		_roomMaxPlayers = _map.getMaxAllPlayers();
		if (event.isTeamed())
			_teamsCount = Rnd.get(_map.getTeamCount());
		else
			_teamsCount = 0;
	}

	public void leaveRoom(Player player)
	{
		_players.remove(player.getObjectId());

		player.removeListener(_leaveListener);
	}

	public int getMaxPlayers()
	{
		return _roomMaxPlayers;
	}

	public int getTeamsCount()
	{
		return _teamsCount;
	}

	public int getSlotsLeft()
	{
		return getMaxPlayers() - getPlayersCount();
	}

	public AbstractFightClub getGame()
	{
		return _event;
	}

	public int getPlayersCount()
	{
		return _players.size();
	}

	public FightClubMap getMap()
	{
		return _map;
	}

	/**
	 * !!! @Don't @change @that @list!!!
	 * @return
	 */
	public Collection<Player> getAllPlayers()
	{
		return _players.values();
	}

	synchronized void addAlonePlayer(Player player)
	{
		player.setFightClubGameRoom(this);
		addPlayerToTeam(player);
	}

	public boolean containsPlayer(Player player)
	{
		return _players.containsKey(player.getObjectId());
	}

	/**
	 * Adding players to team and party
	 * @param player
	 */
	private void addPlayerToTeam(Player player)
	{
		_players.put(player.getObjectId(), player);
	}

	public static CLASSES getPlayerClassGroup(Player player)
	{
		CLASSES classType = null;
		for (CLASSES iClassType : CLASSES.values())
			for (ClassId id : iClassType.getClasses())
				if (id == player.getClassId())
					classType = iClassType;
		return classType;
	}
}
