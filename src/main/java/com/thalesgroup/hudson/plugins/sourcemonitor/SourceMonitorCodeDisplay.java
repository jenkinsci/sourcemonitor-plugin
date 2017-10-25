package com.thalesgroup.hudson.plugins.sourcemonitor;

import hudson.model.Run;
import java.io.Serializable;

// serializable required for multibranch pipeline
public class SourceMonitorCodeDisplay implements Serializable {

    private Run owner;
    private FileStats file;

    public SourceMonitorCodeDisplay(Run owner, FileStats file) {
        this.owner = owner;
        this.file = file;
    }

    public Run getOwner() {
        return owner;
    }

    public void setOwner(Run owner) {
        this.owner = owner;
    }

    public FileStats getFile() {
        return file;
    }

    public void setFile(FileStats file) {
        this.file = file;
    }
}
