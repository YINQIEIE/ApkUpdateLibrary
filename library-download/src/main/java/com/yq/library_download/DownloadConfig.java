package com.yq.library_download;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/4/13.
 */

public class DownloadConfig implements Serializable {

    private String url;
    private String updateMsg;//更新信息
    private String savePath;
    private String localName;
    private String version;
    private boolean forceToUpdate;
    private boolean append;

    public DownloadConfig(String url, String updateMsg, String savePath, String localName, String version, boolean forceToUpdate, boolean append) {
        this.url = url;
        this.updateMsg = updateMsg;
        this.savePath = savePath;
        this.localName = localName;
        this.version = version;
        this.forceToUpdate = forceToUpdate;
        this.append = append;
    }

    public String getUpdateMsg() {
        return updateMsg;
    }

    public void setUpdateMsg(String updateMsg) {
        this.updateMsg = updateMsg;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isForceToUpdate() {
        return forceToUpdate;
    }

    public void setForceToUpdate(boolean forceToUpdate) {
        this.forceToUpdate = forceToUpdate;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }
}
