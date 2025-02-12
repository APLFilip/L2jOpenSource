package l2f.gameserver.model.premium;

public enum PremiumKeys
{
	EXP,
	SP,
	SIEGE,
	ADENA,
	DROP,
	SPOIL,
	CRAFT,
	MASTERWORK_CRAFT,
	WEIGHT_LIMIT;
	
	private static final PremiumKeys[] VALUES = values();
	
	private PremiumKeys()
	{
	}
	
	public static PremiumKeys find(String name)
	{
		for (PremiumKeys key : VALUES)
		{
			if (key.name().equalsIgnoreCase(name))
			{
				return key;
			}
		}
		return null;
	}
}
