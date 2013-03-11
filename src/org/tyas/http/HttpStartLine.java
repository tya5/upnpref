package org.tyas.http;

public interface HttpStartLine
{
	public String getLine();

	public interface Parser<T extends HttpStartLine> {
		public T parse(String line);
	}
}
