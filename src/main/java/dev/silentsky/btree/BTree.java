package dev.silentsky.btree;

import dev.silentsky.disk.File;
import dev.silentsky.disk.Index;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
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
        // zero out any leftover indices that could screw with searching
//        for (int i = page.numberOfElements; i < 2 * File.tree.order; i++) {
//            page.keys[i].key = -1;
//        }

        int elementIndex = page.searchBisection(key, 0, page.numberOfElements - 1);

        if (page.keys[elementIndex].key == key && elementIndex < page.numberOfElements) {
            return -1; // element already exists
        }

        if (page.pagePointers[elementIndex] == -1) {
            return elementIndex; // found free spot at elementIndex
        }

        Page nextPage = File.readPage(page.pagePointers[elementIndex]);
        return searchPage(nextPage, key);
    }

    public Map.Entry<Integer, Integer> search(int key) {
        int recordIndex, pageIndex;

        this.currentPage = root;
        recordIndex = searchPage(root, key);
        pageIndex = currentPage.index;

        return Map.entry(pageIndex, recordIndex);
    }

    public void insert(Record record) {
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
        File.writePage(pageToInsert, pageToInsert.index);

        // handle overflow
        if (pageToInsert.numberOfElements > 2 * order)
            handleOverflow(pageToInsert);

        root = File.readPage(root.index);
    }

    public void handleOverflow(Page page) {
        if (compensate(page)) {
            return;
        }

        if (page.isRoot) {
            root = new Page(order, File.getNextPageIndex());
            root.pagePointers[0] = page.index;
            root.isRoot = true;
            root.pageDepth = 0;

            page.isRoot = false;
            page.pageDepth += 1;

            File.writePage(page, page.index);
            splitChildPage(root, 0);
        }
        else {
            Page parent = File.readPage(page.parentPagePointer);
            splitChildPage(parent, parent.findChild(page.index));
            parent = File.readPage(parent.index);

            if (parent.numberOfElements > 2 * order) {
                handleOverflow(parent);
            }
        }
    }

    public boolean compensate(Page page) {

        if (page.parentPagePointer < 0) return false;

        Page parent = File.readPage(page.parentPagePointer);

        int pageChildIndex = parent.findChild(page.index);

        int neighbourRightIndex = pageChildIndex + 1;
        if (parent.pagePointers[neighbourRightIndex] != -1) {
            Page neighbourRight = File.readPage(parent.pagePointers[neighbourRightIndex]);
            if (neighbourRight.numberOfElements + page.numberOfElements < File.tree.order * 4 - 1 && neighbourRight.numberOfElements > order) {

                // perform shift with right neighbour
                compensateThreePages(page, parent, neighbourRight, pageChildIndex);

                return true;
            }
        }

        int neighbourLeftIndex = pageChildIndex - 1;
        if (neighbourLeftIndex >= 0) {
            Page neighbourLeft = File.readPage(parent.pagePointers[neighbourLeftIndex]);
            if (neighbourLeft.numberOfElements + page.numberOfElements < File.tree.order * 4 - 1 && neighbourLeft.numberOfElements > order) {

                // perform shift with left neighbour
                compensateThreePages(neighbourLeft, parent, page, neighbourLeftIndex);

                return true;
            }
        }

        return false;
    }

    void compensateThreePages(Page left, Page parent, Page right, int parentElementIndex) {
        int totalKeyCount = left.numberOfElements + 1 + right.numberOfElements;

        Index[] mergedKeys = new Index[totalKeyCount];

        if (left.numberOfElements >= 0) System.arraycopy(left.keys, 0, mergedKeys, 0, left.numberOfElements);

        mergedKeys[left.numberOfElements] = parent.keys[parentElementIndex];

        for (int i = 0; i < right.numberOfElements; i++) {
            mergedKeys[i + left.numberOfElements + 1] = right.keys[i];
        }

        Arrays.sort(mergedKeys);

        int leftElements, rightElements;
        if (mergedKeys.length % 2 == 0) {
            leftElements = mergedKeys.length / 2;
            rightElements = mergedKeys.length / 2 - 1;
        }
        else {
            leftElements = mergedKeys.length / 2;
            rightElements = leftElements;
        }
        System.arraycopy(mergedKeys, 0, left.keys, 0, leftElements);
        left.numberOfElements = leftElements;

        parent.keys[parentElementIndex] = mergedKeys[leftElements];

        for (int i = 0; i < rightElements; i++) {
            right.keys[i] = mergedKeys[i + leftElements + 1];
        }
        right.numberOfElements = rightElements;

        File.writePage(left, left.index);
        File.writePage(parent, parent.index);
        File.writePage(right, right.index);
    }

    public void splitChildPage(Page parent, int childIndex) {

        Page child;
        child = File.readPage(parent.pagePointers[childIndex]);

        Page newPage = new Page(order, File.getNextPageIndex());
        newPage.pageDepth = parent.pageDepth + 1;

        newPage.numberOfElements = File.tree.order;
        for (int i = 0; i < File.tree.order; i++) {
            newPage.keys[i] = child.keys[i + File.tree.order + 1];

            // increase page depth of child's children
            if (child.pagePointers[i] >= 0) {
                Page p = File.readPage(child.pagePointers[i]);
                p.pageDepth = child.pageDepth + 1;
                File.writePage(p, child.pagePointers[i]);
            }
        }

        if (!child.isLeaf()) {
            for (int i = 0; i <= File.tree.order; i++) {
                newPage.pagePointers[i] = child.pagePointers[i + File.tree.order + 1];
                if (newPage.pagePointers[i] >= 0) {
                    Page p = File.readPage(newPage.pagePointers[i]);
                    p.parentPagePointer = newPage.index;
                    p.pageDepth = newPage.pageDepth + 1;
                    File.writePage(p, newPage.pagePointers[i]);
                }
            }
        }

        child.numberOfElements = File.tree.order;

        for (int i = parent.numberOfElements; i >= childIndex + 1; i--) {
            parent.pagePointers[i + 1] = parent.pagePointers[i];
        }
        for (int i = parent.numberOfElements - 1; i >= childIndex; i--) {
            parent.keys[i + 1] = parent.keys[i];
        }
        parent.keys[childIndex] = child.keys[order];
        parent.pagePointers[childIndex + 1] = newPage.index;
        parent.numberOfElements += 1;

        child.parentPagePointer = parent.index;
        newPage.parentPagePointer = parent.index;

        File.writePage(parent, parent.index);
        File.writePage(child, child.index);
        File.writePage(newPage, newPage.index);
    }

    public void updateRecord(int key, Record record) {
        int pageIndex, recordIndex;

        Map.Entry<Integer, Integer> r = search(key);
        pageIndex = r.getKey();
        recordIndex = r.getValue();

        if (recordIndex >= 0) {
            System.out.println("update: Record with key " + key + " doesnt exist!");
            return;
        }

        Page pageToUpdate = File.readPage(pageIndex);
        recordIndex = pageToUpdate.searchBisection(key, 0, pageToUpdate.numberOfElements - 1);

        Record initialRecord = File.readRecord(recordIndex);
        
        initialRecord.data = record.data;
        initialRecord.identity = record.identity;

        pageToUpdate.updateRecord(record, recordIndex);
    }

    public void delete(int key) {
        int pageIndex, recordIndex;

        Map.Entry<Integer, Integer> r = search(key);
        pageIndex = r.getKey();
        recordIndex = r.getValue();

        if (recordIndex >= 0) {
            System.out.println("delete: Record doesnt exist!");
            return;
        }

        Page pageToDelete = File.readPage(pageIndex);
        recordIndex = pageToDelete.searchBisection(key, 0, pageToDelete.numberOfElements - 1);

        if (pageToDelete.isLeaf()) {
            deleteFromLeaf(pageToDelete, recordIndex);
        }
        else {
            deleteFromNonLeaf(pageToDelete, recordIndex);
        }

    }

    void deleteFromLeaf(Page page, int keyIndex) {
        page.deleteKey(keyIndex);

        File.readPage(page.index);
        if (page.numberOfElements >= order) {
            return;
        }

        handleUnderflow(page);
    }

    void deleteFromNonLeaf(Page page, int keyIndex) {
        Page replacementLeaf = null;
        boolean leftSubtreeReplacement = false;

        // find page from which to get the record to replace
        if (page.pagePointers[keyIndex] != -1) {
            replacementLeaf = File.readPage(page.pagePointers[keyIndex]);

            while(!replacementLeaf.isLeaf()) {
                // find rightmost valid child
                for (int i = File.tree.order * 2; i >= 0; i--) {
                    if (replacementLeaf.pagePointers[i] != -1) {
                        replacementLeaf = File.readPage(replacementLeaf.pagePointers[i]);
                        break;
                    }
                }
                leftSubtreeReplacement = true;
            }
        }
        if (page.pagePointers[keyIndex + 1] != -1) {
            replacementLeaf = File.readPage(page.pagePointers[keyIndex + 1]);

            while(!replacementLeaf.isLeaf()) {
                // find leftmost valid child
                for (int i = 0; i <= File.tree.order; i++) {
                    if (replacementLeaf.pagePointers[i] != -1) {
                        replacementLeaf = File.readPage(replacementLeaf.pagePointers[i]);
                        break;
                    }
                }
            }
        }

        assert replacementLeaf != null;
        // find record to replace with
        if (leftSubtreeReplacement) {
            page.keys[keyIndex] = replacementLeaf.keys[replacementLeaf.numberOfElements - 1];
            replacementLeaf.deleteKey(replacementLeaf.numberOfElements - 1);
        }
        else {
            page.keys[keyIndex] = replacementLeaf.keys[0];
            replacementLeaf.deleteKey(0);
        }
        File.writePage(replacementLeaf, replacementLeaf.index);
        File.writePage(page, page.index);

        if (page.numberOfElements < order) {
            handleUnderflow(page);
        }
    }

    void handleUnderflow(Page page) {
        // compensation
        if (compensate(page)) {
            return;
        }

        if (page.isRoot) {
            if (page.numberOfElements > 0) {
                return; // root can have an underflow
            }

            // delete root, its only child becomes a new root
            Page child = File.readPage(page.pagePointers[0]);
            child.parentPagePointer = -1;
            child.isRoot = true;
            page.isRoot = false;
            page.pagePointers[0] = -1;
            page.numberOfElements = -1; // mark for deletion during reorganization
            File.tree.root = child;

            child.reduceDepth();

            File.writePage(child, child.index);
            File.writePage(page, page.index);
            return;
        }


        // merge
        Page parent = File.readPage(page.parentPagePointer);
        merge(page, parent, parent.findChild(page.index));
        parent = File.readPage(parent.index);

        if (parent.numberOfElements < order) {
            handleUnderflow(parent);
        }


    }

    public void merge(Page child, Page parent, int childIndex) {
        Page neighbour = null;

        boolean mergeToChild = true; // in a situation when child is the rightmost element, we should merge to the neighbour instead
        if (childIndex == parent.numberOfElements) {
            // take the left neighbour
            neighbour = File.readPage(parent.pagePointers[childIndex - 1]);

            mergeToChild = false;
        }
        else {
            neighbour = File.readPage(parent.pagePointers[childIndex + 1]);
        }

        if (mergeToChild) {
            child.keys[order - 1] = parent.keys[childIndex];
            // remove this key from parent
            for (int i = childIndex; i < parent.numberOfElements - 1; i++) {
                parent.keys[i] = parent.keys[i + 1];
            }
            for (int i = childIndex + 1; i < parent.numberOfElements; i++) {
                parent.pagePointers[i] = parent.pagePointers[i + 1];
            }
            parent.pagePointers[parent.numberOfElements] = -1;
            parent.numberOfElements -= 1;

            // move keys from neighbour to child
            for (int i = 0; i < neighbour.numberOfElements; i++) {
                child.keys[order + i] = neighbour.keys[i];
            }
            for (int i = 0; i <= neighbour.numberOfElements; i++) {
                child.pagePointers[order + i] = neighbour.pagePointers[i];
            }
            child.numberOfElements += neighbour.numberOfElements + 1;
            neighbour.numberOfElements = -1; // mark for deletion later (during tree reorganization)
        }
        else { // merging to neighbour
            neighbour.keys[order] = parent.keys[childIndex - 1];

            // remove this key from parent
            for (int i = childIndex; i < parent.numberOfElements - 1; i++) {
                parent.keys[i] = parent.keys[i + 1];
            }
            for (int i = childIndex + 1; i < parent.numberOfElements; i++) {
                parent.pagePointers[i] = parent.pagePointers[i + 1];
            }
            parent.pagePointers[parent.numberOfElements] = -1;
            parent.numberOfElements -= 1;

            // move keys from child to neighbour
            for (int i = 0; i < child.numberOfElements; i++) {
                neighbour.keys[order + i + 1] = child.keys[i];
            }
            for (int i = 0; i <= child.numberOfElements; i++) {
                neighbour.pagePointers[order + i + 1] = child.pagePointers[i];
            }
            neighbour.numberOfElements += child.numberOfElements + 1;
            child.numberOfElements = -1; // mark for deletion later (during tree reorganization)
        }

        File.writePage(parent, parent.index);
        File.writePage(child, child.index);
        File.writePage(neighbour, neighbour.index);

    }

    public void print() {
        root.print();
    }

    public void checkValidity(){
        int status = root.checkValidity();

        if (status < 0) {
            System.out.println("Error in tree!");
        }
        else {
            System.out.println("tree is correct");
        }

    }

    public int getPageSize() {
        return 24 * order + 36;
    }
    public static int getMetadataSize() {
        return 4 * 3;
    }

    public static int getRecordBlockSize() {
        return 128;
    }
}
