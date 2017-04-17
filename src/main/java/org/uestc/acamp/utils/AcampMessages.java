package org.uestc.acamp.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.device.ControllerDevice;
import org.uestc.acamp.network.NetworkManager;
import org.uestc.acamp.protocol.AcampMessage;
import org.uestc.acamp.protocol.AcampMessageConstant;
import org.uestc.acamp.protocol.AcampMessageElement;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * Created by sammy on 17-3-27.
 */
public class AcampMessages {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // Build Acamp Message to ethernet byte array, need to pass ApDevice object.
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

    // Build configuration from json file
    public static AcampMessage buildConfigurationUpdateMessage(ObjectNode jsonTree) {

        JsonNode apId = jsonTree.get("apId");
        if (apId == null) {
            return null;
        }
        ApDevice ap = NetworkManager.apDeviceList.get(apId.intValue());
        if (ap == null) {
            return null;
        }
        AcampMessageElement element = null;
        AcampMessage.Builder sendConfigurationUpdateMessageBuilder = new AcampMessage.Builder()
                .setMessageType(AcampMessageConstant.MessageType.CONFIGURATION_UPDATE_REQUEST)
                .setApId(apId.intValue())
                .setProtocolType(AcampMessageConstant.ProtocolType.CONTROL_MESSAGE)
                .setProtocolVersion(AcampMessageConstant.ProtocolVersion.CURRENT_VER)
                .setSequenceNumber(ap.getControllerSequenceNumber());

        Iterator<Map.Entry<String,JsonNode>> it = jsonTree.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            switch (entry.getKey()) {
                case "ssid":
                    String ssid = entry.getValue().textValue();
                    element = new AcampMessageElement.Builder()
                            .setMessageElementType(AcampMessageConstant.MessageElementType.SSID)
                            .setMessageElementValue(ssid.getBytes()).build();
                    sendConfigurationUpdateMessageBuilder.addMessageElement(element);
                    ap.setSsid(ssid);
                    break;
                case "channel":
                    byte[] channel = ByteBuffer.allocate(1).put((byte)entry.getValue().intValue()).array();
                    element = new AcampMessageElement.Builder()
                            .setMessageElementType(AcampMessageConstant.MessageElementType.CHANNEL)
                            .setMessageElementValue(channel).build();
                    sendConfigurationUpdateMessageBuilder.addMessageElement(element);
                    ap.setChannel(channel[0]);
                    break;
                case "hwmode":
                    byte[] hwmode = ByteBuffer.allocate(1).put((byte)entry.getValue().intValue()).array();
                    element = new AcampMessageElement.Builder()
                            .setMessageElementType(AcampMessageConstant.MessageElementType.HARDWARE_MODE)
                            .setMessageElementValue(hwmode).build();
                    sendConfigurationUpdateMessageBuilder.addMessageElement(element);
                    ap.setHwMode(AcampMessageConstant.HardwareMode.getEnumHardwareMode(hwmode[0]));
                    break;
                case "txpower":
                    byte[] txpower = ByteBuffer.allocate(1).put((byte)entry.getValue().intValue()).array();
                    element = new AcampMessageElement.Builder()
                            .setMessageElementType(AcampMessageConstant.MessageElementType.TX_POWER)
                            .setMessageElementValue(txpower).build();
                    sendConfigurationUpdateMessageBuilder.addMessageElement(element);
                    ap.setTxPower(txpower[0]);
                    break;
                case "macFilterMode":
                    byte[] macFilterMode = ByteBuffer.allocate(1).put((byte)entry.getValue().intValue()).array();
                    element = new AcampMessageElement.Builder()
                            .setMessageElementType(AcampMessageConstant.MessageElementType.MAC_FILTER_MODE)
                            .setMessageElementValue(macFilterMode).build();
                    sendConfigurationUpdateMessageBuilder.addMessageElement(element);
                    ap.setMacFilterMode(AcampMessageConstant.MacFilterMode.getEnumMacFilterMode(macFilterMode[0]));
                    break;
                case "macFilterList":
                    LinkedList<MacAddress> macList = new LinkedList<>();
                    JsonNode macAddressList = entry.getValue();
                    ByteBuffer bb = ByteBuffer.allocate(6 * macAddressList.size());
                    if (macAddressList.isArray()) {
                        for (JsonNode macAddressNode: macAddressList) {
                            MacAddress macAddress = MacAddress.valueOf(macAddressNode.textValue());
                            macList.add(macAddress);
                            bb.put(macAddress.toBytes());
                        }
                    }
                    element = new AcampMessageElement.Builder()
                            .setMessageElementType(AcampMessageConstant.MessageElementType.RESET_MAC_FILTER_LIST)
                            .setMessageElementValue(bb.array()).build();
                    sendConfigurationUpdateMessageBuilder.addMessageElement(element);
                    ap.setMacFilterList(macList);
                    break;
                default:
                    break;
            }
        }
        AcampMessage configurationUpdateMessage = sendConfigurationUpdateMessageBuilder.build();
        return configurationUpdateMessage;
    }

    public static AcampMessage buildConfigurationRequest(ApDevice ap) {

        // Send Configuration Request
        AcampMessageElement desiredConfigurationMsgEle = new AcampMessageElement.Builder()
                .setMessageElementType(AcampMessageConstant.MessageElementType.DESIRED_CONFIGURATION_LIST)
                .setMessageElementValue(AcampMessages.convertDesiredConfigurationList2Bytes(
                        AcampMessageConstant.MessageElementType.SSID, AcampMessageConstant.MessageElementType.CHANNEL, AcampMessageConstant.MessageElementType.HARDWARE_MODE,
                        AcampMessageConstant.MessageElementType.SUPPRESS_SSID, AcampMessageConstant.MessageElementType.SECURITY_OPTION, AcampMessageConstant.MessageElementType.MAC_FILTER_MODE,
                        AcampMessageConstant.MessageElementType.MAC_FILTER_LIST, AcampMessageConstant.MessageElementType.TX_POWER, AcampMessageConstant.MessageElementType.WPA_PASSWORD
                ))
                .build();

        AcampMessage sendConfigurationRequest = new AcampMessage.Builder()
                .setMessageType(AcampMessageConstant.MessageType.CONFIGURATION_REQUEST)
                .setApId(ap.getApId())
                .setProtocolVersion(AcampMessageConstant.ProtocolVersion.CURRENT_VER)
                .setProtocolType(AcampMessageConstant.ProtocolType.CONTROL_MESSAGE)
                .setSequenceNumber(ap.getControllerSequenceNumber())
                .addMessageElement(desiredConfigurationMsgEle)
                .build();

        return sendConfigurationRequest;
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
