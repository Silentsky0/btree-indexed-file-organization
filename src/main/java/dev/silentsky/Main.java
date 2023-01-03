package dev.silentsky;

import dev.silentsky.btree.BTree;
import dev.silentsky.btree.Record;
import dev.silentsky.disk.File;

public class Main {
    public static void main(String[] args) {
        new File(2, "tree.file", "data.file", true, 300);

        BTree tree = File.tree;

        Record r = new Record(44);
        tree.insert(r);

        r = new Record(12);
        tree.insert(r);

        r = new Record(21);
        tree.insert(r);

        r = new Record(27);
        tree.insert(r);

        r = new Record(28);
        tree.insert(r);

        r = new Record(29);
        tree.insert(r);

        r = new Record(30);
        tree.insert(r);

        tree.print();
        System.out.println();

        r = new Record(4);
        tree.insert(r);

        tree.print();
        System.out.println();

        r = new Record(19);
        tree.insert(r);

        r = new Record(2);
        tree.insert(r);

        r = new Record(5);
        tree.insert(r);

        r = new Record(100);
        tree.insert(r);

        r = new Record(36);
        tree.insert(r);

        r = new Record(6);
        tree.insert(r);

        r = new Record(7);
        tree.insert(r);

        r = new Record(23);
        tree.insert(r);

        r = new Record(25);
        tree.insert(r);

        r = new Record(3);
        tree.insert(r);

        r = new Record(67);
        tree.insert(r);

        r = new Record(81);
        tree.insert(r);

        r = new Record(24);
        tree.insert(r);

        tree.print();
        System.out.println();

        tree.delete(100);

        tree.delete(44);

        tree.print();
        System.out.println();

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



        tree.print();

    }
}