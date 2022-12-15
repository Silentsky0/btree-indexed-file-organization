package dev.silentsky.btree;

import dev.silentsky.disk.File;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class BTree {

    @Setter
    @Getter
    int order;

    @Getter
    @Setter
    int height;

    @Getter
    @Setter
    Page root;

    @Getter
    @Setter
    Page currentPage;

    public BTree(int order) {
        this.order = order;
        this.height = 1;
        this.root = new Page(order, true, File.getNextPageIndex());

        Record r = new Record(20);
        root.insertRecord(r, 0);
    }

    /**
     *
     * @return index of element on page
     */
    int searchPage(Page page, int key) {
        int elementIndex = page.searchBisection(key, 0, page.numberOfElements - 1);

        if (page.keys[elementIndex].key == key) {
            return -1; // element already exists
        }

        if (page.pagePointers[elementIndex] == -1) {
            return elementIndex; // found free spot at elementIndex
        }

        Page nextPage = File.readPage(page.pagePointers[elementIndex]);
        return searchPage(nextPage, key);
    }

    /**
     * @return true if element was found, false otherwise
     */
    public Map.Entry<Integer, Integer> search(int key) {
        int recordIndex, pageIndex;

        this.currentPage = root;
        recordIndex = searchPage(root, key);
        pageIndex = currentPage.index;

        if (recordIndex < 0)
            recordIndex = 0;

        return Map.entry(pageIndex, recordIndex);
    }

    public void insert2(Record record) {
        int pageIndex, recordIndex;

        Map.Entry<Integer, Integer> r = search(record.id);
        pageIndex = r.getKey();
        recordIndex = r.getValue();

        if (recordIndex < 0) {
            System.out.println("Record already exists!");
            return;
        }

        Page pageToInsert = File.readPage(pageIndex);
        pageToInsert.insertRecord(record, recordIndex);

        while (pageToInsert.numberOfElements > 2 * order) {
            // compensation

            if (pageToInsert.isRoot) {
                Page rootCopy = pageToInsert;
                root = new Page(order, File.getNextPageIndex());
                root.pagePointers[0] = rootCopy.index;
                root.isRoot = true;
                rootCopy.isRoot = false;
                File.writePage(rootCopy, rootCopy.index);
                splitChildPage(root, 0);
            }else {
                Page parent = File.readPage(pageToInsert.parentPagePointer);



            }
            pageToInsert = File.readPage(pageToInsert.index);
            int parentPage = pageToInsert.parentPagePointer;
            if (parentPage == -1)
                break;
            pageToInsert = File.readPage(parentPage);
        }

        File.writePage(pageToInsert, pageToInsert.index);

        root = File.readPage(root.index);
        print();
    }

//    public void insert(Record record) {
//        int pageIndex, recordIndex;
//
//        Map.Entry<Integer, Integer> r = search(record.id);
//        pageIndex = r.getKey();
//        recordIndex = r.getValue();
//
//        if (recordIndex < 0) {
//            System.out.println("Record already exists!");
//            return;
//        }
//
//        Page pageToInsert = File.readPage(pageIndex);
//        pageToInsert.insert(record);
//
//
//        File.writeMetadata();
//    }


    public void splitChildPage(Page parent, int childIndex) {

        Page child;
        child = File.readPage(childIndex);

        Page newPage = new Page(order, File.getNextPageIndex());

        newPage.numberOfElements = File.tree.order;
        for (int i = 0; i < File.tree.order; i++) {
            newPage.keys[i] = child.keys[i + File.tree.order];
        }

        if (!child.isLeaf()) {
            for (int i = 0; i < File.tree.order; i++) {
                newPage.pagePointers[i] = child.pagePointers[i + File.tree.order];
            }
        }

        child.numberOfElements = File.tree.order;

        parent.insertIndex(child.keys[File.tree.order], childIndex);

        child.parentPagePointer = parent.index;
        newPage.parentPagePointer = parent.index;

        File.writePage(parent, parent.index);
        File.writePage(child, child.index);
        File.writePage(newPage, newPage.index);

    }

    public void insertNonFull(Page page, Record record) {
        if (page.isLeaf()) {
            int index = searchPage(page, record.id);
            page.insertRecord(record, index);
        }
        else {
            int index = searchPage(page, record.id);

            Page child = File.readPage(index);
            insertNonFull(child, record);
        }
    }

    public void print() {
        root.print();
    }

    public int getPageSize() {
        return 24 * order + 24;
    }
    public static int getMetadataSize() {
        return 4 * 3;
    }
}
