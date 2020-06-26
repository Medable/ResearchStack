package org.researchstack.backbone.omron.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.researchstack.backbone.omron.model.enumerate.Protocol;

import jp.co.ohq.ble.enumerate.OHQDeviceCategory;

public class DeviceInfo implements Parcelable {
    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel source) {
            return new DeviceInfo(source);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };
    private String address;
    private String localName;
    private String completeLocalName;
    private String modelName;
    private String deviceCategory;
    private String protocol;
    private Integer consentCode;

    public DeviceInfo() {
    }

    protected DeviceInfo(Parcel in) {
        this.address = in.readString();
        this.localName = in.readString();
        this.completeLocalName = in.readString();
        this.modelName = in.readString();
        this.deviceCategory = in.readString();
        this.protocol = in.readString();
        this.consentCode = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getCompleteLocalName() {
        return completeLocalName;
    }

    public void setCompleteLocalName(String completeLocalName) {
        this.completeLocalName = completeLocalName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public OHQDeviceCategory getDeviceCategory() {
        if (null == deviceCategory) {
            return null;
        }
        return OHQDeviceCategory.valueOf(deviceCategory);
    }

    public void setDeviceCategory(OHQDeviceCategory deviceCategory) {
        if (null == deviceCategory) {
            this.deviceCategory = null;
        } else {
            this.deviceCategory = deviceCategory.name();
        }
    }

    public Protocol getProtocol() {
        if (null == protocol) {
            return null;
        }
        return Protocol.valueOf(protocol);
    }

    public void setProtocol(Protocol protocol) {
        if (null == protocol) {
            this.protocol = null;
        } else {
            this.protocol = protocol.name();
        }
    }

    public Integer getConsentCode() {
        return consentCode;
    }

    public void setConsentCode(Integer consentCode) {
        this.consentCode = consentCode;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "address='" + address + '\'' +
                ", localName='" + localName + '\'' +
                ", completeLocalName='" + completeLocalName + '\'' +
                ", modelName='" + modelName + '\'' +
                ", deviceCategory='" + deviceCategory + '\'' +
                ", protocol='" + protocol + '\'' +
                ", consentCode=" + consentCode +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address);
        dest.writeString(this.localName);
        dest.writeString(this.completeLocalName);
        dest.writeString(this.modelName);
        dest.writeString(this.deviceCategory);
        dest.writeString(this.protocol);
        dest.writeValue(this.consentCode);
    }
}