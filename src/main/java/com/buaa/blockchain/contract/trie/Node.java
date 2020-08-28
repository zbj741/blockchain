package com.buaa.blockchain.contract.trie;
public class Node {

    /* RLP encoded value of the Trie-node */
    private final Values value;
    private boolean dirty;

    public Node(Values val) {
        this(val, false);
    }

    public Node(Values val, boolean dirty) {
        this.value = val;
        this.dirty = dirty;
    }

    public Node copy() {
        return new Node(this.value, this.dirty);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Values getValue() {
        return value;
    }
}
