package org.tyas.upnp.ssdp;

import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpResponse;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class SsdpServer
{
	static final int DEFAULT_RECEIVE_BUFFER_SIZE = 1024;

	public interface Handler
	{
		void onAdvertisement(SsdpAdvertisement.Const adv, Context ctx);
		void onSearchRequest(SsdpSearchRequest.Const req, Context ctx);
		void onSearchResponse(SsdpSearchResponse.Const resp, Context ctx);
	}

	public interface Context
	{
		DatagramSocket getSocket();
		DatagramPacket getPacket();
	}

	private int mBuffSize;
	private Set<Handler> mHandlerSet = new HashSet<Handler>();

	private Handler mHandler = new Handler() {
			@Override public void onAdvertisement(SsdpAdvertisement.Const adv, Context ctx) {
				SsdpServer.this.onAdvertisement(adv, ctx);
			}
			@Override public void onSearchRequest(SsdpSearchRequest.Const req, Context ctx) {
				SsdpServer.this.onSearchRequest(req, ctx);
			}
			@Override public void onSearchResponse(SsdpSearchResponse.Const resp, Context ctx) {
				SsdpServer.this.onSearchResponse(resp, ctx);
			}
		};

	public SsdpServer(int buffSize, Handler handler) {
		mBuffSize = buffSize;
		mHandler = handler;
	}

	public SsdpServer(int buffSize) {
		mBuffSize = buffSize;
	}

	public SsdpServer(Handler handler) {
		this(DEFAULT_RECEIVE_BUFFER_SIZE, handler);
	}

	public SsdpServer() {
		this(DEFAULT_RECEIVE_BUFFER_SIZE);
	}

	public Runnable accept(final DatagramSocket socket) throws IOException {
		if (socket == null) return null;

		byte [] buff = new byte[mBuffSize];
		final DatagramPacket pkt = new DatagramPacket(buff, buff.length);
		socket.receive(pkt);
		final Context ctx = new Context() {
				@Override public DatagramSocket getSocket() { return socket; }
				@Override public DatagramPacket getPacket() { return pkt; }
			};

		return new Runnable() {
			@Override public void run() {
				try {
					InputStream in = new ByteArrayInputStream(pkt.getData());
					HttpMessage.Input msg = HttpMessage.readMessage(in);
					
					HttpResponse.Input resp = HttpResponse.getByInput(msg);
					if (resp != null) {
						SsdpSearchResponse.Const ssresp = SsdpSearchResponse.getByHttpResponse(resp);
						if (ssresp != null) {
							performOnSearchResponse(ssresp, ctx);
							return;
						}
					}
					
					HttpRequest.Input req = HttpRequest.getByInput(msg);
					if (req != null) {
						
						SsdpAdvertisement.Const ssadv = SsdpAdvertisement.getByHttpRequest(req);
						if (ssadv != null) {
							performOnAdvertisement(ssadv, ctx);
							return;
						}

						SsdpSearchRequest.Const ssreq = SsdpSearchRequest.getByHttpRequest(req);
						if (ssreq != null) {
							performOnSearchRequest(ssreq, ctx);
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public void addHandler(Handler handler) {
		if (handler != null) {
			mHandlerSet.add(handler);
		}
	}

	public void removeHandler(Handler handler) {
		if (handler != null) {
			mHandlerSet.remove(handler);
		}
	}

	private void performOnAdvertisement(SsdpAdvertisement.Const adv, Context ctx) {
		if (mHandler != null) mHandler.onAdvertisement(adv, ctx);

		for (Handler handler: mHandlerSet) {
			if (handler != null) handler.onAdvertisement(adv, ctx);
		}
	}
	
	private void performOnSearchRequest(SsdpSearchRequest.Const req, Context ctx) {
		if (mHandler != null) mHandler.onSearchRequest(req, ctx);

		for (Handler handler: mHandlerSet) {
			if (handler != null) handler.onSearchRequest(req, ctx);
		}
	}
	
	private void performOnSearchResponse(SsdpSearchResponse.Const resp, Context ctx) {
		if (mHandler != null) mHandler.onSearchResponse(resp, ctx);

		for (Handler handler: mHandlerSet) {
			if (handler != null) handler.onSearchResponse(resp, ctx);
		}
	}

	protected void onAdvertisement(SsdpAdvertisement.Const adv, Context ctx) {}
	protected void onSearchRequest(SsdpSearchRequest.Const req, Context ctx) {}
	protected void onSearchResponse(SsdpSearchResponse.Const resp, Context ctx) {}
}
