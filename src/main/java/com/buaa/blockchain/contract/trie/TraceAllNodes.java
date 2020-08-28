package com.buaa.blockchain.contract.trie;

import org.spongycastle.util.encoders.Hex;


public class TraceAllNodes implements TrieImpl.ScanAction {

    StringBuilder output = new StringBuilder();

    @Override
    public void doOnNode(byte[] hash, Values node) {

        output.append(Hex.toHexString(hash)).append(" ==> ").append(node.toString()).append("\n");
    }

    public String getOutput() {
        return output.toString();
    }
}
