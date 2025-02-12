package l2f.gameserver.network.clientpackets;

import l2f.gameserver.cache.CrestCache;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.ExPledgeCrestLarge;

public class RequestPledgeCrestLarge extends L2GameClientPacket
{
	// format: chd
	private int _crestId;

	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (_crestId == 0)
			return;
		byte[] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);
		if (data != null)
		{
			ExPledgeCrestLarge pcl = new ExPledgeCrestLarge(_crestId, data);
			sendPacket(pcl);
		}
	}
}