package l2f.gameserver.handler.admincommands.impl;

import l2f.commons.threading.RunnableImpl;
import l2f.gameserver.Config;
import l2f.gameserver.BalancerConfig;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.dao.OlympiadNobleDAO;
import l2f.gameserver.data.StringHolder;
import l2f.gameserver.data.htm.HtmCache;
import l2f.gameserver.data.xml.holder.*;
import l2f.gameserver.data.xml.parser.ClassesStatsBalancerParser;
import l2f.gameserver.data.xml.parser.EventParser;
import l2f.gameserver.data.xml.parser.FightClubMapParser;
import l2f.gameserver.data.xml.parser.NpcStatsBalancerParser;
import l2f.gameserver.data.xml.parser.NpcParser;
import l2f.gameserver.data.xml.parser.PremiumParser;
import l2f.gameserver.database.DatabaseFactory;
import l2f.gameserver.handler.admincommands.IAdminCommandHandler;
import l2f.gameserver.instancemanager.SpawnManager;
import l2f.gameserver.model.GameObject;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.ChangeLogManager;
import l2f.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2f.gameserver.model.quest.Quest;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.network.serverpackets.NpcHtmlMessage;
import l2f.gameserver.tables.FishTable;
import l2f.gameserver.tables.PetDataTable;
import l2f.gameserver.tables.SkillTable;
import l2f.gameserver.utils.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.sql.Connection;
import java.sql.SQLException;

public class AdminReload implements IAdminCommandHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminReload.class);

	private static enum Commands
	{
		admin_reload,
		admin_reload_config,
		admin_reload_multisell,
		admin_reload_gmaccess,
		admin_reload_htm,
		admin_reload_qs,
		admin_reload_qs_help,
		admin_reload_skills,
		admin_reload_npc,
		admin_reload_spawn,
		admin_reload_fish,
		admin_reload_abuse,
		admin_reload_translit,
		admin_reload_shops,
		admin_reload_static,
		admin_reload_pets,
		admin_reload_locale,
		admin_reload_nobles,
		admin_reload_im,
		admin_reload_events,
		admin_reload_fc_maps,
		admin_reload_changelog,
		admin_reload_premium,
        admin_reload_balancenpc,
		admin_reload_balanceclasses,
		admin_reload_damageclasses
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if (!activeChar.getPlayerAccess().CanReload)
			return false;

		switch (command)
		{
			case admin_reload:
				break;
			case admin_reload_config:
			{
				try
				{
					Config.load();
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Error: " + e.getMessage() + "!");
					return false;
				}
				activeChar.sendMessage("Config reloaded!");
				break;
			}
			case admin_reload_multisell:
			{
				try
				{
					MultiSellHolder.getInstance().reload();
				}
				catch (Exception e)
				{
					return false;
				}
				activeChar.sendMessage("Multisell list reloaded!");
				break;
			}
			case admin_reload_gmaccess:
			{
				try
				{
					Config.loadGMAccess();
					for (Player player : GameObjectsStorage.getAllPlayersForIterate())
						if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
							player.setPlayerAccess(Config.gmlist.get(player.getObjectId()));
						else
							player.setPlayerAccess(Config.gmlist.get(new Integer(0)));
				}
				catch (Exception e)
				{
					return false;
				}
				activeChar.sendMessage("GMAccess reloaded!");
				break;
			}
			case admin_reload_htm:
			{
				HtmCache.getInstance().clear();
				if (Config.HTM_CACHE_MODE == 2)
				{
					HtmCache.getInstance().reload();
				}
				activeChar.sendMessage("HtmCache reloaded!");
				break;
			}
			case admin_reload_qs:
			{
				if (fullString.endsWith("all"))
					for (Player p : GameObjectsStorage.getAllPlayersForIterate())
						reloadQuestStates(p);
				else
				{
					GameObject t = activeChar.getTarget();

					if (t != null && t.isPlayer())
					{
						Player p = (Player) t;
						reloadQuestStates(p);
					}
					else
						reloadQuestStates(activeChar);
				}
				break;
			}
			case admin_reload_qs_help:
			{
				activeChar.sendMessage("");
				activeChar.sendMessage("Quest Help:");
				activeChar.sendMessage("reload_qs_help - This Message.");
				activeChar.sendMessage("reload_qs <selected target> - reload all quest states for target.");
				activeChar.sendMessage("reload_qs <no target or target is not player> - reload quests for self.");
				activeChar.sendMessage("reload_qs all - reload quests for all players in world.");
				activeChar.sendMessage("");
				break;
			}
			case admin_reload_skills:
			{
				ThreadPoolManager.getInstance().execute(new Runnable() {
					@Override
					public void run()
					{
						SkillTable.getInstance().reload();
					}
				});
				activeChar.sendMessage("Skills Reloaded!");
				break;
			}
			case admin_reload_npc:
			{
				NpcParser.getInstance().reload();
				break;
			}
			case admin_reload_spawn:
			{
				ThreadPoolManager.getInstance().execute(new RunnableImpl()
				{
					@Override
					public void runImpl() throws Exception
					{
						SpawnManager.getInstance().reloadAll();
					}
				});
				break;
			}
			case admin_reload_fish:
			{
				FishTable.getInstance().reload();
				break;
			}
			case admin_reload_abuse:
			{
				Config.abuseLoad();
				break;
			}
			case admin_reload_translit:
			{
				Strings.reload();
				break;
			}
			case admin_reload_shops:
			{
				BuyListHolder.reload();
				break;
			}
			case admin_reload_static:
			{
				//StaticObjectsTable.getInstance().reloadStaticObjects();
				break;
			}
			case admin_reload_pets:
			{
				PetDataTable.getInstance().reload();
				break;
			}
			case admin_reload_locale:
			{
				StringHolder.getInstance().reload();
				break;
			}
			case admin_reload_nobles:
			{
				OlympiadNobleDAO.getInstance().select();
				OlympiadDatabase.loadNoblesRank();
				break;
			}
			case admin_reload_im:
			{
				ProductHolder.getInstance().reload();
				break;
			}
			case admin_reload_events:
			{
				EventHolder.getInstance().clear();
				EventParser.getInstance().load();
				activeChar.sendMessage("Events Reloaded!");
				break;
			}
			case admin_reload_fc_maps:
			{
				FightClubMapHolder.getInstance().clear();
				FightClubMapParser.getInstance().load();
				activeChar.sendMessage("Maps Reloaded!");
				break;
			}
			case admin_reload_changelog:
			{
				ChangeLogManager.getInstance().reloadChangeLog();
				activeChar.sendMessage("Changelog reloaded!");
				break;
			}
            case admin_reload_balancenpc:
            {
				NpcStatsBalancerParser.getInstance().reload();
				activeChar.sendMessage("Balance Npcs reloaded!");
				break;
            }
			case admin_reload_balanceclasses:
			{
				ClassesStatsBalancerParser.getInstance().reload();
				activeChar.sendMessage("Balance Classes reloaded!");
				break;
			}
			case admin_reload_damageclasses:
			{
				BalancerConfig.LoadConfig();
				activeChar.sendMessage("Balance properties data have been reloaded.");
				break;
			}
			case admin_reload_premium:
			{
				PremiumHolder.getInstance().clear();
				PremiumParser.getInstance().reload();
				break;
			}
		}
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/reload.htm"));
		return true;
	}

	private void reloadQuestStates(Player p)
	{
		for (QuestState qs : p.getAllQuestsStates())
			p.removeQuestState(qs.getQuest().getName());
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			Quest.restoreQuestStates(p, con);
		}
		catch (SQLException e)
		{
			LOG.error("Error while reloading Quest States ", e);
		}
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}