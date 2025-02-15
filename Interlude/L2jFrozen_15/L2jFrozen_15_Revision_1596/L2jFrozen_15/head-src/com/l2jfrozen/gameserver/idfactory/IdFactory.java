package com.l2jfrozen.gameserver.idfactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @author  Olympic
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/04/11 10:06:12 $
 */
public abstract class IdFactory
{
	private static Logger LOGGER = Logger.getLogger(IdFactory.class);
	
	protected static final String[] ID_UPDATES =
	{
		"UPDATE items                 SET owner_id = ?    WHERE owner_id = ?",
		"UPDATE items                 SET object_id = ?   WHERE object_id = ?",
		"UPDATE character_quests      SET char_id = ?     WHERE char_id = ?",
		"UPDATE character_friends     SET char_id = ?     WHERE char_id = ?",
		"UPDATE character_friends     SET friend_id = ?   WHERE friend_id = ?",
		"UPDATE character_hennas      SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE character_recipebook  SET char_id = ?     WHERE char_id = ?",
		"UPDATE character_shortcuts   SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE character_shortcuts   SET shortcut_id = ? WHERE shortcut_id = ? AND type = 1", // items
		"UPDATE character_macroses    SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE character_skills      SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE character_skills_save SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE character_subclasses  SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE characters            SET obj_Id = ?      WHERE obj_Id = ?",
		"UPDATE characters            SET clanid = ?      WHERE clanid = ?",
		"UPDATE clan_data             SET clan_id = ?     WHERE clan_id = ?",
		"UPDATE siege_clans           SET clan_id = ?     WHERE clan_id = ?",
		"UPDATE clan_data             SET ally_id = ?     WHERE ally_id = ?",
		"UPDATE clan_data             SET leader_id = ?   WHERE leader_id = ?",
		"UPDATE pets                  SET item_obj_id = ? WHERE item_obj_id = ?",
		"UPDATE character_hennas     SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE itemsonground         SET object_id = ?   WHERE object_id = ?",
		"UPDATE auction_bid          SET bidderId = ?      WHERE bidderId = ?",
		"UPDATE auction_watch        SET charObjId = ?     WHERE charObjId = ?",
		"UPDATE clanhall             SET ownerId = ?       WHERE ownerId = ?"
	};
	
	protected static final String[] ID_CHECKS =
	{
		"SELECT owner_id    FROM items                 WHERE object_id >= ?   AND object_id < ?",
		"SELECT object_id   FROM items                 WHERE object_id >= ?   AND object_id < ?",
		"SELECT char_id     FROM character_quests      WHERE char_id >= ?     AND char_id < ?",
		"SELECT char_id     FROM character_friends     WHERE char_id >= ?     AND char_id < ?",
		"SELECT char_id     FROM character_friends     WHERE friend_id >= ?   AND friend_id < ?",
		"SELECT char_obj_id FROM character_hennas      WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_id     FROM character_recipebook  WHERE char_id >= ?     AND char_id < ?",
		"SELECT char_obj_id FROM character_shortcuts   WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_macroses    WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_skills      WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_skills_save WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_subclasses  WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT obj_Id      FROM characters            WHERE obj_Id >= ?      AND obj_Id < ?",
		"SELECT clanid      FROM characters            WHERE clanid >= ?      AND clanid < ?",
		"SELECT clan_id     FROM clan_data             WHERE clan_id >= ?     AND clan_id < ?",
		"SELECT clan_id     FROM siege_clans           WHERE clan_id >= ?     AND clan_id < ?",
		"SELECT ally_id     FROM clan_data             WHERE ally_id >= ?     AND ally_id < ?",
		"SELECT leader_id   FROM clan_data             WHERE leader_id >= ?   AND leader_id < ?",
		"SELECT item_obj_id FROM pets                  WHERE item_obj_id >= ? AND item_obj_id < ?",
		"SELECT object_id   FROM itemsonground        WHERE object_id >= ?   AND object_id < ?"
	};
	
	protected boolean initialized;
	
	public static final int FIRST_OID = 0x10000000;
	public static final int LAST_OID = 0x7FFFFFFF;
	public static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;
	
	protected static IdFactory instance = null;
	
	protected IdFactory()
	{
		cleanUpDB();
	}
	
	static
	{
		switch (Config.IDFACTORY_TYPE)
		{
			case Compaction:
				instance = new CompactionIDFactory();
				break;
			case BitSet:
				instance = new BitSetIDFactory();
				break;
			case Stack:
				instance = new StackIDFactory();
				break;
		}
	}
	
	private void cleanUpDB()
	{
		try (Connection conn = L2DatabaseFactory.getInstance().getConnection();)
		{
			int cleanCount = 0;
			
			final Statement stmt = conn.createStatement();
			// Character related
			cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.char_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.char_obj_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.char_obj_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM character_quests WHERE character_quests.char_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.char_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.char_obj_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM character_skills WHERE character_skills.char_obj_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.char_obj_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.char_obj_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM cursed_weapons WHERE cursed_weapons.playerId NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM heroes WHERE heroes.char_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM olympiad_nobles WHERE olympiad_nobles.char_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items)");
			cleanCount += stmt.executeUpdate("DELETE FROM seven_signs WHERE seven_signs.char_obj_id NOT IN (SELECT obj_Id FROM characters)");
			// Auction
			cleanCount += stmt.executeUpdate("DELETE FROM auction WHERE auction.id IN (SELECT id FROM clanhall WHERE ownerId <> 0)");
			cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auctionId IN (SELECT id FROM clanhall WHERE ownerId <> 0)");
			// Clan related
			stmt.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid)");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT obj_Id FROM characters)");
			cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data)");
			cleanCount += stmt.executeUpdate("DELETE FROM clanhall_functions WHERE clanhall_functions.hall_id NOT IN (SELECT id FROM clanhall WHERE ownerId <> 0)");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data)");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data)");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data)");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan1 NOT IN (SELECT clan_id FROM clan_data)");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan2 NOT IN (SELECT clan_id FROM clan_data)");
			cleanCount += stmt.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data)");
			stmt.executeUpdate("UPDATE castle SET taxpercent=0 WHERE castle.id NOT IN (SELECT hasCastle FROM clan_data)");
			// Character & clan related
			cleanCount += stmt.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT obj_Id FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data)");
			stmt.executeUpdate("UPDATE characters SET clanid=0 WHERE characters.clanid NOT IN (SELECT clan_id FROM clan_data)");
			// Forum related
			cleanCount += stmt.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2");
			cleanCount += stmt.executeUpdate("DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums)");
			cleanCount += stmt.executeUpdate("DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums)");
			
			stmt.close();
			LOGGER.info("Cleaned " + cleanCount + " elements from database.");
		}
		catch (SQLException e)
		{
		}
	}
	
	/**
	 * @return
	 * @throws SQLException
	 */
	protected int[] extractUsedObjectIDTable() throws SQLException
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			// create a temporary table
			final Statement s = con.createStatement();
			try
			{
				s.executeUpdate("DROP TABLE temporaryObjectTable");
			}
			catch (SQLException e)
			{
			}
			s.executeUpdate("DELETE FROM itemsonground WHERE object_id IN (SELECT object_id FROM items)");
			s.executeUpdate("CREATE table temporaryObjectTable (object_id int NOT NULL PRIMARY KEY)");
			
			s.executeUpdate("INSERT INTO temporaryObjectTable (object_id) SELECT obj_id FROM characters");
			s.executeUpdate("INSERT INTO temporaryObjectTable (object_id) SELECT object_id FROM items");
			s.executeUpdate("INSERT INTO temporaryObjectTable (object_id) SELECT clan_id FROM clan_data");
			// s.executeUpdate("insert into temporaryObjectTable (object_id)" + " select crest_id from clan_data where crest_id > 0");
			s.executeUpdate("INSERT INTO temporaryObjectTable (object_id) SELECT object_id FROM itemsonground");
			
			ResultSet result = s.executeQuery("SELECT COUNT(object_id) FROM temporaryObjectTable");
			
			result.next();
			final int size = result.getInt(1);
			final int[] tmp_obj_ids = new int[size];
			// LOGGER.info("tmp table size: " + tmp_obj_ids.length);
			result.close();
			
			result = s.executeQuery("SELECT object_id FROM temporaryObjectTable ORDER BY object_id");
			
			int idx = 0;
			while (result.next())
			{
				tmp_obj_ids[idx++] = result.getInt(1);
			}
			
			result.close();
			s.close();
			
			return tmp_obj_ids;
		}
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	public static IdFactory getInstance()
	{
		return instance;
	}
	
	public abstract int getNextId();
	
	/**
	 * return a used Object ID back to the pool
	 * @param id
	 */
	public abstract void releaseId(int id);
	
	public abstract int size();
}
