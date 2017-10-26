package com.thalesgroup.hudson.plugins.sourcemonitor;

import hudson.model.Run;
import java.io.Serializable;

// serializable required for multibranch pipeline
public class SourceMonitorCodeDisplay implements Serializable {
    private static final long serialVersionUID = 1L;

    private Run owner;
    private FileStats file;

    public SourceMonitorCodeDisplay(Run owner, FileStats file) {
        this.owner = owner;
        this.file = file;
    }

    /** Getters and Setters */
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
