package l2f.gameserver.model.entity.events.objects;

import l2f.commons.threading.RunnableImpl;
import l2f.commons.util.Rnd;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.events.impl.KrateisCubeEvent;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.utils.Location;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;

public class KrateisCubePlayerObject implements Serializable, Comparable<KrateisCubePlayerObject>
{
	private class RessurectTask extends RunnableImpl
	{
		private int _seconds = 10;

		public RessurectTask()
		{
			//
		}

		@Override
		public void runImpl() throws Exception
		{
			_seconds -= 1;
			if (_seconds == 0)
			{
				KrateisCubeEvent cubeEvent = _player.getEvent(KrateisCubeEvent.class);
				List<Location> waitLocs = cubeEvent.getObjects(KrateisCubeEvent.WAIT_LOCS);

				_ressurectTask = null;

				_player.teleToLocation(Rnd.get(waitLocs));
				_player.doRevive();
			}
			else
			{
				_player.sendPacket(new SystemMessage2(SystemMsg.RESURRECTION_WILL_TAKE_PLACE_IN_THE_WAITING_ROOM_AFTER_S1_SECONDS).addInteger(_seconds));
				_ressurectTask = ThreadPoolManager.getInstance().schedule(this, 1000L);
			}
		}
	}

	private final Player _player;
	private final long _registrationTime;

	private boolean _showRank;
	private int _points;

	private Future<?> _ressurectTask;

	public KrateisCubePlayerObject(Player player)
	{
		_player = player;
		_registrationTime = System.currentTimeMillis();
	}

	public String getName()
	{
		return _player.getName();
	}

	public boolean isShowRank()
	{
		return _showRank;
	}

	public int getPoints()
	{
		return _points;
	}

	public void setPoints(int points)
	{
		_points = points;
	}

	public void setShowRank(boolean showRank)
	{
		_showRank = showRank;
	}

	public long getRegistrationTime()
	{
		return _registrationTime;
	}

	public int getObjectId()
	{
		return _player.getObjectId();
	}

	public Player getPlayer()
	{
		return _player;
	}

	public void startRessurectTask()
	{
		if (_ressurectTask != null)
			return;

		_ressurectTask = ThreadPoolManager.getInstance().schedule(new RessurectTask(), 1000L);
	}

	public void stopRessurectTask()
	{
		if (_ressurectTask != null)
		{
			_ressurectTask.cancel(false);
			_ressurectTask = null;
		}
	}

	@Override
	public int compareTo(KrateisCubePlayerObject o)
	{
		if (getPoints() == o.getPoints())
			return (int) ((getRegistrationTime() - o.getRegistrationTime()) / 1000L);
		return getPoints() - o.getPoints();
	}
}
