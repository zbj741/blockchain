package com.buaa.blockchain.utils;

import com.buaa.blockchain.contract.component.ContractException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/12
 * @since JDK1.8
 */
public class SerialTestCase {

    @Test
    public void testSerialize() throws JsonProcessingException {
        ObjectMapper obm = new ObjectMapper();

        Map rtnMap = new HashMap();
        rtnMap.put("1", new IssuePlan(12, 12, BigDecimal.valueOf(200l)));
        rtnMap.put("2", new IssuePlan(12, 12, BigDecimal.valueOf(200l)));

        String str = obm.writeValueAsString(rtnMap);
        System.out.println(str);

        Map obj = obm.readValue(str, Map.class);

        Map<Long, IssuePlan> issuePlanMap = castStorage(obj, new TypeReference<Map<Long, IssuePlan>>() {});
        System.out.println(issuePlanMap.get(1l).getIssueMonthNum());
        System.out.println(issuePlanMap.get(1l).getWorkYears());
        System.out.println(issuePlanMap.get(1l).getIssueMonthNum());
    }

    private Map castStorage(Object data, TypeReference typeReference){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(data);
            return (Map) objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new ContractException("加载合约数据出错");
        }
    }

    private class IssuePlan {
        private Integer workYears;
        private Integer issueMonthNum;
        private BigDecimal monthAmount;

        public IssuePlan() {
        }

        public IssuePlan(Integer workYears, Integer issueMonthNum, BigDecimal monthAmount) {
            this.workYears = workYears;
            this.issueMonthNum = issueMonthNum;
            this.monthAmount = monthAmount;
        }

        public Integer getWorkYears() {
            return workYears;
        }

        public BigDecimal getMonthAmount() {
            return monthAmount;
        }

        public Integer getIssueMonthNum() {
            return issueMonthNum;
        }
    }

    private class IssueOrder {
        private Long timestamp;
        private BigDecimal basicAmount;
        private BigDecimal userAmount;

        public IssueOrder(Long timestamp, BigDecimal basicAmount, BigDecimal userAmount) {
            this.timestamp = timestamp;
            this.basicAmount = basicAmount;
            this.userAmount = userAmount;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public BigDecimal getBasicAmount() {
            return basicAmount;
        }

        public BigDecimal getUserAmount() {
            return userAmount;
        }
    }
}
