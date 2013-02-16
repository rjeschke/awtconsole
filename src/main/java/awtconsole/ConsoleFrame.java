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

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

class ConsoleFrame extends Frame implements WindowListener, KeyListener
{
	private static final long serialVersionUID = 8625221192850547631L;
	private int columns, rows;
	private int charWidth, charHeight, charStride, screenWidth, iMask;
	private int[] chars;
	private BufferedImage screen;
	private int[] pixels;
	private AWTConsole console;
	private boolean closeEnabled = true, isFullscreen = false, fractionalZoom = false, antiAliasing = false;
	private volatile boolean changedCharset = true;
	private int startX, startY, stretchX, stretchY;
	
	protected ConsoleFrame(AWTConsole console, String title, int columns, int rows, CharsetType charset, boolean fullscreen, boolean fractionalZoom)
	{
		super(title);
		this.isFullscreen = fullscreen;
		this.console = console;
		this.rows = rows;
		this.columns = columns;
		final Charset ch = Charset.factory(charset);
		this.charHeight = ch.getHeight();
		this.charWidth = ch.getWidth();
		this.screenWidth = columns * this.charWidth;
		this.charStride = ch.getStride();
		this.iMask = 1 << ((this.charStride * 8) - 1); 
		this.chars = ch.getData();
		this.setSize(this.columns * this.charWidth, this.rows * this.charHeight);
		this.setResizable(false);
		this.addWindowListener(this);
		this.addKeyListener(this);
		this.setFocusTraversalKeysEnabled(false);
		this.setBackground(Color.BLACK);
		this.fractionalZoom = fractionalZoom;
		if(fullscreen)
		{
			this.setUndecorated(true);
		}
	}
	
	public void drawChar(int x, int y, int ch, int fgc, int bgc)
	{
		final int chOffs = ch * this.charHeight;
		final int px = x * this.charWidth, py = y * this.charHeight;
		for(int sy = 0; sy < this.charHeight; sy++)
		{
			final int c = this.chars[chOffs + sy];
			final int o = px + (py + sy) * this.screenWidth;
			int mask = this.iMask;
			for(int i = 0; i < this.charWidth; i++)
			{
				this.pixels[o + i] = (c & mask) != 0 ? fgc : bgc;
				mask >>= 1;
			}
		}
	}

	public void enableAntialiasing(boolean on)
	{
		this.antiAliasing = on;
	}
	
	public void drawScreen(int[] screen, int[] colors)
	{
		int py = 0;
		for(int y = 0; y < this.rows; y++)
		{
			for(int x = 0; x < this.columns; x++)
			{
				final int cell = screen[x + y * this.columns];
				final int chOffs = (cell & 255) * this.charHeight;
				final int fgc = colors[(cell >> 8) & 255];
				final int bgc = colors[(cell >> 16) & 255];
				final int px = x * this.charWidth;
				for(int sy = 0; sy < this.charHeight; sy++)
				{
					final int c = this.chars[chOffs + sy];
					final int o = px + (py + sy) * this.screenWidth;
					int mask = this.iMask;
					for(int i = 0; i < this.charWidth; i++)
					{
						this.pixels[o + i] = (c & mask) != 0 ? fgc : bgc;
						mask >>= 1;
					}
				}
			}
			py += this.charHeight;
		}
	}
	
	protected BufferedImage getImage()
	{
		return this.screen;
	}
	
	public void setCloseEnabled(boolean on)
	{
		this.closeEnabled = on;
	}
	
	@Override
	public void windowClosing(WindowEvent e)
	{
		if(this.closeEnabled)
			this.dispose();
	}

	public void setCharsetType(CharsetType type)
	{
		final Charset ch = Charset.factory(type);
		this.charHeight = ch.getHeight();
		this.charWidth = ch.getWidth();
		this.charStride = ch.getStride();
		this.iMask = 1 << ((this.charStride * 8) - 1); 
		this.chars = ch.getData();
		this.screenWidth = this.columns * this.charWidth;
		this.changedCharset = true;
		this.setupFrame();
	}
	
	private void setupFrame()
	{
		Insets i = this.getInsets();
		final int w = this.columns * this.charWidth;
		final int h = this.rows * this.charHeight;
		if(this.isFullscreen)
		{
			GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
			DisplayMode dm = graphicsDevice.getDisplayMode();
			this.isFullscreen = true;
			final float fx = (float)dm.getWidth() / (float)w; 
			final float fy = (float)dm.getHeight() / (float)h;
			final float f = this.fractionalZoom ? (fx < fy ? fx : fy) : (float)Math.floor(fx < fy ? fx : fy);
			this.stretchX = (int)(w * f);
			this.stretchY = (int)(h * f);
			this.startX = (dm.getWidth() - this.stretchX) >> 1;
			this.startY = (dm.getHeight() - this.stretchY) >> 1;
			this.setSize(dm.getWidth(), dm.getHeight());
			this.setLocation(0, 0);
		}
		else
		{
			this.setSize(i.left + i.right + w, i.top + i.bottom + h);
		}

		this.screen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		this.pixels = ((DataBufferInt)this.screen.getRaster().getDataBuffer()).getData();
		Arrays.fill(this.pixels, 0xff000000);
	}

	@Override
	public void update(Graphics g)
	{
		this.paint(g);
	}
	
	@Override
	public void paint(Graphics g)
	{
		if(!this.isFullscreen)
		{
			Insets i = this.getInsets();
			if(this.screen != null)
				g.drawImage(this.screen, i.left, i.top, null);
		}
		else
		{
			if(this.screen != null)
			{
				if(this.changedCharset)
				{
					this.changedCharset = false;
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, this.getWidth(), this.getHeight());
				}
				if(this.antiAliasing)
				{
					Graphics2D g2 = (Graphics2D)g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2.drawImage(this.screen, this.startX, this.startY, this.stretchX, this.stretchY, null);
				}
				else
				{
					g.drawImage(this.screen, this.startX, this.startY, this.stretchX, this.stretchY, null);
				}
			}
		}
	}
	
	@Override
	public void windowOpened(WindowEvent e)
	{
	    this.setupFrame();
		this.console.initialized();
	}

	private int translateKey(char keyChar, int keyCode)
	{
		if(keyChar > 0 && keyChar < 65535)
			return keyChar;
		switch(keyCode)
		{
		case KeyEvent.VK_UP:		case KeyEvent.VK_KP_UP:		return Keys.CURSOR_UP;
		case KeyEvent.VK_DOWN:		case KeyEvent.VK_KP_DOWN:	return Keys.CURSOR_DOWN;
		case KeyEvent.VK_LEFT:		case KeyEvent.VK_KP_LEFT:	return Keys.CURSOR_LEFT;
		case KeyEvent.VK_RIGHT:		case KeyEvent.VK_KP_RIGHT:	return Keys.CURSOR_RIGHT;
		case KeyEvent.VK_HOME:		return Keys.HOME;
		case KeyEvent.VK_END:		return Keys.END;
		case KeyEvent.VK_CLEAR:		return Keys.CLEAR;
		case KeyEvent.VK_INSERT:	return Keys.INSERT;
		case KeyEvent.VK_PAGE_UP:	return Keys.PAGE_UP;
		case KeyEvent.VK_PAGE_DOWN:	return Keys.PAGE_DOWN;
		case KeyEvent.VK_F1:		return Keys.F1;
		case KeyEvent.VK_F2:		return Keys.F2;
		case KeyEvent.VK_F3:		return Keys.F3;
		case KeyEvent.VK_F4:		return Keys.F4;
		case KeyEvent.VK_F5:		return Keys.F5;
		case KeyEvent.VK_F6:		return Keys.F6;
		case KeyEvent.VK_F7:		return Keys.F7;
		case KeyEvent.VK_F8:		return Keys.F8;
		case KeyEvent.VK_F9:		return Keys.F9;
		case KeyEvent.VK_F10:		return Keys.F10;
		case KeyEvent.VK_F11:		return Keys.F11;
		case KeyEvent.VK_F12:		return Keys.F12;
		default:					return 0;
		}
	}
	
	@Override public void keyPressed(KeyEvent e)
	{
		if(e.getKeyChar() == 10 && e.isAltDown())
		{
			e.consume();
			this.console.keyTyped(Keys.TOGGLE_FULLSCREEN);
		}
		else
		{
			final int c = this.translateKey(e.getKeyChar(), e.getKeyCode());
			if(c != 0)
			{
				this.console.keyTyped(c);
				e.consume();
			}
		}
	}
	
	@Override public void keyReleased(KeyEvent e) { /* */ }
	@Override public void keyTyped(KeyEvent e) { /* */ }
	@Override public void windowActivated(WindowEvent e) { /* */ }
	@Override public void windowClosed(WindowEvent e) { /* */ }
	@Override public void windowDeactivated(WindowEvent e) { /* */ }
	@Override public void windowDeiconified(WindowEvent e) { /* */ }
	@Override public void windowIconified(WindowEvent e) { /* */ }
}
