package ai.boxes;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author: rage
 * @date: 19.12.11 21:07
 */
public class TreasureBox75 extends Fighter
{
	public TreasureBox75(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);

		if(killer == null)
			return;

		Player player = killer.getPlayer();
		if(player == null)
			return;

		int i0 = Rnd.get(10000);
		if( i0 < 8366 )
		{
			getActor().dropItem(player, 8627, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7470 )
		{
			getActor().dropItem(player, 8633, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8366 )
		{
			getActor().dropItem(player, 8639, 5);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8366 )
		{
			getActor().dropItem(player, 8638, 6);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8715 )
		{
			getActor().dropItem(player, 8632, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6693 )
		{
			getActor().dropItem(player, 8626, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 140 )
		{
			getActor().dropItem(player, 729, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1046 )
		{
			getActor().dropItem(player, 730, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6275 )
		{
			getActor().dropItem(player, 1540, 4);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10260, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10261, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10262, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10263, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10264, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10265, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10266, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10267, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10268, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1059 )
		{
			getActor().dropItem(player, 5595, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1059 )
		{
			getActor().dropItem(player, 9898, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 2824 )
		{
			getActor().dropItem(player, 10269, 3);
		}
		i0 = Rnd.get(10000);
		if( i0 < 7059 )
		{
			getActor().dropItem(player, 8739, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 5883 )
		{
			getActor().dropItem(player, 8740, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4902 )
		{
			getActor().dropItem(player, 8741, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4412 )
		{
			getActor().dropItem(player, 8742, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8898 )
		{
			getActor().dropItem(player, 21183, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8898 )
		{
			getActor().dropItem(player, 21184, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 4449 )
		{
			getActor().dropItem(player, 21185, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 8898 )
		{
			getActor().dropItem(player, 1538, 2);
		}
		i0 = Rnd.get(10000);
		if( i0 < 6674 )
		{
			getActor().dropItem(player, 3936, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1236 )
		{
			getActor().dropItem(player, 9654, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 1236 )
		{
			getActor().dropItem(player, 9655, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 134 )
		{
			getActor().dropItem(player, 5908, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 134 )
		{
			getActor().dropItem(player, 5911, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 134 )
		{
			getActor().dropItem(player, 5914, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 63 )
		{
			getActor().dropItem(player, 6364, 1);
		}
		i0 = Rnd.get(10000);
		if( i0 < 187 )
		{
			getActor().dropItem(player, 21748, 1);
		}
	}
}