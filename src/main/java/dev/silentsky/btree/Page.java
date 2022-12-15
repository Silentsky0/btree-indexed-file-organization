package dev.silentsky.btree;

import dev.silentsky.disk.File;
import dev.silentsky.disk.Index;

import java.io.IOException;
import java.util.Arrays;

public class Page {
    public int index; // not written to file


    public int numberOfElements;
    public boolean isRoot;
    public int parentPagePointer;
    public int pageDepth;

    public Index[] keys;
    public Integer[] pagePointers;

    public Page(int treeOrder, boolean isRoot, int parentPagePointer, int pageDepth, int index) {
        this.isRoot = isRoot;
        this.parentPagePointer = parentPagePointer;
        this.pageDepth = pageDepth;
        this.numberOfElements = 0;

        this.keys = new Index[2 * treeOrder + 1];
        this.pagePointers = new Integer[2 * treeOrder + 1 + 1];
        this.index = index;

        for (int i = 0; i < 2 * treeOrder + 1; i++) this.keys[i] = new Index(-1, -1);
        Arrays.fill(pagePointers, -1);
    }

    public Page(int treeOrder, int index) {
        this(treeOrder, false, -1, -1, index);
    }

    public Page(int treeOrder, boolean isRoot, int index) {
        this(treeOrder, isRoot, -1, 0, index);
    }

    @Override
    public String toString() {
        return "Page{" +
                "index=" + index +
                ", numberOfElements=" + numberOfElements +
                ", isRoot=" + isRoot +
                ", parentPagePointer=" + parentPagePointer +
                ", pageDepth=" + pageDepth +
                ", keys=" + Arrays.toString(keys) +
                ", pagePointers=" + Arrays.toString(pagePointers) +
                '}';
    }

    public void insertRecord(Record record, int index) {
        for (int i = numberOfElements; i > index; i--) {
            keys[i].key = keys[i - 1].key;
            keys[i].dataPointer = keys[i - 1].dataPointer;
            pagePointers[i + 1] = pagePointers[i];
        }

        keys[index].key = record.id;
        pagePointers[index + 1] = -1;

        // TODO fileAddRecord - save record to data file

        numberOfElements += 1;
    }
    public void insertIndex(Index ind, int index) {
        for (int i = numberOfElements; i > index; i--) {
            keys[i] = keys[i + 1];
            pagePointers[i + 1] = pagePointers[i];
        }

        keys[index] = ind;
        pagePointers[index + 1] = -1;
        numberOfElements += 1;
    }

    /**
     * search page for key using bisection
     * @return index where the key was found (or where it should be)
     */
    public int searchBisection(int key, int left, int right) {
        if (left > right) return left;

        int pivot = (left + right) / 2;

        if (key < this.keys[pivot].getKey())
            return searchBisection(key, left, pivot - 1);
        if (key > this.keys[pivot].getKey())
            return searchBisection(key, pivot + 1, right);

        return pivot;
    }

    public boolean isLeaf() {
        for(Integer p : pagePointers) if (p != -1) return false;
        return true;
    }

    public void print() {
        if (parentPagePointer == -1) {
            System.out.println("- root page index " + index + " num of elements " + numberOfElements + " -");
        }else {
            System.out.println("- page index " + index + " num of elements " + numberOfElements + " parent " + parentPagePointer + " -");
        }
        for (int i = 0; i < numberOfElements; i++) {
            System.out.print(keys[i].key + " ");
        }
        System.out.println();

        for (Integer pagePointer : pagePointers) {
            if (pagePointer < 0) break;
            Page child = File.readPage(pagePointer);
            child.print();
        }
    }
}
