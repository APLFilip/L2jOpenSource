package ai.SkyshadowMeadow;

import java.util.List;

import l2f.commons.threading.RunnableImpl;
import l2f.commons.util.Rnd;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.ai.Fighter;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.network.serverpackets.components.NpcString;
import l2f.gameserver.tables.SkillTable;

/**
 * @author Grivesky
  * - AI for Sel Mahum Shef (18,908).
  * - Is next, skill buff the player, the player resaya HP.
  * - AI is tested and works.
 */
public class SelMahumShef extends Fighter
{
	private long _wait_timeout = System.currentTimeMillis() + 30000;
	private boolean _firstTime = true;
	public static final NpcString[] _text = {NpcString.SCHOOL3, NpcString.SCHOOL4};

	public SelMahumShef(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if (actor != null)
		{
			if (_wait_timeout < System.currentTimeMillis())
			{
				List<Player> players = World.getAroundPlayers(actor, 100, 100);
				for (Player p : players)
					actor.doCast(SkillTable.getInstance().getInfo(6330, 1), p, true);
				_wait_timeout = (System.currentTimeMillis() + 30000);
			}

			for (NpcInstance npc : actor.getAroundNpc(150, 150))
			{
				if (npc.isMonster() && npc.getNpcId() == 18927)
				{
					if (_firstTime)
					{
						// Включаем паузу что бы не зафлудить чат.
						_firstTime = false;
						Functions.npcSay(actor, _text[Rnd.get(_text.length)]);
						ThreadPoolManager.getInstance().schedule(new NewText(), 20000); // Время паузы
					}
				}
			}

			return super.thinkActive();
		}
		return true;
	}

	private class NewText extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			if (actor == null)
				return;

			// Выключаем паузу
			_firstTime = true;
		}
	}
}