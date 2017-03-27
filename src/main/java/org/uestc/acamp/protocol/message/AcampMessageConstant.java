package org.uestc.acamp.protocol.message;

/**
 * Created by sammy on 17-3-27.
 * this is a class used to store some constant from our protocol.
 */
public class AcampMessageConstant  {

    /**
     * Protocol header version filed
     * Length: 8 bits
     */
    public enum ProtocolVersion {
        CURRENT_VER((byte)0x03)
        ;

        private final byte value;

        ProtocolVersion(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * Protocol header Type filed
     * Length: 8 bits
     */
    public enum ProtocolType {
        CONTROL_MESSAGE((byte)0x00),
        ;

        private final byte value;

        ProtocolType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * Protocol header Message Type filed
     * Length: 2 bytes
     */
    public enum MessageType {
        KEEP_ALIVE_REQUEST((short)0x0001),
        KEEP_ALIVE_RESPONSE((short)0x0002),
        DISCOVERY_REQUEST((short)0x0003),
        DISCOVERY_RESPONSE((short)0x0004),
        REGISTER_REQUEST((short)0x0101),
        REGISTER_RESPONSE((short)0x0102),
        UNREGISTER_REQUEST((short)0x0103),
        UNREGISTER_RESPONSE((short)0x0104),
        CONFIGURATION_REQUEST((short)0x0201),
        CONFIGURATION_RESPONSE((short)0x0202),
        CONFIGURATION_UPDATE_REQUEST((short)0x0203),
        CONFIGURATION_UPDATE_RESPONSE((short)0x0204),
        SYSTEM_REQUEST((short)0x0307),
        SYSTEM_RESPONSE((short)0x0308)
        ;

        private final short value;

        MessageType(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }
    }

    /**
     * Protocol Message Element Type filed
     * Length: 2 bytes
     */
    public enum MessageElementType {
        RESULT_CODE((short)0x0001),
        REASON_CODE((short)0x0002),
        ASSIGNED_APID((short)0x0003),
        DISCOVERY_TYPE((short)0x0004),
        REGISTERED_SERVICE((short)0x0005),
        CONTROLLER_NAME((short)0x0006),
        CONTROLLER_DESCRIPTOR((short)0x0007),
        CONTROLLER_IP_ADDRESS((short)0x0008),
        CONTROLLER_MAC_ADDRESS((short)0x0009),
        AP_NAME((short)0x000a),
        AP_DESCRIPTOR((short)0x000b),
        AP_IP_ADDRESS((short)0x000c),
        AP_MAC_ADDRESS((short)0x000d),
        RETURNED_MESSAGE_ELEMENT((short)0x000e),
        CONTROLLER_NEXT_SEQUENCE_NUMBER((short)0x0010),
        DESIRED_CONFIGURATION_LIST((short)0x0011),
        SSID((short)0x0101),
        CHANNEL((short)0x0102),
        HARDWARE_MODE((short)0x0103),
        SUPPRESS_SSID((short)0x0104),
        SECURITY_OPTION((short)0x0105),
        MAC_FILTER_MODE((short)0x0106),
        MAC_FILTER_LIST((short)0x0107),
        WPA_PASSWORD((short)0x0202),
        ADD_MAC_FILTER_LIST((short)0x0501),
        DEL_MAC_FILTER_LIST((short)0x0502),
        CLEAR_MAC_FILTER_LIST((short)0x0503),
        SYSTEM_COMMAND((short)0x0401)
        ;

        private final short value;

        MessageElementType(short value) {
            this.value = value;
        }
    }
}
