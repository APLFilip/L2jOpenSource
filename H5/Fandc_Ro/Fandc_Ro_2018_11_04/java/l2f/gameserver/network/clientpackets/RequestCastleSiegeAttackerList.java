package l2f.gameserver.network.clientpackets;

import l2f.gameserver.data.xml.holder.ResidenceHolder;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.residence.Residence;
import l2f.gameserver.network.serverpackets.CastleSiegeAttackerList;

public class RequestCastleSiegeAttackerList extends L2GameClientPacket
{
	private int _unitId;

	@Override
	protected void readImpl()
	{
		_unitId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if (player == null)
			return;

		Residence residence = ResidenceHolder.getInstance().getResidence(_unitId);
		if (residence != null)
			sendPacket(new CastleSiegeAttackerList(residence));
	}
}