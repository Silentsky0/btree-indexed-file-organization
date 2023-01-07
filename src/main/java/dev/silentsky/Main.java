package dev.silentsky;

import dev.silentsky.btree.BTree;
import dev.silentsky.btree.Record;
import dev.silentsky.disk.File;
import dev.silentsky.input.Parser;

public class Main {
    public static void main(String[] args) {

        if (args[0].equals("-a") || args[0].equals("--automatic")) {
            if (args.length < 2) {
                System.out.println("Please provide a correct path to instructions    example: -a instr.txt");
                return;
            }

            Parser.handleInstructions(args[1]);
        }
        else if (args[0].equals("-i") || args[0].equals("--interactive")) {
            // TODO
        }
        else {
            Parser.handleInstructions("intructions.txt");
        }


//        new File(2, "tree.file", "data.file", true, 300);
//
//        BTree tree = File.tree;
//
//        Record r = new Record(44);
//        tree.insert(r);
//
//        r = new Record(12);
//        tree.insert(r);
//
//        r = new Record(21);
//        tree.insert(r);
//
//        r = new Record(27);
//        tree.insert(r);
//
//        r = new Record(28);
//        tree.insert(r);
//
//        r = new Record(29);
//        tree.insert(r);
//
//        r = new Record(30);
//        tree.insert(r);
//
//        r = new Record(33);
//        tree.insert(r);
//
//        r = new Record(35);
//        tree.insert(r);
//
//        tree.delete(12);
//
//
//        tree.delete(33);
//        tree.delete(30);
//        tree.delete(35);
//        tree.delete(29);
//        tree.print();
//        System.out.println();
//
//        tree.delete(28);
//        tree.print();
//        System.out.println();
//
//        r = new Record(2);
//        tree.insert(r);
//
//        r = new Record(9);
//        tree.insert(r);
//
//        tree.print();
//        System.out.println();
//
//        r = new Record(12);
//        tree.insert(r);
//
//        tree.print();
//        System.out.println();
//
//        r = new Record(3);
//        tree.insert(r);
//        tree.print();
//        System.out.println();



//        tree.print();
//        System.out.println();
//
//        r = new Record(4);
//        tree.insert(r);
//
//        tree.print();
//        System.out.println();
//
//        r = new Record(19);
//        tree.insert(r);
//
//        r = new Record(2);
//        tree.insert(r);
//
//        r = new Record(5);
//        tree.insert(r);
//
//        r = new Record(100);
//        tree.insert(r);
//
//        r = new Record(36);
//        tree.insert(r);
//
//        r = new Record(6);
//        tree.insert(r);
//
//        r = new Record(7);
//        tree.insert(r);
//
//        r = new Record(23);
//        tree.insert(r);
//
//        r = new Record(25);
//        tree.insert(r);
//
//        r = new Record(3);
//        tree.insert(r);
//
//        r = new Record(67);
//        tree.insert(r);
//
//        r = new Record(81);
//        tree.insert(r);
//
//        r = new Record(24);
//        tree.insert(r);
//
//        tree.print();
//        System.out.println();
//
//        tree.checkValidity();
//
//        tree.delete(100);
//
//        tree.delete(44);
//
//        tree.print();
//        System.out.println();
//
//        tree.checkValidity();

//        r = new Record();
//        tree.insert(r);
//
//        tree.print();
//        System.out.println();
//
//        r = new Record();
//        tree.insert(r);
//
//        tree.print();
//        System.out.println();
//
//        r = new Record();
//        tree.insert(r);
//
//        tree.print();
//        System.out.println();
//
//        for (int i = 0; i < 10; i++ ) {
//            r = new Record();
//            tree.insert(r);
//        }
    }
}