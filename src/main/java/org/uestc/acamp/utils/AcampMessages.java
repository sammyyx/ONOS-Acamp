package org.uestc.acamp.utils;

import org.onlab.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.device.ControllerDevice;
import org.uestc.acamp.protocol.AcampMessage;
import org.uestc.acamp.protocol.AcampMessageConstant;
import org.uestc.acamp.protocol.AcampMessageElement;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by sammy on 17-3-27.
 */
public class AcampMessages {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static byte[] buildAcampMessage(AcampMessage message, ApDevice ap) {
        byte[] sendBytes;
        Ethernet sendEthernetPacket = new Ethernet();
        sendEthernetPacket.setDestinationMACAddress(ap.getMacAddress());
        sendEthernetPacket.setSourceMACAddress(ControllerDevice.getHardwareAddress());
        sendEthernetPacket.setEtherType(Ethernet.TYPE_IPV4);

        IPv4 sendIpPacket = new IPv4();
        sendIpPacket.setSourceAddress(ControllerDevice.getIp4Address().toInt());
        sendIpPacket.setDestinationAddress(ap.getIpAddress().toInt());
        sendIpPacket.setTtl((byte) 64);

        UDP SendUdpPacket = new UDP();
        SendUdpPacket.setSourcePort(AcampMessageConstant.getServicePort());
        SendUdpPacket.setDestinationPort(ap.getUdpPort().toInt());

        SendUdpPacket.setPayload(message);
        sendIpPacket.setPayload(SendUdpPacket);
        sendEthernetPacket.setPayload(sendIpPacket);
        return sendEthernetPacket.serialize();
    }

    public static byte[] convertDesiredConfigurationList2Bytes(AcampMessageConstant.MessageElementType...types) {
        ByteBuffer bb = ByteBuffer.allocate(2*types.length);
        for (AcampMessageConstant.MessageElementType type: types) {
            bb.putShort(type.getValue());
        }
        return bb.array();
    }

    public static byte[] getIpAddressBytes(Ip4Address ipAddress) {
        byte[] ipBytes = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(ipBytes);
        bb.putInt(ipAddress.getIp4Address().toInt());
        return ipBytes;
    }

    public static long genSequenceNumber() {
        Random random = new Random();
        return intConvert2Long(random.nextInt());
    }

    public static int shortConvert2Int(short before) {
        int after = before & 0x0ffff;
        return after;
    }

    public static long intConvert2Long(int before) {
        long after = before & 0x0ffffffff;
        return after;
    }
}
