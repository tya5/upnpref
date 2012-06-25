package org.tyas.upnp.ssdp;

import org.tyas.http.Http;
import org.tyas.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

public class SsdpSearchResponse implements Ssdp.SearchResponse
{
	private Http.Response mResp;
	private HttpResponse  mRespMutable;

	private SsdpSearchResponse(Http.Response msg) {
		mRespMutable = null;
		mResp = msg;
	}

	public SsdpSearchResponse() {
		mResp = mRespMutable = new HttpResponse(Http.VERSION_1_1, "200", "OK");
	}

	@Override public long getMaxAge() { return mResp.getMaxAge(); }

	@Override public URL getDescriptionUrl() {
		try {
			return mResp.getLocation().toURL();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override public String getSearchTarget() { return mResp.getFirst(Ssdp.ST); }

	@Override public String getUniqueServiceName() { return mResp.getFirst(Ssdp.USN); }

	@Override public int getBootId() { return mResp.getInt(Ssdp.BOOTID, 0); }

	@Override public int getConfigId() { return mResp.getInt(Ssdp.CONFIGID, 0); }

	@Override public int getSearchPort() { return mResp.getInt(Ssdp.SEARCHPORT, -1); }

	public SsdpSearchResponse setMaxAge(long max) {
		mRespMutable.setMaxAge(max);
		return this;
	}

	public SsdpSearchResponse setDescriptionUrl(String url) {
		mRespMutable.setLocation(url);
		return this;
	}

	public SsdpSearchResponse setSearchTarget(String target) {
		mRespMutable.putFirst(Ssdp.ST, target);
		return this;
	}

	public SsdpSearchResponse setUniqueServiceName(String usn) {
		mRespMutable.putFirst(Ssdp.USN, usn);
		return this;
	}
	
	public SsdpSearchResponse setBootId(int id) {
		mRespMutable.setInt(Ssdp.BOOTID, id);
		return this;
	}

	public SsdpSearchResponse setConfigId(int id) {
		mRespMutable.setInt(Ssdp.CONFIGID, id);
		return this;
	}

	public SsdpSearchResponse setSearchPort(int port) {
		mRespMutable.setInt(Ssdp.SEARCHPORT, port);
		return this;
	}

	public void send(OutputStream out) throws IOException {
		mRespMutable.send(out, (byte [])null);
	}

	public static Ssdp.SearchResponse parse(InputStream in) throws IOException {
		return new SsdpSearchResponse(HttpResponse.parse(in).getResponse());
	}
}
