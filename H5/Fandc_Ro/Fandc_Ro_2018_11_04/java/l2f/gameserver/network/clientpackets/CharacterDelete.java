package l2f.gameserver.network.clientpackets;


import l2f.gameserver.database.mysql;
import l2f.gameserver.model.pledge.Clan;
import l2f.gameserver.network.GameClient;
import l2f.gameserver.network.serverpackets.CharacterDeleteFail;
import l2f.gameserver.network.serverpackets.CharacterDeleteSuccess;
import l2f.gameserver.network.serverpackets.CharacterSelectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterDelete extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterDelete.class);

	// cd
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		int clan = clanStatus();
		int online = onlineStatus();
		if (clan > 0 || online > 0)
		{
			if (clan == 2)
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
			else if (clan == 1)
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
			else if (online > 0)
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_DELETION_FAILED));
			return;
		}

		GameClient client = getClient();
		client.markToDeleteChar(_charSlot);
		sendPacket(new CharacterDeleteSuccess());

		CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}

	private int clanStatus()
	{
		int obj = getClient().getObjectIdForSlot(_charSlot);
		if (obj == -1)
			return 0;
		if (mysql.simple_get_int("clanid", "characters", "obj_Id=" + obj) > 0)
		{
			if (mysql.simple_get_int("leader_id", "clan_subpledges", "leader_id=" + obj + " AND type = " + Clan.SUBUNIT_MAIN_CLAN) > 0)
				return 2;
			return 1;
		}
		return 0;
	}

	private int onlineStatus()
	{
		int obj = getClient().getObjectIdForSlot(_charSlot);
		if (obj == -1)
			return 0;
		if (mysql.simple_get_int("online", "characters", "obj_Id=" + obj) > 0)
			return 1;
		return 0;
	}
}