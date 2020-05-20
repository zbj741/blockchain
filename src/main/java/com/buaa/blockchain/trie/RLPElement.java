package com.buaa.blockchain.trie;

import java.io.Serializable;


public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
