package awtconsole;

class ScreenBuffer
{
	public final int[] screen;
	
	protected ScreenBuffer(int width, int height)
	{
		this.screen = new int[width * height];
	}
	
	public void set(int[] src)
	{
		for(int i = 0; i < this.screen.length; i++) this.screen[i] = src[i];
	}

	public void get(int[] dest)
	{
		for(int i = 0; i < this.screen.length; i++) dest[i] = this.screen[i];
	}
}
