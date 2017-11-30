package com.thalesgroup.hudson.plugins.sourcemonitor;

import java.io.Serializable;

// serializable required for multibranch pipeline
public class FunctionStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private int complexity;
    private int statements;
    private String function;

    public FunctionStats(int complexity, int statements, String function) {
        this.complexity = complexity;
        this.statements = statements;
        this.function = function;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public int getStatements() {
        return statements;
    }

    public void setStatements(int statements) {
        this.statements = statements;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
