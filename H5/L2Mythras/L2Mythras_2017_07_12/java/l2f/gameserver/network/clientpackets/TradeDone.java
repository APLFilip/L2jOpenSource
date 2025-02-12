package l2f.gameserver.network.clientpackets;

import java.util.List;

import l2f.commons.math.SafeMath;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Request;
import l2f.gameserver.model.Request.L2RequestType;
import l2f.gameserver.model.entity.CCPHelpers.itemLogs.ItemActionType;
import l2f.gameserver.model.entity.CCPHelpers.itemLogs.ItemLogHandler;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.model.items.TradeItem;
import l2f.gameserver.network.serverpackets.SendTradeDone;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.TradePressOtherOk;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.utils.Log;


public class TradeDone extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		Player parthner1 = getClient().getActiveChar();
		if (parthner1 == null)
			return;
		Request request = parthner1.getRequest();
		if (request == null || !request.isTypeOf(L2RequestType.TRADE))
		{
			parthner1.sendActionFailed();
			return;
		}

		if (!request.isInProgress())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		if (parthner1.isOutOfControl())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		Player parthner2 = request.getOtherPlayer(parthner1);
		if (parthner2 == null)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			parthner1.sendActionFailed();
			return;
		}

		if (parthner2.getRequest() != request)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		if (_response == 0)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner2.sendPacket(SendTradeDone.FAIL, new SystemMessage2(SystemMsg.C1_HAS_CANCELLED_THE_TRADE).addString(parthner1.getName()));
			return;
		}

		if (!parthner1.isInRangeZ(parthner2, Creature.INTERACTION_DISTANCE))
		{
			parthner1.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return;
		}

		// first party accepted the trade
		// notify clients that "OK" button has been pressed.
		request.confirm(parthner1);
		parthner2.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_CONFIRMED_THE_TRADE).addString(parthner1.getName()), TradePressOtherOk.STATIC);

		if (!request.isConfirmed(parthner2)) // Check for dual confirmation
		{
			parthner1.sendActionFailed();
			return;
		}

		List<TradeItem> tradeList1 = parthner1.getTradeList();
		List<TradeItem> tradeList2 = parthner2.getTradeList();
		int slots = 0;
		long weight = 0;
		boolean success = false;

		parthner1.getInventory().writeLock();
		parthner2.getInventory().writeLock();
		try
		{
			slots = 0;
			weight = 0;

			for (TradeItem ti : tradeList1)
			{
				ItemInstance item = parthner1.getInventory().getItemByObjectId(ti.getObjectId());
				if (item == null || item.getCount() < ti.getCount() || !item.canBeTraded(parthner1))
					return;

				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(ti.getCount(), ti.getItem().getWeight()));
				if (!ti.getItem().isStackable() || parthner2.getInventory().getItemByItemId(ti.getItemId()) == null)
					slots++;
			}

			if (!parthner2.getInventory().validateWeight(weight))
			{
				parthner2.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}

			if (!parthner2.getInventory().validateCapacity(slots))
			{
				parthner2.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			slots = 0;
			weight = 0;

			for (TradeItem ti : tradeList2)
			{
				ItemInstance item = parthner2.getInventory().getItemByObjectId(ti.getObjectId());
				if (item == null || item.getCount() < ti.getCount() || !item.canBeTraded(parthner2))
					return;

				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(ti.getCount(), ti.getItem().getWeight()));
				if (!ti.getItem().isStackable() || parthner1.getInventory().getItemByItemId(ti.getItemId()) == null)
					slots++;
			}

			if (!parthner1.getInventory().validateWeight(weight))
			{
				parthner1.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}

			if (!parthner1.getInventory().validateCapacity(slots))
			{
				parthner1.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			Log.LogMsgToItems(parthner1.toString() + " Trade With " + parthner2.toString());

			for (TradeItem ti : tradeList1)
			{
				ItemInstance item = parthner1.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount(), "Trade");
				parthner2.getInventory().addItem(item, "Trade");
			}

			for (TradeItem ti : tradeList2)
			{
				ItemInstance item = parthner2.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount(), "Trade");
				parthner1.getInventory().addItem(item, "Trade");
			}

			parthner1.sendPacket(SystemMsg.YOUR_TRADE_WAS_SUCCESSFUL);
			parthner2.sendPacket(SystemMsg.YOUR_TRADE_WAS_SUCCESSFUL);

			ItemLogHandler.getInstance().addLog(parthner1, parthner2, tradeList2, tradeList1, ItemActionType.TRADE);

			success = true;
		}
		finally
		{
			parthner2.getInventory().writeUnlock();
			parthner1.getInventory().writeUnlock();

			request.done();

			parthner1.sendPacket(success ? SendTradeDone.SUCCESS : SendTradeDone.FAIL);
			parthner2.sendPacket(success ? SendTradeDone.SUCCESS : SendTradeDone.FAIL);
		}
	}
}