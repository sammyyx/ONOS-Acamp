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
package org.uestc.acamp.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.network.NetworkManager;
import org.uestc.acamp.protocol.AcampMessage;
import org.uestc.acamp.utils.AcampMessages;
import org.uestc.acamp.utils.ApDevices;

import java.io.IOException;

/**
 * Acamp CLI command
 * First argument: info | set
 * Second argument(optional): provided json configuration
 */
@Command(scope = "onos", name = "acamp",
         description = "AP Control and Manage command")
public class AcampCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "commandType", description = "Whether command is getting or setting",
            required = true, multiValued = false)
    String type = null;

    @Argument(index = 1, name = "jsonConfig", description = "Configuration in json format",
            required = false, multiValued = false)
    String jsonConfig = null;

    @Override
    protected void execute() {
        switch (type) {
            case "info":
                print("connected ap:%s", ApDevices.connectedApJsonObject());
                break;
            case "set":
                ObjectNode jsonTree = null;
                try {
                    jsonTree = (ObjectNode) mapper().readTree(jsonConfig);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonNode apId = jsonTree.get("apId");
                if (apId == null) {
                    print("Error:%s", "Configuration error!");
                    return;
                }
                ApDevice ap = NetworkManager.apDeviceList.get(apId.intValue());
                if (ap == null) {
                    print("Error:%s", "Ap Not Found!");
                    return;
                }
                print("reading config");
                AcampMessage sendConfigurationUpdate = AcampMessages.buildConfigurationUpdateMessage(jsonTree);
                byte[] retransmitMessage = AcampMessages.buildAcampMessage(sendConfigurationUpdate, ap);
                ap.setRetransmitMessage(retransmitMessage);
                NetworkManager.sendMessageFromPort(retransmitMessage, ap.getConnectPoint());
                ap.startRetransmitTimer();
                ap.setControllerSequenceNumber(ap.getControllerSequenceNumber() + 1);
                break;
        }
    }
}
