package dev.silentsky.disk;

import dev.silentsky.btree.BTree;
import dev.silentsky.btree.Page;
import dev.silentsky.btree.Record;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Vector;


@Getter
@Setter
public class File {
    String path;
    String dataPath;

    public static BTree tree;

    static int pageIndex = -1;
    static int recordIndex = -1;

    private static RandomAccessFile indexFile;
    private static RandomAccessFile dataFile;

    private static Vector<Page> pageBuffer;

    public File(int order, String path, String dataPath, boolean initNewFile, int pageIndexLimit) {
        java.io.File treeFile = new java.io.File(path);
        this.path = path;

        try {
            indexFile = new RandomAccessFile(treeFile, "rw");
            if (initNewFile) {
                indexFile.setLength(0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        java.io.File data = new java.io.File(dataPath);
        this.dataPath = dataPath;
        try {
            dataFile = new RandomAccessFile(data, "rw");
            if (initNewFile) {
                dataFile.setLength(0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Record.limit = pageIndexLimit;

        pageBuffer = new Vector<>();
        pageBuffer.setSize(1);

        tree = new BTree(order);
        File.writePage(tree.getRoot(), tree.getRoot().index);
        File.writeMetadata();
    }

    public static void writePage(Page page, int index) {

        if (page.pageDepth >= pageBuffer.size()) {
            pageBuffer.setSize(page.pageDepth + 1);
        }
        pageBuffer.set(page.pageDepth, page);

        int address = index * tree.getPageSize() + BTree.getMetadataSize();

        byte[] pageBytes = new byte[tree.getPageSize()];
        ByteBuffer buffer = ByteBuffer.wrap(pageBytes);

        buffer.putInt(page.numberOfElements);
        buffer.putInt(page.parentPagePointer);
        if (page.isRoot) {
            buffer.putInt(1);
        }
        else {
            buffer.putInt(0);
        }
        buffer.putInt(page.pageDepth);

        buffer.putInt(page.pagePointers[0]);
        for (int i = 0; i < page.numberOfElements; i++) {
            buffer.putInt(page.keys[i].key);
            buffer.putInt(page.keys[i].dataPointer);
            buffer.putInt(page.pagePointers[i + 1]);
        }

        try {
            indexFile.seek(address);
            indexFile.write(pageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Page readPage(int index) {

        Optional<Page> bufferedPage = pageBuffer.stream().filter(page -> page.index == index).findAny();
        if (bufferedPage.isPresent()) {
            tree.setCurrentPage(bufferedPage.get());
            return bufferedPage.get();
        }

        int address = index * tree.getPageSize() + BTree.getMetadataSize();

        byte[] pageBytes = new byte[tree.getPageSize()];
        Page page = new Page(tree.getOrder(), index);

        try {
            indexFile.seek(address);
            indexFile.read(pageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer buffer = ByteBuffer.wrap(pageBytes);

        page.index = index;

        page.numberOfElements = buffer.getInt();
        page.parentPagePointer = buffer.getInt();
        int isRoot = buffer.getInt();
        page.isRoot = isRoot != 0;
        page.pageDepth = buffer.getInt();

        page.pagePointers[0] = buffer.getInt();
        for (int i = 0; i < page.numberOfElements; i++) {
            page.keys[i].key = buffer.getInt();
            page.keys[i].dataPointer = buffer.getInt();
            page.pagePointers[i + 1] = buffer.getInt();
        }

        if (page.pageDepth >= pageBuffer.size()) {
            pageBuffer.setSize(page.pageDepth + 1);
        }
        pageBuffer.set(page.pageDepth, page);


        tree.setCurrentPage(page);

        return page;
    }

    public static void writeMetadata(){

        byte[] metadataBytes = new byte[BTree.getMetadataSize()];
        ByteBuffer buffer = ByteBuffer.wrap(metadataBytes);

        tree.setRoot(File.readPage(tree.getRoot().index)); // update root

        buffer.putInt(tree.getOrder());
        buffer.putInt(tree.getHeight());
        buffer.putInt(tree.getRoot().index);

        try {
            indexFile.seek(0);
            indexFile.write(metadataBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readMetadata() {
        byte[] metadataBytes = new byte[BTree.getMetadataSize()];
        ByteBuffer buffer = ByteBuffer.wrap(metadataBytes);

        try {
            indexFile.seek(0);
            indexFile.read(metadataBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tree.setOrder(buffer.getInt());
        tree.setHeight(buffer.getInt());

        Page readRoot = readPage(buffer.getInt());
        tree.setRoot(readRoot);
    }

    public static int writeRecord(Page page, Record record) {
        int recordIndex = getNextRecordIndex();

        byte[] recordBytes = record.toByteArray();

        try {
            dataFile.seek((long) recordIndex * BTree.getRecordBlockSize());
            dataFile.write(recordBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return recordIndex;
    }

    public static void writeRecord(Record record, int recordIndex) {
        byte[] recordBytes = record.toByteArray();

        try {
            dataFile.seek((long) recordIndex * BTree.getRecordBlockSize());
            dataFile.write(recordBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Record readRecord(int recordIndex) {
        byte[] recordBytes = new byte[BTree.getRecordBlockSize()];
        try {
            dataFile.seek(recordIndex);
            dataFile.read(recordBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Record(recordBytes);
    }

    public static int getNextPageIndex() {
        pageIndex += 1;

        System.out.println("New page of index " + pageIndex);

        return pageIndex;
    }

    public static int getNextRecordIndex() {
        recordIndex += 1;
        return recordIndex;
    }
}
