package l2f.gameserver.model;

import l2f.gameserver.stats.triggers.TriggerInfo;
import l2f.gameserver.stats.triggers.TriggerType;

/**
 * Holder para guardar cada opcion de augmentation
 *
 * @author GipsyGrierosu Andrei
 */
public class Options
{
	private final int _id;
	private final String _augType;
	private final TriggerInfo _trigger;

	public enum AugmentationFilter
	{
		NONE,
		ACTIVE_SKILL,
		PASSIVE_SKILL,
		CHANCE_SKILL,
		STATS
	}

	public Options(int augId, String augType, int skillId, int skillLevel, TriggerType triggerType, double triggerChance)
	{
		_id = augId;
		_augType = augType;
		_trigger = new TriggerInfo(skillId, skillLevel, triggerType, triggerChance);
	}

	public int getAugmentationId()
	{
		return _id;
	}

	public String getAugType()
	{
		return _augType;
	}

	public TriggerInfo getTrigger()
	{
		return _trigger;
	}
}
