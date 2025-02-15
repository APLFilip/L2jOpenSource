package l2f.gameserver.network.clientpackets;

import l2f.gameserver.model.Player;

public class SetPrivateStoreMsgSell extends L2GameClientPacket
{
	private static final int MAX_MSG_LENGTH = 29;
	
	private String _storename;

	@Override
	protected void readImpl()
	{
		_storename = readS();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if ((_storename != null) && (_storename.length() > MAX_MSG_LENGTH))
			return;

		activeChar.setSellStoreName(_storename);
	}
}