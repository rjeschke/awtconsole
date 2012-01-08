package awtconsole;

public class Keys
{
	public final static int CURSOR_UP = 1;			//
	public final static int CURSOR_DOWN = 2;		//
	public final static int CURSOR_LEFT = 3;		//
	public final static int CURSOR_RIGHT = 4;		//
	public final static int HOME = 5;				//
	public final static int END = 6;				//
	public final static int PAGE_UP = 7;			//
	public final static int BACKSPACE = 8;
	public final static int TAB = 9;
	public final static int ENTER = 10;
	public final static int PAGE_DOWN = 11;			//
	public final static int INSERT = 12;			//
	public final static int F1 = 13;				//
	public final static int F2 = 14;				//
	public final static int F3 = 15;				//
	public final static int F4 = 16;				//
	public final static int F5 = 17;				//
	public final static int F6 = 18;				//
	public final static int F7 = 19;				//
	public final static int F8 = 20;				//
	public final static int F9 = 21;				//
	public final static int F10 = 22;				//
	public final static int F11 = 23;				//
	public final static int F12 = 24;				//
	public final static int TOGGLE_FULLSCREEN = 25;	//
	public final static int CLEAR = 26;				// NUM_5
	public final static int ESCAPE = 27;
	public final static int SPACE = 32;
	public final static int DELETE = 127;
	
	private final static String[] NAMES = new String[] {
		"CURSOR_UP", "CURSOR_DOWN", "CURSOR_LEFT", "CURSOR_RIGHT", "HOME",
		"END", "PAGE_UP", "BACKSPACE", "TAB", "ENTER", "PAGE_DOWN", "INSERT",
		"F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
		"TOGGLE_FULLSCREEN", // 24
		"CLEAR", // 25
		"ESCAPE", // 26
		"SPACE", // 27
		"DELETE" // 28
	};
	
	public static int fromString(String name)
	{
		if(name == null || name.length() == 0)
			return 0;
		for(int i = 0; i < NAMES.length; i++)
		{
			if(NAMES[i].equals(name.toUpperCase()))
			{
				switch(i)
				{
				case 25:	return TOGGLE_FULLSCREEN;
				case 27:	return SPACE;
				case 28:	return DELETE;
				default:	return i + 1;
				}
			}
		}
		return name.charAt(0);
	}
	
	public static String toString(int key)
	{
		if(key > 0 && key < 28)
			return NAMES[key - 1];
		switch(key)
		{
		case 32:	return NAMES[27];
		case 127:	return NAMES[28];
		default:	return Character.toString((char)key);
		}
	}
	
//	public final static int KEY_MASK = 0xffff;
//	public final static int SHIFT_MASK = 0x10000;
//	public final static int ALT_MASK = 0x20000;
//	public final static int CTRL_MASK = 0x40000;
}
