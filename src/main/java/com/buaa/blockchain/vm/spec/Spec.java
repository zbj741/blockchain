package com.buaa.blockchain.vm.spec;

import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.vm.GasTable;
import com.buaa.blockchain.vm.OpCode;
import com.buaa.blockchain.vm.program.exception.OutOfGasException;


public interface Spec {

    public GasTable getGasTable();

    /**
     * 返回预编译合约集合列表
     *
     * @return
     */
    PrecompiledContracts getPrecompiledContracts();

    /**
     * Returns the gas limit for an internal CALL.
     *
     * @param op
     *            the call opcode, e.g. CALL, CALLCODE and DELEGATECALL
     * @param requestedGas
     *            the requested gas
     * @param availableGas
     *            the available gas
     * @return
     * @throws OutOfGasException
     */
    long getCallGas(OpCode op, long requestedGas, long availableGas) throws OutOfGasException;

    /**
     * Returns the gas limit for an internal CREATE.
     *
     * @param availableGas
     *            the available gas
     * @return
     */
    long getCreateGas(long availableGas);

    /**
     * Returns the basic transaction cost.
     *
     * @param tx
     *            a transaction
     * @return
     */
    long getTransactionCost(Transaction tx);

    /**
     * Returns the max size of a contract.
     *
     * @return
     */
    int maxContractSize();

    /**
     * Whether to create an empty contract or not when running out of gas.
     *
     * @return
     */
    boolean createEmptyContractOnOOG();

    /**
     * EIP1052: https://eips.ethereum.org/EIPS/eip-1052 EXTCODEHASH opcode
     */
    boolean eip1052();

    /**
     * EIP145: https://eips.ethereum.org/EIPS/eip-145 Bitwise shifting instructions
     * in EVM
     */
    boolean eip145();

    /**
     * EIP 1283: https://eips.ethereum.org/EIPS/eip-1283 Net gas metering for SSTORE
     * without dirty maps
     */
    boolean eip1283();

    /**
     * EIP 1014: https://eips.ethereum.org/EIPS/eip-1014 Skinny CREATE2: same as
     * CREATE but with deterministic address
     */
    boolean eip1014();
}
