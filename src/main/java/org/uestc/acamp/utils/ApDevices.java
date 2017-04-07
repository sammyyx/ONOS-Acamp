package org.uestc.acamp.utils;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ObjectArrays;
import org.onlab.packet.MacAddress;
import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.network.NetworkManager;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by sammy on 17-3-28.
 * tools for ApDevice
 */
public class ApDevices {
    public static int apId = 0;

    public static int assignNewApId() {
        apId++;
        return apId;
    }

    public static ArrayNode connectedApJsonObject() {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (ApDevice ap:NetworkManager.apDeviceList.values()) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("name", ap.getApName());
            node.put("id", ap.getApId());
            node.put("descriptor", ap.getApDescriptor());
            node.put("channel", ap.getChannel());
            node.put("ssid", ap.getSsid());
            node.put("suppressedSsid", ap.getSuppressSsid());
            node.put("hwmode", ap.getHwMode().getValue());
            node.put("txpower", ap.getTxPower());
            node.put("macFilterMode", ap.getMacFilterMode().getValue());
            ArrayNode maclist = node.putArray("macFilterList");
            for (MacAddress macAddress: ap.getMacFilterList()) {
                maclist.add(macAddress.toString());
            }
            array.add(node);
        }
        return array;
    }
}
