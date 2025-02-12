package npc.model;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

import bosses.ValakasManager;

/**
 * @author pchayka
 */

public final class ValakasGatekeeperInstance extends NpcInstance
{
	private static final Location TELEPORT_POSITION1 = new Location(183831, -115457, -3296);

	public ValakasGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_passage"))
		{
			if(!ValakasManager.isEnableEnterToLair())
			{
				player.sendMessage("Valakas is now reborning and there's no way to enter the hall now.");
				return;
			}
			if(!ValakasManager.consumeRequiredItems(player))
			{
				player.sendMessage("In order to enter the Hall of Flames you should carry at least one Flotaing Stone");
				return;
			}
			player.teleToLocation(TELEPORT_POSITION1);
			return;
		}
		else if(command.equalsIgnoreCase("request_valakas"))
		{
			ValakasManager.enterTheLair(player);
			return;
		}
		else
			super.onBypassFeedback(player, command);
	}
}