package awtconsole;

/**
 * Enumeration of different charsets. Charsets are codepage 850 standard DOS/Windows
 * console charsets. 
 * @author René 'Neotec' Jeschke
 */
public enum CharsetType
{
	/** Standard, 4x6 pixels */
	CHARS_4x6,
	/** Standard, 5x12 pixels */
	CHARS_5x12,
	/** Standard, 6x8 pixels */
	CHARS_6x8,
	/** Standard, 7x12 pixels */
	CHARS_7x12,
	/** Standard, 8x8 pixels */
	CHARS_8x8,
	/** Standard, 8x12 pixels */
	CHARS_8x12,
	/** Standard, 8x16 pixels */
	CHARS_8x16,
	/** Standard, 10x18 pixels */
	CHARS_10x18,
	/** Standard, 12x16 pixels */
	CHARS_12x16,
	/** Standard, 16x8 pixels */
	CHARS_16x8,
	/** Standard, 16x12 pixels */
	CHARS_16x12;
	
	public static CharsetType fromString(String value)
	{
		final CharsetType[] all = CharsetType.values();
		for(CharsetType c : all)
		{
			if(c.toString().toUpperCase().equals(value.toUpperCase()))
				return c;
		}
		return CHARS_8x12;
	}
}
