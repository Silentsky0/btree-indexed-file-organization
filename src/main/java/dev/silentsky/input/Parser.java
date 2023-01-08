package dev.silentsky.input;


import dev.silentsky.btree.Page;
import dev.silentsky.btree.Record;
import dev.silentsky.disk.File;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class Parser {

    public static boolean verbose = false;
    private static BufferedWriter output;

    public static void handleInstructions(String instructionsFile) {
        try {
            BufferedReader input =  new BufferedReader(new FileReader(instructionsFile));
            output = new BufferedWriter(new FileWriter("logs.csv"));

            int treeOrder = Integer.parseInt(input.readLine());
            int keyIndexLimit = Integer.parseInt(input.readLine());

            new File(treeOrder, "tree.file", "data.file", true, keyIndexLimit);

            output.write("Instruction;Number of records;Mem operations;Disk operations;Elapsed time [ms]\n");

            while (true) {
                String line = input.readLine();

                Instant start = Instant.now();
                boolean status =  parse(line);
                Instant finish = Instant.now();

                if (!status) break;

                double timeElapsed = (double) Duration.between(start, finish).toNanos() / 1000000;

                if (verbose) System.out.println("Time elapsed: " + timeElapsed + "\n");
                output.write(timeElapsed + "\n");
            }

            output.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void handleInteractive() {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Enter the order of the tree:");
            int treeOrder = Integer.parseInt(input.readLine());

            System.out.println("Enter the maximum key index value:");
            int keyIndexLimit = Integer.parseInt(input.readLine());

            System.out.println("Enter path of index file:");
            String treeFile = input.readLine();

            System.out.println("Enter path of data file");
            String dataFile = input.readLine();

            new File(treeOrder, treeFile, dataFile, true, keyIndexLimit);

            while (true) {
                String line = input.readLine();

                Instant start = Instant.now();
                boolean status =  parse(line);
                Instant finish = Instant.now();

                long timeElapsed = Duration.between(start, finish).toMillis();

                if (verbose) System.out.println("Time elapsed: " + timeElapsed);
                output.write(timeElapsed + ";\n");

                if (!status) break;
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static boolean parse(String instruction) throws IOException {
        if (instruction == null || instruction.isEmpty() || instruction.isBlank()) {
            System.out.println("line with instruction is empty!");
            return false;
        }

        String[] instructionTokens = instruction.split(" ");
        if (!instructionTokens[0].equals("end")) output.write(instructionTokens[0] + ";");

        long diskOperationsStart = File.diskPageWrites + File.diskPageReads;
        long memOperationsStart = File.memPageReads + File.memPageWrites;

        switch (instructionTokens[0]) {
            case "i":
                if (instructionTokens.length == 1 || instructionTokens[1].isEmpty()) { // record with random key
                    if (verbose) System.out.println("Inserting random record");
                    File.tree.insert(new Record());
                }
                else { // record with specified key
                    if (verbose) System.out.println("Inserting record of key " + Integer.parseInt(instructionTokens[1]));
                    File.tree.insert(new Record(Integer.parseInt(instructionTokens[1])));
                }
                output.write("1;");
                output.write((File.memPageReads + File.memPageWrites - memOperationsStart) + ";" + (File.diskPageReads + File.diskPageWrites - diskOperationsStart) + ";");
                if (verbose) System.out.println("Disk operations taken:  mem: " + (File.memPageReads + File.memPageWrites - memOperationsStart) + " disk: " + (File.diskPageReads + File.diskPageWrites - diskOperationsStart));
                return true;
            case "d":
                if (!instructionTokens[1].isEmpty()) { // key has to be specified
                    if (verbose) System.out.println("Deleting record of key " + Integer.parseInt(instructionTokens[1]));
                    File.tree.delete(Integer.parseInt(instructionTokens[1]));

                    output.write("1;");
                    output.write((File.memPageReads + File.memPageWrites - memOperationsStart) + ";" + (File.diskPageReads + File.diskPageWrites - diskOperationsStart) + ";");

                    if (verbose) System.out.println("Disk operations taken:  mem: " +
                            (File.memPageReads + File.memPageWrites - memOperationsStart) + " disk: " + (File.diskPageReads + File.diskPageWrites - diskOperationsStart));
                }
                return true;
            case "p":
                File.tree.print();
                System.out.println();

                output.write("0;");
                output.write((File.memPageReads + File.memPageWrites - memOperationsStart) + ";" + (File.diskPageReads + File.diskPageWrites - diskOperationsStart) + ";");
                if (verbose) System.out.println("Disk operations taken:  mem: " +
                        (File.memPageReads + File.memPageWrites - memOperationsStart) + " disk: " + (File.diskPageReads + File.diskPageWrites - diskOperationsStart));
                return true;
            case "u":
                if (!instructionTokens[1].isEmpty()) { // u <key> <identity> <name> <surname> <age>

                    if (!instructionTokens[2].isEmpty()
                            && !instructionTokens[3].isEmpty()
                            && !instructionTokens[4].isEmpty()
                            && !instructionTokens[5].isEmpty()) {

                        String identity = instructionTokens[2];
                        Record record = new Record(identity.substring(0, 2),
                                Integer.parseInt(identity.substring(3, 7)),
                                instructionTokens[3],
                                instructionTokens[4],
                                Integer.parseInt(instructionTokens[5]));

                        File.tree.updateRecord(Integer.parseInt(instructionTokens[1]), record);
                    }
                    else {
                        System.out.println("Update record: insufficient amount of information!");
                        System.out.println("    correct syntax: u <key> <identity> <name> <surname> <age>");
                    }
                    output.write("1;");
                    output.write((File.memPageReads + File.memPageWrites - memOperationsStart) + ";" + (File.diskPageReads + File.diskPageWrites - diskOperationsStart) + ";");
                    if (verbose) System.out.println("Disk operations taken:  mem: " +
                            (File.memPageReads + File.memPageWrites - memOperationsStart) + " disk: " + (File.diskPageReads + File.diskPageWrites - diskOperationsStart));

                }
                else {
                    System.out.println("Update record: key wasn't provided!");
                    System.out.println("    correct syntax: u <key> <identity> <name> <surname> <age>");
                }
                return true;
            case "ir":
                if (!instructionTokens[1].isEmpty()) {
                    int elementsToGenerate = Integer.parseInt(instructionTokens[1]);
                    if (verbose) System.out.println("Inserting " + elementsToGenerate + " random records");
                    int generated = 0;
                    while (generated < elementsToGenerate) {
                        boolean status = File.tree.insert(new Record());

                        if (status) {
                            generated += 1;
                        }
                    }
                    output.write(elementsToGenerate + ";");
                    output.write((File.memPageReads + File.memPageWrites - memOperationsStart) + ";" + (File.diskPageReads + File.diskPageWrites - diskOperationsStart) + ";");
                    if (verbose) System.out.println("Disk operations taken:  mem: " +
                            (File.memPageReads + File.memPageWrites - memOperationsStart) + " disk: " + (File.diskPageReads + File.diskPageWrites - diskOperationsStart));
                }
                return true;
            case "ip":
                if (instructionTokens[1].isEmpty()) { // record with random key
                    if (verbose) System.out.println("Inserting random record");
                    File.tree.insert(new Record());
                }
                else { // record with specified key
                    if (verbose) System.out.println("Inserting record of key " + Integer.parseInt(instructionTokens[1]));
                    File.tree.insert(new Record(Integer.parseInt(instructionTokens[1])));
                }
                File.tree.print();
                output.write("1;");
                output.write((File.memPageReads + File.memPageWrites - memOperationsStart) + ";" + (File.diskPageReads + File.diskPageWrites - diskOperationsStart) + ";");
                if (verbose) System.out.println("Disk operations taken:  mem: " +
                        (File.memPageReads + File.memPageWrites - memOperationsStart) + " disk: " + (File.diskPageReads + File.diskPageWrites - diskOperationsStart));
                return true;
            case "dp":
                if (!instructionTokens[1].isEmpty()) { // key has to be specified
                    if (verbose) System.out.println("Deleting record of key " + Integer.parseInt(instructionTokens[1]));
                    File.tree.delete(Integer.parseInt(instructionTokens[1]));
                }
                File.tree.print();
                output.write("1;");
                output.write((File.memPageReads + File.memPageWrites - memOperationsStart) + ";" + (File.diskPageReads + File.diskPageWrites - diskOperationsStart) + ";");
                if (verbose) System.out.println("Disk operations taken:  mem: " +
                        (File.memPageReads + File.memPageWrites - memOperationsStart) + " disk: " + (File.diskPageReads + File.diskPageWrites - diskOperationsStart));
                return true;
            case "end":
                File.writeMetadata();
                File.writeBufferContents();
                File.tree.checkValidity();
                File.closeFile();
                return false;

        }

        return false;
    }
}
