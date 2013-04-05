package org.tyas.upnp.ssdp;

public interface SsdpRootDeviceSpec
{
	public int getBootId();

	public int getConfigId();

	public String getDeviceDescriptionLocation();

	public int getUnicastSearchPort();
}