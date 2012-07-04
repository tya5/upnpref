package org.tyas.upnp.event;

public class Main
{
	public static void main(String [] args) {

		DatagramPacket pkt = SubscribeRequest
			.getSubscribeRequest(args[2], CALLBACK, 60)
			.toDatagramPacket();

		pkt.setAddress(InetAddress.getByName(args[0]));
		pkt.setPort(Integer.decode(args[1]));
		new DatagramSocket().send(pkt);
	}
}
