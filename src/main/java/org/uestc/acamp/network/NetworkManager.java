package org.uestc.acamp.network;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.device.ControllerDevice;
import org.uestc.acamp.process.ApPreRunState;
import org.uestc.acamp.protocol.AcampMessage;
import org.uestc.acamp.protocol.AcampMessageConstant;
import org.uestc.acamp.utils.AcampMessages;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.onlab.packet.IPv4.PROTOCOL_UDP;

/**
 * Created by sammy on 17-3-27.
 * This is where our program begins.
 * Registering the packetService from onos. Processing Acamp Message according to
 * Finite State Machine.
 */
@Component(immediate = true)
public class NetworkManager {

    // Can not define in a class that is not a component
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    private ApplicationId appId;
    private AcampPacketProcessor processor;
    public static Map<Integer, ApDevice> apDeviceList;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {
        // Service binding
        ControllerDevice.initHwAddress();
        ControllerDevice.packetService = packetService;

        // For MultiAps supports
        apDeviceList = Collections.synchronizedMap(new HashMap<Integer, ApDevice>());

        appId = coreService.registerApplication("org.uestc.acamp");
        processor = new AcampPacketProcessor();
        packetService.addProcessor(processor, PacketProcessor.director(2));

        // Filtering the packet-in packet by its udp port
        TrafficSelector.Builder acampSelector = DefaultTrafficSelector.builder();
        acampSelector.matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_UDP)
                .matchUdpDst(TpPort.tpPort(AcampMessageConstant.getServicePort()));
        packetService.requestPackets(acampSelector.build(), PacketPriority.REACTIVE, appId);

        // Filtering the arp request for controller's mac
        TrafficSelector.Builder arpSelector = DefaultTrafficSelector.builder();
        arpSelector.matchEthType(Ethernet.TYPE_ARP)
                .matchArpTpa(ControllerDevice.getIp4Address());
        packetService.requestPackets(arpSelector.build(), PacketPriority.REACTIVE, appId);

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        // Remove packet processor
        packetService.removeProcessor(processor);
        processor = null;

        // Remove flows that used to filtering the packet
        TrafficSelector.Builder acampSelector = DefaultTrafficSelector.builder();
        acampSelector.matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_UDP)
                .matchUdpDst(TpPort.tpPort(AcampMessageConstant.getServicePort()));

        TrafficSelector.Builder arpSelector = DefaultTrafficSelector.builder();
        arpSelector.matchEthType(Ethernet.TYPE_ARP)
                .matchArpTpa(ControllerDevice.getIp4Address());

        packetService.cancelPackets(arpSelector.build(), PacketPriority.REACTIVE, appId);
        packetService.cancelPackets(acampSelector.build(), PacketPriority.REACTIVE, appId);

        log.info("Stopped");
    }

    private class AcampPacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext packetContext) {

            Ethernet ethernetPacket = packetContext.inPacket().parsed();

            // Process Arp Request and tell ap where the controller is
            if (ethernetPacket.getEtherType() == Ethernet.TYPE_ARP) {
                log.info("received an arp, trying to process");
                ARP arp = (ARP) ethernetPacket.getPayload();
                byte[] apIpAddress = arp.getSenderProtocolAddress();
                byte[] apMacAddress = arp.getSenderHardwareAddress();
                arp.setSenderProtocolAddress(arp.getTargetProtocolAddress());
                arp.setSenderHardwareAddress(ControllerDevice.getHardwareAddress().toBytes());
                arp.setTargetHardwareAddress(apMacAddress);
                arp.setTargetProtocolAddress(apIpAddress);
                arp.setOpCode(ARP.OP_REPLY);
                Ethernet sendArp = new Ethernet();
                sendArp.setEtherType(Ethernet.TYPE_ARP);
                sendArp.setDestinationMACAddress(ethernetPacket.getSourceMACAddress());
                sendArp.setSourceMACAddress(ControllerDevice.getHardwareAddress());
                sendArp.setPayload(arp);
                sendMessageFromPort(sendArp.serialize(), packetContext.inPacket().receivedFrom());
                return;
            }

            if (ethernetPacket.getEtherType() != Ethernet.TYPE_IPV4) {
                return;
            }
            IPv4 ipv4Packet = (IPv4) ethernetPacket.getPayload();
            if (ipv4Packet.getProtocol() != IPv4.PROTOCOL_UDP) {
                return;
            }
            UDP udpPacket = (UDP) ipv4Packet.getPayload();
            if (udpPacket.getDestinationPort() != AcampMessageConstant.getServicePort()) {
                return;
            }

            AcampMessage acampMessage = new AcampMessage.Deserializer().deserialize(udpPacket.getPayload().serialize());
            ApDevice ap = apDeviceList.get(acampMessage.getApId());
            if (ap == null) {
                ap = new ApDevice();
                ap.setMacAddress(ethernetPacket.getSourceMAC());
                ap.setIpAddress(Ip4Address.valueOf(ipv4Packet.getSourceAddress()));
                log.info("ap's ip address:" + ap.getIpAddress());
                ap.setUdpPort(TpPort.tpPort(udpPacket.getSourcePort()));
                ap.setConnectPoint(packetContext.inPacket().receivedFrom());
                ap.setControllerSequenceNumber(AcampMessages.genSequenceNumber());
                ap.setApSequenceNumber(acampMessage.getSequenceNumber());
            }

            log.info("apid:" + ap.getApId());
            log.info("ap state" + ap.getState());

            // Message validation check, first check whether the message is
            // request or response
            // 1 - request
            // 0 - response
            if (acampMessage.getMessageType().getValue() % 2 == 1) {
                if (acampMessage.getSequenceNumber() != ap.getApSequenceNumber() &&
                        acampMessage.getSequenceNumber() != ap.getApSequenceNumber() - 1) {
                    return;
                }
                if (acampMessage.getSequenceNumber() == ap.getApSequenceNumber() - 1) {
                    NetworkManager.sendMessageFromPort(ap.getCachedMessage(), ap.getConnectPoint());
                    return;
                }
            }
            else {
                if (acampMessage.getSequenceNumber() != ap.getControllerSequenceNumber() &&
                        acampMessage.getSequenceNumber() != ap.getControllerSequenceNumber() - 1) {
                    return;
                }
            }

            switch(acampMessage.getMessageType()) {
                case DISCOVERY_REQUEST:
                    ap.getState().getDiscoveryRequest(ap, acampMessage);
                    break;
                case REGISTER_REQUEST:
                    ap.getState().getRegisterRequest(ap, acampMessage);
                    break;
                case CONFIGURATION_RESPONSE:
                    ap.getState().getConfigurationResponse(ap, acampMessage);
                    break;
                case CONFIGURATION_UPDATE_RESPONSE:
                    ap.getState().getConfigurationUpdateResponse(ap, acampMessage);
                    break;
                case SYSTEM_RESPONSE:
                    ap.getState().getSystemResponse(ap, acampMessage);
                    break;
                case KEEP_ALIVE_REQUEST:
                    ap.getState().getKeepAliveRequest(ap, acampMessage);
                    break;
                case UNREGISTER_REQUEST:
                    ap.getState().getUnregisterResponse(ap, acampMessage);
                    break;
                default:
                    break;
            }
        }
    }

    public static void sendMessageFromPort(byte[] message, ConnectPoint throughPoint) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder().setOutput(throughPoint.port());
        ControllerDevice.packetService.emit(new DefaultOutboundPacket(throughPoint.deviceId(),
                builder.build(), ByteBuffer.wrap(message)));
    }
}
