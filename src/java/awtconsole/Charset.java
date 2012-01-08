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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class Charset
{
	private final int width, height, stride;
	private final int[] data;
	
	protected Charset(int width, int height, int stride, int[] data)
	{
		this.width = width;
		this.height = height;
		this.stride = stride;
		this.data = data;
	}
	
	public int getWidth()
	{
		return this.width;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public int getStride()
	{
		return this.stride;
	}
	
	protected int[] getData()
	{
		return this.data;
	}
	
	public static Charset factory(CharsetType type)
	{
		switch(type)
		{
		case CHARS_4x6:		return fromFile("/awtconsole/chars/chars_4x6.bin");
		case CHARS_5x12:	return fromFile("/awtconsole/chars/chars_5x12.bin");
		case CHARS_6x8:		return fromFile("/awtconsole/chars/chars_6x8.bin");
		case CHARS_7x12:	return fromFile("/awtconsole/chars/chars_7x12.bin");
		case CHARS_8x8:		return fromFile("/awtconsole/chars/chars_8x8.bin");
		case CHARS_8x12:	return fromFile("/awtconsole/chars/chars_8x12.bin");
		case CHARS_8x16:	return fromFile("/awtconsole/chars/chars_8x16.bin");
		case CHARS_10x18:	return fromFile("/awtconsole/chars/chars_10x18.bin");
		case CHARS_12x16:	return fromFile("/awtconsole/chars/chars_12x16.bin");
		case CHARS_16x8:	return fromFile("/awtconsole/chars/chars_16x8.bin");
		case CHARS_16x12:	return fromFile("/awtconsole/chars/chars_16x12.bin");
		default:			return null;
		}
	}
	
	public static Charset fromFile(String filename)
	{
		Charset ret = null;
		if(new File(filename).exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(filename);
				ret = fromStream(fis);
				fis.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			InputStream in = Charset.class.getResourceAsStream(filename);
			if(in != null)
			{
				ret = fromStream(in);
				try
				{
					in.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	public static Charset fromStream(InputStream in)
	{
		try
		{
			final int w = in.read();
			final int h = in.read();
			final int s = (w + 7) >> 3;
			final int[] d = new int[h * 256];
			for(int i = 0; i < d.length; i++)
			{
				int t = 0;
				for(int n = 0; n < s; n++)
					t = (t << 8) | in.read();
				d[i] = t;
			}
			return new Charset(w, h, s, d);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
