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
package awtconsole.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class IntOutputStream extends OutputStream
{
	private final OutputStream out;
	
	public IntOutputStream(OutputStream out)
	{
		if(out instanceof BufferedOutputStream)
			this.out = out;
		else
			this.out = new BufferedOutputStream(out);
	}

	public IntOutputStream(String filename) throws IOException
	{
		this(new FileOutputStream(filename));
	}

	public IntOutputStream(File file) throws IOException
	{
		this(new FileOutputStream(file));
	}

	public void writeI16(int value) throws IOException
	{
		this.out.write((value >> 8) & 255);
		this.out.write(value & 255);
	}

	public void writeI24(int value) throws IOException
	{
		this.out.write((value >> 16) & 255);
		this.out.write((value >> 8) & 255);
		this.out.write(value & 255);
	}
	
	public void writeI32(int value) throws IOException
	{
		this.out.write((value >> 24) & 255);
		this.out.write((value >> 16) & 255);
		this.out.write((value >> 8) & 255);
		this.out.write(value & 255);
	}
	
	public void writeI64(long value) throws IOException
	{
		this.out.write((int)((value >> 56L) & 255L));
		this.out.write((int)((value >> 48L) & 255L));
		this.out.write((int)((value >> 40L) & 255L));
		this.out.write((int)((value >> 32L) & 255L));
		this.out.write((int)((value >> 24L) & 255L));
		this.out.write((int)((value >> 16L) & 255L));
		this.out.write((int)((value >> 8L) & 255L));
		this.out.write((int)(value & 255L));
	}

	@Override
	public void write(int b) throws IOException
	{
		this.out.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		this.out.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		this.out.write(b, off, len);
	}

	@Override
	public void flush() throws IOException
	{
		this.out.flush();
	}
	
	@Override
	public void close() throws IOException
	{
		this.out.close();
	}
}
