package org.tyas.upnp.ssdp;

import org.tyas.http.Http;
import org.tyas.http.HttpRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class SsdpSearchRequest implements Ssdp.SearchRequest
{
	private Http.Request mReq;
	private HttpRequest  mReqMutable;

	private SsdpSearchRequest(Http.Request msg) {
		mReqMutable = null;
		mReq = msg;
	}

	public SsdpSearchRequest() {
		mReq = mReqMutable = new HttpRequest(Ssdp.M_SEARCH, "*", Http.VERSION_1_1);
	}

	@Override public String getHost() { return mReq.getHost(); }

	@Override public int getMaxWaitTime() { return mReq.getInt(Ssdp.MX, -1); }

	@Override public String getSearchTarget() { return mReq.getFirst(Ssdp.ST); }

	public SsdpSearchRequest setHost(String host) {
		mReqMutable.setHost(host);
		return this;
	}

	public SsdpSearchRequest setMaxWaitTime(int seconds) {
		mReqMutable.setInt(Ssdp.MX, seconds);
		return this;
	}

	public SsdpSearchRequest setSearchTarget(String target) {
		mReqMutable.putFirst(Ssdp.ST, target);
		return this;
	}

	public void send(OutputStream out) throws IOException {
		mReqMutable.send(out, (byte [])null);
	}

	public static Ssdp.SearchRequest parse(InputStream in) throws IOException {
		return new SsdpSearchRequest(HttpRequest.parse(in).getRequest());
	}
}
