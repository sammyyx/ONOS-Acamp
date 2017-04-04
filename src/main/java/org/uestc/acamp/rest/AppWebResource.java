/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uestc.acamp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.MacAddress;
import org.onosproject.rest.AbstractWebResource;
import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.network.NetworkManager;
import org.uestc.acamp.protocol.AcampMessage;
import org.uestc.acamp.protocol.AcampMessageConstant;
import org.uestc.acamp.protocol.AcampMessageElement;
import org.uestc.acamp.utils.AcampMessages;

import javax.crypto.Mac;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sample web resource.
 */
@Path("config")
public class AppWebResource extends AbstractWebResource {

    /**
     * Get hello world greeting.
     *
     * @return 200 OK
     */
    @GET
    @Path("get/{apId}")
    public Response getGreeting(@PathParam("apId") int apId) {
        String name = "null";
        ObjectNode node = mapper().createObjectNode();
        ApDevice ap = NetworkManager.apDeviceList.get(apId);
        if (ap == null) {
            return ok(node.put("ap", "not found")).build();
        }
        node.put("name", ap.getApName());
        node.put("descriptor", ap.getApDescriptor());
        node.put("channel", ap.getChannel());
        node.put("ssid", ap.getSsid());
        node.put("suppressedSsid", ap.getSuppressSsid());
        node.put("hwmode", ap.getHwMode().getValue());
        node.put("txpower", ap.getTxPower());
        node.put("macfilterMode", ap.getMacFilterMode().getValue());
        return ok(node).build();
    }

    @POST
    @Path("set")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response configurationDelivery(InputStream inputStream) {
        ObjectNode node = mapper().createObjectNode();
        if (inputStream == null) {
            return ok(node.put("empty", "configuration")).build();
        }
        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(inputStream);
            JsonNode apId = jsonTree.get("apId");
            if (apId == null) {
                return ok(node.put("error", "configuration")).build();
            }
            ApDevice ap = NetworkManager.apDeviceList.get(apId.intValue());
            if (ap == null) {
                return ok(node.put("ap", "not found")).build();
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
                        ap.setTxPower(macFilterMode[0]);
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
                                .setMessageElementType(AcampMessageConstant.MessageElementType.MAC_FILTER_LIST)
                                .setMessageElementValue(bb.array()).build();
                        sendConfigurationUpdateMessageBuilder.addMessageElement(element);
                        ap.setMacFilterList(macList);
                        break;
                    default:
                        break;
                }
            }
            AcampMessage sendConfigurationUpdateMessage = sendConfigurationUpdateMessageBuilder.build();
            NetworkManager.sendMessageFromPort(AcampMessages.buildAcampMessage(sendConfigurationUpdateMessage, ap), ap.getConnectPoint());
            ap.startRetransmitTimer();
            ap.setControllerSequenceNumber(ap.getControllerSequenceNumber() + 1);
            node.put("Complete", "configuration update");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(node).build();
    }

}
