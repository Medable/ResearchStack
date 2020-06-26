package org.researchstack.backbone.omron.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.LocalName;

import java.util.ArrayList;
import java.util.List;

import jp.co.ohq.ble.advertising.EachUserData;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;

public class DiscoveredDevice implements Parcelable {
    public static final Creator<DiscoveredDevice> CREATOR = new Creator<DiscoveredDevice>() {
        @Override
        public DiscoveredDevice createFromParcel(Parcel source) {
            return new DiscoveredDevice(source);
        }

        @Override
        public DiscoveredDevice[] newArray(int size) {
            return new DiscoveredDevice[size];
        }
    };
    @NonNull
    private final String mAddress;
    @Nullable
    private List<ADStructure> mAdvertisementData = new ArrayList<>();
    @Nullable
    private OHQDeviceCategory mDeviceCategory;
    private int mRssi;
    @Nullable
    private String mModelName;
    @Nullable
    private String mLocalName;

    public DiscoveredDevice(@NonNull final String address) {
        mAddress = address;
    }

    protected DiscoveredDevice(Parcel in) {
        this.mAddress = in.readString();
        this.mAdvertisementData = new ArrayList<>();
        in.readList(this.mAdvertisementData, ADStructure.class.getClassLoader());
        int tmpMDeviceCategory = in.readInt();
        this.mDeviceCategory = tmpMDeviceCategory == -1 ? null : OHQDeviceCategory.values()[tmpMDeviceCategory];
        this.mRssi = in.readInt();
        this.mModelName = in.readString();
        this.mLocalName = in.readString();
    }

    @NonNull
    public String getAddress() {
        return mAddress;
    }

    @Nullable
    public List<ADStructure> getAdvertisementData() {
        return mAdvertisementData;
    }

    public void setAdvertisementData(@Nullable List<ADStructure> advertisementData) {
        mAdvertisementData = advertisementData;
    }

    @Nullable
    public OHQDeviceCategory getDeviceCategory() {
        return mDeviceCategory;
    }

    public void setDeviceCategory(@Nullable OHQDeviceCategory deviceCategory) {
        mDeviceCategory = deviceCategory;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    @Nullable
    public String getModelName() {
        return mModelName;
    }

    public void setModelName(@Nullable String modelName) {
        mModelName = modelName;
    }

    @Nullable
    public String getLocalName() {
        return mLocalName;
    }

    public void setLocalName(@Nullable String localName) {
        mLocalName = localName;
    }

    @Override
    public String toString() {
        return "DiscoveredDevice{" +
                "mAddress='" + mAddress + '\'' +
                ", mAdvertisementData=" + mAdvertisementData +
                ", mDeviceCategory=" + mDeviceCategory +
                ", mRssi=" + mRssi +
                ", mModelName='" + mModelName + '\'' +
                ", mLocalName='" + mLocalName + '\'' +
                '}';
    }

    @Nullable
    public String getCompleteLocalName() {
        if (null == mAdvertisementData) {
            return null;
        }
        String ret = null;
        for (ADStructure structure : mAdvertisementData) {
            if (structure instanceof LocalName) {
                ret = ((LocalName) structure).getLocalName();
                break;
            }
        }
        return ret;
    }

    public boolean isOmronExtensionProtocolSupported() {
        return null != getEachUserData();
    }

    @Nullable
    public EachUserData getEachUserData() {
        if (null == mAdvertisementData) {
            return null;
        }
        EachUserData ret = null;
        for (ADStructure structure : mAdvertisementData) {
            if (structure instanceof EachUserData) {
                ret = (EachUserData) structure;
                break;
            }
        }
        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mAddress);
        dest.writeList(this.mAdvertisementData);
        dest.writeInt(this.mDeviceCategory == null ? -1 : this.mDeviceCategory.ordinal());
        dest.writeInt(this.mRssi);
        dest.writeString(this.mModelName);
        dest.writeString(this.mLocalName);
    }
}