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

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.uestc.acamp.utils.ApDevices;

/**
 * Sample Apache Karaf CLI command
 */
@Command(scope = "onos", name = "acamp",
         description = "Sample Apache Karaf CLI command")
public class AcampCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "commandType", description = "Whether command is getting or setting",
            required = true, multiValued = false)
    String type = null;

    @Override
    protected void execute() {
        switch (type) {
            case "info":
                print("connected ap:%s", ApDevices.connectedApJsonObject());
                break;
        }
    }
}
