package l2s.gameserver.network.l2.s2c.updatetype;

/**
 * @author UnAfraid
 */
public enum NpcInfoType implements IUpdateTypeComponent
{
	// 0
	ID(0x00, 4),
	ATTACKABLE(0x01, 1),
	UNKNOWN1(0x02, 4),
	NAME(0x03, 2),
	POSITION(0x04, (3 * 4)),
	HEADING(0x05, 4),
	UNKNOWN2(0x06, 4),
	ATK_CAST_SPEED(0x07, (2 * 4)),
	
	// 1
	SPEED_MULTIPLIER(0x08, (2 * 4)),
	EQUIPPED(0x09, (3 * 4)),
	ALIVE(0x0A, 1),
	RUNNING(0x0B, 1),
	SWIM_OR_FLY(0x0E, 1),
	TEAM(0x0F, 1),
	
	// 2
	ENCHANT(0x10, 4),
	FLYING(0x11, 4),
	CLONE(0x12, 4),
	UNKNOWN8(0x13, 4),
	DISPLAY_EFFECT(0x16, 4),
	TRANSFORMATION(0x17, 4),
	
	// 3
	CURRENT_HP(0x18, 4),
	CURRENT_MP(0x19, 4),
	MAX_HP(0x1A, 4),
	MAX_MP(0x1B, 4),
	SUMMONED(0x1C, 1),
	UNKNOWN12(0x1D, (2 * 4)),
	TITLE(0x1E, 2),
	NAME_NPCSTRINGID(0x1F, 4),
	
	// 4
	TITLE_NPCSTRINGID(0x20, 4),
	PVP_FLAG(0x21, 1),
	REPUTATION(0x22, 4),
	CLAN(0x23, (5 * 4)),
	ABNORMALS(0x24, 0),
	VISUAL_STATE(0x25, 1);

	public static final NpcInfoType[] VALUES = values();
	
	private final int _mask;
	private final int _blockLength;
	
	private NpcInfoType(int mask, int blockLength)
	{
		_mask = mask;
		_blockLength = blockLength;
	}
	
	@Override
	public int getMask()
	{
		return _mask;
	}
	
	public int getBlockLength()
	{
		return _blockLength;
	}
}