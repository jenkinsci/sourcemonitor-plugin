/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.sourcemonitor;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Set;

public class SourceMonitorStep extends Step {
    private String summaryFilePath;
    private String detailsFilePath;
    private int maxComplexityThresholdMaximum = 0;
    private int maxComplexityThresholdMinimum = 0;
    private double averageComplexityThresholdMaximum = 0;
    private double averageComplexityThresholdMinimum = 0;
    private int commentCoverageThresholdMaximum = 0;
    private int commentCoverageThresholdMinimum = 0;
    private int maxStatementsThresholdMaximum = 0;
    private int maxStatementsThresholdMinimum = 0;

    @DataBoundConstructor
    public SourceMonitorStep(){

    }

    @DataBoundSetter
    public void setMaxStatementsThresholdMinimum(int maxStatementsThresholdMinimum) {
        this.maxStatementsThresholdMinimum = maxStatementsThresholdMinimum;
    }

    @DataBoundSetter
    public void setMaxStatementsThresholdMaximum(int maxStatementsThresholdMaximum) {
        this.maxStatementsThresholdMaximum= maxStatementsThresholdMaximum;
    }

    @DataBoundSetter
    public void setDetailsFilePath(String detailsFilePath) {
        this.detailsFilePath = detailsFilePath;
    }

    @DataBoundSetter
    public void setSummaryFilePath(String summaryFilePath) {
        this.summaryFilePath = summaryFilePath;
    }

    @DataBoundSetter
    public void setMaxComplexityThresholdMaximum(int maxComplexityThresholdMaximum) {
        this.maxComplexityThresholdMaximum = maxComplexityThresholdMaximum;
    }

    @DataBoundSetter
    public void setMaxComplexityThresholdMinimum(int maxComplexityThresholdMinimum) {
        this.maxComplexityThresholdMinimum = maxComplexityThresholdMinimum;
    }

    @DataBoundSetter
    public void setAverageComplexityThresholdMaximum(double averageComplexityThresholdMaximum) {
        this.averageComplexityThresholdMaximum = averageComplexityThresholdMaximum;
    }

    @DataBoundSetter
    public void setAverageComplexityThresholdMinimum(double averageComplexityThresholdMinimum) {
        this.averageComplexityThresholdMinimum = averageComplexityThresholdMinimum;
    }

    @DataBoundSetter
    public void setCommentCoverageThresholdMaximum(int commentCoverageThresholdMaximum) {
        this.commentCoverageThresholdMaximum = commentCoverageThresholdMaximum;
    }

    @DataBoundSetter
    public void setCommentCoverageThresholdMinimum(int commentCoverageThresholdMinimum) {
        this.commentCoverageThresholdMinimum = commentCoverageThresholdMinimum;
    }

    public String getSummaryFilePath() {
        return summaryFilePath;
    }
    public int getMaxComplexityThresholdMaximum() {
        return maxComplexityThresholdMaximum;
    }
    public int getMaxComplexityThresholdMinimum() {
        return maxComplexityThresholdMinimum;
    }
    public double getAverageComplexityThresholdMaximum() {
        return averageComplexityThresholdMaximum;
    }
    public double getAverageComplexityThresholdMinimum() {
        return averageComplexityThresholdMinimum;
    }
    public int getCommentCoverageThresholdMaximum() {
        return commentCoverageThresholdMaximum;
    }
    public int getCommentCoverageThresholdMinimum() {
        return commentCoverageThresholdMinimum;
    }

    public int getMaxStatementsThresholdMaximum() {
        return maxStatementsThresholdMaximum;
    }

    public int getMaxStatementsThresholdMinimum() {
        return maxStatementsThresholdMinimum;
    }

    public String getDetailsFilePath() {
        return detailsFilePath;
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new SourceMonitorStepExecution(this, stepContext);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, Run.class, Launcher.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "sourceMonitor";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Source Monitor Parser";
        }

        public FormValidation doCheckSummaryFilePath(@QueryParameter String value, @QueryParameter String detailsFilePath) {
            if (value.isEmpty() && detailsFilePath.isEmpty()) {
                return FormValidation.error("A valid file/path is required in either the details or summary field.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckMaxComplexityThresholdMaximum(@QueryParameter int value,
                                                                   @QueryParameter int maxComplexityThresholdMinimum) {
            if (value == 0) return FormValidation.warning("Max complexity health reporting disabled");
            if (value <= maxComplexityThresholdMinimum) {
                return FormValidation.error("The threshold maximum must be greater than the threshold minimum.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckAverageComplexityThresholdMaximum(@QueryParameter double value,
                                                                       @QueryParameter int averageComplexityThresholdMinimum) {
            if (value == 0) return FormValidation.warning("Average complexity health reporting disabled");
            if (value <= averageComplexityThresholdMinimum) {
                return FormValidation.error("The threshold maximum must be greater than the threshold minimum.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckCommentCoverageThresholdMaximum(@QueryParameter double value,
                                                                     @QueryParameter int commentCoverageThresholdMinimum) {
            if (value == 0) return FormValidation.warning("Comment coverage health reporting disabled");
            if (value <= commentCoverageThresholdMinimum) {
                return FormValidation.error("The threshold maximum must be greater than the threshold minimum.");
            }
            return FormValidation.ok();
        }

    }
}
