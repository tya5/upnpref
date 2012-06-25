package org.tyas.upnp.ssdp;

public class SsdpMServer
{
	private MulticastSocket mSocket;

	public SsdpMServer() {
		this(Ssdp.DEFAULT_PORT);
	}

	public SsdpServer(int port) {
		mSocket = new MulticastSocket(port);
	}

	public SsdpServer(SocketAddress bindaddr) {
		mSocket = new MulticastSocket(bindaddr);
	}

	public MulticastSocket getMulticastSocket() {
		return mSocket;
	}

	public Runnable accept() {
		final DatagramPacket pkt = new DatagramPacket();
		mSocket.receive(pkt);

		return new Runnable() {
			@Override public void run() {
				InputStream in = new ByteArrayInputStream(pkt.getData());
				Http.InputMessage im = Http.readMessage(in);

				try {
					HttpRequest req = new HttpRequest(im.getMessage());
					SsdpAdvertisement adv = new SsdpAdvertisement(req);
					try {
						onAdvertisement(adv);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				} catch (Exception e) {}
			}
		};
	}
}