package org.researchstack.backbone.omron.model.entity;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;

public class SessionData {
    @Nullable
    private Map<OHQSessionOptionKey, Object> option;
    @Nullable
    private OHQDeviceCategory deviceCategory;
    @Nullable
    private String modelName;
    @Nullable
    private String currentTime;
    @Nullable
    private Integer batteryLevel;
    @Nullable
    private Integer userIndex;
    @Nullable
    private Map<OHQUserDataKey, Object> userData;
    @Nullable
    private Long databaseChangeIncrement;
    @Nullable
    private Integer sequenceNumberOfLatestRecord;
    @Nullable
    private List<Map<OHQMeasurementRecordKey, Object>> measurementRecords;
    @Nullable
    private OHQCompletionReason completionReason;

    @Nullable
    public Map<OHQSessionOptionKey, Object> getOption() {
        return option;
    }

    public void setOption(@Nullable Map<OHQSessionOptionKey, Object> option) {
        this.option = option;
    }

    @Nullable
    public OHQDeviceCategory getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(@Nullable OHQDeviceCategory deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    @Nullable
    public String getModelName() {
        return modelName;
    }

    public void setModelName(@Nullable String modelName) {
        this.modelName = modelName;
    }

    @Nullable
    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(@Nullable String currentTime) {
        this.currentTime = currentTime;
    }

    @Nullable
    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(@Nullable Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Nullable
    public Integer getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(@Nullable Integer userIndex) {
        this.userIndex = userIndex;
    }

    @Nullable
    public Map<OHQUserDataKey, Object> getUserData() {
        return userData;
    }

    public void setUserData(@Nullable Map<OHQUserDataKey, Object> userData) {
        this.userData = userData;
    }

    @Nullable
    public Long getDatabaseChangeIncrement() {
        return databaseChangeIncrement;
    }

    public void setDatabaseChangeIncrement(@Nullable Long databaseChangeIncrement) {
        this.databaseChangeIncrement = databaseChangeIncrement;
    }

    @Nullable
    public Integer getSequenceNumberOfLatestRecord() {
        return sequenceNumberOfLatestRecord;
    }

    public void setSequenceNumberOfLatestRecord(@Nullable Integer sequenceNumberOfLatestRecord) {
        this.sequenceNumberOfLatestRecord = sequenceNumberOfLatestRecord;
    }

    @Nullable
    public List<Map<OHQMeasurementRecordKey, Object>> getMeasurementRecords() {
        return measurementRecords;
    }

    public void setMeasurementRecords(@Nullable List<Map<OHQMeasurementRecordKey, Object>> measurementRecords) {
        this.measurementRecords = measurementRecords;
    }

    @Nullable
    public OHQCompletionReason getCompletionReason() {
        return completionReason;
    }

    public void setCompletionReason(@Nullable OHQCompletionReason completionReason) {
        this.completionReason = completionReason;
    }

    @Override
    public String toString() {
        return "SessionData{" +
                "option=" + option +
                ", deviceCategory=" + deviceCategory +
                ", modelName='" + modelName + '\'' +
                ", currentTime='" + currentTime + '\'' +
                ", batteryLevel=" + batteryLevel +
                ", userIndex=" + userIndex +
                ", userData=" + userData +
                ", databaseChangeIncrement=" + databaseChangeIncrement +
                ", sequenceNumberOfLatestRecord=" + sequenceNumberOfLatestRecord +
                ", measurementRecords=" + measurementRecords +
                ", completionReason=" + completionReason +
                '}';
    }
}