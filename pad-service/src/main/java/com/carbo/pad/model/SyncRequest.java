package com.carbo.pad.model;

import java.util.List;
import java.util.Set;

public class SyncRequest {
    private List<Pad> update;

    private Set<String> remove;

    private Set<String> get;

    public List<Pad> getUpdate() {
        return this.update;
    }

    public void setUpdate(List<Pad> update) {
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
