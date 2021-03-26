package com.buaa.blockchain.vm.spec;

import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.vm.GasTable;
import com.buaa.blockchain.vm.OpCode;
import com.buaa.blockchain.vm.program.exception.OutOfGasException;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/15
 * @since JDK1.8
 */
public class BaseSpec implements Spec{

        private static final GasTable feeSchedule = new GasTable();
        private static final PrecompiledContracts precompiledContracts = new BasePrecompiledContracts();

        public GasTable getGasTable() {
                return feeSchedule;
        }

        @Override
        public PrecompiledContracts getPrecompiledContracts() {
                return precompiledContracts;
        }

        @Override
        public long getCallGas(OpCode op, long requestedGas, long availableGas) throws OutOfGasException {
                return availableGas;
        }

        @Override
        public long getCreateGas(long availableGas) {
                return availableGas;
        }

        @Override
        public long getTransactionCost(Transaction tx) {
                GasTable fs = getGasTable();

                long cost = tx.isCreate() ? fs.getTRANSACTION_CREATE_CONTRACT() : fs.getTRANSACTION();
                for (byte b : tx.getData()) {
                cost += (b == 0) ? fs.getTX_ZERO_DATA() : fs.getTX_NO_ZERO_DATA();
                }

                return cost;
        }

        @Override
        public int maxContractSize() {
                return Integer.MAX_VALUE;
        }

        @Override
        public boolean createEmptyContractOnOOG() {
                return false;
        }


        public byte[] getData() {
                return new byte[32];
        }

        @Override
        public boolean eip1052() {
                return false;
        }

        @Override
        public boolean eip145() {
                return false;
        }

        @Override
        public boolean eip1283() {
                return false;
        }

        @Override
        public boolean eip1014() {
                return false;
        }
}
