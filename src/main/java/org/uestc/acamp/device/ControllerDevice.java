package org.uestc.acamp.device;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;
import org.onosproject.net.packet.PacketService;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by sammy on 17-3-27.
 */
public class ControllerDevice {
    private static MacAddress hardwareAddress;
    private static Ip4Address ip4Address;
    public static final String name = "AcampController";
    public static final String descriptor = "control and manage ap";
    public static final String ifname = "eth0";

    public static PacketService packetService;

    public static void initHwAddress() {
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = netInterfaces.nextElement();
                if (netInterface.getName().equals(ifname)) {
                    hardwareAddress = MacAddress.valueOf(netInterface.getHardwareAddress());
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements())
                    {
                        InetAddress ip = (InetAddress) addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address)
                        {
                            ip4Address = Ip4Address.valueOf(ip);
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static MacAddress getHardwareAddress() {
        return hardwareAddress;
    }

    public static Ip4Address getIp4Address() {
        return ip4Address;
    }
}
