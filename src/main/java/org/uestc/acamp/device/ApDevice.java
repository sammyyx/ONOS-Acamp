package org.uestc.acamp.device;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TpPort;
import org.onosproject.net.ConnectPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uestc.acamp.network.NetworkManager;
import org.uestc.acamp.process.ApFiniteStateMachine;
import org.uestc.acamp.process.ApPreRunState;
import org.uestc.acamp.protocol.AcampMessage;
import org.uestc.acamp.protocol.AcampMessageConstant;
import org.uestc.acamp.protocol.AcampMessageElement;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sammy on 17-3-27.
 * This is a class representing the Access Point in ACAMP Protocol.
 * Including the unique APID and its configuration, also the finite machine process.
 */
public class ApDevice {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private int apId;
    private String apName;
    private String apDescriptor;
    private byte registeredService;
    private ApFiniteStateMachine state;
    private MacAddress macAddress;
    private Ip4Address ipAddress;
    private TpPort udpPort;
    private ConnectPoint connectPoint;
    private long apSequenceNumber;
    private long controllerSequenceNumber;
    private byte[] retransmitMessage;
    private byte[] cachedMessage;
    private int retransmitCount = 0;
    private int retransmitInterval = 3;
    private Timer waitKeepAliveTimer;
    private Timer retransmitTimer;

    private String ssid;
    private byte channel;
    private AcampMessageConstant.HardwareMode hwMode;
    private boolean suppressSsid = false;
    private AcampMessageConstant.MacFilterMode macFilterMode = AcampMessageConstant.MacFilterMode.NO_MAC_ACCESS_CONTROL;
    private LinkedList<MacAddress> macFilterList;
    private byte txPower;
    private String wpaPassword;

    public ApDevice() {
        state = new ApPreRunState();
    }

    public ApFiniteStateMachine getState() {
        return state;
    }

    public void setState(ApFiniteStateMachine state) {
        this.state = state;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    public Ip4Address getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(Ip4Address ipAddress) {
        this.ipAddress = ipAddress;
    }

    public TpPort getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(TpPort udpPort) {
        this.udpPort = udpPort;
    }

    public ConnectPoint getConnectPoint() {
        return connectPoint;
    }

    public void setConnectPoint(ConnectPoint connectPoint) {
        this.connectPoint = connectPoint;
    }

    public int getApId() {
        return apId;
    }

    public void setApId(int apId) {
        this.apId = apId;
    }

    public long getApSequenceNumber() {
        return apSequenceNumber;
    }

    public void setApSequenceNumber(long apSequenceNumber) {
        this.apSequenceNumber = apSequenceNumber;
    }

    public long getControllerSequenceNumber() {
        return controllerSequenceNumber;
    }

    public void setControllerSequenceNumber(long controllerSequenceNumber) {
        this.controllerSequenceNumber = controllerSequenceNumber;
    }

    public String getApName() {
        return apName;
    }

    public void setApName(String apName) {
        this.apName = apName;
    }

    public String getApDescriptor() {
        return apDescriptor;
    }

    public void setApDescriptor(String apDescriptor) {
        this.apDescriptor = apDescriptor;
    }

    public byte getRegisteredService() {
        return registeredService;
    }

    public void setRegisteredService(byte registeredService) {
        this.registeredService = registeredService;
    }

    public void updateApDevice(AcampMessageElement me) {
        switch (me.getMessageElementType()) {
            case REGISTERED_SERVICE:
                this.setRegisteredService(ByteBuffer.wrap(me.getMessageElementValue()).get());
                break;
            case AP_NAME:
                this.setApName(new String(me.getMessageElementValue()));
                break;
            case AP_DESCRIPTOR:
                this.setApDescriptor(new String(me.getMessageElementValue()));
                break;
            case AP_IP_ADDRESS:
                this.setIpAddress(Ip4Address.valueOf(me.getMessageElementValue()));
                break;
            case AP_MAC_ADDRESS:
                this.setMacAddress(MacAddress.valueOf(me.getMessageElementValue()));
                break;
            case SSID:
                this.setSsid(new String(me.getMessageElementValue()));
                break;
            case CHANNEL:
                this.setChannel(ByteBuffer.wrap(me.getMessageElementValue()).get());
                break;
            case HARDWARE_MODE:
                this.setHwMode(AcampMessageConstant.HardwareMode.getEnumHardwareMode(ByteBuffer.wrap(me.getMessageElementValue()).get()));
                break;
            case SUPPRESS_SSID:
                this.setSuppressSsid(ByteBuffer.wrap(me.getMessageElementValue()).get() != 0x0);
                break;
            case MAC_FILTER_MODE:
                this.setMacFilterMode(AcampMessageConstant.MacFilterMode.getEnumMacFilterMode(ByteBuffer.wrap(me.getMessageElementValue()).get()));
                break;
            case MAC_FILTER_LIST:
                LinkedList<MacAddress> macAddresses = new LinkedList<>();
                ByteBuffer bb = ByteBuffer.wrap(me.getMessageElementValue());
                while(bb.hasRemaining() || bb.remaining() >= 6) {
                    byte[] bytes = new byte[6];
                    for (int i = 0; i < 6; i ++) {
                        bytes[i] = bb.get();
                    }
                    macAddresses.add(MacAddress.valueOf(bytes));
                }
                this.setMacFilterList(macAddresses);
                break;
            case TX_POWER:
                this.setTxPower(ByteBuffer.wrap(me.getMessageElementValue()).get());
                break;
            case WPA_PASSWORD:
                this.setWpaPassword(new String(me.getMessageElementValue()));
                break;
            default:
                break;
        }
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public AcampMessageConstant.HardwareMode getHwMode() {
        return hwMode;
    }

    public void setHwMode(AcampMessageConstant.HardwareMode hwMode) {
        this.hwMode = hwMode;
    }

    public boolean getSuppressSsid() {
        return suppressSsid;
    }

    public void setSuppressSsid(boolean suppressSsid) {
        this.suppressSsid = suppressSsid;
    }

    public AcampMessageConstant.MacFilterMode getMacFilterMode() {
        return macFilterMode;
    }

    public void setMacFilterMode(AcampMessageConstant.MacFilterMode macFilterMode) {
        this.macFilterMode = macFilterMode;
    }

    public LinkedList<MacAddress> getMacFilterList() {
        return macFilterList;
    }

    public void setMacFilterList(LinkedList<MacAddress> macFilterList) {
        this.macFilterList = macFilterList;
    }

    public byte getTxPower() {
        return txPower;
    }

    public void setTxPower(byte txPower) {
        this.txPower = txPower;
    }

    public String getWpaPassword() {
        return wpaPassword;
    }

    public void setWpaPassword(String wpaPassword) {
        this.wpaPassword = wpaPassword;
    }

    public byte[] getRetransmitMessage() {
        return retransmitMessage;
    }

    public void setRetransmitMessage(byte[] retransmitMessage) {
        this.retransmitMessage = retransmitMessage;
    }

    public byte[] getCachedMessage() {
        return cachedMessage;
    }

    public void setCachedMessage(byte[] cachedMessage) {
        this.cachedMessage = cachedMessage;
    }

    public int getRetransmitCount() {
        return retransmitCount;
    }

    public void setRetransmitCount(int retransmitCount) {
        this.retransmitCount = retransmitCount;
    }

    public int getRetransmitInterval() {
        return retransmitInterval;
    }

    public void setRetransmitInterval(int retransmitInterval) {
        this.retransmitInterval = retransmitInterval;
    }

    public Timer getWaitKeepAliveTimer() {
        return waitKeepAliveTimer;
    }

    public Timer getRetransmitTimer() {
        return retransmitTimer;
    }

    public void startWaitKeepAliveTimer() {
        waitKeepAliveTimer = new Timer();
        WaitKeepAliveTimerTask timerTask = new WaitKeepAliveTimerTask();
        waitKeepAliveTimer.schedule(timerTask, AcampMessageConstant.getWaitKeepAliveMaxTime() * 1000);
    }

    public void startRetransmitTimer() {
        retransmitTimer = new Timer();
        RetransmitTimerTask timerTask = new RetransmitTimerTask();
        retransmitTimer.schedule(timerTask, retransmitInterval * 1000);
    }

    public void updateWaitKeepAliveTimer() {
        waitKeepAliveTimer.cancel();
        startWaitKeepAliveTimer();
    }

    public void resetRetransmitCounters() {
        this.retransmitTimer.cancel();
        this.retransmitCount = 0;
        this.retransmitInterval = 3;
    }

    public class WaitKeepAliveTimerTask extends TimerTask {

        @Override
        public void run() {
            NetworkManager.apDeviceList.remove(apId);
            log.info("keep alive time out: delete ap");
        }
    }

    public class RetransmitTimerTask extends TimerTask {

        @Override
        public void run() {
            if (retransmitCount >= AcampMessageConstant.getRetransmitMaxCount()) {
                NetworkManager.apDeviceList.remove(apId);
                retransmitTimer.cancel();
                log.info("over max retransmit count: delete ap");
                return;
            }
            NetworkManager.sendMessageFromPort(retransmitMessage, connectPoint);
            log.info("retransmit packet");
            retransmitInterval = retransmitInterval * 2;
            retransmitCount++;
            startRetransmitTimer();
        }
    }
}
