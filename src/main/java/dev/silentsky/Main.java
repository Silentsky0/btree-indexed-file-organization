package dev.silentsky;

import dev.silentsky.btree.BTree;
import dev.silentsky.btree.Record;
import dev.silentsky.disk.File;
import dev.silentsky.input.Parser;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {

        if (args.length >= 3) {
            if (args[2].equals("-v") || args[2].equals("--verbose")) {
                Parser.verbose = true;
            }
        }

        if (args[0].equals("-a") || args[0].equals("--automatic")) {
            if (args.length < 2) {
                System.out.println("Please provide a correct path to instructions    example: -a instr.txt");
                return;
            }

            Parser.handleInstructions(args[1]);
        }
        else if (args[0].equals("-i") || args[0].equals("--interactive")) {
            Parser.handleInteractive();
        }
        else {
            Parser.handleInstructions("instructions.txt");
        }
    }
}