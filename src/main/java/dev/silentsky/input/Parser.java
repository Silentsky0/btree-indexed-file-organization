package dev.silentsky.input;


import dev.silentsky.btree.Record;
import dev.silentsky.disk.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
                if (!instructionTokens[1].isEmpty()) {
                    // File.tree.updateRecord();
                    // TODO update record - this input needs name, surname, age
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
                return false;

        }

        return false;
    }
}
