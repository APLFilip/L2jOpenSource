package l2f.gameserver.skills.effects;

import l2f.gameserver.model.Effect;
import l2f.gameserver.stats.Env;

public final class EffectBuff extends Effect
{
	public EffectBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		// Ady - Hardcoded buff immunity for Day of Doom debuff (Prevents from receiving buff)
		if (getSkill().getId() == 5145)
			_effected.startBuffImmunity();
	}

	@Override
	public void onExit()
	{
		super.onExit();

		// Ady - Hardcoded buff immunity for Day of Doom debuff (Prevents from receiving buff)
		if (getSkill().getId() == 5145)
			_effected.stopBuffImmunity();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}