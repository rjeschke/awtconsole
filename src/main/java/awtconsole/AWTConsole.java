/*
 * Copyright (C) 2011 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package awtconsole;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import awtconsole.io.IntInputStream;
import awtconsole.io.IntOutputStream;

/**
 * <code><p>AWTConsole</code> is a console emulation using AWT, mainly designed for
 * console applications, e.g. roguelikes.</p> 
 * <p>Various charsets are included, 256 colors and a fake fullscreen mode is available.
 * The ASCII codepage used is 850 (see http://de.wikipedia.org/wiki/Codepage_850 for infos).</p>
 * Example:
 * <p><pre>
 * AWTConsole console = new AWTConsole("Console", 80, 25, CharsetType.CHARS_8x16);
 * console.enableWindowClosing(false);
 * console.show();
 * 
 * console.printStringAt(0, 0, "Hello world!", 15, 0);
 * console.update();
 * for(;;)
 * {
 * 	final int ch = console.waitKey();
 * 	if(ch == Keys.TOGGLE_FULLSCREEN) console.toggleFullscreen(false);
 * 	if(ch == Keys.ESCAPE) break;
 * }
 * console.close();
 * </pre></p>
 * @author René 'Neotec' Jeschke
 */
public class AWTConsole
{
	private int columns, rows;
	private ConsoleFrame frame;
	private final int[] iColors = new int[256];
	private final int[] iPreColors = new int[256];
	private final String title;
	private CharsetType charset;
	private int[] screen, backup;
	private List<Integer> keyBuffer = new Vector<Integer>();
	private volatile boolean initialized = false;
	private boolean enableWindowClosing = true, fullscreen = false;
	private final Cursor transparentCursor;
	private float gamma = 1.0f;
	private List<ScreenBuffer> buffers = new ArrayList<ScreenBuffer>();
	private static HashMap<Integer, Integer> UNI2ASCII = new HashMap<Integer, Integer>();
	private static boolean mappingDone = false;
	
	private final int[] stdColors = new int[]
	{
			0x000000, 0x800000, 0x008000, 0x808000,
			0x000080, 0x800080, 0x008080, 0xc0c0c0,
			0x808080, 0xff0000, 0x00ff00, 0xffff00,
			0x0000ff, 0xff00ff, 0x00ffff, 0xffffff
	};

	/**
	 * Constructs a new AWTConsole
	 *
	 * @param title the frame's title
	 * @param columns number of columns
	 * @param rows number of rows
	 * @param charset the character set to use
	 */
	public AWTConsole(String title, int columns, int rows, CharsetType charset)
	{
		if(!mappingDone)
		{
			initializeMapping();
			mappingDone = true;
		}
		this.frame = new ConsoleFrame(this, title, columns, rows, charset, false, false);
		this.title = title;
		this.charset = charset;
		this.columns = columns;
		this.rows = rows;
		this.screen = new int[columns * rows];
		this.backup = new int[this.screen.length * 3];
		
		this.cls();
		
		for(int i = 0; i < 256; i++)
			this.iPreColors[i] = this.iColors[i] = this.stdColors[i & 15] | 0xff000000;

		this.transparentCursor = 
			Toolkit.getDefaultToolkit().createCustomCursor(
					new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB_PRE),
					//Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, new int[16 * 16], 0, 16)), 
					new Point(0, 0), "invisibleCursor");
	}
	
	public void enableAntialiasing(boolean on)
	{
		this.frame.enableAntialiasing(on);
	}
	
	/**
	 * Gets the current charset used.
	 * @return the charset type
	 */
	public CharsetType getCharsetType()
	{
		return this.charset;
	}
	
	/**
	 * Sets the current charset.
	 * @param type the charset to use
	 */
	public void setCharsetType(CharsetType type)
	{
		if(type != this.charset)
		{
			this.charset = type;
			this.frame.setCharsetType(type);
			this.invalidateScreen();
			this.update();
		}
	}
	
	public void invalidateScreen()
	{
		Arrays.fill(this.backup, -1);
	}
	
	/**
	 * Enables or disables window closing by clicking the close button.
	 * 
	 * @param on <code>true</code> to enable closing, <code>false</code> to disable it
	 */
	public void enableWindowClosing(boolean on)
	{
		this.frame.setCloseEnabled(this.enableWindowClosing = on);
	}
	
	/**
	 * Gets the height of this console
	 * @return this console's height
	 */
	public int getHeight()
	{
		return this.rows;
	}
	
	/**
	 * Gets the width of this console
	 * @return this console's width
	 */
	public int getWidth()
	{
		return this.columns;
	}
	
	/**
	 * Sets the console screens gamma value
	 * @param value gamma to use
	 */
	public void setGamma(float value)
	{
		this.gamma = 1.0f / (value < 0.01f ? 0.01f : value > 3.0f ? 3.0f : value);
		
		for(int i = 0; i < 256; i++)
		{
			this.setColor(i, this.iPreColors[i]);
		}
	}
	
	/**
	 * Gets the screen buffer of this console. Cell layout is: 0x00BBFFCC, where
	 * <code>BB</code> is the background color, <code>FF</code> is the foreground color and
	 * <code>CC</code> is the char using codepage 850 ASCII.
	 * @return this console's screen buffer
	 */
	public int[] getScreen()
	{
		return this.screen;
	}

	/**
	 * Creates an auxiliary screen buffer
	 * @return the index of the newly created buffer
	 */
	public int createBuffer()
	{
		final int ret = this.buffers.size();
		this.buffers.add(new ScreenBuffer(this.columns, this.rows));
		return ret;
	}
	
	/**
	 * Copies the current screen contents into an auxiliary buffer
	 * @param index the auxiliary buffer index
	 */
	public void setBuffer(int index)
	{
		this.buffers.get(index).set(this.screen);
	}
	
	/**
	 * Copies the contents of an auxiliary buffer into the screen buffer
	 * @param index the auxiliary buffer index
	 */
	public void getBuffer(int index)
	{
		this.buffers.get(index).get(this.screen);
	}
	
	private void printThin(int x, int y, int dir, int fg, int bg)
	{
		final int tmp = this.evalThin(this.getCell(x, y) & 255);
		this.printAsciiAt(x, y, tmp == 0 ? this.getThinChar(dir) : this.getThinChar(dir | tmp), fg, bg);
	}
	
	public boolean loadPalette(InputStream stream)
	{
		try
		{
			final IntInputStream in = new IntInputStream(stream);
			for(int i = 0; i < 256; i++)
				this.setColor(i, in.readI24());
			in.close();
		}
		catch(IOException e)
		{
			return false;
		}
		return true;
	}
	
	public boolean loadPalette(String filename)
	{
		try
		{
			final IntInputStream in = new IntInputStream(filename);
			for(int i = 0; i < 256; i++)
				this.setColor(i, in.readI24());
			in.close();
		}
		catch(IOException e)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Draws a border using the thin ASCII drawing chars. Overlapping frames will be
	 * rendered correctly.
	 * @param x column to start
	 * @param y row to start
	 * @param w the width
	 * @param h the height
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void drawThinFrame(int x, int y, int w, int h, int fg, int bg)
	{
		if(w < 2 || h < 2) return;

		this.printThin(x, y, 6, fg, bg);
		this.printThin(x + w - 1, y, 12, fg, bg);
		this.printThin(x, y + h - 1, 3, fg, bg);
		this.printThin(x + w - 1, y + h - 1, 9, fg, bg);
		
		for(int i = 1; i < w - 1; i++)
		{
			this.printThin(x + i, y, 10, fg, bg);
			this.printThin(x + i, y + h - 1, 10, fg, bg);
		}
		
		for(int i = 1; i < h - 1; i++)
		{
			this.printThin(x, y + i, 5, fg, bg);
			this.printThin(x + w - 1, y + i, 5, fg, bg);
		}
	}

	private void printThick(int x, int y, int dir, int fg, int bg)
	{
		final int tmp = this.evalThick(this.getCell(x, y) & 255);
		this.printAsciiAt(x, y, tmp == 0 ? this.getThickChar(dir) : this.getThickChar(dir | tmp), fg, bg);
	}
	
	/**
	 * Draws a border using the thick (double) ASCII drawing chars. Overlapping frames will be
	 * rendered correctly.
	 * @param x column to start
	 * @param y row to start
	 * @param w the width
	 * @param h the height
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void drawThickFrame(int x, int y, int w, int h, int fg, int bg)
	{
		if(w < 2 || h < 2) return;

		this.printThick(x, y, 6, fg, bg);
		this.printThick(x + w - 1, y, 12, fg, bg);
		this.printThick(x, y + h - 1, 3, fg, bg);
		this.printThick(x + w - 1, y + h - 1, 9, fg, bg);
		
		for(int i = 1; i < w - 1; i++)
		{
			this.printThick(x + i, y, 10, fg, bg);
			this.printThick(x + i, y + h - 1, 10, fg, bg);
		}
		
		for(int i = 1; i < h - 1; i++)
		{
			this.printThick(x, y + i, 5, fg, bg);
			this.printThick(x + w - 1, y + i, 5, fg, bg);
		}
	}

	private int evalThin(int ch)
	{
		for(int i = 0; i < 16; i++)
		{
			if(THIN_CHARS[i] != 0 && THIN_CHARS[i] == ch) return i;
		}
		return 0;
	}
	
	private int getThinChar(int dirs)
	{
		return THIN_CHARS[dirs & 15];
	}
	
	private int evalThick(int ch)
	{
		for(int i = 0; i < 16; i++)
		{
			if(THICK_CHARS[i] != 0 && THICK_CHARS[i] == ch) return i;
		}
		return 0;
	}

	private int getThickChar(int dirs)
	{
		return THICK_CHARS[dirs & 15];
	}

	/**
	 * Gets the cell value at the specified position.
	 * @param x the column
	 * @param y the row
	 * @return the cell's value
	 */
	public int getCell(int x, int y)
	{
		final int x1 = x < 0 ? 0 : x >= this.columns ? this.columns - 1 : x;
		final int y1 = y < 0 ? 0 : y >= this.rows ? this.rows - 1 : y;
		return this.screen[x1 + y1 * this.columns] & 0xffffff;
	}
	
	/**
	 * Sets the cell value at the given position.
	 * @param x the column
	 * @param y the row
	 * @param cell the cell's new value
	 */
	public void setCell(int x, int y, int cell)
	{
		final int x1 = x < 0 ? 0 : x >= this.columns ? this.columns - 1 : x;
		final int y1 = y < 0 ? 0 : y >= this.rows ? this.rows - 1 : y;
		this.screen[x1 + y1 * this.columns] = cell & 0xffffff;
	}
	
	/**
	 * Prints an unicode char at the given position.
	 * @param x the column
	 * @param y the row
	 * @param ch the char
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void printCharAt(int x, int y, char ch, int fg, int bg)
	{
		this.printAsciiAt(x, y, unicode2ascii(ch), fg, bg);
	}
	
	/**
	 * Prints an ASCII char at the given position.
	 * @param x the column
	 * @param y the row
	 * @param ch the char
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void printAsciiAt(int x, int y, int ch, int fg, int bg)
	{
		final int c = ch & 255;
		final int fgc = fg & 255;
		final int bgc = bg & 255;
		final int x1 = x < 0 ? 0 : x >= this.columns ? this.columns - 1 : x;
		final int y1 = y < 0 ? 0 : y >= this.rows ? this.rows - 1 : y;
		this.screen[x1 + y1 * this.columns] = (bgc << 16) | (fgc << 8) | (c & 255);
	}

	/**
	 * Prints a string at the given position.
	 * @param x the column
	 * @param y the row
	 * @param str the string to print
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void printStringAt(int x, int y, String str, int fg, int bg)
	{
		char[] chars = str.toCharArray();
		final int fgc = fg & 255;
		final int bgc = bg & 255;
		int x1 = x < 0 ? 0 : x >= this.columns ? this.columns - 1 : x;
		int y1 = y < 0 ? 0 : y >= this.rows ? this.rows - 1 : y;
		
		for(int i = 0; i < chars.length; i++)
		{
			final int c = unicode2ascii(chars[i]);
			this.screen[x1 + y1 * this.columns] = (bgc << 16) | (fgc << 8) | (c & 255);
			x1++;
			if(x1 == this.columns)
			{
				x1 = 0;
				y1++;
				if(y1 == this.rows)
				{
					x1 = this.columns - 1;
					y1 = this.rows - 1;
				}
			}
		}
	}

	/**
	 * Scrolls the screen in the given direction.
	 * @param dir the direction to scroll to
	 */
	public void scroll(Direction dir)
	{
		this.scroll(0, 0, this.columns, this.rows, dir);
	}
	
	/**
	 * Scrolls part of the screen in the given direction.
	 * @param x the column to start
	 * @param y the row to start
	 * @param w the width
	 * @param h the height
	 * @param dir the direction to scroll to
	 */
	public void scroll(int x, int y, int w, int h, Direction dir)
	{
		if(w < 1 || h < 1) return;
		
		switch(dir)
		{
		case N:
			for(int n = 0; n < h - 1; n++)
			{
				for(int i = 0; i < w; i++)
				{
					this.setCell(x + i, y + n, this.getCell(x + i, y + n + 1));
				}
			}
			for(int i = 0; i < w; i++)
				this.setCell(x + i, y + h - 1, 32);
			break;
		case NE:
			this.scroll(x, y, w, h, Direction.N);
			this.scroll(x, y, w, h, Direction.E);
			break;
		case E:
			for(int n = 0; n < h; n++)
			{
				for(int i = w - 1; i > 0; i--)
				{
					this.setCell(x + i, y + n, this.getCell(x + i - 1, y + n));
				}
			}
			for(int i = 0; i < h; i++)
				this.setCell(x, y + i, 32);
			break;
		case SE:
			this.scroll(x, y, w, h, Direction.S);
			this.scroll(x, y, w, h, Direction.E);
			break;
		case S:
			for(int n = h - 1; n > 0; n--)
			{
				for(int i = 0; i < w; i++)
				{
					this.setCell(x + i, y + n, this.getCell(x + i, y + n - 1));
				}
			}
			for(int i = 0; i < w; i++)
				this.setCell(x + i, y, 32);
			break;
		case SW:
			this.scroll(x, y, w, h, Direction.S);
			this.scroll(x, y, w, h, Direction.W);
			break;
		case W:
			for(int n = 0; n < h; n++)
			{
				for(int i = 0; i < w - 1; i++)
				{
					this.setCell(x + i, y + n, this.getCell(x + i + 1, y + n));
				}
			}
			for(int i = 0; i < h; i++)
				this.setCell(x + w - 1, y + i, 32);
			break;
		case NW:
			this.scroll(x, y, w, h, Direction.N);
			this.scroll(x, y, w, h, Direction.W);
			break;
		}
	}
	
	/**
	 * Reads a line of text from the user.
	 * @param x column for visual feedback
	 * @param y row for visual feedback
	 * @param maxlen maximum length of input string
	 * @param fg the foreground color
	 * @param bg the background color
	 * @return the string 
	 */
	public String readLine(int x, int y, int maxlen, int fg, int bg)
	{
		char[] input = new char[maxlen];
		Arrays.fill(input, ' ');
		int len = 0, pos = 0;
		for(;;)
		{
			for(int i = 0; i < maxlen; i++)
			{
				if(i == pos)
					this.printCharAt(x + i, y, input[i], bg, fg);
				else
					this.printCharAt(x + i, y, input[i], fg, bg);
			}
			this.update();
			int ch = this.waitKey();
			if(ch < 32 || ch == Keys.DELETE)
			{
				if(ch == Keys.ENTER)
					break;
				switch(ch)
				{
				case Keys.CURSOR_LEFT:
					if(pos > 0) pos--;
					break;
				case Keys.CURSOR_RIGHT:
					if(pos < len && pos < maxlen - 1) pos++;
					break;
				case Keys.HOME:
				case Keys.CURSOR_DOWN:
					pos = 0;
					break;
				case Keys.END:
				case Keys.CURSOR_UP:
					pos = len < maxlen ? len : maxlen - 1;
					break;
				case Keys.BACKSPACE:
					if(pos > 0)
					{
						for(int i = pos; i < maxlen; i++)
							input[i - 1] = input[i];
						input[maxlen - 1] = ' ';
						pos--;
						len--;
					}
					break;
				case Keys.INSERT:
					if(pos < maxlen - 1)
					{
						for(int i = maxlen - 1; i > pos; i--)
							input[i] = input[i - 1];
						input[pos] = ' ';
						if(len < maxlen) len++;
					}
					break;
				case Keys.DELETE:
					if(pos < maxlen - 1)
					{
						if(pos < len) len--;
						for(int i = pos; i < maxlen - 1; i++)
							input[i] = input[i + 1];
						input[maxlen - 1] = ' ';
					}					
					break;
				}
			}
			else
			{
				input[pos] = (char)ch;
				if(pos + 1 > len) len = pos + 1;
				if(pos < maxlen - 1)
				{
					pos++;
				}
			}
		}
		for(int i = 0; i < maxlen; i++)
		{
			this.printCharAt(x + i, y, input[i], fg, bg);
		}
		this.update();
		return new String(input, 0, len);
	}
	
	/**
	 * Clears the screen.
	 */
	public void cls()
	{
		Arrays.fill(this.screen, 32);
	}
	
	/**
	 * Fills the screen with given unicode char and colors.
	 * @param ch the unicode char
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void fillChar(char ch, int fg, int bg)
	{
		Arrays.fill(this.screen, ((bg & 255) << 16) | ((fg & 255) << 8) | unicode2ascii(ch));
	}
	
	/**
	 * Fills the screen with given ASCII char and colors.
	 * @param ch the ASCII char
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void fillAscii(int ch, int fg, int bg)
	{
		Arrays.fill(this.screen, ((bg & 255) << 16) | ((fg & 255) << 8) | (ch & 255));
	}

	/**
	 * Fills part of the screen with given unicode char and colors.
	 * @param x the column to start
	 * @param y the row to start
	 * @param w the width
	 * @param h the height
	 * @param ch the unicode char
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void fillChar(int x, int y, int w, int h, char ch, int fg, int bg)
	{
		final int cell = ((bg & 255) << 16) | ((fg & 255) << 8) | unicode2ascii(ch);
		for(int n = 0; n < h; n++)
			for(int i = 0; i < w; i++)
				this.setCell(x + i, y + n, cell);
	}

	/**
	 * Fills part of the screen with given ASCII char and colors.
	 * @param x the column to start
	 * @param y the row to start
	 * @param w the width
	 * @param h the height
	 * @param ch the ASCII char
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void fillAscii(int x, int y, int w, int h, int ch, int fg, int bg)
	{
		final int cell = ((bg & 255) << 16) | ((fg & 255) << 8) | (ch & 255);
		for(int n = 0; n < h; n++)
			for(int i = 0; i < w; i++)
				this.setCell(x + i, y + n, cell);
	}

	/**
	 * Changes the color of a screen region.
	 * @param x the column to start
	 * @param y the row to start
	 * @param w the width
	 * @param h the height
	 * @param fg the foreground color
	 * @param bg the background color
	 */
	public void colorize(int x, int y, int w, int h, int fg, int bg)
	{
		final int c = ((bg & 255) << 16) | ((fg & 255) << 8);
		for(int n = 0; n < h; n++)
			for(int i = 0; i < w; i++)
				this.setCell(x + i, y + n, (this.getCell(x + i, y + n) & 255) | c);
	}
	
	/**
	 * Renders the contents of the screen buffer. This method must be called
	 * to make any change done to the screen buffer visible.
	 */
	public void update()
	{
		for(int y = 0; y < this.rows; y++)
		{
			for(int x = 0; x < this.columns; x++)
			{
				int p = x + y * this.columns;
				final int c = this.screen[p];
				final int f = this.iColors[(c >> 8) & 255];
				final int b = this.iColors[(c >> 16) & 255];
				p *= 3;
				if(this.backup[p] != c || f != this.backup[p + 1] || b != this.backup[p + 2])
				{
					this.frame.drawChar(x, y, c & 255, f, b);
					this.backup[p] = c;
					this.backup[p + 1] = f;
					this.backup[p + 2] = b;
				}
			}
		}
		this.frame.repaint();
		
		if(this.recording)
		{
			long delta;
			if(this.recordingStarted == -1L)
			{
				delta = 0;
				this.recordingStarted = System.nanoTime();
			}
			else
			{
				delta = System.nanoTime() - this.recordingStarted;
			}
			this.saveScreen(new File(this.recordPath, String.format("%08d.scr", this.imageCounter++)).toString(), delta);
		}
	}
	
	/**
	 * Displays the console's frame.
	 */
	public void show()
	{
		if(!this.frame.isVisible())
		{
			this.frame.setVisible(true);
			while(!this.initialized)
				this.sleep(5);
			((Component)this.frame).setCursor(this.transparentCursor);
		}
	}

	/**
	 * Sleeps for given milliseconds
	 * @param millis the number of milliseconds to sleep
	 * @return <code>true</code> if uninterrupted, <code>false</code> otherwise
	 */
	public boolean sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Closes and disposes this console.
	 */
	public void close()
	{
		this.frame.dispose();
	}
	
	/**
	 * Sets a color inside of this console's color table
	 * @param index the color's index (0 - 255)
	 * @param rgb the color in 0xRRGGBB format
	 */
	public void setColor(int index, int rgb)
	{
		this.iPreColors[index & 255] = 0xff000000 | rgb;
		
		int r = (int)(Math.pow(((rgb & 0xff0000) >> 16) / 255.0f, this.gamma) * 255.0f);
		int g = (int)(Math.pow(((rgb & 0x00ff00) >> 8) / 255.0f, this.gamma) * 255.0f);
		int b = (int)(Math.pow((rgb & 0x0000ff) / 255.0f, this.gamma) * 255.0f);
		
		this.iColors[index & 255] = 0xff000000 | (r << 16) | (g << 8) | b;
	}
	
	/**
	 * Gets a color from this console's color table
	 * @param index the color's index (0 - 255)
	 * @return the color in 0xRRGGBB format
	 */
	public int getColor(int index)
	{
		return this.iPreColors[index & 255] & 0xffffff;
	}

	protected void keyTyped(int ch)
	{
		this.keyBuffer.add(Integer.valueOf(ch));
	}

	/**
	 * Returns a keypress.  
	 * @return <code>0</code> if no key inside the buffer, a char otherwise
	 */
	public int getKey()
	{
		return this.keyBuffer.size() > 0 ? this.keyBuffer.remove(0).intValue() : 0;
	}
	
	/**
	 * Waits until a key is available and returns it
	 * @return the keypress as a char
	 */
	public int waitKey()
	{
		while(this.keyBuffer.size() == 0)
			this.sleep(5);
		return this.keyBuffer.remove(0).intValue();
	}
	
	protected void initialized()
	{
		this.initialized = true;
	}

	/**
	 * Toggles (fake) fullscreen mode.
	 * @param fractionalZoom whether to scale using integers(<code>false</code>) or floats(</code>true</code>)
	 */
	public void toggleFullscreen(boolean fractionalZoom)
	{
		this.initialized = false;
		this.fullscreen = !this.fullscreen;
		this.frame.dispose();
		this.frame = new ConsoleFrame(this, this.title, this.columns, this.rows, this.charset, this.fullscreen, fractionalZoom);
		this.frame.setCloseEnabled(this.enableWindowClosing);
		Arrays.fill(this.backup, -1);
		this.show();
		this.update();
	}

	/**
	 * Converts an ASCII char using codepage 850 to a unicode char
	 * @param c the ASCII char to convert
	 * @return the converted unicode char
	 */
	public static char ascii2unicode(int c)
	{
		return A2U[c & 255];
	}
	
	/**
	 * Comverts an unicode char to an ASCII char using codepage 850
	 * @param c the unicode char to convert
	 * @return the converted ASCII char
	 */
	public static char unicode2ascii(int c)
	{
		Integer a = UNI2ASCII.get(Integer.valueOf(c));
		return a != null ? (char)a.intValue() : '?';
	}

	private static void initializeMapping()
	{
		UNI2ASCII.clear();
		for(int i = 0; i < A2U.length; i++)
		{
			UNI2ASCII.put(Integer.valueOf(A2U[i]), Integer.valueOf(i));
		}
	}

	private final static char[] A2U = new char[] {
		0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
		21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,
		41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,
		61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,
		81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,
		100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,
		115,116,117,118,119,120,121,122,123,124,125,126,127,199,252,
		233,226,228,224,229,231,234,235,232,239,238,236,196,197,201,
		230,198,244,246,242,251,249,255,214,220,248,163,216,215,402,
		225,237,243,250,241,209,170,186,191,174,172,189,188,161,171,
		187,9617,9618,9619,9474,9508,193,194,192,169,9571,9553,9559,
		9565,162,165,9488,9492,9524,9516,9500,9472,9532,227,195,
		9562,9556,9577,9574,9568,9552,9580,164,240,208,202,203,200,
		305,205,206,207,9496,9484,9608,9604,166,204,9600,211,223,
		212,210,245,213,181,254,222,218,219,217,253,221,175,180,173,
		177,8215,190,182,167,247,184,176,168,183,185,179,178,9632,
		160
	};

	private final static int[] THIN_CHARS = new int[] {
		0x00, 0x00, 0x00, 0xc0,
		0x00, 0xb3, 0xda, 0xc3,
		0x00, 0xd9, 0xc4, 0xc1,
		0xbf, 0xb4, 0xc2, 0xc5
	};

	private final static int[] THICK_CHARS = new int[] {
		0x00, 0x00, 0x00, 0xc8,
		0x00, 0xba, 0xc9, 0xcc,
		0x00, 0xbc, 0xcd, 0xca,
		0xbb, 0xb9, 0xcb, 0xce
	};

	private long recordingStarted = 0;
	private File recordPath;
	private int imageCounter = 0;
	private volatile boolean recording = false;
	
	public void startRecording(String path)
	{
		this.recordingStarted = -1;
		this.recordPath = new File(path);
		this.imageCounter = 0;
		this.recording = true;
	}

	public void stopRecording()
	{
		this.recording = false;
	}

	public boolean loadScreen(String filename, boolean skipPalette)
	{
		boolean flag = false;
		try
		{
			final FileInputStream in = new FileInputStream(filename);
			flag = this.loadScreen(in, skipPalette);
			in.close();
		}
		catch(IOException e)
		{
			return false;
		}
		return flag;
	}
	
	public boolean loadScreen(String filename)
	{
		return this.loadScreen(filename, false);
	}
	
	public boolean loadScreen(InputStream stream, boolean skipPalette)
	{
		try
		{
			final IntInputStream in = new IntInputStream(stream);
			if(in.readI32() != 0xdeadaffe)
			{
				in.close();
				return false;
			}
			in.readI64();
			final int w = in.readI16();
			final int h = in.readI16();
			if(w != this.columns || h != this.rows)
			{
				in.close();
				return false;
			}
			if(skipPalette)
			{
				for(int i = 0; i < 256; i++)
					in.readI24();
			}
			else
			{
				for(int i = 0; i < 256; i++)
					this.setColor(i, in.readI24());
			}
			for(int i = 0; i < this.screen.length; i++)
				this.screen[i] = in.readI24();
			in.close();
		}
		catch(IOException e)
		{
			return false;
		}
		return true;
	}

	public boolean loadScreen(InputStream stream)
	{
		return this.loadScreen(stream, false);
	}

	public boolean saveScreen(String filename)
	{
		return this.saveScreen(filename, 0);
	}
	
	public boolean saveScreen(String filename, long timestamp)
	{
		try
		{
			final IntOutputStream out = new IntOutputStream(filename);
			out.writeI32(0xdeadaffe);
			out.writeI64(timestamp);
			out.writeI16(this.columns);
			out.writeI16(this.rows);
			for(int i = 0; i < 256; i++)
				out.writeI24(this.iColors[i]);
			for(int i = 0; i < this.screen.length; i++)
				out.writeI24(this.screen[i]);
			out.close();
		}
		catch(IOException e)
		{
			return false;
		}
		return true;
	}
}

