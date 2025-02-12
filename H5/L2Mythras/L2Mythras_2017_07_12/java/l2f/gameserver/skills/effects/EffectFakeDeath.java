package l2f.gameserver.skills.effects;

import l2f.gameserver.ai.CtrlEvent;
import l2f.gameserver.model.Effect;
import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.ChangeWaitType;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;
import l2f.gameserver.network.serverpackets.Revive;
import l2f.gameserver.network.serverpackets.SystemMessage;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.stats.Env;

public final class EffectFakeDeath extends Effect
{
	public EffectFakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Player player = (Player) getEffected();
		player.setFakeDeath(true);
		player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
		player.broadcastPacket(new L2GameServerPacket[] { new ChangeWaitType(player, 2) });
		player.abortCast(true, false);
		player.abortAttack(true, false);
		player.broadcastCharInfo();
	}

	@Override
	public void onExit()
	{
		super.onExit();

		Player player = (Player) getEffected();
		player.setNonAggroTime(System.currentTimeMillis() + 5000L);
		player.setFakeDeath(false);
		player.broadcastPacket(new L2GameServerPacket[] { new ChangeWaitType(player, 3) });
		player.broadcastPacket(new L2GameServerPacket[] { new Revive(player) });
		player.broadcastCharInfo();
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;

		double manaDam = calc();

		if (manaDam > getEffected().getCurrentMp() && getSkill().isToggle())
		{
			getEffected().sendPacket(SystemMsg.NOT_ENOUGH_MP);
			getEffected().sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}

		getEffected().reduceCurrentMp(manaDam, null);
		return true;
	}
}