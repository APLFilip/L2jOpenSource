package l2f.gameserver.stats.conditions;

import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Effect;
import l2f.gameserver.skills.EffectType;
import l2f.gameserver.stats.Env;

public final class ConditionTargetHasBuff extends Condition
{
	private final EffectType _effectType;
	private final int _level;

	public ConditionTargetHasBuff(EffectType effectType, int level)
	{
		_effectType = effectType;
		_level = level;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		if (target == null)
			return false;
		Effect effect = target.getEffectList().getEffectByType(_effectType);
		if (effect == null)
			return false;
		if (_level == -1 || effect.getSkill().getLevel() >= _level)
			return true;
		return false;
	}
}
