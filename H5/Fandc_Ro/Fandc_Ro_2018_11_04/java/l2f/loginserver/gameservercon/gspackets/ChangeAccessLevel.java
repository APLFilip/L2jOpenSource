package l2f.loginserver.gameservercon.gspackets;


import l2f.loginserver.accounts.Account;
import l2f.loginserver.gameservercon.ReceivablePacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeAccessLevel extends ReceivablePacket
{
	public static final Logger _log = LoggerFactory.getLogger(ChangeAccessLevel.class);

	private String account;
	private int level;	
	private int banExpire;

	@Override
	protected void readImpl()
	{
		account = readS();
		level = readD();
		banExpire = readD();
	}

	@Override
	protected void runImpl()
	{
		final Account acc = new Account(account);
		acc.restore();
		if (acc.getAccessLevel() <= 0)
			acc.setAccessLevel(level);
		acc.setBanExpire(banExpire);
		acc.update();
	}
}