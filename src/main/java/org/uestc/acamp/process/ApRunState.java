package org.uestc.acamp.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.network.NetworkManager;
import org.uestc.acamp.protocol.AcampMessage;
import org.uestc.acamp.protocol.AcampMessageConstant;
import org.uestc.acamp.protocol.AcampMessageElement;
import org.uestc.acamp.utils.AcampMessages;

/**
 * Created by sammy on 17-3-28.
 */
public class ApRunState implements ApFiniteStateMachine {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public void getDiscoveryRequest(ApDevice ap, AcampMessage acampMessage) {

    }

    @Override
    public void getRegisterRequest(ApDevice ap, AcampMessage acampMessage) {

    }

    @Override
    public void getConfigurationResponse(ApDevice ap, AcampMessage acampMessage) {
        log.info("get configuration response in run state");
        for (AcampMessageElement me: acampMessage.getMessageElements()) {
            ap.updateApDevice(me);
        }
        log.info("finish update configuration");
        ap.updateWaitKeepAliveTimer();
        ap.resetRetransmitCounters();
    }

    @Override
    public void getConfigurationUpdateResponse(ApDevice ap, AcampMessage acampMessage) {
        log.info("receive configuration update response");
        ap.updateWaitKeepAliveTimer();
        ap.resetRetransmitCounters();
    }

    @Override
    public void getSystemResponse(ApDevice ap, AcampMessage acampMessage) {

    }

    @Override
    public void getKeepAliveRequest(ApDevice ap, AcampMessage acampMessage) {
        AcampMessage sendKeepAliveResponse = new AcampMessage.Builder()
                .setMessageType(AcampMessageConstant.MessageType.KEEP_ALIVE_RESPONSE)
                .setApId(ap.getApId())
                .setProtocolVersion(AcampMessageConstant.ProtocolVersion.CURRENT_VER)
                .setProtocolType(AcampMessageConstant.ProtocolType.CONTROL_MESSAGE)
                .setSequenceNumber(ap.getApSequenceNumber())
                .build();

        byte[] cachedMessage = AcampMessages.buildAcampMessage(sendKeepAliveResponse, ap);
        ap.setCachedMessage(cachedMessage);

        NetworkManager.sendMessageFromPort(cachedMessage, ap.getConnectPoint());

        ap.setApSequenceNumber(ap.getApSequenceNumber() + 1);

        ap.updateWaitKeepAliveTimer();
    }

    @Override
    public void getUnregisterResponse(ApDevice ap, AcampMessage acampMessage) {

    }
}
