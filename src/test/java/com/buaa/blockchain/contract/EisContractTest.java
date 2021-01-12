package com.buaa.blockchain.contract;

import com.buaa.blockchain.contract.component.ContractException;
import com.buaa.blockchain.utils.ReflectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EisContract Tester.
 *
 * @author nolan.zhang
 * @version 1.0
 * @since <pre>Jan 7, 2021</pre>
 */
public class EisContractTest {
    private Map storage = new HashMap();
    private List logList = new ArrayList();
    private Class classZ;
    private Object contract;

    @Before
    public void before() throws Exception {
//        this.classZ = EisContract.class;
        this.contract = ReflectUtil.getInstance().newInstance(this.classZ, Map.class,  storage);

        Field f1 = classZ.getSuperclass().getDeclaredField("LOGS");
        f1.setAccessible(true);
        f1.set(this.contract, this.logList);
    }

    /**
     * Method: deposit(Long userId, BigDecimal amount)
     */
    @Test
    public void testDeposit() throws Exception {
        invoke("deposit", new Object[]{
            1l, BigDecimal.valueOf(1000l)
        });

        String topic = getTopic(0);
        Assert.assertEquals("DEPOSIT_EVENT", topic);
        Assert.assertEquals(1, logList.size());

        printAll();
    }



    /**
     * Method: createPayOrder(Long userId, Long entId, BigDecimal salaryBase)
     */
    @Test
    public void testCreatePayOrder() throws Exception {
        Long expectUserId = 1l;
        Long expectEntId = 2l;
        BigDecimal expectSalaryBase = BigDecimal.valueOf(5000l);
        invoke("createPayOrder", new Object[]{
                expectUserId, expectEntId, expectSalaryBase
        });

        final Long entId = getParam(0, 4, Long.class);
        final Long userId = getParam(0, 6, Long.class);
        final BigDecimal salaryBase = getParam(0, 2, BigDecimal.class);
        Assert.assertEquals(expectEntId, entId);
        Assert.assertEquals(expectUserId, userId);
        Assert.assertEquals(expectSalaryBase, salaryBase);
    }

    /**
     * Method: payOrderByUser(Long userId, String period)
     */
    @Test
    public void testPayOrderByUser() throws Exception {
        Long expectUserId = 1l;
        Long expectEntId = 2l;
        String expectPeriod = "202101";
        BigDecimal expectSalaryBase = BigDecimal.valueOf(5000l);
        try {
            invoke("payOrderByUser", new Object[]{
                    expectUserId, expectPeriod
            });
        } catch (Exception exception) {
            Assert.assertEquals("当期缴存暂未生成", exception.getMessage());
        }

        invoke("createPayOrder", new Object[]{
                expectUserId, expectEntId, expectSalaryBase
        });
        invoke("payOrderByUser", new Object[]{
                expectUserId, expectPeriod
        });
        Assert.assertEquals(expectUserId, getParam(3, 1, Long.class));
        Assert.assertEquals(expectSalaryBase.multiply(BigDecimal.valueOf(0.08)), getParam(3, 2, BigDecimal.class));
    }

    /**
     * Method: payOrderByEnt(Long entId, Long userId, String period)
     */
    @Test
    public void testPayOrderByEnt() throws Exception {
        Long expectUserId = 1l;
        Long expectEntId = 2l;
        String expectPeriod = "202101";
        BigDecimal expectSalaryBase = BigDecimal.valueOf(5000l);
        try {
            invoke("payOrderByEnt", new Object[]{
                    expectEntId, expectUserId, expectPeriod
            });
        } catch (Exception exception) {
            Assert.assertEquals("当期缴存暂未生成", exception.getMessage());
        }

        invoke("createPayOrder", new Object[]{
                expectUserId, expectEntId, expectSalaryBase
        });
        invoke("payOrderByEnt", new Object[]{
                expectEntId, expectUserId, expectPeriod
        });
        Assert.assertEquals(expectEntId, getParam(3, 1, Long.class));
        Assert.assertEquals(expectSalaryBase.multiply(BigDecimal.valueOf(0.14)), getParam(3, 2, BigDecimal.class));
    }

    /**
     * Method: createIssuePlan(Long userId, String beginWork)
     */
    @Test(expected = ContractException.class)
    public void testCreateIssuePlanThrowException() {
        String expectBeginDate = "2020-01-01";
        Long expectUserId = 1l;
        try {
            invoke("createIssuePlan", new Object[]{
                    expectUserId, expectBeginDate
            });
        } catch (Exception exception) {
            Assert.assertEquals("年龄不符合退休条件", exception.getMessage());
        }

        invoke("createIssuePlan", new Object[]{
                expectUserId, expectBeginDate
        });
    }

    @Test
    public void testCreateIssuePlan() throws Exception {
        String expectBeginDate = "1981-01-01";
        Long expectUserId = 1l;
        invoke("createIssuePlan", new Object[]{
                expectUserId, expectBeginDate
        });
        Assert.assertEquals(expectUserId, getParam(0, 0, Long.class));
        Assert.assertEquals(Integer.valueOf(40), getParam(0, 1, Integer.class));
        Assert.assertEquals(Integer.valueOf(233), getParam(0, 2, Integer.class));
    }

    @Test(expected = ContractException.class)
    public void testIssueException() {
        Long expectUserId = 1l;
        BigDecimal lastYearAvgVal = BigDecimal.valueOf(1000l);
        BigDecimal userIndex = BigDecimal.valueOf(1.5);
        invoke("issue", new Object[]{
                expectUserId, lastYearAvgVal, userIndex
        });
    }
    /**
     * Method: issue(Long userId, BigDecimal lastYearAvgVal, BigDecimal userIndex)
     */
    @Test
    public void testIssue() throws Exception {
        Long expectUserId = 1l;
        Long expectEntId = 2l;
        String period = "202101";

        invoke("deposit", new Object[]{
                expectEntId, BigDecimal.valueOf(10000l)
        });
        invoke("deposit", new Object[]{
                expectUserId, BigDecimal.valueOf(10000l)
        });

        invoke("createPayOrder", new Object[]{
                expectUserId, expectEntId, BigDecimal.valueOf(3000l)
        });
        invoke("payOrderByUser", new Object[]{
                expectUserId, period
        });
        invoke("payOrderByEnt", new Object[]{
                expectEntId, expectUserId, period
        });


        invoke("createIssuePlan", new Object[]{
                expectUserId, "1980-01-01"
        });

        BigDecimal lastYearAvgVal = BigDecimal.valueOf(1000l);
        BigDecimal userIndex = BigDecimal.valueOf(1.5);
        invoke("issue", new Object[]{
                expectUserId, lastYearAvgVal, userIndex
        });

        Assert.assertEquals(expectUserId, getParam(12, 0, Long.class));
        Assert.assertEquals(BigDecimal.valueOf(205.3075), getParam(12, 3, BigDecimal.class));
        Assert.assertEquals(BigDecimal.valueOf(1.0435), getParam(12, 4, BigDecimal.class));
    }

    @Test
    public void testCastVar() throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
        Long expectUserId = 1l;
        Long expectEntId = 2l;
        String period = "202101";

        invoke("deposit", new Object[]{
                expectEntId, BigDecimal.valueOf(10000l)
        });
        invoke("deposit", new Object[]{
                expectUserId, BigDecimal.valueOf(10000l)
        });
        invoke("createPayOrder", new Object[]{
                expectUserId, expectEntId, BigDecimal.valueOf(3000l)
        });
        String str = new ObjectMapper().writeValueAsString(this.storage);
        this.storage = new ObjectMapper().readValue(str, Map.class);

        this.contract = ReflectUtil.getInstance().newInstance(this.classZ, Map.class,  storage);
        Field f1 = classZ.getSuperclass().getDeclaredField("LOGS");
        f1.setAccessible(true);
        f1.set(this.contract, this.logList);


        invoke("payOrderByUser", new Object[]{
                expectUserId, period
        });
        System.out.println(this.storage);
    }

    private void invoke(String  method, Object... params){
        try {
            ReflectUtil.getInstance().invoke(classZ, this.contract, method, params);
        }  catch (Exception e){
            if(e.getCause().getClass().isAssignableFrom(ContractException.class)){
                throw new ContractException(e.getCause().getMessage());
            }
            e.printStackTrace();
        }
    }

    private String getTopic(int logIndex){
        String str = (String) this.logList.get(logIndex);
        return str.split(":")[0];
    }

    private <T> T getParam(int logIndex, int paramIndex, Class classType){
        String str = (String) this.logList.get(logIndex);
        String params = str.split(":")[1];
        Object rtnObj = params.split(",")[paramIndex];
        if(classType.isAssignableFrom(String.class)){
            return (T)rtnObj;
        }else if(classType.isAssignableFrom(BigDecimal.class)){
            return (T) new BigDecimal(String.valueOf(rtnObj));
        }else if(classType.isAssignableFrom(Integer.class)) {
            return (T) new Integer(String.valueOf(rtnObj));
        }else if(classType.isAssignableFrom(Long.class)){
            return (T) new Long(String.valueOf(rtnObj));
        }else if(classType.isAssignableFrom(Boolean.class)){
            return (T) Boolean.valueOf(String.valueOf(rtnObj));
        }
        return (T)rtnObj;
    }

    private void printAll(){
        printLogs();
        printStorage();
    }

    private void printLogs(){
        print(this.logList);
    }

    private void printStorage(){
        print(this.storage);
    }

    private void print(Object data){
        try {
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


}
