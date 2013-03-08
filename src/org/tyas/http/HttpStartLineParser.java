package org.tyas.http;

public interface HttpStartLineParser<T extends HttpStartLine>
{
	public T parse(String line);
}
