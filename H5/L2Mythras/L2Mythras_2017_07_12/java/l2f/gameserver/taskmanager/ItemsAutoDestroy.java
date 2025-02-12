package l2f.gameserver.taskmanager;

import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.model.items.ItemInstance;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ItemsAutoDestroy
{
	private static final long MILLIS_TO_CHECK_DESTROY_THREAD = 1000L;
	private static final long MILLIS_TO_DELETE_HERB = 60000L;
	
	private final Queue<ItemInstance> itemsToDelete;

	private ItemsAutoDestroy()
	{
		itemsToDelete = new ConcurrentLinkedQueue<>();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckItemsForDestroy(), MILLIS_TO_CHECK_DESTROY_THREAD, MILLIS_TO_CHECK_DESTROY_THREAD);
	}

	private static class ItemsAutoDestroyHolder
	{
		private static final ItemsAutoDestroy instance = new ItemsAutoDestroy();
	}
	
	public static ItemsAutoDestroy getInstance()
	{
		return ItemsAutoDestroyHolder.instance;
	}

	public void addItem(ItemInstance item, long destroyTime)
	{
		item.setTimeToDeleteAfterDrop(System.currentTimeMillis() + destroyTime);
		itemsToDelete.add(item);
	}

	public void addHerb(ItemInstance herb)
	{
		herb.setTimeToDeleteAfterDrop(System.currentTimeMillis() + MILLIS_TO_DELETE_HERB);
		itemsToDelete.add(herb);
	}
	
	private Collection<ItemInstance> getItemsToDelete()
	{
		return itemsToDelete;
	}

	private static class CheckItemsForDestroy implements Runnable
	{
		@Override
		public void run()
		{
			long currentTime = System.currentTimeMillis();
			for (ItemInstance item : getInstance().getItemsToDelete())
				if (item == null || item.getTimeToDeleteAfterDrop() == 0 || item.getLocation() != ItemInstance.ItemLocation.VOID)
					getInstance().getItemsToDelete().remove(item);
				else if (item.getTimeToDeleteAfterDrop() < currentTime)
				{
					item.deleteMe();
					getInstance().getItemsToDelete().remove(item);
				}
		}
	}
}