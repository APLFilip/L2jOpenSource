package l2f.gameserver.handler.admincommands.impl;

import l2f.gameserver.handler.admincommands.IAdminCommandHandler;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.GameObject;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.model.base.TeamType;
import l2f.gameserver.model.entity.Hero;
import l2f.gameserver.network.serverpackets.components.SystemMsg;

import java.util.List;

public class AdminTeam implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_setteam
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		TeamType team = TeamType.NONE;
		int range = -1;
		if (wordList.length >= 2)
		{
			for (TeamType t : TeamType.values())
			{
				if (wordList[1].equalsIgnoreCase(t.name()))
					team = t;
			}
			if (wordList.length >= 3)
				range = Integer.parseInt(wordList[2]);
		}
		
		if (range > 0)
		{
			List<Player> aroundPlayer = World.getAroundPlayers(activeChar, range, 500);
			for (Player player : aroundPlayer)
				player.setTeam(team);
			activeChar.sendMessage("You have changed Team of " + aroundPlayer.size() + " Players!");
		}
		else
		{
			GameObject object = activeChar.getTarget();
			if (object == null || !object.isCreature())
			{
				activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				return false;
			}
	
			((Creature)object).setTeam(team);
			
			if (object.isPlayer())
			{
				Player pObject = object.getPlayer();
				if (pObject.isHero())
					if (team != TeamType.NONE)
						Hero.removeSkills(pObject);
					else
						Hero.addSkills(pObject);
				
				pObject.broadcastRelationChanged();
			}
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
