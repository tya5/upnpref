package org.tyas.http;

import java.util.Map;
import java.util.List;

public interface HttpMessageFactory<L extends HttpStartLine, M extends HttpMessage<L>>
{
	public M createMessage(L startLine, HttpHeaders headers);
}