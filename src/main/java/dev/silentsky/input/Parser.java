package dev.silentsky.input;


import dev.silentsky.btree.Record;
import dev.silentsky.disk.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser {

    public void handleInstructions(String instructionsFile) throws IOException {
        BufferedReader input =  new BufferedReader(new FileReader(instructionsFile));

        int treeOrder = Integer.parseInt(input.readLine());
        int pageIndexLimit = Integer.parseInt(input.readLine());

        new File(treeOrder, "tree.file", "data.file", true, pageIndexLimit);

        while(parse(input.readLine())) {
            System.out.println();
        }


    }
    public boolean parse(String instruction) {
        if (instruction.isEmpty() || instruction.isBlank()) {
            System.out.println("instruction is empty!");
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
            case "s":
                break;
            case "u":
                if (!instructionTokens[1].isEmpty()) {
                    //File.tree.updateRecord();
                }
                break;
        }

        return false;
    }
}
