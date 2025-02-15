package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Env;

public class EffectMutePhisycal extends Effect
{
	public EffectMutePhisycal(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!_effected.startPMuted())
		{
			Skill castingSkill = _effected.getCastingSkill();
			if(castingSkill != null && (!castingSkill.isMagic() || (!Config.SHIELD_SLAM_BLOCK_IS_MUSIC && castingSkill.isMusic())))
				_effected.abortCast(true, true);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopPMuted();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}