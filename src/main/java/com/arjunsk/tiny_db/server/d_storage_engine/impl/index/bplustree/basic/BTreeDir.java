package com.arjunsk.tiny_db.server.d_storage_engine.impl.index.bplustree.basic;

import com.arjunsk.tiny_db.server.a_frontend.common.domain.clause.D_Constant;
import com.arjunsk.tiny_db.server.d_storage_engine.common.file.BlockId;
import com.arjunsk.tiny_db.server.d_storage_engine.impl.index.bplustree.basic.common.BTPage;
import com.arjunsk.tiny_db.server.d_storage_engine.impl.index.bplustree.basic.common.DirEntry;
import com.arjunsk.tiny_db.server.d_storage_engine.common.transaction.Transaction;
import com.arjunsk.tiny_db.server.b_query_engine.common.catalog.table.TablePhysicalLayout;

/**
 * A B-tree directory block.
 *
 * @author Edward Sciore
 */
public class BTreeDir {
    private Transaction tx;
    private TablePhysicalLayout recordValueLayout;
    private BTPage contents;
    private String filename;

    /**
     * Creates an object to hold the contents of the specified
     * B-tree block.
     *
     * @param blk             a reference to the specified B-tree block
     * @param recordValueLayout the metadata of the B-tree directory file
     * @param tx              the calling transaction
     */
    public BTreeDir(Transaction tx, BlockId blk, TablePhysicalLayout recordValueLayout) {
        this.tx = tx;
        this.recordValueLayout = recordValueLayout;
        contents = new BTPage(tx, blk, recordValueLayout);
        filename = blk.getFileName();
    }

    /**
     * Closes the directory page.
     */
    public void close() {
        contents.close();
    }

    /**
     * Returns the block number of the B-tree leaf block
     * that contains the specified search key.
     *
     * @param searchkey the search key value
     * @return the block number of the leaf block containing that search key
     */
    public int search(D_Constant searchkey) {
        BlockId childblk = findChildBlock(searchkey);
        while (contents.getFlag() > 0) {
            contents.close();
            contents = new BTPage(tx, childblk, recordValueLayout);
            childblk = findChildBlock(searchkey);
        }
        return childblk.getBlockNumber();
    }

    /**
     * Creates a new root block for the B-tree.
     * The new root will have two children:
     * the old root, and the specified block.
     * Since the root must always be in block 0 of the file,
     * the contents of the old root will get transferred to a new block.
     *
     * @param e the directory entry to be added as a child of the new root
     */
    public void makeNewRoot(DirEntry e) {
        D_Constant firstval = contents.getDataVal(0);
        int level = contents.getFlag();
        BlockId newblk = contents.split(0, level); //ie, transfer all the records
        DirEntry oldroot = new DirEntry(firstval, newblk.getBlockNumber());
        insertEntry(oldroot);
        insertEntry(e);
        contents.setFlag(level + 1);
    }

    /**
     * Inserts a new directory entry into the B-tree block.
     * If the block is at level 0, then the entry is inserted there.
     * Otherwise, the entry is inserted into the appropriate
     * child node, and the return value is examined.
     * A non-null return value indicates that the child node
     * split, and so the returned entry is inserted into
     * this block.
     * If this block splits, then the method similarly returns
     * the entry information of the new block to its caller;
     * otherwise, the method returns null.
     *
     * @param e the directory entry to be inserted
     * @return the directory entry of the newly-split block, if one exists; otherwise, null
     */
    public DirEntry insert(DirEntry e) {
        if (contents.getFlag() == 0) return insertEntry(e);
        BlockId childblk = findChildBlock(e.dataVal());
        BTreeDir child = new BTreeDir(tx, childblk, recordValueLayout);
        DirEntry myentry = child.insert(e);
        child.close();
        return (myentry != null) ? insertEntry(myentry) : null;
    }

    private DirEntry insertEntry(DirEntry e) {
        int newslot = 1 + contents.findSlotBefore(e.dataVal());
        contents.insertDir(newslot, e.dataVal(), e.blockNumber());
        if (!contents.isFull()) return null;
        // else page is full, so split it
        int level = contents.getFlag();
        int splitpos = contents.getNumRecs() / 2;
        D_Constant splitval = contents.getDataVal(splitpos);
        BlockId newblk = contents.split(splitpos, level);
        return new DirEntry(splitval, newblk.getBlockNumber());
    }

    private BlockId findChildBlock(D_Constant searchkey) {
        int slot = contents.findSlotBefore(searchkey);
        if (contents.getDataVal(slot + 1).equals(searchkey)) slot++;
        int blknum = contents.getChildNum(slot);
        return new BlockId(filename, blknum);
    }
}
