package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.StatsSet;


public class Sowing extends Skill
{
	public Sowing(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		Player player = (Player) activeChar;
		int seedId = player.getUseSeed();
		boolean altSeed = ItemHolder.getInstance().getTemplate(seedId).isAltSeed();

		// remove seed from inventory
		if(!player.getInventory().destroyItemByItemId(seedId, 1L))
		{
			activeChar.sendActionFailed();
			return;
		}

		player.sendPacket(SystemMessagePacket.removeItems(seedId, 1L));

		for(Creature target : targets)
			if(target != null)
			{
				MonsterInstance monster = (MonsterInstance) target;
				if(monster.isSeeded())
					continue;

				// обработка
				double SuccessRate = Config.MANOR_SOWING_BASIC_SUCCESS;

				double diffPlayerTarget = Math.abs(activeChar.getLevel() - target.getLevel());
				double diffSeedTarget = Math.abs(Manor.getInstance().getSeedLevel(seedId) - target.getLevel());

				// Штраф, на разницу уровней между мобом и игроком
				// 5% на каждый уровень при разнице >5 - по умолчанию
				if(diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET)
					SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;

				// Штраф, на разницу уровней между семечкой и мобом
				// 5% на каждый уровень при разнице >5 - по умолчанию
				if(diffSeedTarget > Config.MANOR_DIFF_SEED_TARGET)
					SuccessRate -= (diffSeedTarget - Config.MANOR_DIFF_SEED_TARGET) * Config.MANOR_DIFF_SEED_TARGET_PENALTY;

				if(altSeed)
					SuccessRate *= Config.MANOR_SOWING_ALT_BASIC_SUCCESS / Config.MANOR_SOWING_BASIC_SUCCESS;

				// Минимальный шанс успеха всегда 1%
				if(SuccessRate < 1)
					SuccessRate = 1;

				if(player.isGM())
					activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Sowing.Chance", player).addNumber((long) SuccessRate));

				if(Rnd.chance(SuccessRate) && monster.setSeeded(player, seedId, altSeed))
					activeChar.sendPacket(Msg.THE_SEED_WAS_SUCCESSFULLY_SOWN);
				else
					activeChar.sendPacket(Msg.THE_SEED_WAS_NOT_SOWN);
			}
	}
}