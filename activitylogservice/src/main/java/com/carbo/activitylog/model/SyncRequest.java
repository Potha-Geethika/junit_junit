package com.carbo.activitylog.model;

import java.util.List;
import java.util.Set;

public class SyncRequest<T> {
    private List<T> update;

    private Set<String> remove;

    private Set<String> get;

    public List<T> getUpdate() {
        return this.update;
    }

    public void setUpdate(List<T> update) {
        this.update = update;
    }

    public Set<String> getRemove() {
        return this.remove;
    }

    public void setRemove(Set<String> remove) {
        this.remove = remove;
    }

    public Set<String> getGet() {
        return this.get;
    }

    public void setGet(Set<String> get) {
        this.get = get;
    }

}
