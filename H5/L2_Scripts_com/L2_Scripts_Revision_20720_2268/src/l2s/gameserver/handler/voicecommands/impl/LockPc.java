package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CharacterControlPanel;
import l2s.gameserver.scripts.Functions;

public class LockPc extends Functions implements IVoicedCommandHandler
{
	private static final String[] COMMANDS = { "lock" };
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		String nextPage = CharacterControlPanel.getInstance().useCommand(activeChar, "hwidPage", "-h user_cp ");
		if ((nextPage == null) || (nextPage.isEmpty()))
			return true;
		String html = "command/" + nextPage;
    
		String dialog = HtmCache.getInstance().getHtml(html, activeChar);
    
		dialog = CharacterControlPanel.getInstance().replacePage(dialog, activeChar, "", "-h user_cp ");
    
		show(dialog, activeChar);
    
		return true;
	}
  
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}
