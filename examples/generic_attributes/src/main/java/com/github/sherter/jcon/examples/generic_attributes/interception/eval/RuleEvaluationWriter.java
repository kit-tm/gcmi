package com.github.sherter.jcon.examples.generic_attributes.interception.eval;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Writes results of the rule installation evaluation to a file.
 */
public class RuleEvaluationWriter {
    private Long ruleInstallationTime = 0l;
    private Long conflictCheckTime = 0l;
    private int numberRules = 0;

    private Long flowModTimer = 0l;
    private Long conflictRecognitionTimer = 0l;

    private static RuleEvaluationWriter INSTANCE;

    private RuleEvaluationWriter() {

    }

    public static RuleEvaluationWriter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RuleEvaluationWriter();
        }

        return INSTANCE;
    }


    public void flowModReceived() {
        flowModTimer = System.currentTimeMillis();
    }

    public void startConflictCheck() {
        conflictRecognitionTimer = System.currentTimeMillis();
    }

    public void endConflictCheck() {
        conflictCheckTime += (System.currentTimeMillis() - conflictRecognitionTimer);
    }

    public void flowModTransformed() {
        ruleInstallationTime += (System.currentTimeMillis() - flowModTimer);
        numberRules += 1;
    }

    public void writeResults() {
        String fileName = "mf_03.csv";

        String path = System.getProperty("user.home") + File.separator + "Documents" + File.separator
                + "ma-koerver-thesis" + File.separator + "thesis" + File.separator + "eval"
                + File.separator + "z_rule_time" + File.separator + fileName;

        File resultsFile = new File(path);

        if (!resultsFile.exists()) {
            try {
                resultsFile.createNewFile();

                PrintWriter writer = new PrintWriter(path, "UTF-8");
                writer.print("num_rules,flowmod_time,conflict_recog_time");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String installationTime =
                    "\n" + numberRules + "," + ruleInstallationTime + "," + conflictCheckTime;
            Files.write(Paths.get(path), installationTime.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
