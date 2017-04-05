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
 * Acamp web resource.
 */
@Path("config")
public class AcampWebResource extends AbstractWebResource {

    /**
     * Get current connected aps' configuration
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

    /**
     * Set Ap configuration according to apId
     *
     * @return 200 OK
     */
    @POST
    @Path("set")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response configurationDelivery(InputStream inputStream) {
        ObjectNode node = mapper().createObjectNode();
        ObjectNode jsonTree = null;
        if (inputStream == null) {
            return ok(node.put("empty", "configuration")).build();
        }
        try {
            jsonTree = (ObjectNode) mapper().readTree(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonNode apId = jsonTree.get("apId");
        if (apId == null) {
            return ok(node.put("error", "configuration")).build();
        }
        ApDevice ap = NetworkManager.apDeviceList.get(apId.intValue());
        if (ap == null) {
            return ok(node.put("ap", "not found")).build();
        }
        AcampMessage sendConfigurationUpdateMessage = AcampMessages.buildConfigurationUpdateMessage(jsonTree);
        byte[] retransmitMessage = AcampMessages.buildAcampMessage(sendConfigurationUpdateMessage, ap);
        ap.setRetransmitMessage(retransmitMessage);
        NetworkManager.sendMessageFromPort(retransmitMessage, ap.getConnectPoint());
        ap.startRetransmitTimer();
        ap.setControllerSequenceNumber(ap.getControllerSequenceNumber() + 1);

        node.put("Complete", "configuration update");
        return ok(node).build();
    }

}
