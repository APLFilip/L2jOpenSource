package com.l2jfrozen.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ArmorSetsTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance.ItemLocation;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2EtcItem;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import main.EngineModsManager;

/**
 * This class manages inventory
 * @version $Revision: 1.13.2.9.2.12 $ $Date: 2005/03/29 23:15:15 $ rewritten 23.2.2006 by Advi
 */
public abstract class Inventory extends ItemContainer
{
	// protected static final Logger LOGGER = Logger.getLogger(Inventory.class);
	private static final String SELECT_ITEMS_BY_OWNER_ID = "SELECT owner_id, object_id, item_id, count, enchant_level, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2, mana_left FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY object_id DESC";
	
	public interface PaperdollListener
	{
		public void notifyEquiped(int slot, L2ItemInstance inst);
		
		public void notifyUnequiped(int slot, L2ItemInstance inst);
	}
	
	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_LEAR = 1;
	public static final int PAPERDOLL_REAR = 2;
	public static final int PAPERDOLL_NECK = 3;
	public static final int PAPERDOLL_LFINGER = 4;
	public static final int PAPERDOLL_RFINGER = 5;
	public static final int PAPERDOLL_HEAD = 6;
	public static final int PAPERDOLL_RHAND = 7;
	public static final int PAPERDOLL_LHAND = 8;
	public static final int PAPERDOLL_GLOVES = 9;
	public static final int PAPERDOLL_CHEST = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_BACK = 13;
	public static final int PAPERDOLL_LRHAND = 14;
	public static final int PAPERDOLL_FACE = 15;
	public static final int PAPERDOLL_HAIR = 16;
	public static final int PAPERDOLL_DHAIR = 17;
	public static final int PAPERDOLL_TOTALSLOTS = 18;
	
	// Speed percentage mods
	public static final double MAX_ARMOR_WEIGHT = 12000;
	
	private final L2ItemInstance[] paperdoll;
	private final List<PaperdollListener> paperdollListeners;
	
	// protected to be accessed from child classes only
	protected int totalWeight;
	
	// used to quickly check for using of items of special type
	private int wearedMask;
	
	final class FormalWearListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(final int slot, final L2ItemInstance item)
		{
			if (!(getOwner() != null && getOwner() instanceof L2PcInstance))
			{
				return;
			}
			
			L2PcInstance owner = (L2PcInstance) getOwner();
			if (item.getItemId() == 6408)
			{
				owner.setIsWearingFormalWear(false);
			}
			
			owner = null;
		}
		
		@Override
		public void notifyEquiped(final int slot, final L2ItemInstance item)
		{
			if (!(getOwner() != null && getOwner() instanceof L2PcInstance))
			{
				return;
			}
			
			L2PcInstance owner = (L2PcInstance) getOwner();
			
			// If player equip Formal Wear unequip weapons and abort cast/attack
			if (item.getItemId() == 6408)
			{
				owner.setIsWearingFormalWear(true);
				if (owner.isCastingNow())
				{
					owner.abortCast();
				}
				if (owner.isAttackingNow())
				{
					owner.abortAttack();
				}
				unEquipItemInSlot(PAPERDOLL_RHAND);
				unEquipItemInSlot(PAPERDOLL_LHAND);
				unEquipItemInSlot(PAPERDOLL_LRHAND);
			}
			else
			{
				if (!owner.isWearingFormalWear())
				{
					return;
				}
			}
			
			owner = null;
		}
	}
	
	/**
	 * Recorder of alterations in inventory
	 */
	public static final class ChangeRecorder implements PaperdollListener
	{
		private final Inventory inventory;
		private final List<L2ItemInstance> changed;
		
		/**
		 * Constructor of the ChangeRecorder
		 * @param inventory
		 */
		ChangeRecorder(final Inventory inventory)
		{
			this.inventory = inventory;
			changed = new ArrayList<>();
			this.inventory.addPaperdollListener(this);
		}
		
		/**
		 * Add alteration in inventory when item equiped
		 */
		@Override
		public void notifyEquiped(final int slot, final L2ItemInstance item)
		{
			if (!changed.contains(item))
			{
				changed.add(item);
			}
		}
		
		/**
		 * Add alteration in inventory when item unequiped
		 */
		@Override
		public void notifyUnequiped(final int slot, final L2ItemInstance item)
		{
			if (!changed.contains(item))
			{
				changed.add(item);
			}
		}
		
		/**
		 * Returns alterations in inventory
		 * @return L2ItemInstance[] : array of alterated items
		 */
		public L2ItemInstance[] getChangedItems()
		{
			return changed.toArray(new L2ItemInstance[changed.size()]);
		}
	}
	
	final class BowListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(final int slot, final L2ItemInstance item)
		{
			if (slot != PAPERDOLL_LRHAND)
			{
				return;
			}
			
			if (Config.ASSERT)
			{
				assert null == getPaperdollItem(PAPERDOLL_LRHAND);
			}
			
			if (item.getItemType() == L2WeaponType.BOW)
			{
				L2ItemInstance arrow = getPaperdollItem(PAPERDOLL_LHAND);
				
				if (arrow != null)
				{
					setPaperdollItem(PAPERDOLL_LHAND, null);
				}
				
				L2PcInstance player = null;
				L2Skill skill = null;
				if (item.getItemId() == 9140)
				{
					if (getOwner() instanceof L2PcInstance)
					{
						player = (L2PcInstance) getOwner();
						
						skill = SkillTable.getInstance().getInfo(3261, 1);
						player.removeSkill(skill);
						player.sendSkillList();
						
						skill = null;
					}
				}
				
				arrow = null;
			}
		}
		
		@Override
		public void notifyEquiped(final int slot, final L2ItemInstance item)
		{
			if (slot != PAPERDOLL_LRHAND)
			{
				return;
			}
			
			if (Config.ASSERT)
			{
				assert item == getPaperdollItem(PAPERDOLL_LRHAND);
			}
			
			if (item.getItemType() == L2WeaponType.BOW)
			{
				L2ItemInstance arrow = findArrowForBow(item.getItem());
				if (arrow != null)
				{
					setPaperdollItem(PAPERDOLL_LHAND, arrow);
					// InventoryUpdate();
				}
				
				L2PcInstance player = null;
				L2Skill skill = null;
				if (item.getItemId() == 9140)
				{
					if (getOwner() instanceof L2PcInstance)
					{
						player = (L2PcInstance) getOwner();
						
						skill = SkillTable.getInstance().getInfo(3261, 1);
						player.addSkill(skill, false);
						player.sendSkillList();
						
						skill = null;
					}
				}
				
				arrow = null;
			}
		}
	}
	
	final class StatsListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(final int slot, final L2ItemInstance item)
		{
			if (slot == PAPERDOLL_LRHAND)
			{
				return;
			}
			
			getOwner().removeStatsOwner(item);
		}
		
		@Override
		public void notifyEquiped(final int slot, final L2ItemInstance item)
		{
			if (slot == PAPERDOLL_LRHAND)
			{
				return;
			}
			
			getOwner().addStatFuncs(item.getStatFuncs(getOwner()));
		}
	}
	
	final class ItemPassiveSkillsListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(final int slot, final L2ItemInstance item)
		{
			L2PcInstance player;
			
			if (getOwner() instanceof L2PcInstance)
			{
				player = (L2PcInstance) getOwner();
			}
			else
			{
				return;
			}
			
			L2Skill passiveSkill = null;
			L2Skill enchant4Skill = null;
			
			final L2Item it = item.getItem();
			if (it instanceof L2Weapon)
			{
				passiveSkill = ((L2Weapon) it).getSkill();
				enchant4Skill = ((L2Weapon) it).getEnchant4Skill();
			}
			else if (it instanceof L2Armor)
			{
				passiveSkill = ((L2Armor) it).getSkill();
			}
			
			if (!player.isItemEquippedByItemId(item.getItemId()))
			{
				
				if (passiveSkill != null)
				{
					player.removeSkill(passiveSkill, false);
					player.sendSkillList();
				}
				
				if (enchant4Skill != null)
				{
					player.removeSkill(enchant4Skill, false);
					player.sendSkillList();
				}
				
			}
			
			player = null;
			passiveSkill = null;
			enchant4Skill = null;
		}
		
		@Override
		public void notifyEquiped(final int slot, final L2ItemInstance item)
		{
			L2PcInstance player;
			if (getOwner() instanceof L2PcInstance)
			{
				player = (L2PcInstance) getOwner();
			}
			else
			{
				return;
			}
			
			L2Skill passiveSkill = null;
			L2Skill enchant4Skill = null;
			
			final L2Item it = item.getItem();
			if (it instanceof L2Weapon)
			{
				// Check for Penality
				player.refreshExpertisePenalty();
				player.refreshMasteryWeapPenality();
				// If player get penality he will not recive SA bonus like retail
				if (player.getExpertisePenalty() == 0)
				{
					// Passive skills from Weapon (SA)
					passiveSkill = ((L2Weapon) it).getSkill();
				}
				
				if (item.getEnchantLevel() >= 4)
				{
					enchant4Skill = ((L2Weapon) it).getEnchant4Skill();
				}
			}
			else if (it instanceof L2Armor)
			{
				// Check for Penality
				player.refreshExpertisePenalty();
				player.refreshMasteryPenality();
				// Passive skills from Armor
				passiveSkill = ((L2Armor) it).getSkill();
			}
			
			if (passiveSkill != null)
			{
				if (!passiveSkill.is_singleEffect() || player.getInventory().checkHowManyEquipped(item.getItemId()) == 1)
				{
					
					player.addSkill(passiveSkill, false);
					player.sendSkillList();
					
				}
				
			}
			
			if (enchant4Skill != null)
			{
				if (!enchant4Skill.is_singleEffect() || player.getInventory().checkHowManyEquipped(item.getItemId()) == 1)
				{
					
					player.addSkill(enchant4Skill, false);
					player.sendSkillList();
				}
				
			}
			
			passiveSkill = null;
			enchant4Skill = null;
			player = null;
		}
	}
	
	final class ArmorSetListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(final int slot, final L2ItemInstance item)
		{
			if (!(getOwner() instanceof L2PcInstance))
			{
				return;
			}
			
			final L2PcInstance player = (L2PcInstance) getOwner();
			
			// checks if player worns chest item
			final L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
			if (chestItem == null)
			{
				return;
			}
			
			// checks if there is armorset for chest item that player worns
			final L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
			if (armorSet == null)
			{
				return;
			}
			
			// checks if equiped item is part of set
			if (armorSet.containItem(slot, item.getItemId()))
			{
				if (armorSet.containAll(player))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
					
					if (skill != null)
					{
						player.addSkill(skill, false);
						player.sendSkillList();
					}
					else
					{
						LOGGER.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getSkillId() + ".");
					}
					
					if (armorSet.containShield(player)) // has shield from set
					{
						L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
						
						if (skills != null)
						{
							player.addSkill(skills, false);
							player.sendSkillList();
						}
						else
						{
							LOGGER.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
						}
						
						skills = null;
					}
					
					if (armorSet.isEnchanted6(player)) // has all parts of set enchanted to 6 or more
					{
						final int skillId = armorSet.getEnchant6skillId();
						
						if (skillId > 0)
						{
							L2Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
							
							if (skille != null)
							{
								player.addSkill(skille, false);
								player.sendSkillList();
							}
							else
							{
								LOGGER.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getEnchant6skillId() + ".");
							}
							
							skille = null;
						}
					}
					
					skill = null;
				}
			}
			else if (armorSet.containShield(item.getItemId()))
			{
				if (armorSet.containAll(player))
				{
					L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
					
					if (skills != null)
					{
						player.addSkill(skills, false);
						player.sendSkillList();
					}
					else
					{
						LOGGER.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
					}
					
					skills = null;
				}
			}
		}
		
		@Override
		public void notifyUnequiped(final int slot, final L2ItemInstance item)
		{
			if (!(getOwner() instanceof L2PcInstance))
			{
				return;
			}
			
			L2PcInstance player = (L2PcInstance) getOwner();
			
			boolean remove = false;
			int removeSkillId1 = 0; // set skill
			int removeSkillId2 = 0; // shield skill
			int removeSkillId3 = 0; // enchant +6 skill
			
			if (slot == PAPERDOLL_CHEST)
			{
				L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(item.getItemId());
				if (armorSet == null)
				{
					return;
				}
				
				remove = true;
				removeSkillId1 = armorSet.getSkillId();
				removeSkillId2 = armorSet.getShieldSkillId();
				removeSkillId3 = armorSet.getEnchant6skillId();
				
				armorSet = null;
			}
			else
			{
				L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
				if (chestItem == null)
				{
					return;
				}
				
				L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
				if (armorSet == null)
				{
					return;
				}
				
				if (armorSet.containItem(slot, item.getItemId())) // removed part of set
				{
					remove = true;
					removeSkillId1 = armorSet.getSkillId();
					removeSkillId2 = armorSet.getShieldSkillId();
					removeSkillId3 = armorSet.getEnchant6skillId();
				}
				else if (armorSet.containShield(item.getItemId())) // removed shield
				{
					remove = true;
					removeSkillId2 = armorSet.getShieldSkillId();
				}
				
				armorSet = null;
				chestItem = null;
			}
			
			if (remove)
			{
				if (removeSkillId1 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId1, 1);
					
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						LOGGER.warn("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId1 + ".");
					}
					
					skill = null;
				}
				
				if (removeSkillId2 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId2, 1);
					
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						LOGGER.warn("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId2 + ".");
					}
					
					skill = null;
				}
				
				if (removeSkillId3 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId3, 1);
					
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						LOGGER.warn("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId3 + ".");
					}
					
					skill = null;
				}
				player.sendSkillList();
			}
			
			player = null;
		}
	}
	
	/*
	 * final class FormalWearListener implements PaperdollListener { public void notifyUnequiped(int slot, L2ItemInstance item) { if (!(getOwner() != null && getOwner() instanceof L2PcInstance)) return; L2PcInstance owner = (L2PcInstance)getOwner(); if (item.getItemId() == 6408)
	 * owner.setIsWearingFormalWear(false); } public void notifyEquiped(int slot, L2ItemInstance item) { if (!(getOwner() != null && getOwner() instanceof L2PcInstance)) return; L2PcInstance owner = (L2PcInstance)getOwner(); // If player equip Formal Wear unequip weapons and abort cast/attack if
	 * (item.getItemId() == 6408) { owner.setIsWearingFormalWear(true); if (owner.isCastingNow()) owner.abortCast(); if (owner.isAttackingNow()) owner.abortAttack(); setPaperdollItem(PAPERDOLL_LHAND, null); setPaperdollItem(PAPERDOLL_RHAND, null); setPaperdollItem(PAPERDOLL_LRHAND, null); } else { if
	 * (!owner.isWearingFormalWear()) return; // Don't let weapons be equipped if player is wearing Formal Wear if (slot == PAPERDOLL_LHAND || slot == PAPERDOLL_RHAND || slot == PAPERDOLL_LRHAND) { setPaperdollItem(slot, null); } } } }
	 */
	/**
	 * Constructor of the inventory
	 */
	protected Inventory()
	{
		paperdoll = new L2ItemInstance[0x12];
		paperdollListeners = new ArrayList<>();
		addPaperdollListener(new ArmorSetListener());
		addPaperdollListener(new BowListener());
		addPaperdollListener(new ItemPassiveSkillsListener());
		addPaperdollListener(new StatsListener());
		addPaperdollListener(new FormalWearListener());
	}
	
	protected abstract ItemLocation getEquipLocation();
	
	/**
	 * Returns the instance of new ChangeRecorder
	 * @return ChangeRecorder
	 */
	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}
	
	/**
	 * Drop item from inventory and updates database
	 * @param  process   : String Identifier of process triggering this action
	 * @param  item      : L2ItemInstance to be dropped
	 * @param  actor     : L2PcInstance Player requesting the item drop
	 * @param  reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return           L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(final String process, final L2ItemInstance item, final L2PcInstance actor, final L2Object reference)
	{
		synchronized (item)
		{
			if (!itemsList.contains(item))
			{
				return null;
			}
			
			removeItem(item);
			item.setOwnerId(process, 0, actor, reference);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			
			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and updates database
	 * @param  process   : String Identifier of process triggering this action
	 * @param  objectId  : int Item Instance identifier of the item to be dropped
	 * @param  count     : int Quantity of items to be dropped
	 * @param  actor     : L2PcInstance Player requesting the item drop
	 * @param  reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return           L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(final String process, final int objectId, final int count, final L2PcInstance actor, final L2Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		
		// Adjust item quantity and create new instance to drop
		if (item.getCount() > count)
		{
			item.changeCount(process, -count, actor, reference);
			item.setLastChange(L2ItemInstance.MODIFIED);
			item.updateDatabase();
			
			item = ItemTable.getInstance().createItem(process, item.getItemId(), count, actor, reference);
			
			item.updateDatabase();
			refreshWeight();
			
			return item;
		}
		// Directly drop entire item
		return dropItem(process, item, actor, reference);
	}
	
	/**
	 * Adds item to inventory for further adjustments and Equip it if necessary (itemlocation defined)<BR>
	 * <BR>
	 * @param item : L2ItemInstance to be added from inventory
	 */
	@Override
	protected void addItem(final L2ItemInstance item)
	{
		super.addItem(item);
		
		if (item.isEquipped())
		{
			equipItem(item);
		}
	}
	
	/**
	 * Removes item from inventory for further adjustments.
	 * @param item : L2ItemInstance to be removed from inventory
	 */
	@Override
	protected void removeItem(final L2ItemInstance item)
	{
		// Unequip item if equiped
		// if (item.isEquipped()) unEquipItemInSlotAndRecord(item.getEquipSlot());
		for (int i = 0; i < paperdoll.length; i++)
		{
			if (paperdoll[i] == item)
			{
				unEquipItemInSlot(i);
			}
		}
		
		super.removeItem(item);
	}
	
	/**
	 * Returns the item in the paperdoll slot
	 * @param  slot
	 * @return      L2ItemInstance
	 */
	public L2ItemInstance getPaperdollItem(final int slot)
	{
		return paperdoll[slot];
	}
	
	/**
	 * Returns the item in the paperdoll L2Item slot
	 * @param  slot
	 * @return      L2ItemInstance
	 */
	public L2ItemInstance getPaperdollItemByL2ItemId(final int slot)
	{
		switch (slot)
		{
			case 0x01:
				return paperdoll[0];
			case 0x04:
				return paperdoll[1];
			case 0x02:
				return paperdoll[2];
			case 0x08:
				return paperdoll[3];
			case 0x20:
				return paperdoll[4];
			case 0x10:
				return paperdoll[5];
			case 0x40:
				return paperdoll[6];
			case 0x80:
				return paperdoll[7];
			case 0x0100:
				return paperdoll[8];
			case 0x0200:
				return paperdoll[9];
			case 0x0400:
				return paperdoll[10];
			case 0x0800:
				return paperdoll[11];
			case 0x1000:
				return paperdoll[12];
			case 0x2000:
				return paperdoll[13];
			case 0x4000:
				return paperdoll[14];
			case 0x040000:
				return paperdoll[15];
			case 0x010000:
				return paperdoll[16];
			case 0x080000:
				return paperdoll[17];
		}
		return null;
	}
	
	/**
	 * Returns the ID of the item in the paperdol slot
	 * @param  slot : int designating the slot
	 * @return      int designating the ID of the item
	 */
	public int getPaperdollItemId(final int slot)
	{
		L2ItemInstance item = paperdoll[slot];
		
		if (item != null)
		{
			return item.getItemId();
		}
		else if (slot == PAPERDOLL_HAIR)
		{
			item = paperdoll[PAPERDOLL_DHAIR];
			
			if (item != null)
			{
				return item.getItemId();
			}
		}
		return 0;
	}
	
	public int getPaperdollAugmentationId(final int slot)
	{
		final L2ItemInstance item = paperdoll[slot];
		
		if (item != null)
		{
			if (item.getAugmentation() != null)
			{
				return item.getAugmentation().getAugmentationId();
			}
			return 0;
		}
		return 0;
	}
	
	/**
	 * Returns the objectID associated to the item in the paperdoll slot
	 * @param  slot : int pointing out the slot
	 * @return      int designating the objectID
	 */
	public int getPaperdollObjectId(final int slot)
	{
		L2ItemInstance item = paperdoll[slot];
		
		if (item != null)
		{
			return item.getObjectId();
		}
		else if (slot == PAPERDOLL_HAIR)
		{
			item = paperdoll[PAPERDOLL_DHAIR];
			
			if (item != null)
			{
				return item.getObjectId();
			}
		}
		return 0;
	}
	
	/**
	 * Adds new inventory's paperdoll listener
	 * @param listener
	 */
	public synchronized void addPaperdollListener(final PaperdollListener listener)
	{
		if (Config.ASSERT)
		{
			assert !paperdollListeners.contains(listener);
		}
		
		paperdollListeners.add(listener);
	}
	
	/**
	 * Removes a paperdoll listener
	 * @param listener to be deleted
	 */
	public synchronized void removePaperdollListener(final PaperdollListener listener)
	{
		paperdollListeners.remove(listener);
	}
	
	/**
	 * Equips an item in the given slot of the paperdoll. <U><I>Remark :</I></U> The item <B>HAS TO BE</B> already in the inventory
	 * @param  slot : int pointing out the slot of the paperdoll
	 * @param  item : L2ItemInstance pointing out the item to add in slot
	 * @return      L2ItemInstance designating the item placed in the slot before
	 */
	public L2ItemInstance setPaperdollItem(final int slot, final L2ItemInstance item)
	{
		final L2ItemInstance old = paperdoll[slot];
		
		if (old != item)
		{
			if (old != null)
			{
				paperdoll[slot] = null;
				// Put old item from paperdoll slot to base location
				old.setLocation(getBaseLocation());
				old.setLastChange(L2ItemInstance.MODIFIED);
				
				// Get the mask for paperdoll
				int mask = 0;
				
				for (int i = 0; i < PAPERDOLL_LRHAND; i++)
				{
					L2ItemInstance pi = paperdoll[i];
					
					if (pi != null)
					{
						mask |= pi.getItem().getItemMask();
					}
					
					pi = null;
				}
				
				wearedMask = mask;
				
				// Notify all paperdoll listener in order to unequip old item in slot
				for (final PaperdollListener listener : paperdollListeners)
				{
					if (listener == null)
					{
						continue;
					}
					
					listener.notifyUnequiped(slot, old);
					
					EngineModsManager.onUnequip(getOwner());
				}
				
				if (old.isAugmented())
				{
					if (getOwner() != null && getOwner() instanceof L2PcInstance)
					{
						old.getAugmentation().removeBoni((L2PcInstance) getOwner());
					}
				}
				
				old.updateDatabase();
			}
			
			// Add new item in slot of paperdoll
			if (item != null)
			{
				paperdoll[slot] = item;
				item.setLocation(getEquipLocation(), slot);
				item.setLastChange(L2ItemInstance.MODIFIED);
				wearedMask |= item.getItem().getItemMask();
				
				for (final PaperdollListener listener : paperdollListeners)
				{
					listener.notifyEquiped(slot, item);
				}
				
				EngineModsManager.onEquip(getOwner());
				
				item.updateDatabase();
			}
		}
		return old;
	}
	
	/**
	 * Return the mask of weared item
	 * @return int
	 */
	public int getWearedMask()
	{
		return wearedMask;
	}
	
	public int getSlotFromItem(final L2ItemInstance item)
	{
		int slot = -1;
		final int location = item.getEquipSlot();
		
		switch (location)
		{
			case PAPERDOLL_UNDER:
				slot = L2Item.SLOT_UNDERWEAR;
				break;
			case PAPERDOLL_LEAR:
				slot = L2Item.SLOT_L_EAR;
				break;
			case PAPERDOLL_REAR:
				slot = L2Item.SLOT_R_EAR;
				break;
			case PAPERDOLL_NECK:
				slot = L2Item.SLOT_NECK;
				break;
			case PAPERDOLL_RFINGER:
				slot = L2Item.SLOT_R_FINGER;
				break;
			case PAPERDOLL_LFINGER:
				slot = L2Item.SLOT_L_FINGER;
				break;
			case PAPERDOLL_HAIR:
				slot = L2Item.SLOT_HAIR;
				break;
			case PAPERDOLL_FACE:
				slot = L2Item.SLOT_FACE;
				break;
			case PAPERDOLL_DHAIR:
				slot = L2Item.SLOT_DHAIR;
				break;
			case PAPERDOLL_HEAD:
				slot = L2Item.SLOT_HEAD;
				break;
			case PAPERDOLL_RHAND:
				slot = L2Item.SLOT_R_HAND;
				break;
			case PAPERDOLL_LHAND:
				slot = L2Item.SLOT_L_HAND;
				break;
			case PAPERDOLL_GLOVES:
				slot = L2Item.SLOT_GLOVES;
				break;
			// case PAPERDOLL_CHEST: slot = item.getItem().getBodyPart(); break;// fall through
			case PAPERDOLL_CHEST:
				slot = L2Item.SLOT_CHEST;
				break; // TODO
			case PAPERDOLL_LEGS:
				slot = L2Item.SLOT_LEGS;
				break;
			case PAPERDOLL_BACK:
				slot = L2Item.SLOT_BACK;
				break;
			case PAPERDOLL_FEET:
				slot = L2Item.SLOT_FEET;
				break;
			case PAPERDOLL_LRHAND:
				slot = L2Item.SLOT_LR_HAND;
				break;
		}
		
		return slot;
	}
	
	/**
	 * Unequips item in body slot and returns alterations.
	 * @param  slot : int designating the slot of the paperdoll
	 * @return      L2ItemInstance[] : list of changes
	 */
	// public synchronized L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot)
	public L2ItemInstance[] unEquipItemInBodySlotAndRecord(final int slot)
	{
		final Inventory.ChangeRecorder recorder = newRecorder();
		
		try
		{
			unEquipItemInBodySlot(slot);
			
			if (getOwner() instanceof L2PcInstance)
			{
				((L2PcInstance) getOwner()).refreshExpertisePenalty();
				((L2PcInstance) getOwner()).refreshMasteryPenality();
				((L2PcInstance) getOwner()).refreshMasteryWeapPenality();
				
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		
		return recorder.getChangedItems();
	}
	
	/**
	 * Sets item in slot of the paperdoll to null value
	 * @param  pdollSlot : int designating the slot
	 * @return           L2ItemInstance designating the item in slot before change
	 */
	// public synchronized L2ItemInstance unEquipItemInSlot(int pdollSlot)
	public L2ItemInstance unEquipItemInSlot(final int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}
	
	/**
	 * Unepquips item in slot and returns alterations
	 * @param  slot : int designating the slot
	 * @return      L2ItemInstance[] : list of items altered
	 */
	// public synchronized L2ItemInstance[] unEquipItemInSlotAndRecord(int slot)
	public L2ItemInstance[] unEquipItemInSlotAndRecord(final int slot)
	{
		final Inventory.ChangeRecorder recorder = newRecorder();
		
		try
		{
			unEquipItemInSlot(slot);
			
			if (getOwner() instanceof L2PcInstance)
			{
				((L2PcInstance) getOwner()).refreshExpertisePenalty();
				((L2PcInstance) getOwner()).refreshMasteryPenality();
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		
		return recorder.getChangedItems();
	}
	
	/**
	 * Unequips item in slot (i.e. equips with default value)
	 * @param slot : int designating the slot
	 */
	private void unEquipItemInBodySlot(final int slot)
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("--- unequip body slot:" + slot);
		}
		
		int pdollSlot = -1;
		
		switch (slot)
		{
			case L2Item.SLOT_L_EAR:
				pdollSlot = PAPERDOLL_LEAR;
				break;
			case L2Item.SLOT_R_EAR:
				pdollSlot = PAPERDOLL_REAR;
				break;
			case L2Item.SLOT_NECK:
				pdollSlot = PAPERDOLL_NECK;
				break;
			case L2Item.SLOT_R_FINGER:
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			case L2Item.SLOT_L_FINGER:
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			case L2Item.SLOT_HAIR:
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_FACE:
				pdollSlot = PAPERDOLL_FACE;
				break;
			case L2Item.SLOT_DHAIR:
				setPaperdollItem(PAPERDOLL_HAIR, null);
				setPaperdollItem(PAPERDOLL_FACE, null);// this should be the same as in DHAIR
				pdollSlot = PAPERDOLL_DHAIR;
				break;
			case L2Item.SLOT_HEAD:
				pdollSlot = PAPERDOLL_HEAD;
				break;
			case L2Item.SLOT_R_HAND:
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case L2Item.SLOT_L_HAND:
				pdollSlot = PAPERDOLL_LHAND;
				break;
			case L2Item.SLOT_GLOVES:
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			case L2Item.SLOT_CHEST:
				pdollSlot = PAPERDOLL_CHEST;
				break; // TODO: пїЅпїЅпїЅпїЅ "пїЅпїЅпїЅпїЅпїЅ"
			case L2Item.SLOT_FULL_ARMOR:
				pdollSlot = PAPERDOLL_CHEST;
				break;
			case L2Item.SLOT_LEGS:
				pdollSlot = PAPERDOLL_LEGS;
				break;
			case L2Item.SLOT_BACK:
				pdollSlot = PAPERDOLL_BACK;
				break;
			case L2Item.SLOT_FEET:
				pdollSlot = PAPERDOLL_FEET;
				break;
			case L2Item.SLOT_UNDERWEAR:
				pdollSlot = PAPERDOLL_UNDER;
				break;
			case L2Item.SLOT_LR_HAND:
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, null);// this should be the same as in LRHAND
				pdollSlot = PAPERDOLL_LRHAND;
				break;
		}
		if (pdollSlot >= 0)
		{
			setPaperdollItem(pdollSlot, null);
		}
	}
	
	/**
	 * Equips item and returns list of alterations
	 * @param  item : L2ItemInstance corresponding to the item
	 * @return      L2ItemInstance[] : list of alterations
	 */
	public L2ItemInstance[] equipItemAndRecord(final L2ItemInstance item)
	{
		final Inventory.ChangeRecorder recorder = newRecorder();
		
		try
		{
			equipItem(item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		
		return recorder.getChangedItems();
	}
	
	/**
	 * Equips item in slot of paperdoll.
	 * @param item : L2ItemInstance designating the item and slot used.
	 */
	public synchronized void equipItem(final L2ItemInstance item)
	{
		if (getOwner() instanceof L2PcInstance && ((L2PcInstance) getOwner()).getPrivateStoreType() != 0)
		{
			return;
		}
		
		if (getOwner() instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) getOwner();
			
			// Like L2OFF weapon hero and crown aren't removed after restart
			if (!player.isGM())
			{
				if (!player.isHero())
				{
					final int itemId = item.getItemId();
					
					if (itemId >= 6611 && itemId <= 6621 || itemId == 6842)
					{
						return;
					}
				}
			}
		}
		
		final int targetSlot = item.getItem().getBodyPart();
		switch (targetSlot)
		{
			case L2Item.SLOT_LR_HAND:
			{
				if (setPaperdollItem(PAPERDOLL_LHAND, null) != null)
				{
					// exchange 2h for 2h
					setPaperdollItem(PAPERDOLL_RHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				
				setPaperdollItem(PAPERDOLL_RHAND, item);
				setPaperdollItem(PAPERDOLL_LRHAND, item);
				
				break;
			}
			case L2Item.SLOT_L_HAND:
			{
				if (!(item.getItem() instanceof L2EtcItem) || item.getItem().getItemType() != L2EtcItemType.ARROW)
				{
					final L2ItemInstance old1 = setPaperdollItem(PAPERDOLL_LRHAND, null);
					
					if (old1 != null)
					{
						setPaperdollItem(PAPERDOLL_RHAND, null);
					}
				}
				
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_LHAND, item);
				break;
			}
			case L2Item.SLOT_R_HAND:
			{
				if (paperdoll[PAPERDOLL_LRHAND] != null)
				{
					setPaperdollItem(PAPERDOLL_LRHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, null);
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case L2Item.SLOT_L_EAR:
			case L2Item.SLOT_R_EAR:
			case L2Item.SLOT_L_EAR | L2Item.SLOT_R_EAR:
			{
				if (paperdoll[PAPERDOLL_LEAR] == null)
				{
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				else if (paperdoll[PAPERDOLL_REAR] == null)
				{
					setPaperdollItem(PAPERDOLL_REAR, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LEAR, null);
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				
				break;
			}
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT_R_FINGER:
			case L2Item.SLOT_L_FINGER | L2Item.SLOT_R_FINGER:
			{
				if (paperdoll[PAPERDOLL_LFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				else if (paperdoll[PAPERDOLL_RFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LFINGER, null);
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				
				break;
			}
			case L2Item.SLOT_NECK:
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			case L2Item.SLOT_FULL_ARMOR:
				setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case L2Item.SLOT_CHEST:
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case L2Item.SLOT_LEGS:
			{
				// handle full armor
				L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if (chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
				{
					setPaperdollItem(PAPERDOLL_CHEST, null);
				}
				
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_LEGS, item);
				chest = null;
				break;
			}
			case L2Item.SLOT_FEET:
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;
			case L2Item.SLOT_GLOVES:
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;
			case L2Item.SLOT_HEAD:
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;
			case L2Item.SLOT_HAIR:
				if (setPaperdollItem(PAPERDOLL_DHAIR, null) != null)
				{
					setPaperdollItem(PAPERDOLL_DHAIR, null);
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
				}
				
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			case L2Item.SLOT_FACE:
				if (setPaperdollItem(PAPERDOLL_DHAIR, null) != null)
				{
					setPaperdollItem(PAPERDOLL_DHAIR, null);
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				
				setPaperdollItem(PAPERDOLL_FACE, item);
				break;
			case L2Item.SLOT_DHAIR:
				if (setPaperdollItem(PAPERDOLL_HAIR, null) != null)
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				
				setPaperdollItem(PAPERDOLL_DHAIR, item);
				break;
			case L2Item.SLOT_UNDERWEAR:
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			case L2Item.SLOT_BACK:
				setPaperdollItem(PAPERDOLL_BACK, item);
				break;
			default:
				LOGGER.warn("unknown body slot:" + targetSlot);
		}
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	protected void refreshWeight()
	{
		int weight = 0;
		
		for (final L2ItemInstance item : itemsList)
		{
			if (item != null && item.getItem() != null)
			{
				weight += item.getItem().getWeight() * item.getCount();
			}
		}
		
		totalWeight = weight;
	}
	
	/**
	 * Returns the totalWeight.
	 * @return int
	 */
	public int getTotalWeight()
	{
		return totalWeight;
	}
	
	/**
	 * Return the L2ItemInstance of the arrows needed for this bow.<BR>
	 * <BR>
	 * @param  bow : L2Item designating the bow
	 * @return     L2ItemInstance pointing out arrows for bow
	 */
	public L2ItemInstance findArrowForBow(final L2Item bow)
	{
		if (bow == null)
		{
			return null;
		}
		
		// Check if char has the bow equiped
		if (bow.getItemType() != L2WeaponType.BOW)
		{
			return null;
		}
		
		int arrowsId = 0;
		
		switch (bow.getCrystalType())
		{
			default: // broken weapon.csv ??
			case L2Item.CRYSTAL_NONE:
				arrowsId = 17;
				break; // Wooden arrow
			case L2Item.CRYSTAL_D:
				arrowsId = 1341;
				break; // Bone arrow
			case L2Item.CRYSTAL_C:
				arrowsId = 1342;
				break; // Fine steel arrow
			case L2Item.CRYSTAL_B:
				arrowsId = 1343;
				break; // Silver arrow
			case L2Item.CRYSTAL_A:
				arrowsId = 1344;
				break; // Mithril arrow
			case L2Item.CRYSTAL_S:
				arrowsId = 1345;
				break; // Shining arrow
		}
		// Get the L2ItemInstance corresponding to the item identifier and return it
		return getItemByItemId(arrowsId);
	}
	
	/**
	 * Get back items in inventory from database
	 */
	@Override
	public void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_ITEMS_BY_OWNER_ID);)
		{
			statement.setInt(1, getOwner().getObjectId());
			statement.setString(2, getBaseLocation().name());
			statement.setString(3, getEquipLocation().name());
			ResultSet rset = statement.executeQuery();
			
			L2ItemInstance item;
			
			while (rset.next())
			{
				item = L2ItemInstance.restoreFromDb(rset);
				
				if (item == null)
				{
					continue;
				}
				
				if (getOwner() instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) getOwner();
					
					if (!player.isGM())
					{
						if (!player.isHero())
						{
							final int itemId = item.getItemId();
							
							if (itemId >= 6611 && itemId <= 6621 || itemId == 6842)
							{
								item.setLocation(ItemLocation.INVENTORY);
							}
						}
					}
					
					player = null;
				}
				
				L2World.getInstance().storeObject(item);
				
				// If stackable item is found in inventory just add to current quantity
				if (item.isStackable() && getItemByItemId(item.getItemId()) != null)
				{
					addItem("Restore", item, null, getOwner());
				}
				else
				{
					addItem(item);
				}
			}
			
			rset.close();
			refreshWeight();
			
			rset = null;
			item = null;
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not restore inventory : ", e);
		}
	}
	
	/**
	 * Re-notify to paperdoll listeners every equipped item
	 */
	public void reloadEquippedItems()
	{
		L2ItemInstance item;
		
		int slot;
		
		for (final L2ItemInstance element : paperdoll)
		{
			item = element;
			if (item == null)
			{
				continue;
			}
			
			slot = item.getEquipSlot();
			
			for (final PaperdollListener listener : paperdollListeners)
			{
				if (listener == null)
				{
					continue;
				}
				
				listener.notifyUnequiped(slot, item);
				listener.notifyEquiped(slot, item);
			}
		}
		
		item = null;
	}
}
