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

import org.apache.commons.lang.math.NumberUtils;
import java.util.*;

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
    	Map<String, String> previousReportMetrics = previousReport != null ? previousReport.getSummaryMetrics() : new HashMap<String, String>();

    	for (Map.Entry<String,String> entry:report.getSummaryMetrics().entrySet()) {
    	    if (NumberUtils.isNumber(entry.getValue())) {
                builder.append("<li>");
                builder.append(entry.getKey());
                builder.append(" : ");
                builder.append(entry.getValue());
                if (previousReportMetrics.containsKey(entry.getKey())) {
                    printDifference(Double.parseDouble(entry.getValue()), Double.parseDouble(previousReportMetrics.get(entry.getKey())), builder);
                }
                builder.append("</li>");
            }
    	}
        return builder.toString();
    }

    private static void printDifference(double current, double previous, StringBuilder builder){
        // Calculate the difference.
        double difference = current - previous;

        builder.append(" (");

        if(difference >= 0){
            builder.append('+');
        } else {
            builder.append('-');
        }

        builder.append(difference);
        builder.append(")");
    }
}
