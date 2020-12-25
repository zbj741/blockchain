package com.buaa.blockchain.vm.spec;

import com.buaa.blockchain.vm.GasTable;
import com.buaa.blockchain.vm.OpCode;
import com.buaa.blockchain.vm.program.exception.OutOfGasException;

public class DefaultSpec extends BaseSpec{

    private static class DefaultGasTable extends GasTable {
        public int getBALANCE() {
            return 400;
        }

        public int getEXT_CODE_SIZE() {
            return 700;
        }

        public int getEXT_CODE_COPY() {
            return 700;
        }

        public int getSLOAD() {
            return 200;
        }

        public int getCALL() {
            return 700;
        }

        public int getSUICIDE() {
            return 5000;
        }

        public int getNEW_ACCT_SUICIDE() {
            return 25000;
        }

        public int getEXP_BYTE_GAS() {
            return 50;
        }
    }

    private static final GasTable feeSchedule = new DefaultGasTable();
    private static final PrecompiledContracts precompiledContracts = new DefaultPrecompiledContracts();

    public DefaultSpec() {
    }

    @Override
    public GasTable getGasTable() {
        return feeSchedule;
    }

    @Override
    public PrecompiledContracts getPrecompiledContracts() {
        return precompiledContracts;
    }

    private static long maxAllowed(long available) {
        return available - available / 64;
    }

    @Override
    public long getCallGas(OpCode op, long requestedGas, long availableGas) throws OutOfGasException {
        long maxAllowed = maxAllowed(availableGas);
        return requestedGas > maxAllowed ? maxAllowed : requestedGas;
    }

    @Override
    public long getCreateGas(long availableGas) {
        return maxAllowed(availableGas);
    }

    @Override
    public int maxContractSize() {
        return 0x6000;
    }
}
