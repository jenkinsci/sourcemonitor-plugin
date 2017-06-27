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

import javax.annotation.Nonnull;


public class SourceMonitorPublisher extends Recorder implements Serializable, SimpleBuildStep{

    private static final long serialVersionUID = 1L;

    private final String summaryFilePath;
    
    @DataBoundConstructor
    public SourceMonitorPublisher(String summaryFilePath){
        this.summaryFilePath = summaryFilePath;
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


    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        if(this.canContinue(run.getResult())){

            listener.getLogger().println("Parsing sourcemonitor results");

            PrintStream logger = listener.getLogger();
            SourceMonitorParser parser = new SourceMonitorParser(new FilePath(filePath, summaryFilePath));

            SourceMonitorReport report;
            try{
                report = filePath.act(parser);
            }catch(IOException | InterruptedException ioe){
                ioe.printStackTrace(logger);
                run.setResult(Result.FAILURE);
                return;
            }

            // Set thresholds for the report.
            //report.setMaxComplexityThreshold(10);


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
