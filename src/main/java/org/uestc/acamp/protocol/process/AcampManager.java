package org.uestc.acamp.protocol.process;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uestc.acamp.protocol.message.AcampMessageConstant;

/**
 * Created by sammy on 17-3-27.
 * This is where our program begins.
 * Registering the packetService from onos. Processing Acamp Message according to
 * Finite State Machine.
 */
@Component(immediate = true)
public class AcampManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {
        int type  = AcampMessageConstant.MessageType.REGISTER_REQUEST.getValue();
        log.info("type:" + type);
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

}
