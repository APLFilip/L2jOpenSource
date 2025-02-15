package com.l2jfrozen.gameserver.network.serverpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.templates.StatsSet;

/**
 * Seven Signs Record Update packet type id 0xf5 format: c cc (Page Num = 1 -> 4, period) 1: [ddd cc dd ddd c ddd c] 2: [hc [cd (dc (S))] 3: [ccc (cccc)] 4: [(cchh)]
 * @author Tempy
 */
public class SSQStatus extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(SSQStatus.class);
	
	private final L2PcInstance activevChar;
	private final int page;
	
	public SSQStatus(final L2PcInstance player, final int recordPage)
	{
		activevChar = player;
		page = recordPage;
	}
	
	@Override
	protected final void writeImpl()
	{
		final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
		final int totalDawnMembers = SevenSigns.getInstance().getTotalMembers(SevenSigns.CABAL_DAWN);
		final int totalDuskMembers = SevenSigns.getInstance().getTotalMembers(SevenSigns.CABAL_DUSK);
		
		writeC(0xf5);
		
		writeC(page);
		writeC(SevenSigns.getInstance().getCurrentPeriod()); // current period?
		
		int dawnPercent = 0;
		int duskPercent = 0;
		
		switch (page)
		{
			case 1:
				// [ddd cc dd ddd c ddd c]
				writeD(SevenSigns.getInstance().getCurrentCycle());
				
				final int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();
				
				switch (currentPeriod)
				{
					case SevenSigns.PERIOD_COMP_RECRUITING:
						writeD(SystemMessageId.INITIAL_PERIOD.getId());
						break;
					case SevenSigns.PERIOD_COMPETITION:
						writeD(SystemMessageId.QUEST_EVENT_PERIOD.getId());
						break;
					case SevenSigns.PERIOD_COMP_RESULTS:
						writeD(SystemMessageId.RESULTS_PERIOD.getId());
						break;
					case SevenSigns.PERIOD_SEAL_VALIDATION:
						writeD(SystemMessageId.VALIDATION_PERIOD.getId());
						break;
				}
				
				switch (currentPeriod)
				{
					case SevenSigns.PERIOD_COMP_RECRUITING:
					case SevenSigns.PERIOD_COMP_RESULTS:
						writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());
						break;
					case SevenSigns.PERIOD_COMPETITION:
					case SevenSigns.PERIOD_SEAL_VALIDATION:
						writeD(SystemMessageId.UNTIL_MONDAY_6PM.getId());
						break;
				}
				
				writeC(SevenSigns.getInstance().getPlayerCabal(activevChar));
				writeC(SevenSigns.getInstance().getPlayerSeal(activevChar));
				
				writeD(SevenSigns.getInstance().getPlayerStoneContrib(activevChar)); // Seal Stones Turned-In
				writeD(SevenSigns.getInstance().getPlayerAdenaCollect(activevChar)); // Ancient Adena to Collect
				
				final double dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(SevenSigns.CABAL_DAWN);
				final int dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(SevenSigns.CABAL_DAWN);
				
				final double duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(SevenSigns.CABAL_DUSK);
				final int duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(SevenSigns.CABAL_DUSK);
				
				final double totalStoneScore = duskStoneScore + dawnStoneScore;
				
				/*
				 * Scoring seems to be proportionate to a set base value, so base this on the maximum obtainable score from festivals, which is 500.
				 */
				int duskStoneScoreProp = 0;
				int dawnStoneScoreProp = 0;
				
				if (totalStoneScore != 0)
				{
					duskStoneScoreProp = Math.round((float) duskStoneScore / (float) totalStoneScore * 500);
					dawnStoneScoreProp = Math.round((float) dawnStoneScore / (float) totalStoneScore * 500);
				}
				
				final int duskTotalScore = SevenSigns.getInstance().getCurrentScore(SevenSigns.CABAL_DUSK);
				final int dawnTotalScore = SevenSigns.getInstance().getCurrentScore(SevenSigns.CABAL_DAWN);
				
				final int totalOverallScore = duskTotalScore + dawnTotalScore;
				
				if (totalOverallScore != 0)
				{
					dawnPercent = Math.round((float) dawnTotalScore / (float) totalOverallScore * 100);
					duskPercent = Math.round((float) duskTotalScore / (float) totalOverallScore * 100);
				}
				
				if (Config.DEBUG)
				{
					LOGGER.info("Dusk Stone Score: " + duskStoneScore + " - Dawn Stone Score: " + dawnStoneScore);
					LOGGER.info("Dusk Festival Score: " + duskFestivalScore + " - Dawn Festival Score: " + dawnFestivalScore);
					LOGGER.info("Dusk Score: " + duskTotalScore + " - Dawn Score: " + dawnTotalScore);
					LOGGER.info("Overall Score: " + totalOverallScore);
					LOGGER.info("");
					if (totalStoneScore == 0)
					{
						LOGGER.info("Dusk Prop: 0 - Dawn Prop: 0");
					}
					else
					{
						LOGGER.info("Dusk Prop: " + duskStoneScore / totalStoneScore * 500 + " - Dawn Prop: " + dawnStoneScore / totalStoneScore * 500);
					}
					LOGGER.info("Dusk %: " + duskPercent + " - Dawn %: " + dawnPercent);
				}
				
				/* DUSK */
				writeD(duskStoneScoreProp); // Seal Stone Score
				writeD(duskFestivalScore); // Festival Score
				writeD(duskTotalScore); // Total Score
				
				writeC(duskPercent); // Dusk %
				
				/* DAWN */
				writeD(dawnStoneScoreProp); // Seal Stone Score
				writeD(dawnFestivalScore); // Festival Score
				writeD(dawnTotalScore); // Total Score
				
				writeC(dawnPercent); // Dawn %
				break;
			case 2:
				// c cc hc [cd (dc (S))]
				writeH(1);
				
				writeC(5); // Total number of festivals
				
				for (int i = 0; i < 5; i++)
				{
					writeC(i + 1); // Current client-side festival ID
					writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);
					
					final int duskScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DUSK, i);
					final int dawnScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DAWN, i);
					
					// Dusk Score \\
					writeD(duskScore);
					
					StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DUSK, i);
					String[] partyMembers = highScoreData.getString("members").split(",");
					
					if (partyMembers != null)
					{
						writeC(partyMembers.length);
						
						for (final String partyMember : partyMembers)
						{
							writeS(partyMember);
						}
					}
					else
					{
						writeC(0);
					}
					
					// Dawn Score \\
					writeD(dawnScore);
					
					highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DAWN, i);
					partyMembers = highScoreData.getString("members").split(",");
					
					if (partyMembers != null)
					{
						writeC(partyMembers.length);
						
						for (final String partyMember : partyMembers)
						{
							writeS(partyMember);
						}
					}
					else
					{
						writeC(0);
					}
				}
				break;
			case 3:
				// c cc [ccc (cccc)]
				writeC(10); // Minimum limit for winning cabal to retain their seal
				writeC(35); // Minimum limit for winning cabal to claim a seal
				writeC(3); // Total number of seals
				
				for (int i = 1; i < 4; i++)
				{
					final int dawnProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
					final int duskProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);
					
					if (Config.DEBUG)
					{
						LOGGER.info(SevenSigns.getSealName(i, true) + " = Dawn Prop: " + dawnProportion + "(" + dawnProportion / totalDawnMembers * 100 + "%)" + ", Dusk Prop: " + duskProportion + "(" + duskProportion / totalDuskMembers * 100 + "%)");
					}
					
					writeC(i);
					writeC(SevenSigns.getInstance().getSealOwner(i));
					
					if (totalDuskMembers == 0)
					{
						if (totalDawnMembers == 0)
						{
							writeC(0);
							writeC(0);
						}
						else
						{
							writeC(0);
							writeC(Math.round((float) dawnProportion / (float) totalDawnMembers * 100));
						}
					}
					else
					{
						if (totalDawnMembers == 0)
						{
							writeC(Math.round((float) duskProportion / (float) totalDuskMembers * 100));
							writeC(0);
						}
						else
						{
							writeC(Math.round((float) duskProportion / (float) totalDuskMembers * 100));
							writeC(Math.round((float) dawnProportion / (float) totalDawnMembers * 100));
						}
					}
				}
				break;
			case 4:
				// c cc [cc (cchh)]
				writeC(winningCabal); // Overall predicted winner
				writeC(3); // Total number of seals
				
				for (int i = 1; i < 4; i++)
				{
					final int dawnProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
					final int duskProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);
					dawnPercent = Math.round(dawnProportion / (totalDawnMembers == 0 ? 1 : (float) totalDawnMembers) * 100);
					duskPercent = Math.round(duskProportion / (totalDuskMembers == 0 ? 1 : (float) totalDuskMembers) * 100);
					final int sealOwner = SevenSigns.getInstance().getSealOwner(i);
					
					writeC(i);
					
					switch (sealOwner)
					{
						case SevenSigns.CABAL_NULL:
							switch (winningCabal)
							{
								case SevenSigns.CABAL_NULL:
									writeC(SevenSigns.CABAL_NULL);
									writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									break;
								case SevenSigns.CABAL_DAWN:
									if (dawnPercent >= 35)
									{
										writeC(SevenSigns.CABAL_DAWN);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
									}
									break;
								case SevenSigns.CABAL_DUSK:
									if (duskPercent >= 35)
									{
										writeC(SevenSigns.CABAL_DUSK);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
									}
									break;
							}
							break;
						case SevenSigns.CABAL_DAWN:
							switch (winningCabal)
							{
								case SevenSigns.CABAL_NULL:
									if (dawnPercent >= 10)
									{
										writeC(SevenSigns.CABAL_DAWN);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
										break;
									}
									writeC(SevenSigns.CABAL_NULL);
									writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									break;
								case SevenSigns.CABAL_DAWN:
									if (dawnPercent >= 10)
									{
										writeC(sealOwner);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
								case SevenSigns.CABAL_DUSK:
									if (duskPercent >= 35)
									{
										writeC(SevenSigns.CABAL_DUSK);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else if (dawnPercent >= 10)
									{
										writeC(SevenSigns.CABAL_DAWN);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
							}
							break;
						case SevenSigns.CABAL_DUSK:
							switch (winningCabal)
							{
								case SevenSigns.CABAL_NULL:
									if (duskPercent >= 10)
									{
										writeC(SevenSigns.CABAL_DUSK);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
										break;
									}
									writeC(SevenSigns.CABAL_NULL);
									writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									break;
								case SevenSigns.CABAL_DAWN:
									if (dawnPercent >= 35)
									{
										writeC(SevenSigns.CABAL_DAWN);
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else if (duskPercent >= 10)
									{
										writeC(sealOwner);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
								case SevenSigns.CABAL_DUSK:
									if (duskPercent >= 10)
									{
										writeC(sealOwner);
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(SevenSigns.CABAL_NULL);
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
							}
							break;
					}
					writeH(0);
				}
				break;
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] F5 RecordUpdate";
	}
}
