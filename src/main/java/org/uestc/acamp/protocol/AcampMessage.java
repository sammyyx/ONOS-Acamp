package org.uestc.acamp.protocol;

import org.onlab.packet.IPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uestc.acamp.utils.AcampMessages;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by sammy on 17-3-27.
 * Builder for building an acamp message.
 */
public class AcampMessage implements IPacket {

    private AcampMessageConstant.ProtocolVersion protocolVersion;
    private AcampMessageConstant.ProtocolType protocolType;
    private int apId;
    private long sequenceNumber;
    private AcampMessageConstant.MessageType messageType;
    private int messageLength;
    private LinkedList<AcampMessageElement> messageElements;

    // Builder constructor
    public AcampMessage(Builder builder) {
        this.protocolVersion = builder.protocolVersion;
        this.protocolType = builder.protocolType;
        this.apId = builder.apId;
        this.sequenceNumber = builder.sequenceNumber;
        this.messageType = builder.messageType;
        this.messageLength = builder.messageLength;
        this.messageElements = builder.messageElements;
    }

    public AcampMessageConstant.ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public AcampMessageConstant.ProtocolType getProtocolType() {
        return protocolType;
    }

    public int getApId() {
        return apId;
    }

    public LinkedList<AcampMessageElement> getMessageElements() {
        return messageElements;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public AcampMessageConstant.MessageType getMessageType() {
        return messageType;
    }

    public int getMessageLength() {
        return messageLength;
    }

    //  Convert Object to byte array for communication purpose.
    @Override
    public byte[] serialize() {
        byte[] data = new byte[this.getMessageLength()];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.put(this.getProtocolVersion().getValue());
        bb.put(this.getProtocolType().getValue());
        bb.putShort((short)this.getApId());
        bb.putInt((int)this.getSequenceNumber());
        bb.putShort(this.getMessageType().getValue());
        bb.putShort((short)this.getMessageLength());
        bb.putInt(0x0000);
        for (AcampMessageElement messageElement: this.getMessageElements()) {
            bb.putShort(messageElement.getMessageElementType().getValue());
            int messageElementLength = messageElement.getMessageElementLength();
            bb.putShort((short)messageElementLength);
            byte[] value = messageElement.getMessageElementValue();
            for (int i = 0; i < messageElementLength; i++) {
                bb.put(value[i]);
            }
        }
        return data;
    }

    //  Acamp Message Builder
    //  Must end with build() function.
    public static class Builder {
        private AcampMessageConstant.ProtocolVersion protocolVersion = AcampMessageConstant.ProtocolVersion.CURRENT_VER;
        private AcampMessageConstant.ProtocolType protocolType = AcampMessageConstant.ProtocolType.CONTROL_MESSAGE;
        private int apId = 0;
        private long sequenceNumber = 0;
        private AcampMessageConstant.MessageType messageType = null;
        private int messageLength = AcampMessageConstant.getHeaderLength();
        private LinkedList<AcampMessageElement> messageElements = new LinkedList<AcampMessageElement>();

        public Builder setProtocolVersion(AcampMessageConstant.ProtocolVersion protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder setProtocolType(AcampMessageConstant.ProtocolType protocolType) {
            this.protocolType = protocolType;
            return this;
        }

        public Builder setApId(int apId) {
            this.apId = apId;
            return this;
        }

        public Builder setSequenceNumber(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public Builder setMessageType(AcampMessageConstant.MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder setMessageLength(int messageLength) {
            this.messageLength = messageLength;
            return this;
        }

        public Builder addMessageElement(AcampMessageElement messageElement) {

            //  Total length =
            //  message header length + message element header + message element length
            this.messageLength += 4;
            this.messageLength += messageElement.getMessageElementLength();

            this.messageElements.add(messageElement);
            return this;
        }

        public AcampMessage build() {
            return new AcampMessage(this);
        }
    }

    public static class Deserializer {
        private Builder builder = new Builder();
        private final Logger log = LoggerFactory.getLogger(getClass());

        public AcampMessage deserialize(byte[] data) {
            ByteBuffer bb = ByteBuffer.wrap(data);
            builder.setProtocolVersion(AcampMessageConstant.ProtocolVersion.getEnumProtocolVersion(bb.get()))
                    .setProtocolType(AcampMessageConstant.ProtocolType.getEnumProtocolType(bb.get()))
                    .setApId(AcampMessages.shortConvert2Int(bb.getShort()))
                    .setSequenceNumber(AcampMessages.intConvert2Long(bb.getInt()))
                    .setMessageType(AcampMessageConstant.MessageType.getEnumMessageType(bb.getShort()))
                    .setMessageLength(AcampMessages.shortConvert2Int(bb.getShort()));

            int reserved = bb.getInt();

            while(bb.hasRemaining() && bb.remaining() >= 5) {
                AcampMessageElement element = new AcampMessageElement();
                element.setMessageElementType(AcampMessageConstant.MessageElementType.getEnumMessageElementType(bb.getShort()));
                element.setMessageElementLength(AcampMessages.shortConvert2Int(bb.getShort()));
                int messageElementLength = element.getMessageElementLength();
                byte[] value = new byte[messageElementLength];
                for (int i = 0; i < messageElementLength; i++) {
                    value[i] = bb.get();
                }
                element.setMessageElementValue(value);
                builder.addMessageElement(element);
            }

            log.info("finish deserialization");

            return builder.build();
        }
    }

    @Override
    public IPacket getPayload() {
        return null;
    }

    @Override
    public IPacket setPayload(IPacket iPacket) {
        return null;
    }

    @Override
    public IPacket getParent() {
        return null;
    }

    @Override
    public IPacket setParent(IPacket iPacket) {
        return null;
    }

    @Override
    public void resetChecksum() {

    }

    @Override
    public IPacket deserialize(byte[] bytes, int i, int i1) {
        return null;
    }

    @Override
    public Object clone() {
        return null;
    }
}
