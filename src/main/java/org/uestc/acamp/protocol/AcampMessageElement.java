package org.uestc.acamp.protocol;

import org.onosproject.net.flow.TrafficSelector;

/**
 * Created by sammy on 17-3-27.
 * This class used to define message element from Acamp protocol.
 */
public class AcampMessageElement {
    private AcampMessageConstant.MessageElementType messageElementType;
    private int messageElementLength;
    private byte[] messageElementValue;

    public AcampMessageElement() {

    }

    public AcampMessageElement(Builder builder) {
        this.messageElementType = builder.messageElementType;
        this.messageElementLength = builder.messageElementLength;
        this.messageElementValue = builder.messageElementValue;
    }

    public static class Builder {
        private AcampMessageConstant.MessageElementType messageElementType;
        private int messageElementLength;
        private byte[] messageElementValue;

        public Builder setMessageElementType(AcampMessageConstant.MessageElementType messageElementType) {
            this.messageElementType = messageElementType;
            return this;
        }

        public Builder setMessageElementValue(byte[] messageElementValue) {
            this.messageElementValue = messageElementValue;
            return this;
        }

        public Builder setMessageElementLength(int messageElementLength) {
            this.messageElementLength = messageElementLength;
            return this;
        }

        public AcampMessageElement build() {
            this.messageElementLength = messageElementValue.length;
            return new AcampMessageElement(this);
        }
    }

    public AcampMessageConstant.MessageElementType getMessageElementType() {
        return messageElementType;
    }

    public void setMessageElementType(AcampMessageConstant.MessageElementType messageElementType) {
        this.messageElementType = messageElementType;
    }

    public int getMessageElementLength() {
        return messageElementLength;
    }

    public void setMessageElementLength(int messageElementLength) {
        this.messageElementLength = messageElementLength;
    }

    public byte[] getMessageElementValue() {
        return messageElementValue;
    }


    public void setMessageElementValue(byte[] messageElementValue) {
        this.messageElementValue = messageElementValue;
    }
}
