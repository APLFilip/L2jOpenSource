package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.datatables.csv.RecipeTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2RecipeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.2.5.2.5 $ $Date: 2005/04/06 16:13:51 $
 */

public class Recipes implements IItemHandler
{
	private final int[] ITEM_IDS;
	
	public Recipes()
	{
		final RecipeTable rc = RecipeTable.getInstance();
		ITEM_IDS = new int[rc.getRecipesCount()];
		for (int i = 0; i < rc.getRecipesCount(); i++)
		{
			ITEM_IDS[i] = rc.getRecipeList(i).getRecipeId();
		}
	}
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2RecipeList rp = RecipeTable.getInstance().getRecipeByItemId(item.getItemId());
		if (activeChar.hasRecipeList(rp.getId()))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.RECIPE_ALREADY_REGISTERED);
			activeChar.sendPacket(sm);
			sm = null;
		}
		else
		{
			if (rp.isDwarvenRecipe())
			{
				if (activeChar.hasDwarvenCraft())
				{
					if (rp.getLevel() > activeChar.getDwarvenCraft())
					{
						// can't add recipe, becouse create item level too low
						SystemMessage sm = new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
						activeChar.sendPacket(sm);
						sm = null;
					}
					else if (activeChar.getDwarvenRecipeBook().length >= activeChar.GetDwarfRecipeLimit())
					{
						// Up to $s1 recipes can be registered.
						SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
						sm.addNumber(activeChar.GetDwarfRecipeLimit());
						activeChar.sendPacket(sm);
						sm = null;
					}
					else
					{
						activeChar.registerDwarvenRecipeList(rp);
						activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADDED);
						sm.addString(item.getItemName());
						activeChar.sendPacket(sm);
						sm = null;
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
					activeChar.sendPacket(sm);
					sm = null;
				}
			}
			else
			{
				if (activeChar.hasCommonCraft())
				{
					if (rp.getLevel() > activeChar.getCommonCraft())
					{
						// can't add recipe, becouse create item level too low
						SystemMessage sm = new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
						activeChar.sendPacket(sm);
						sm = null;
					}
					else if (activeChar.getCommonRecipeBook().length >= activeChar.GetCommonRecipeLimit())
					{
						// Up to $s1 recipes can be registered.
						SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
						sm.addNumber(activeChar.GetCommonRecipeLimit());
						activeChar.sendPacket(sm);
						sm = null;
					}
					else
					{
						activeChar.registerCommonRecipeList(rp);
						activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADDED);
						sm.addString(item.getItemName());
						activeChar.sendPacket(sm);
						sm = null;
					}
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
					activeChar.sendPacket(sm);
					sm = null;
				}
			}
		}
		activeChar = null;
		rp = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
