package dev.silentsky;

import dev.silentsky.btree.BTree;
import dev.silentsky.btree.Page;
import dev.silentsky.btree.Record;
import dev.silentsky.disk.File;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        new File(2, "test.file", true);

        BTree tree = File.tree;

        Record r = new Record(44);
        tree.insert2(r);

        r = new Record(12);
        tree.insert2(r);

        r = new Record(21);
        tree.insert2(r);

        r = new Record(27);
        tree.insert2(r);

        r = new Record(27);
        tree.insert2(r);

        tree.print();

    }
}