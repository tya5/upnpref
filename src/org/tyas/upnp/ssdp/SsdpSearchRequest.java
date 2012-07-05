package org.tyas.upnp.ssdp;

import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public abstract class SsdpSearchRequest
{
	public interface Base extends HttpRequest.Base
	{
		int getMaxWaitTime();
		String getSearchTarget();
		String getMan();
	}

	public static class Const extends HttpRequest.Const implements Base
	{
		private Const(HttpRequest.Const c) {
			super(c);
		}

		@Override public int getMaxWaitTime() { return getInt(Ssdp.MX, -1); }

		@Override public String getSearchTarget() { return getFirst(Ssdp.ST); }

		@Override public String getMan() { return getFirst(Ssdp.MAN); }
	}

	public static class Builder extends HttpRequest.Builder implements Base
	{
		public Builder() {
			super(Ssdp.M_SEARCH, "*", HttpMessage.VERSION_1_1);
		}

		@Override public int getMaxWaitTime() { return getInt(Ssdp.MX, -1); }

		@Override public String getSearchTarget() { return getFirst(Ssdp.ST); }

		@Override public String getMan() { return getFirst(Ssdp.MAN); }

		public SsdpSearchRequest.Builder setMaxWaitTime(int seconds) {
			setInt(Ssdp.MX, seconds);
			return this;
		}

		public SsdpSearchRequest.Builder setSearchTarget(String target) {
			putFirst(Ssdp.ST, target);
			return this;
		}

		public SsdpSearchRequest.Builder setMan(String man) {
			putFirst(Ssdp.MAN, man);
			return this;
		}
	}

	public static Const getByHttpRequest(HttpRequest.Const req) {
		if (! Ssdp.M_SEARCH.equals(req.getMethod())) return null;

		if (! HttpMessage.VERSION_1_1.equals(req.getVersion())) return null;

		return new Const(req);
	}
}
