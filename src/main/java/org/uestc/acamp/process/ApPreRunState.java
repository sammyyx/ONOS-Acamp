package org.uestc.acamp.process;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.device.ControllerDevice;
import org.uestc.acamp.network.NetworkManager;
import org.uestc.acamp.protocol.AcampMessage;
import org.uestc.acamp.protocol.AcampMessageConstant;
import org.uestc.acamp.protocol.AcampMessageConstant.*;
import org.uestc.acamp.protocol.AcampMessageElement;
import org.uestc.acamp.utils.AcampMessages;
import org.uestc.acamp.utils.ApDevices;

import java.nio.ByteBuffer;

/**
 * Created by sammy on 17-3-29.
 */
public class ApPreRunState implements ApFiniteStateMachine {
    @Override
    public void getDiscoveryRequest(ApDevice ap, AcampMessage acampMessage) {

        String name = ControllerDevice.name;
        AcampMessageElement controllerName = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_NAME)
                .setMessageElementValue(name.getBytes()).build();

        String descriptor = ControllerDevice.descriptor;
        AcampMessageElement controllerDescriptor = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_DESCRIPTOR)
                .setMessageElementValue(descriptor.getBytes()).build();


        AcampMessageElement controllerIp = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_IP_ADDRESS)
                .setMessageElementValue(AcampMessages.getIpAddressBytes(ControllerDevice.getIp4Address()))
                .build();

        AcampMessageElement controllerMac = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_MAC_ADDRESS)
                .setMessageElementValue(ControllerDevice.getHardwareAddress().toBytes())
                .build();

        AcampMessage sendDiscoveryResponse = new AcampMessage.Builder()
                .setMessageType(MessageType.DISCOVERY_RESPONSE)
                .setApId(0)
                .setProtocolVersion(ProtocolVersion.CURRENT_VER)
                .setProtocolType(ProtocolType.CONTROL_MESSAGE)
                .setSequenceNumber(ap.getApSequenceNumber())
                .addMessageElement(controllerName)
                .addMessageElement(controllerDescriptor)
                .addMessageElement(controllerIp)
                .addMessageElement(controllerMac)
                .build();

        byte[] cachedMessage = AcampMessages.buildAcampMessage(sendDiscoveryResponse, ap);
        ap.setCachedMessage(cachedMessage);

        NetworkManager.sendMessageFromPort(cachedMessage, ap.getConnectPoint());

        ap.setApSequenceNumber(ap.getApSequenceNumber() + 1);
    }

    @Override
    public void getRegisterRequest(ApDevice ap, AcampMessage acampMessage) {
        for (AcampMessageElement me: acampMessage.getMessageElements()) {
            ap.updateApDevice(me);
        }

        ap.setControllerSequenceNumber(AcampMessages.genSequenceNumber());
        ap.setApId(ApDevices.assignNewApId());

        AcampMessageElement resultCode = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.RESULT_CODE)
                .setMessageElementValue(ByteBuffer.allocate(2).putShort(ResultCode.SUCCESS.getValue()).array())
                .build();

        AcampMessageElement assignedApId = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.ASSIGNED_APID)
                .setMessageElementValue(ByteBuffer.allocate(2).putShort((short) ap.getApId()).array())
                .build();

        AcampMessageElement registeredService = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.REGISTERED_SERVICE)
                .setMessageElementValue(ByteBuffer.allocate(1).put(RegisteredService.CONFIGURATION_AND_MANAGEMENT.getValue()).array())
                .build();

        AcampMessageElement controllerNextSequenceNumber = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_NEXT_SEQUENCE_NUMBER)
                .setMessageElementValue(ByteBuffer.allocate(4).putInt((int)ap.getControllerSequenceNumber()).array())
                .build();

        AcampMessageElement controllerName = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_NAME)
                .setMessageElementValue(ControllerDevice.name.getBytes())
                .build();

        AcampMessageElement controllerDescriptor = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_DESCRIPTOR)
                .setMessageElementValue(ControllerDevice.descriptor.getBytes())
                .build();

        AcampMessageElement controllerIpAddress = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_IP_ADDRESS)
                .setMessageElementValue(ControllerDevice.getIp4Address().toOctets())
                .build();

        AcampMessageElement controllerMacAddress = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.CONTROLLER_MAC_ADDRESS)
                .setMessageElementValue(ControllerDevice.getHardwareAddress().toBytes())
                .build();

        AcampMessage sendRegisteredResponse = new AcampMessage.Builder()
                .setMessageType(MessageType.REGISTER_RESPONSE)
                .setApId(ap.getApId())
                .setProtocolVersion(ProtocolVersion.CURRENT_VER)
                .setProtocolType(ProtocolType.CONTROL_MESSAGE)
                .setSequenceNumber(ap.getApSequenceNumber())
                .addMessageElement(resultCode)
                .addMessageElement(assignedApId)
                .addMessageElement(registeredService)
                .addMessageElement(controllerNextSequenceNumber)
                .addMessageElement(controllerName)
                .addMessageElement(controllerDescriptor)
                .addMessageElement(controllerIpAddress)
                .addMessageElement(controllerMacAddress)
                .build();

        byte[] cachedMessage = AcampMessages.buildAcampMessage(sendRegisteredResponse, ap);
        ap.setCachedMessage(cachedMessage);

        NetworkManager.sendMessageFromPort(cachedMessage, ap.getConnectPoint());

        ap.setApSequenceNumber(ap.getApSequenceNumber() + 1);

        // Send Configuration Request
        AcampMessageElement desiredConfigurationMsgEle = new AcampMessageElement.Builder()
                .setMessageElementType(MessageElementType.DESIRED_CONFIGURATION_LIST)
                .setMessageElementValue(AcampMessages.convertDesiredConfigurationList2Bytes(
                        MessageElementType.SSID, MessageElementType.CHANNEL, MessageElementType.HARDWARE_MODE,
                        MessageElementType.SUPPRESS_SSID, MessageElementType.SECURITY_OPTION, MessageElementType.MAC_FILTER_MODE,
                        MessageElementType.MAC_FILTER_LIST, MessageElementType.TX_POWER, MessageElementType.WPA_PASSWORD
                ))
                .build();

        AcampMessage sendConfigurationRequest = new AcampMessage.Builder()
                .setMessageType(MessageType.CONFIGURATION_REQUEST)
                .setApId(ap.getApId())
                .setProtocolVersion(ProtocolVersion.CURRENT_VER)
                .setProtocolType(ProtocolType.CONTROL_MESSAGE)
                .setSequenceNumber(ap.getControllerSequenceNumber())
                .addMessageElement(desiredConfigurationMsgEle)
                .build();

        ap.setState(new ApRunState());
        NetworkManager.apDeviceList.put(ap.getApId(), ap);
        ap.startWaitKeepAliveTimer();

        byte[] retransmitMessage = AcampMessages.buildAcampMessage(sendConfigurationRequest, ap);
        ap.setRetransmitMessage(retransmitMessage);

        NetworkManager.sendMessageFromPort(retransmitMessage, ap.getConnectPoint());

        ap.startRetransmitTimer();

        ap.setControllerSequenceNumber(ap.getControllerSequenceNumber() + 1);
    }

    @Override
    public void getConfigurationResponse(ApDevice ap, AcampMessage acampMessage) {

    }

    @Override
    public void getConfigurationUpdateResponse(ApDevice ap, AcampMessage acampMessage) {

    }

    @Override
    public void getSystemResponse(ApDevice ap, AcampMessage acampMessage) {

    }

    @Override
    public void getKeepAliveRequest(ApDevice ap, AcampMessage acampMessage) {

    }

    @Override
    public void getUnregisterResponse(ApDevice ap, AcampMessage acampMessage) {

    }
}
