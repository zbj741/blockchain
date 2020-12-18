package com.buaa.blockchain.vm;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/16
 * @since JDK1.8
 */
public class MessageCall {

    /**
     * Type of internal call. Either CALL, CALLCODE or POST
     */
    private final OpCode type;

    /**
     * gas to pay for the call, remaining gas will be refunded to the caller
     */
    private final long gas;

    /**
     * address of account which code to call
     */
    private final DataWord codeAddress;

    /**
     * the value that can be transfer along with the code execution
     */
    private final DataWord endowment;

    /**
     * start of memory to be input data to the call
     */
    private final DataWord inDataOffs;

    /**
     * size of memory to be input data to the call
     */
    private final DataWord inDataSize;

    /**
     * start of memory to be output of the call
     */
    private DataWord outDataOffs;

    /**
     * size of memory to be output data to the call
     */
    private DataWord outDataSize;

    public MessageCall(OpCode type, long gas, DataWord codeAddress,
                       DataWord endowment, DataWord inDataOffs, DataWord inDataSize,
                       DataWord outDataOffs, DataWord outDataSize) {
        this.type = type;
        this.gas = gas;
        this.codeAddress = codeAddress;
        this.endowment = endowment;
        this.inDataOffs = inDataOffs;
        this.inDataSize = inDataSize;
        this.outDataOffs = outDataOffs;
        this.outDataSize = outDataSize;
    }

    public OpCode getType() {
        return type;
    }

    public long getGas() {
        return gas;
    }

    public DataWord getCodeAddress() {
        return codeAddress;
    }

    public DataWord getEndowment() {
        return endowment;
    }

    public DataWord getInDataOffs() {
        return inDataOffs;
    }

    public DataWord getInDataSize() {
        return inDataSize;
    }

    public DataWord getOutDataOffs() {
        return outDataOffs;
    }

    public DataWord getOutDataSize() {
        return outDataSize;
    }
}
