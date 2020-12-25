package com.buaa.blockchain.vm.spec;

import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.vm.client.Repository;
import com.buaa.blockchain.vm.program.ProgramResult;

public interface PrecompiledContractContext {

    /**
     * Returns the repository track.
     *
     * @return the current repository track
     */
    Repository getTrack();

    /**
     * Returns the program result.
     *
     * @return the current program result
     */
    ProgramResult getResult();

    /**
     * Returns the internal transaction.
     *
     * @return an internal transaction to the precompiled contract
     */
    Transaction getTransaction();
}
