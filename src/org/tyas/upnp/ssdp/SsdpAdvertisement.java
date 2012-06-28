package org.tyas.upnp.ssdp;

import org.tyas.http.Http;
import org.tyas.http.HttpRequest;
import org.tyas.upnp.UpnpUsn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.DatagramPacket;

public class SsdpAdvertisement implements Ssdp.Advertisement
{
	private Http.Request mReq;
	private HttpRequest  mReqMutable;

	private SsdpAdvertisement(Http.Request req) {
		mReqMutable = null;
		mReq = req;
	}

	public SsdpAdvertisement() {
		mReq = mReqMutable = new HttpRequest(Ssdp.NOTIFY, "*", Http.VERSION_1_1);
	}

	@Override public String getHost() { return mReq.getHost(); }

	@Override public long getMaxAge() { return mReq.getMaxAge(); }

	@Override public URL getDescriptionUrl() {
		try {
			return mReq.getLocation().toURL();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("DescriptionUrlString: '" + mReq.getLocation() + "'");
			return null;
		}
	}

	@Override public String getNotificationType() { return mReq.getFirst(Ssdp.NT); }

	@Override public String getNotificationSubType() { return mReq.getFirst(Ssdp.NTS); }

	@Override public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(mReq.getFirst(Ssdp.USN)); }

	@Override public int getBootId() { return mReq.getInt(Ssdp.BOOTID, 0); }
	
	@Override public int getConfigId() { return mReq.getInt(Ssdp.CONFIGID, 0); }
	
	@Override public int getSearchPort() { return mReq.getInt(Ssdp.SEARCHPORT, -1); }

	public SsdpAdvertisement setHost(String host) {
		mReqMutable.setHost(host);
		return this;
	}

	public SsdpAdvertisement setMaxAge(long max) {
		mReqMutable.setMaxAge(max);
		return this;
	}

	public SsdpAdvertisement setDescriptionUrl(String url) {
		mReqMutable.setLocation(url);
		return this;
	}

	public SsdpAdvertisement setBootId(int id) {
		mReqMutable.setInt(Ssdp.BOOTID, id);
		return this;
	}

	public SsdpAdvertisement setConfigId(int id) {
		mReqMutable.setInt(Ssdp.CONFIGID, id);
		return this;
	}

	public SsdpAdvertisement setSearchPort(int port) {
		mReqMutable.setInt(Ssdp.SEARCHPORT, port);
		return this;
	}

	public void notify(OutputStream out) throws IOException {
		mReqMutable.send(out, (byte [])null);
	}

	public DatagramPacket toDatagramPacket() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		notify(out);
		byte [] data = out.toByteArray();
		return new DatagramPacket(data, data.length);
	}

	public static SsdpAdvertisement getByHttpRequest(Http.Request req) {
		if (! isValid(req)) return null;

		return new SsdpAdvertisement(req);
	}

	public static boolean isValid(Http.Request req) {
		if (! Ssdp.NOTIFY.equals(req.getMethod())) return false;

		return true;
	}

	public static Ssdp.Advertisement parse(InputStream in) throws IOException {
		return new SsdpAdvertisement(HttpRequest.parse(in).getRequest());
	}
}
