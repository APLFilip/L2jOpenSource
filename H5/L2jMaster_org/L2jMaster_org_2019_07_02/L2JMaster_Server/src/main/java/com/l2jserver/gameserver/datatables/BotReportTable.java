/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.datatables;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author BiggBoss
 */
public final class BotReportTable
{
	// Zoey76: TODO: Split XML parsing from SQL operations, use IXmlReader instead of SAXParser.
	private static final Logger LOGGER = Logger.getLogger(BotReportTable.class.getName());
	
	private static final int COLUMN_BOT_ID = 1;
	private static final int COLUMN_REPORTER_ID = 2;
	private static final int COLUMN_REPORT_TIME = 3;
	
	public static final int ATTACK_ACTION_BLOCK_ID = -1;
	public static final int TRADE_ACTION_BLOCK_ID = -2;
	public static final int PARTY_ACTION_BLOCK_ID = -3;
	public static final int ACTION_BLOCK_ID = -4;
	public static final int CHAT_BLOCK_ID = -5;
	
	private static final String SQL_LOAD_REPORTED_CHAR_DATA = "SELECT * FROM bot_reported_char_data";
	private static final String SQL_INSERT_REPORTED_CHAR_DATA = "INSERT INTO bot_reported_char_data VALUES (?,?,?)";
	private static final String SQL_CLEAR_REPORTED_CHAR_DATA = "DELETE FROM bot_reported_char_data";
	
	private Map<Integer, Long> _ipRegistry;
	private Map<Integer, ReporterCharData> _charRegistry;
	private Map<Integer, ReportedCharData> _reports;
	private Map<Integer, PunishHolder> _punishments;
	
	BotReportTable()
	{
		if (Config.BOTREPORT_ENABLE)
		{
			_ipRegistry = new HashMap<>();
			_charRegistry = new ConcurrentHashMap<>();
			_reports = new ConcurrentHashMap<>();
			_punishments = new ConcurrentHashMap<>();
			
			try
			{
				File punishments = new File("./config/botreport_punishments.xml");
				if (!punishments.exists())
				{
					throw new FileNotFoundException(punishments.getName());
				}
				
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				parser.parse(punishments, new PunishmentsLoader());
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "BotReportTable: Could not load punishments from /config/botreport_punishments.xml", e);
			}
			
			loadReportedCharData();
			scheduleResetPointTask();
		}
	}
	
	/**
	 * Loads all reports of each reported bot into this cache class.<br>
	 * Warning: Heavy method, used only on server start up
	 */
	private void loadReportedCharData()
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement st = con.createStatement();
			ResultSet rset = st.executeQuery(SQL_LOAD_REPORTED_CHAR_DATA))
		{
			long lastResetTime = 0;
			try
			{
				String[] hour = Config.BOTREPORT_RESETPOINT_HOUR;
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
				c.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
				
				if (System.currentTimeMillis() < c.getTimeInMillis())
				{
					c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) - 1);
				}
				
				lastResetTime = c.getTimeInMillis();
			}
			catch (Exception e)
			{
				
			}
			
			while (rset.next())
			{
				int botId = rset.getInt(COLUMN_BOT_ID);
				int reporter = rset.getInt(COLUMN_REPORTER_ID);
				long date = rset.getLong(COLUMN_REPORT_TIME);
				if (_reports.containsKey(botId))
				{
					_reports.get(botId).addReporter(reporter, date);
				}
				else
				{
					ReportedCharData rcd = new ReportedCharData();
					rcd.addReporter(reporter, date);
					_reports.put(rset.getInt(COLUMN_BOT_ID), rcd);
				}
				
				if (date > lastResetTime)
				{
					ReporterCharData rcd = _charRegistry.get(reporter);
					if (rcd != null)
					{
						rcd.setPoints(rcd.getPointsLeft() - 1);
					}
					else
					{
						rcd = new ReporterCharData();
						rcd.setPoints(6);
						_charRegistry.put(reporter, rcd);
					}
				}
			}
			
			LOGGER.info("BotReportTable: Loaded " + _reports.size() + " bot reports");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "BotReportTable: Could not load reported char data!", e);
		}
	}
	
	/**
	 * Save all reports for each reported bot down to database.<br>
	 * Warning: Heavy method, used only at server shutdown
	 */
	public void saveReportedCharData()
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement st = con.createStatement();
			PreparedStatement ps = con.prepareStatement(SQL_INSERT_REPORTED_CHAR_DATA))
		{
			st.execute(SQL_CLEAR_REPORTED_CHAR_DATA);
			
			for (Map.Entry<Integer, ReportedCharData> entrySet : _reports.entrySet())
			{
				Map<Integer, Long> reportTable = entrySet.getValue()._reporters;
				for (int reporterId : reportTable.keySet())
				{
					ps.setInt(1, entrySet.getKey());
					ps.setInt(2, reporterId);
					ps.setLong(3, reportTable.get(reporterId));
					ps.execute();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "BotReportTable: Could not update reported char data in database!", e);
		}
	}
	
	/**
	 * Attempts to perform a bot report. R/W to ip and char id registry is synchronized. Triggers bot punish management<br>
	 * @param reporter (L2PcInstance who issued the report)
	 * @return True, if the report was registered, False otherwise
	 */
	public boolean reportBot(L2PcInstance reporter)
	{
		L2Object target = reporter.getTarget();
		
		if (target == null)
		{
			return false;
		}
		
		L2PcInstance bot = target.getActingPlayer();
		
		if ((bot == null) || (target.getObjectId() == reporter.getObjectId()))
		{
			return false;
		}
		
		if (bot.isInsideZone(ZoneId.PEACE) || bot.isInsideZone(ZoneId.PVP))
		{
			reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHARACTER_IN_PEACE_OR_BATTLE_ZONE);
			return false;
		}
		
		if (bot.isInOlympiadMode())
		{
			reporter.sendPacket(SystemMessageId.TARGET_NOT_REPORT_CANNOT_REPORT_PEACE_PVP_ZONE_OR_OLYMPIAD_OR_CLAN_WAR_ENEMY);
			return false;
		}
		
		if ((bot.getClan() != null) && bot.getClan().isAtWarWith(reporter.getClan()))
		{
			reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CLAN_WAR_ENEMY);
			return false;
		}
		
		if (bot.getExp() == bot.getStat().getStartingExp())
		{
			reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHAR_WHO_ACQUIRED_XP);
			return false;
		}
		
		ReportedCharData rcd = _reports.get(bot.getObjectId());
		ReporterCharData rcdRep = _charRegistry.get(reporter.getObjectId());
		final int reporterId = reporter.getObjectId();
		
		synchronized (this)
		{
			if (_reports.containsKey(reporterId))
			{
				reporter.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AND_CANNOT_REPORT);
				return false;
			}
			
			final int ip = hashIp(reporter);
			if (!timeHasPassed(_ipRegistry, ip))
			{
				reporter.sendPacket(SystemMessageId.CANNOT_REPORT_TARGET_ALREDY_REPORTED_BY_CLAN_ALLY_MEMBER_OR_SAME_IP);
				return false;
			}
			
			if (rcd != null)
			{
				if (rcd.alredyReportedBy(reporterId))
				{
					reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_1);
					return false;
				}
				
				if (!Config.BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS && rcd.reportedBySameClan(reporter.getClan()))
				{
					reporter.sendPacket(SystemMessageId.CANNOT_REPORT_TARGET_ALREDY_REPORTED_BY_CLAN_ALLY_MEMBER_OR_SAME_IP);
					return false;
				}
			}
			
			if (rcdRep != null)
			{
				if (rcdRep.getPointsLeft() == 0)
				{
					reporter.sendPacket(SystemMessageId.YOU_HAVE_USED_ALL_POINTS_POINTS_ARE_RESET_AT_NOON);
					return false;
				}
				
				long reuse = (System.currentTimeMillis() - rcdRep.getLastReporTime());
				if (reuse < Config.BOTREPORT_REPORT_DELAY)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_REPORT_IN_S1_MINS_YOU_HAVE_S2_POINTS_LEFT);
					sm.addInt((int) (reuse / 60000));
					sm.addInt(rcdRep.getPointsLeft());
					reporter.sendPacket(sm);
					return false;
				}
			}
			
			final long curTime = System.currentTimeMillis();
			
			if (rcd == null)
			{
				rcd = new ReportedCharData();
				_reports.put(bot.getObjectId(), rcd);
			}
			rcd.addReporter(reporterId, curTime);
			
			if (rcdRep == null)
			{
				rcdRep = new ReporterCharData();
			}
			rcdRep.registerReport(curTime);
			
			_ipRegistry.put(ip, curTime);
			_charRegistry.put(reporterId, rcdRep);
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_REPORTED_AS_BOT);
		sm.addCharName(bot);
		reporter.sendPacket(sm);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_USED_REPORT_POINT_ON_C1_YOU_HAVE_C2_POINTS_LEFT);
		sm.addCharName(bot);
		sm.addInt(rcdRep.getPointsLeft());
		reporter.sendPacket(sm);
		
		handleReport(bot, rcd);
		
		return true;
	}
	
	/**
	 * Find the punishs to apply to the given bot and triggers the punish method.
	 * @param bot (L2PcInstance to be punished)
	 * @param rcd (RepotedCharData linked to this bot)
	 */
	private void handleReport(L2PcInstance bot, final ReportedCharData rcd)
	{
		// Report count punishment
		punishBot(bot, _punishments.get(rcd.getReportCount()));
		
		// Range punishments
		for (int key : _punishments.keySet())
		{
			if ((key < 0) && (Math.abs(key) <= rcd.getReportCount()))
			{
				punishBot(bot, _punishments.get(key));
			}
		}
	}
	
	/**
	 * Applies the given punish to the bot if the action is secure
	 * @param bot (L2PcInstance to punish)
	 * @param ph (PunishHolder containing the debuff and a possible system message to send)
	 */
	private void punishBot(L2PcInstance bot, PunishHolder ph)
	{
		if (ph != null)
		{
			ph._punish.applyEffects(bot, bot);
			if (ph._systemMessageId > -1)
			{
				SystemMessageId id = SystemMessageId.getSystemMessageId(ph._systemMessageId);
				if (id != null)
				{
					bot.sendPacket(id);
				}
			}
		}
	}
	
	/**
	 * Adds a debuff punishment into the punishments record. If skill does not exist, will log it and return
	 * @param neededReports (report count to trigger this debuff)
	 * @param skillId
	 * @param skillLevel
	 * @param sysMsg (id of a system message to send when applying the punish)
	 */
	void addPunishment(int neededReports, int skillId, int skillLevel, int sysMsg)
	{
		Skill sk = SkillData.getInstance().getSkill(skillId, skillLevel);
		if (sk != null)
		{
			_punishments.put(neededReports, new PunishHolder(sk, sysMsg));
		}
		else
		{
			LOGGER.warning("BotReportTable: Could not add punishment for " + neededReports + " report(s): Skill " + skillId + "-" + skillLevel + " does not exist!");
		}
	}
	
	void resetPointsAndSchedule()
	{
		synchronized (_charRegistry)
		{
			for (ReporterCharData rcd : _charRegistry.values())
			{
				rcd.setPoints(7);
			}
		}
		
		scheduleResetPointTask();
	}
	
	private void scheduleResetPointTask()
	{
		try
		{
			String[] hour = Config.BOTREPORT_RESETPOINT_HOUR;
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
			c.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
			
			if (System.currentTimeMillis() > c.getTimeInMillis())
			{
				c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new ResetPointTask(), c.getTimeInMillis() - System.currentTimeMillis());
		}
		catch (Exception e)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new ResetPointTask(), 24 * 3600 * 1000);
			LOGGER.log(Level.WARNING, "BotReportTable: Could not properly schedule bot report points reset task. Scheduled in 24 hours.", e);
		}
	}
	
	public static BotReportTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * Returns a integer representative number from a connection
	 * @param player (The L2PcInstance owner of the connection)
	 * @return int (hashed ip)
	 */
	private static int hashIp(L2PcInstance player)
	{
		String con = player.getClient().getConnection().getInetAddress().getHostAddress();
		String[] rawByte = con.split("\\.");
		int[] rawIp = new int[4];
		for (int i = 0; i < 4; i++)
		{
			rawIp[i] = Integer.parseInt(rawByte[i]);
		}
		
		return rawIp[0] | (rawIp[1] << 8) | (rawIp[2] << 16) | (rawIp[3] << 24);
	}
	
	/**
	 * Checks and return if the abstrat barrier specified by an integer (map key) has accomplished the waiting time
	 * @param map (a Map to study (Int = barrier, Long = fully qualified unix time)
	 * @param objectId (an existent map key)
	 * @return true if the time has passed.
	 */
	private static boolean timeHasPassed(Map<Integer, Long> map, int objectId)
	{
		if (map.containsKey(objectId))
		{
			return (System.currentTimeMillis() - map.get(objectId)) > Config.BOTREPORT_REPORT_DELAY;
		}
		return true;
	}
	
	/**
	 * Represents the info about a reporter
	 */
	private final class ReporterCharData
	{
		private long _lastReport;
		private byte _reportPoints;
		
		ReporterCharData()
		{
			_reportPoints = 7;
			_lastReport = 0;
		}
		
		void registerReport(long time)
		{
			_reportPoints -= 1;
			_lastReport = time;
		}
		
		long getLastReporTime()
		{
			return _lastReport;
		}
		
		byte getPointsLeft()
		{
			return _reportPoints;
		}
		
		void setPoints(int points)
		{
			_reportPoints = (byte) points;
		}
	}
	
	/**
	 * Represents the info about a reported character
	 */
	private final class ReportedCharData
	{
		Map<Integer, Long> _reporters;
		
		ReportedCharData()
		{
			_reporters = new HashMap<>();
		}
		
		int getReportCount()
		{
			return _reporters.size();
		}
		
		boolean alredyReportedBy(int objectId)
		{
			return _reporters.containsKey(objectId);
		}
		
		void addReporter(int objectId, long reportTime)
		{
			_reporters.put(objectId, reportTime);
		}
		
		boolean reportedBySameClan(L2Clan clan)
		{
			if (clan == null)
			{
				return false;
			}
			
			for (int reporterId : _reporters.keySet())
			{
				if (clan.isMember(reporterId))
				{
					return true;
				}
			}
			
			return false;
		}
	}
	
	/**
	 * SAX loader to parse /config/botreport_punishments.xml file
	 */
	private final class PunishmentsLoader extends DefaultHandler
	{
		PunishmentsLoader()
		{
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attr)
		{
			if (qName.equals("punishment"))
			{
				int reportCount = -1, skillId = -1, skillLevel = 1, sysMessage = -1;
				try
				{
					reportCount = Integer.parseInt(attr.getValue("neededReportCount"));
					skillId = Integer.parseInt(attr.getValue("skillId"));
					String level = attr.getValue("skillLevel");
					String systemMessageId = attr.getValue("sysMessageId");
					if (level != null)
					{
						skillLevel = Integer.parseInt(level);
					}
					
					if (systemMessageId != null)
					{
						sysMessage = Integer.parseInt(systemMessageId);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				addPunishment(reportCount, skillId, skillLevel, sysMessage);
			}
		}
	}
	
	class PunishHolder
	{
		final Skill _punish;
		final int _systemMessageId;
		
		PunishHolder(final Skill sk, final int sysMsg)
		{
			_punish = sk;
			_systemMessageId = sysMsg;
		}
	}
	
	class ResetPointTask implements Runnable
	{
		@Override
		public void run()
		{
			resetPointsAndSchedule();
			
		}
	}
	
	private static final class SingletonHolder
	{
		static final BotReportTable INSTANCE = new BotReportTable();
	}
}
