package com.github.sherter.jcon.examples.generic_attributes.interception.eval;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Used to write results of conflict resolution evaluation to a file.
 */
public class ConflictEvaluationWriter {
    private static ConflictEvaluationWriter INSTANCE;
    private int numberRules = 0;
    private long conflictTime = 0L;

    private ConflictEvaluationWriter() {

    }

    public static ConflictEvaluationWriter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConflictEvaluationWriter();
        }

        return INSTANCE;
    }

    public void newRuleReceived() {
        numberRules++;
    }

    public void addConflictTime(long time) {
        conflictTime = time;
    }

    public void writeResults() {
        if (conflictTime == 0)
            return;

        numberRules -= 1;
        String fileName = "reassignment.csv";

        String path = System.getProperty("user.home") + File.separator + "Documents" + File.separator
                + "ma-koerver-thesis" + File.separator + "thesis" + File.separator + "eval"
                + File.separator + "z_conflict" + File.separator + fileName;

        File resultsFile = new File(path);

        if (!resultsFile.exists()) {
            try {
                resultsFile.createNewFile();

                PrintWriter writer = new PrintWriter(path, "UTF-8");
                writer.print("num_rules,time");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String installationTime =
                    "\n" + numberRules + "," + conflictTime;
            Files.write(Paths.get(path), installationTime.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
