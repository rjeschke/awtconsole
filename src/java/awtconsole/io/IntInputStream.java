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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IntInputStream extends InputStream
{
	private final InputStream in;
	
	public IntInputStream(InputStream in)
	{
		if(in instanceof BufferedInputStream)
			this.in = in;
		else
			this.in = new BufferedInputStream(in);
	}
	
	public IntInputStream(String filename) throws IOException
	{
		this(new FileInputStream(filename));
	}
	
	public IntInputStream(File file) throws IOException
	{
		this(new FileInputStream(file));
	}

	public int readI16() throws IOException
	{
		int ret = this.in.read();
		ret = (ret << 8) | this.in.read();
		return ret;
	}

	public int readI24() throws IOException
	{
		int ret = this.in.read();
		ret = (ret << 8) | this.in.read();
		ret = (ret << 8) | this.in.read();
		return ret;
	}

	public int readI32() throws IOException
	{
		int ret = this.in.read();
		ret = (ret << 8) | this.in.read();
		ret = (ret << 8) | this.in.read();
		ret = (ret << 8) | this.in.read();
		return ret;
	}

	public long readI64() throws IOException
	{
		long ret = this.in.read();
		ret = (ret << 8L) | this.in.read();
		ret = (ret << 8L) | this.in.read();
		ret = (ret << 8L) | this.in.read();
		ret = (ret << 8L) | this.in.read();
		ret = (ret << 8L) | this.in.read();
		ret = (ret << 8L) | this.in.read();
		ret = (ret << 8L) | this.in.read();
		return ret;
	}
	
	@Override
	public int read() throws IOException
	{
		return this.in.read();
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return this.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		return this.read(b, off, len);
	}
	
	@Override
	public void close() throws IOException
	{
		this.in.close();
	}
	
	@Override
	public int available() throws IOException
	{
		return this.in.available();
	}
	
	@Override
	public synchronized void mark(int readlimit)
	{
		this.in.mark(readlimit);
	}
	
	@Override
	public boolean markSupported()
	{
		return this.in.markSupported();
	}
	
	@Override
	public synchronized void reset() throws IOException
	{
		this.in.reset();
	}
	
	@Override
	public long skip(long n) throws IOException
	{
		return this.in.skip(n);
	}
}
