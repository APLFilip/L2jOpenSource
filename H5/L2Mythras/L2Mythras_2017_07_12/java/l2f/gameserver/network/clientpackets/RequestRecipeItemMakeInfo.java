package l2f.gameserver.network.clientpackets;

import l2f.gameserver.data.xml.holder.RecipeHolder;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Recipe;
import l2f.gameserver.network.serverpackets.RecipeItemMakeInfo;

public class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private int _id;

	/**
	 * packet type id 0xB7
	 * format:		cd
	 */
	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		Recipe recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_id);
		if (recipeList == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		sendPacket(new RecipeItemMakeInfo(activeChar, recipeList, 0xffffffff));
	}
}