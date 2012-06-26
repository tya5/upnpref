package org.tyas.upnp.ssdp;

import org.tyas.http.Http;
import org.tyas.http.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

public class SsdpSearchRequest implements Ssdp.SearchRequest
{
	private Http.Request mReq;
	private HttpRequest  mReqMutable;

	public SsdpSearchRequest(Http.Request req) {
		if (! isValid(req)) throw new RuntimeException("Not SsdpSearchRequest");

		mReqMutable = null;
		mReq = req;
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

	public void send(DatagramSocket sock) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		send(out);
		byte [] data = out.toByteArray();
		DatagramPacket pkt = new DatagramPacket(data, data.length);
		sock.send(pkt);
	}

	public static boolean isValid(Http.Request req) {
		if (! Ssdp.M_SEARCH.equals(req.getMethod())) return false;

		return true;
	}

	public static Ssdp.SearchRequest parse(InputStream in) throws IOException {
		return new SsdpSearchRequest(HttpRequest.parse(in).getRequest());
	}
}
