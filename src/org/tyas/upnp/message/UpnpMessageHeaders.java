package org.tyas.upnp.message;

public class UpnpMessageHeaders extends HttpHeaders
{
	public UpnpMessageHeaders() {
		super();
	}

	public void setNtRootDevice() {
	}

	public void setNt(DeviceUuid uuid) {
		putFirst(UpnpMessage.NT, UpnpMessage.packDeviceUuid(uuid));
	}

	public void setNt(DeviceType dev) {
		putFirst(UpnpMessage.NT, UpnpMessage.packDeviceType(dev));
	}

	public void setNt(ServiceType serv) {
		putFirst(UpnpMessage.NT, UpnpMessage.packServiceType(serv));
	}

	public void setUsn(DeviceUuid uuid, DeviceType type) {
	}

	public void setUsn(DeviceUuid uuid, ServiceType type) {
	}

	public void setNts(String subtype) {
		putFirst(UpnpMessage.NTS, subtype);
	}

	public void setBootId(int bootid) {
		putFirst(UpnpMessage.BOOTID, "" + bootid);
	}

	public void setConfigId(int configid) {
		putFirst(UpnpMessage.CONFIGID, "" + configid);
	}

	public void setSearchPort(int port) {
		putFirst(UpnpMessage.SEARCHPORT, "" + port);
	}
}
