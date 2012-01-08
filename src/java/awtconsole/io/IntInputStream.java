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
