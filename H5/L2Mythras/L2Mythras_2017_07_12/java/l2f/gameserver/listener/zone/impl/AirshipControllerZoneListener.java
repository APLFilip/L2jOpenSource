package l2f.gameserver.listener.zone.impl;

import l2f.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Zone;
import l2f.gameserver.model.entity.boat.ClanAirShip;
import l2f.gameserver.model.instances.ClanAirShipControllerInstance;

public class AirshipControllerZoneListener implements OnZoneEnterLeaveListener
{
	private ClanAirShipControllerInstance _controllerInstance;

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if (_controllerInstance == null && actor instanceof ClanAirShipControllerInstance)
			_controllerInstance = (ClanAirShipControllerInstance) actor;
		else if (actor.isClanAirShip())
			_controllerInstance.setDockedShip((ClanAirShip) actor);
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{
		if (actor.isClanAirShip())
			_controllerInstance.setDockedShip(null);
	}
}
