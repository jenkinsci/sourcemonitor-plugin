package com.thalesgroup.hudson.plugins.sourcemonitor;

import java.io.Serializable;
import hudson.FilePath;
// serializable required for multibranch pipeline
public class ConfigurableParameters implements Serializable {

    private FilePath summaryFilePath;
    private FilePath detailsFilePath;
    private int maxComplexityThresholdMaximum;
    private int maxComplexityThresholdMinimum;
    private double averageComplexityThresholdMaximum;
    private double averageComplexityThresholdMinimum;
    private int commentCoverageThresholdMaximum;
    private int commentCoverageThresholdMinimum;
    private int maxStatementsThresholdMaximum;
    private int maxStatementsThresholdMinimum;
    private String relativeDetailsString;
    private String relativeSummaryString;

    /** Constructors */
    public ConfigurableParameters(FilePath summaryFilePath, FilePath detailsFilePath,
                                  int maxComplexityThresholdMaximum, int maxComplexityThresholdMinimum,
                                  double averageComplexityThresholdMaximum, double averageComplexityThresholdMinimum,
                                  int commentCoverageThresholdMaximum, int commentCoverageThresholdMinimum,
                                  int maxStatementsThresholdMaximum, int maxStatementsThresholdMinimum) {

        this.summaryFilePath = summaryFilePath;
        this.detailsFilePath = detailsFilePath;
        this.maxComplexityThresholdMaximum = maxComplexityThresholdMaximum;
        this.maxComplexityThresholdMinimum = maxComplexityThresholdMinimum;
        this.averageComplexityThresholdMaximum = averageComplexityThresholdMaximum;
        this.averageComplexityThresholdMinimum = averageComplexityThresholdMinimum;
        this.commentCoverageThresholdMaximum = commentCoverageThresholdMaximum;
        this.commentCoverageThresholdMinimum = commentCoverageThresholdMinimum;
        this.maxStatementsThresholdMaximum = maxStatementsThresholdMaximum;
        this.maxStatementsThresholdMinimum = maxStatementsThresholdMinimum;
    }

    public ConfigurableParameters() {
        this.summaryFilePath = null;
        this.detailsFilePath = null;
        this.maxComplexityThresholdMaximum = 0;
        this.maxComplexityThresholdMinimum = 0;
        this.averageComplexityThresholdMaximum = 0;
        this.averageComplexityThresholdMinimum = 0;
        this.commentCoverageThresholdMaximum = 0;
        this.commentCoverageThresholdMinimum = 0;
        this.maxStatementsThresholdMaximum = 0;
        this.maxStatementsThresholdMinimum = 0;
    }

    /** Getters and Setters */
    public FilePath getSummaryFilePath() {
        return summaryFilePath;
    }

    public void setSummaryFilePath(FilePath summaryFilePath) {
        this.summaryFilePath = summaryFilePath;
    }

    public FilePath getDetailsFilePath() {
        return detailsFilePath;
    }

    public void setDetailsFilePath(FilePath detailsFilePath) {
        this.detailsFilePath = detailsFilePath;
    }

    public int getMaxComplexityThresholdMaximum() {
        return maxComplexityThresholdMaximum;
    }

    public void setMaxComplexityThresholdMaximum(int maxComplexityThresholdMaximum) {
        this.maxComplexityThresholdMaximum = maxComplexityThresholdMaximum;
    }

    public int getMaxComplexityThresholdMinimum() {
        return maxComplexityThresholdMinimum;
    }

    public void setMaxComplexityThresholdMinimum(int maxComplexityThresholdMinimum) {
        this.maxComplexityThresholdMinimum = maxComplexityThresholdMinimum;
    }

    public double getAverageComplexityThresholdMaximum() {
        return averageComplexityThresholdMaximum;
    }

    public void setAverageComplexityThresholdMaximum(double averageComplexityThresholdMaximum) {
        this.averageComplexityThresholdMaximum = averageComplexityThresholdMaximum;
    }

    public double getAverageComplexityThresholdMinimum() {
        return averageComplexityThresholdMinimum;
    }

    public void setAverageComplexityThresholdMinimum(double averageComplexityThresholdMinimum) {
        this.averageComplexityThresholdMinimum = averageComplexityThresholdMinimum;
    }

    public int getCommentCoverageThresholdMaximum() {
        return commentCoverageThresholdMaximum;
    }

    public void setCommentCoverageThresholdMaximum(int commentCoverageThresholdMaximum) {
        this.commentCoverageThresholdMaximum = commentCoverageThresholdMaximum;
    }

    public int getCommentCoverageThresholdMinimum() {
        return commentCoverageThresholdMinimum;
    }

    public void setCommentCoverageThresholdMinimum(int commentCoverageThresholdMinimum) {
        this.commentCoverageThresholdMinimum = commentCoverageThresholdMinimum;
    }

    public int getMaxStatementsThresholdMaximum() {
        return maxStatementsThresholdMaximum;
    }

    public void setMaxStatementsThresholdMaximum(int maxStatementsThresholdMaximum) {
        this.maxStatementsThresholdMaximum = maxStatementsThresholdMaximum;
    }

    public int getMaxStatementsThresholdMinimum() {
        return maxStatementsThresholdMinimum;
    }

    public void setMaxStatementsThresholdMinimum(int maxStatementsThresholdMinimum) {
        this.maxStatementsThresholdMinimum = maxStatementsThresholdMinimum;
    }

    public String getRelativeDetailsString() {
        return relativeDetailsString;
    }

    public void setRelativeDetailsString(String relativeDetailsString) {
        this.relativeDetailsString = relativeDetailsString;
    }

    public String getRelativeSummaryString() {
        return relativeSummaryString;
    }

    public void setRelativeSummaryString(String relativeSummaryString) {
        this.relativeSummaryString = relativeSummaryString;
    }
}
