package com.l2jfrozen.gameserver.network.gameserverpackets;

public class AuthRequest extends GameServerBasePacket
{
	/**
	 * Format: cccSddb c desired ID c accept alternative ID c reserve Host s ExternalHostName s InetranlHostName d max players d hexid size b hexid
	 * @param id
	 * @param acceptAlternate
	 * @param hexid
	 * @param externalHost
	 * @param internalHost
	 * @param port
	 * @param reserveHost
	 * @param maxplayer
	 */
	public AuthRequest(final int id, final boolean acceptAlternate, final byte[] hexid, final String externalHost, final String internalHost, final int port, final boolean reserveHost, final int maxplayer)
	{
		writeC(0x01);
		writeC(id);
		writeC(acceptAlternate ? 0x01 : 0x00);
		writeC(reserveHost ? 0x01 : 0x00);
		writeS(externalHost);
		writeS(internalHost);
		writeH(port);
		writeD(maxplayer);
		writeD(hexid.length);
		writeB(hexid);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
