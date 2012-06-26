package org.tyas.upnp.ssdp;

import org.tyas.http.Http;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpResponse;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

public class SsdpServer
{
	static final int DEFAULT_RECEIVE_BUFFER_SIZE = 1024;

	public interface Handler
	{
		void onAdvertisement(Ssdp.Advertisement adv);
		void onSearchRequest(Ssdp.SearchRequest req);
		void onSearchResponse(Ssdp.SearchResponse resp);
	}

	private int mBuffSize;

	private final Handler mHandler = new Handler() {
			@Override public void onAdvertisement(Ssdp.Advertisement adv) {
				SsdpServer.this.onAdvertisement(adv);
			}
			@Override public void onSearchRequest(Ssdp.SearchRequest req) {
				SsdpServer.this.onSearchRequest(req);
			}
			@Override public void onSearchResponse(Ssdp.SearchResponse resp) {
				SsdpServer.this.onSearchResponse(resp);
			}
		};

	public SsdpServer(int buffSize) {
		mBuffSize = buffSize;
	}

	public SsdpServer() {
		this(DEFAULT_RECEIVE_BUFFER_SIZE);
	}

	public Runnable accept(DatagramSocket socket) throws IOException {
		return accept(socket, mHandler);
	}

	public Runnable accept(DatagramSocket socket, final Handler handler) throws IOException {
		if (socket == null) return null;

		byte [] buff = new byte[mBuffSize];
		final DatagramPacket pkt = new DatagramPacket(buff, buff.length);
		socket.receive(pkt);

		return new Runnable() {
			@Override public void run() {
				try {
					InputStream in = new ByteArrayInputStream(pkt.getData());
					Http.Message msg = Http.readMessage(in).getMessage();

					if (HttpResponse.isValid(msg)) {
						HttpResponse resp = new HttpResponse(msg);
						SsdpSearchResponse ssresp = new SsdpSearchResponse(resp);
						if (handler != null) handler.onSearchResponse(ssresp);
					}
					if (HttpRequest.isValid(msg)) {
						HttpRequest req = new HttpRequest(msg);
						if (SsdpAdvertisement.isValid(req)) {
							SsdpAdvertisement ssadv = new SsdpAdvertisement(req);
							if (handler != null) handler.onAdvertisement(ssadv);
						}
						if (SsdpSearchRequest.isValid(req)) {
							SsdpSearchRequest ssreq = new SsdpSearchRequest(req);
							if (handler != null) handler.onSearchRequest(ssreq);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	protected void onAdvertisement(Ssdp.Advertisement adv) {}
	protected void onSearchRequest(Ssdp.SearchRequest req) {}
	protected void onSearchResponse(Ssdp.SearchResponse resp) {}
}