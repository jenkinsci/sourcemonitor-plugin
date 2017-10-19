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

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
public class SourceMonitorPublisher extends Recorder implements Serializable, SimpleBuildStep {
    private static final long serialVersionUID = 1L;

    private String summaryFilePath = null;
    private String detailsFilePath = null;
    private int maxComplexityThresholdMaximum = 0;
    private int maxComplexityThresholdMinimum = 0;
    private double averageComplexityThresholdMaximum = 0;
    private double averageComplexityThresholdMinimum = 0;
    private int commentCoverageThresholdMaximum = 0;
    private int commentCoverageThresholdMinimum = 0;
    private int maxStatementsThresholdMaximum = 0;
    private int maxStatementsThresholdMinimum = 0;

    @DataBoundConstructor
    public SourceMonitorPublisher(String summaryFilePath, String detailsFilePath){
        this.summaryFilePath = summaryFilePath;
        this.detailsFilePath = detailsFilePath;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    protected boolean canContinue(final Result result) {
        return result != Result.ABORTED && result != Result.FAILURE;
    }

	public String getSummaryFilePath() {
		return summaryFilePath;
	}

    @DataBoundSetter
    public void setMaxComplexityThresholdMaximum(int maxComplexityThresholdMaximum) {
        this.maxComplexityThresholdMaximum = maxComplexityThresholdMaximum;
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

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        if(this.canContinue(run.getResult())){

            listener.getLogger().println("Parsing sourcemonitor results");
            SourceMonitorParser parser;

            PrintStream logger = listener.getLogger();
            if (detailsFilePath != null) {
                parser = new SourceMonitorParser(new FilePath(filePath, summaryFilePath), new FilePath(filePath, detailsFilePath));
            }
            else{
                parser = new SourceMonitorParser(new FilePath(filePath, summaryFilePath));
            }


            // Set the parameters for the health metrics.
            parser.setAverageComplexityThresholdMaximum(averageComplexityThresholdMaximum);
            parser.setAverageComplexityThresholdMinimum(averageComplexityThresholdMinimum);
            parser.setCommentCoverageThresholdMaximum(commentCoverageThresholdMaximum);
            parser.setCommentCoverageThresholdMinimum(commentCoverageThresholdMinimum);
            parser.setMaxComplexityThresholdMaximum(maxComplexityThresholdMaximum);
            parser.setMaxComplexityThresholdMinimum(maxComplexityThresholdMinimum);
            parser.setMaxStatementsThresholdMaximum(maxStatementsThresholdMaximum);
            parser.setMaxStatementsThresholdMinimum(maxStatementsThresholdMinimum);

            SourceMonitorReport report;
            try{
                report = filePath.act(parser);
            }catch(IOException | InterruptedException ioe){
                ioe.printStackTrace(logger);
                run.setResult(Result.FAILURE);
                return;
            }

            run.setResult(getBuildResult(run, Result.SUCCESS));

            SourceMonitorResult result = new SourceMonitorResult(report, run);
            SourceMonitorBuildAction buildAction = new SourceMonitorBuildAction(run, result);
            run.addAction(buildAction);

            listener.getLogger().println("End Processing sourcemonitor results");
        }
        return;
    }

    private Result getBuildResult(Run<?, ?> run, Result result) {
        Result currentResult = result;
        Result previousStepResult = run.getResult();
        if (previousStepResult != null) {
            if (previousStepResult != Result.NOT_BUILT && previousStepResult.isWorseOrEqualTo(result)) {
                currentResult = previousStepResult;
            }
        }
        return currentResult;
    }
}
