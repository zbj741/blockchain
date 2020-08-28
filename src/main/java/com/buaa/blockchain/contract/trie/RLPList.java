package com.buaa.blockchain.contract.trie;

import java.util.ArrayList;


public class RLPList extends ArrayList<com.buaa.blockchain.contract.trie.RLPElement> implements com.buaa.blockchain.contract.trie.RLPElement {

    byte[] rlpData;

    public void setRLPData(byte[] rlpData) {
        this.rlpData = rlpData;
    }

    public byte[] getRLPData() {
        return rlpData;
    }

    public static void recursivePrint(com.buaa.blockchain.contract.trie.RLPElement element) {

        if (element == null)
            throw new RuntimeException("RLPElement object can't be null");
        if (element instanceof com.buaa.blockchain.contract.trie.RLPList) {

            com.buaa.blockchain.contract.trie.RLPList rlpList = (com.buaa.blockchain.contract.trie.RLPList) element;
            System.out.print("[");
            for (com.buaa.blockchain.contract.trie.RLPElement singleElement : rlpList)
                recursivePrint(singleElement);
            System.out.print("]");
        } else {
            String hex = ByteUtil.toHexString(element.getRLPData());
            System.out.print(hex + ", ");
        }
    }
}
