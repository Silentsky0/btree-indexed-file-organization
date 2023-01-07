package dev.silentsky.input;


import dev.silentsky.btree.Page;
import dev.silentsky.btree.Record;
import dev.silentsky.disk.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class Parser {

    public static void handleInstructions(String instructionsFile) {
        try {
            BufferedReader input =  new BufferedReader(new FileReader(instructionsFile));

            int treeOrder = Integer.parseInt(input.readLine());
            int keyIndexLimit = Integer.parseInt(input.readLine());

            new File(treeOrder, "tree.file", "data.file", true, keyIndexLimit);

            while(parse(input.readLine())) {
                //System.out.println();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean parse(String instruction) {
        if (instruction == null || instruction.isEmpty() || instruction.isBlank()) {
            System.out.println("line with instruction is empty!");
            return false;
        }

        String[] instructionTokens = instruction.split(" ");

        switch (instructionTokens[0]) {
            case "i":
                if (instructionTokens[1].isEmpty()) { // record with random key
                    File.tree.insert(new Record());
                }
                else { // record with specified key
                    File.tree.insert(new Record(Integer.parseInt(instructionTokens[1])));
                }
                return true;
            case "d":
                if (!instructionTokens[1].isEmpty()) { // key has to be specified
                    File.tree.delete(Integer.parseInt(instructionTokens[1]));
                }
                return true;
            case "p":
                File.tree.print();
                System.out.println();
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


                    // TODO update record - this input needs name, surname, age
                }
                else {
                    System.out.println("Update record: key wasn't provided!");
                    System.out.println("    correct syntax: u <key> <identity> <name> <surname> <age>");
                }
                return true;
            case "ir":
                if (!instructionTokens[1].isEmpty()) {
                    int elementsToGenerate = Integer.parseInt(instructionTokens[1]);
                    for (int i = 0; i < elementsToGenerate; i++) {
                        File.tree.insert(new Record());
                    }
                }
                return true;
            case "end":
                File.writeMetadata();
                File.writeBufferContents();
                return false;

        }

        return false;
    }
}
