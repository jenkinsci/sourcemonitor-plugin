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

import java.util.Iterator;
import java.util.Map;

public class SourceMonitorSummary {

    private SourceMonitorSummary(){
        super();
    }

    public static String createReportSummary(SourceMonitorReport report, SourceMonitorReport previous){
        StringBuilder builder = new StringBuilder();
        builder.append("<a href=\"" + SourceMonitorBuildAction.URL_NAME + "\">SourceMonitor Results</a>");
        builder.append("\n");
        return builder.toString();
    }

    public static String createReportSummaryDetails(SourceMonitorReport report, SourceMonitorReport previousReport){

    	StringBuilder builder = new StringBuilder();
  
    
    	Iterator<Map<String, String>>  iteratorPrevious = null;
    	if(previousReport != null){
    		iteratorPrevious = previousReport.getCheckpoints().iterator();
    	}
    	
    	for (Map<String,String> checkpoint:report.getCheckpoints()){
    		builder.append("<li>");	
    		builder.append("Number of Lines :");
    		builder.append(checkpoint.get("M0"));
            if(previousReport != null){
            	Map<String, String> previousCheckpoint = iteratorPrevious.next();
                printDifference(Integer.parseInt(checkpoint.get("M0")), Integer.parseInt(previousCheckpoint.get("M10")), builder);
            }
            builder.append("</li>");
    	}
        return builder.toString();
    }

    private static void printDifference(int current, int previous, StringBuilder builder){
        float difference = current - previous;
        builder.append(" (");

        if(difference >= 0){
            builder.append('+');
        }
        builder.append(difference);
        builder.append(")");
    }
}
