package org.tyas.upnp.ssdp;

import org.tyas.http.Http;
import org.tyas.http.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class SsdpSearchRequest extends HttpRequest implements Ssdp.SearchRequest
{
	public static class Const extends HttpRequest.Const implements Ssdp.SearchRequest
	{
		private Const(HttpRequest.Const c) {
			super(c);
		}

		@Override public int getMaxWaitTime() { return getInt(Ssdp.MX, -1); }

		@Override public String getSearchTarget() { return getFirst(Ssdp.ST); }

		@Override public String getMan() { return getFirst(Ssdp.MAN); }
	}

	public SsdpSearchRequest() {
		super(Ssdp.M_SEARCH, "*", Http.VERSION_1_1);
	}

	@Override public int getMaxWaitTime() { return getInt(Ssdp.MX, -1); }

	@Override public String getSearchTarget() { return getFirst(Ssdp.ST); }

	@Override public String getMan() { return getFirst(Ssdp.MAN); }

	public SsdpSearchRequest setMaxWaitTime(int seconds) {
		setInt(Ssdp.MX, seconds);
		return this;
	}

	public SsdpSearchRequest setSearchTarget(String target) {
		putFirst(Ssdp.ST, target);
		return this;
	}

	public SsdpSearchRequest setMan(String man) {
		putFirst(Ssdp.MAN, man);
		return this;
	}

	public static SsdpSearchRequest.Const getByHttpRequest(HttpRequest.Const req) {
		if (! Ssdp.M_SEARCH.equals(req.getMethod())) return null;

		if (! Http.VERSION_1_1.equals(req.getVersion())) return null;

		return new SsdpSearchRequest.Const(req);
	}
}
