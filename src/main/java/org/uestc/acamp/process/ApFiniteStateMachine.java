package org.uestc.acamp.process;

import org.uestc.acamp.device.ApDevice;
import org.uestc.acamp.protocol.AcampMessage;

/**
 * Created by sammy on 17-3-27.
 */
public interface ApFiniteStateMachine {
    public void getDiscoveryRequest(ApDevice ap, AcampMessage acampMessage);
    public void getRegisterRequest(ApDevice ap, AcampMessage acampMessage);
    public void getConfigurationResponse(ApDevice ap, AcampMessage acampMessage);
    public void getConfigurationUpdateResponse(ApDevice ap, AcampMessage acampMessage);
    public void getSystemResponse(ApDevice ap, AcampMessage acampMessage);
    public void getKeepAliveRequest(ApDevice ap, AcampMessage acampMessage);
    public void getUnregisterResponse(ApDevice ap, AcampMessage acampMessage);
}
