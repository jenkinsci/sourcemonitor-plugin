package com.thalesgroup.hudson.plugins.sourcemonitor;

import java.io.Serializable;

// serializable required for multibranch pipeline
public class SourceMonitorUtility implements Serializable {

    public static int getStatementsHealth(ConfigurableParameters parameters, int maxStatements){
        int max = parameters.getMaxStatementsThresholdMaximum();
        int min = parameters.getMaxStatementsThresholdMinimum();
        int paramHealth = 0;

        if (maxStatements < min){
            paramHealth = 100;
        }
        else if (maxStatements > max){
            paramHealth = 0;
        }
        else{
            paramHealth = (max-maxStatements)*100/(max - min);
        }
        return paramHealth;
    }

    public static int getComplexityHealth(ConfigurableParameters parameters, int maxComplexity){
        int max = parameters.getMaxComplexityThresholdMaximum();
        int min = parameters.getMaxComplexityThresholdMinimum();
        int paramHealth = 0;

        if (maxComplexity < min){
            paramHealth = 100;
        }
        else if (maxComplexity > max){
            paramHealth = 0;
        }
        else{
            paramHealth = (max-maxComplexity)*100/(max - min);
        }
        return paramHealth;
    }

    public static String getColoredString(int paramHealth, int parameter){

        String coloredString;

        if (paramHealth > 80){
            coloredString = "<td style=\"color:darkgreen\">" + parameter;
        }
        else if(paramHealth > 60){
            coloredString = "<td style=\"color:darkorange\">" + parameter;
        }
        else {
            coloredString = "<td style=\"color:red;font-weight:bold\">" + parameter;
        }

        return coloredString;
    }
}
