package ai.PaganTemplete;

import l2f.gameserver.ai.DefaultAI;
import l2f.gameserver.model.instances.DoorInstance;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.network.serverpackets.components.NpcString;
import l2f.gameserver.utils.ReflectionUtils;

/**
 * @author Grivesky
  * - AI for NPCs Altar Gatekeeper (32051).
  * - Controllers doors.
  * - AI is tested and works.
 */
public class AltarGatekeeper extends DefaultAI
{
	private boolean _firstTime = true;

	public AltarGatekeeper(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if (actor == null)
			return true;

		// The doors to the balcony
		DoorInstance door1 = ReflectionUtils.getDoor(19160014);
		DoorInstance door2 = ReflectionUtils.getDoor(19160015);
		// The doors to the altar
		DoorInstance door3 = ReflectionUtils.getDoor(19160016);
		DoorInstance door4 = ReflectionUtils.getDoor(19160017);

		// Krichim 4 times (as actor spawn in 4 places) as Offe of the doors opened
		if ( !door1.isOpen() && !door2.isOpen() && door3.isOpen() && door4.isOpen() && _firstTime)
		{
			_firstTime = false;
			Functions.npcSay(actor, NpcString.THE_DOOR_TO_THE_3RD_FLOOR_OF_THE_ALTAR_IS_NOW_OPEN);
		}

		return true;
	}
}