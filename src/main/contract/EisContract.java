import com.buaa.blockchain.contract.component.ContractException;
import com.buaa.blockchain.contract.component.IContract;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/6
 * @since JDK1.8
 */
public class EisContract extends IContract {
    private final Long LIMIT_PAY_NUM = 15l * 12l; // 15年月份总数

    private final Long NATION_USER_ID = -1l;

    private final BigDecimal USER_PAY_RATIO = BigDecimal.valueOf(0.08);
    private final BigDecimal ENT_PAY_RATIO = BigDecimal.valueOf(0.14);
    private final BigDecimal TOTAL_PAY_RATIO = USER_PAY_RATIO.add(ENT_PAY_RATIO);

    // 城镇企业职工基本养老保险个人账户养老金计发月数表 (AGE: 40-70年龄区间)
    private static final int[] ISSUE_AGE_MAP = {
            233, 230, 226, 223, 220, 216, 212, 208, 204, 199,
            195, 190, 185, 180, 175, 170, 164, 158, 152, 145,
            60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
            70
    };
    private static final String BALANCE_TYPE = "BALANCE_TYPE";
    private static final String PAY_ORDER_TYPE = "PAY_ORDER_TYPE";
    private static final String PAY_ORDER_MAP_USER_TYPE = "PAY_ORDER_MAP_USER_TYPE";
    private static final String ISSUE_PLAN_TYPE = "ISSUE_PLAN_TYPE";
    private static final String ISSUE_ORDER_TYPE = "ISSUE_ORDER_TYPE";
    private static final String NEXT_ID_TYPE = "NEXT_ID_TYPE";
    private static final String PAY_MONTH_NUM_TYPE = "PAY_MONTH_NUM_TYPE";

    private Map<Long, Balance> balanceRepo;
    private Map<Long, PayOrder> payOrderRepo;   // 1:<缴存明细>; 2:缴存明细; 3:缴存明细
    private Map<String, Long> payOrderUserRepo;   // 示例: 202101-1:1; 202002:1:77. e.g <用户编码>:<PayOrderId>
    private Map<Long, Long> payMonthNumRepo;    // 示例: 1:1; 2:77. e.g <用户编码>:<缴费次数>
    private Map<Long, IssuePlan> issuePlanRepo; // 示例: <UserId>:<退休计划>
    private Map<String, IssueOrder> issueHistoryRepo;  // 示例: 202101-1:<发放明细> e.g <发放期数-用户编码>:<发放明细>
    private Map<String, Long> keyGenerateMap;


    public EisContract(Map storage) {
        if (storage.get(BALANCE_TYPE) == null) {
            balanceRepo = new HashMap<>();
        } else {
            balanceRepo = castStorage(storage.get(BALANCE_TYPE), Long.class, Balance.class);
        }
        storage.put(BALANCE_TYPE, balanceRepo);

        if (storage.get(PAY_ORDER_TYPE) == null) {
            payOrderRepo = new HashMap<>();
        } else {
            payOrderRepo = castStorage(storage.get(PAY_ORDER_TYPE), Long.class, PayOrder.class);
        }
        storage.put(PAY_ORDER_TYPE, payOrderRepo);

        if (storage.get(PAY_ORDER_MAP_USER_TYPE) == null) {
            payOrderUserRepo = new HashMap<>();
        } else {
            payOrderUserRepo = castStorage(storage.get(PAY_ORDER_MAP_USER_TYPE), String.class, Long.class);
        }
        storage.put(PAY_ORDER_MAP_USER_TYPE, payOrderUserRepo);

        if (storage.get(ISSUE_PLAN_TYPE) == null) {
            issuePlanRepo = new HashMap<Long, IssuePlan>();
        } else {
            issuePlanRepo = castStorage(storage.get(ISSUE_PLAN_TYPE), Long.class, IssuePlan.class);
        }
        storage.put(ISSUE_PLAN_TYPE, issuePlanRepo);

        if (storage.get(ISSUE_ORDER_TYPE) == null) {
            issueHistoryRepo = new HashMap<String, IssueOrder>();
        } else {
            issueHistoryRepo = castStorage(storage.get(ISSUE_ORDER_TYPE), String.class, IssueOrder.class);
        }
        storage.put(ISSUE_ORDER_TYPE, issueHistoryRepo);

        if (storage.get(NEXT_ID_TYPE) == null) {
            keyGenerateMap = new HashMap<String, Long>();
        } else {
            keyGenerateMap = castStorage(storage.get(NEXT_ID_TYPE), String.class, Long.class);
        }
        storage.put(NEXT_ID_TYPE, keyGenerateMap);


        if(storage.get(PAY_MONTH_NUM_TYPE) == null){
            payMonthNumRepo = new HashMap<Long, Long>();
        }else{
            payMonthNumRepo = castStorage(storage.get(PAY_MONTH_NUM_TYPE), Long.class, Long.class);
        }
        storage.put(PAY_MONTH_NUM_TYPE, payMonthNumRepo);
    }

    /**
     * 充值金额
     *
     * @param amount
     */
    public void deposit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ONE) <= 0)
            throw new ContractException("充值金额应大于0");

        addBalance(userId, amount);
    }

    /**
     * 生成每月待缴明细
     */
    public void createPayOrder(Long userId, Long entId, BigDecimal salaryBase, String period) {
        checkArgument(salaryBase);
        checkAlreadyGenPayOrder(userId, period);

        final Long payOrderId = nextPayOrderNextId();
        PayOrder payOrder = new PayOrder();
        payOrder.setPeriod(period);
        payOrder.setSalaryBase(salaryBase);
        payOrder.setTimestamp(new Date().getTime());
        payOrder.setEntId(entId);
        payOrder.setEntAmount(salaryBase.multiply(ENT_PAY_RATIO));
        payOrder.setEntStatus(false);
        payOrder.setUserId(userId);
        payOrder.setUserAmount(salaryBase.multiply(USER_PAY_RATIO));
        payOrder.setUserStatus(false);
        payOrder.setPayMonthNum(1l);
        payOrderRepo.put(payOrderId, payOrder);

        LOG("CREATE_PAY_ORDER_EVENT",
                payOrderId,
                period,
                payOrder.getSalaryBase(),
                payOrder.getTimestamp(),
                payOrder.getEntId(),
                payOrder.getEntAmount(),
                payOrder.getUserId(),
                payOrder.getUserAmount(),
                payOrder.getPayMonthNum()
        );
    }

    public void createSupplementPayOrder(Long userId, BigDecimal salaryBase, String period, Long payMonthNum) {
        checkArgument(salaryBase, payMonthNum, userId);
        checkAlreadyGenPayOrder(userId, period);

        BigDecimal userNeedAmount = salaryBase.multiply(BigDecimal.valueOf(payMonthNum)).multiply(TOTAL_PAY_RATIO);

        final Long payOrderId = nextPayOrderNextId();
        PayOrder payOrder = new PayOrder();
        payOrder.setPeriod(period);
        payOrder.setSalaryBase(salaryBase);
        payOrder.setTimestamp(new Date().getTime());
        payOrder.setEntAmount(BigDecimal.ZERO);
        payOrder.setEntStatus(true);
        payOrder.setUserId(userId);
        payOrder.setUserAmount(userNeedAmount);
        payOrder.setUserStatus(false);
        payOrder.setPayMonthNum(payMonthNum);
        payOrderRepo.put(payOrderId, payOrder);
        payOrderUserRepo.put(createPayOrderUserKey(period, userId), payOrderId);

        LOG("CREATE_PAY_ORDER_EVENT",
                payOrderId,
                period,
                payOrder.getSalaryBase(),
                payOrder.getTimestamp(),
                payOrder.getEntId(),
                payOrder.getEntAmount(),
                payOrder.getUserId(),
                payOrder.getUserAmount(),
                payOrder.getPayMonthNum()
        );
    }

    private void checkAlreadyGenPayOrder(Long userId, String period) {
        if (period == null || period.trim().length() != 6) {
            throw new ContractException("period格式不正确。示例: 202011");
        }
        if (payOrderUserRepo.containsKey(createPayOrderUserKey(period, userId))) {
            throw new ContractException("本期(" + period + ")待缴明细已生成");
        }
    }

    private void checkArgument(BigDecimal salaryBase, Long payMonthNum, Long userId) {
        checkArgument(salaryBase);
        if (payMonthNum == null || payMonthNum <= 0l) {
            throw new ContractException("参数格式(payMonthNum)不正确.");
        }
        if (userId == null) {
            throw new ContractException("参数格式(userId)不正确");
        }
    }

    private void checkArgument(BigDecimal salaryBase) {
        if (salaryBase == null || salaryBase.compareTo(BigDecimal.ONE) <= 0) {
            throw new ContractException("缴费基数应大于0");
        }
    }

    /**
     * 养老金缴存(个人)
     */
    public void payOrderByUser(Long payOrderId, Long userId) {
        PayOrder payOrder = payOrderRepo.get(payOrderId);
        if (payOrder == null) {
            throw new ContractException("缴存单不存在");
        }
        if (!payOrder.getUserId().equals(userId)) {
            throw new ContractException("缴存单与缴费用户不一致");
        }
        Balance balance = getBalance(userId);
        if (balance.getAmount().compareTo(payOrder.getUserAmount()) == -1) {
            throw new ContractException("帐户余额不足");
        }

        // 1. 总扣费金额
        subBalance(userId, payOrder.getUserAmount());

        // 2. 冻结金额（统筹帐户及个人帐户）
        Long payNum = payMonthNumRepo.getOrDefault(userId, 0l);
        if (payOrder.getEntId() == null) { // 补缴
            // 2.1 更新个人冻结帐户
            final BigDecimal userNeedPay = payOrder.getUserAmount().multiply(USER_PAY_RATIO).divide(TOTAL_PAY_RATIO, 4, BigDecimal.ROUND_HALF_EVEN);
            freezeBalance(userId, userNeedPay);
            // 2.2 更新统筹冻结帐户
            final BigDecimal nationNeedPay = payOrder.getUserAmount().multiply(ENT_PAY_RATIO).divide(TOTAL_PAY_RATIO, 4, BigDecimal.ROUND_HALF_EVEN);
            freezeNationBalance(nationNeedPay);
            // 2.3 更新缴费次数
            payMonthNumRepo.put(userId, payNum + payOrder.getPayMonthNum());
        } else {
            // 2.1 冻结个人缴费帐户金额
            freezeBalance(userId, payOrder.getUserAmount());
            // 2.2 更新缴费次数
            if(payOrder.getEntStatus()){
                payMonthNumRepo.put(userId, payNum + payOrder.getPayMonthNum());
            }
        }

        // 3. 更新缴费状态
        payOrder.setUserStatus(true);
        payOrderRepo.put(payOrderId, payOrder);

        // 4. 记录支付日志
        LOG("PAY_ORDER_USER_EVENT",
                payOrderId,
                userId,
                payOrder.getUserAmount(),
                new Date().getTime()
        );
    }

    /**
     * 养老金缴存(企业)
     */
    public void payOrderByEnt(Long payOrderId, Long entId, Long userId) {
        PayOrder payOrder = payOrderRepo.get(payOrderId);
        if (payOrder == null) {
            throw new ContractException("缴存单不存在");
        }
        if (!payOrder.getEntId().equals(entId) || !payOrder.getUserId().equals(userId)) {
            throw new ContractException("公司及员工Id与PayOrder不一致");
        }
        Balance balance = getBalance(entId);
        if (balance.getAmount().compareTo(payOrder.getEntAmount()) == -1) {
            throw new ContractException("帐户余额不足");
        }

        subBalance(entId, payOrder.getEntAmount());
        freezeNationBalance(payOrder.getEntAmount());

        if(payOrder.getUserStatus()){
            Long payNum = payMonthNumRepo.getOrDefault(userId, 0l);
            payMonthNumRepo.put(userId, payNum + payOrder.getPayMonthNum());
        }

        payOrder.setEntStatus(true);
        payOrderRepo.put(payOrderId, payOrder);

        LOG("PAY_ORDER_ENT_EVENT",
                payOrderId,
                entId,
                payOrder.getEntAmount(),
                new Date().getTime()
        );
    }

    /**
     * 创建退休计划
     *
     * @param userId 用户ID
     * @param birthday 出生年月
     */
    public void createIssuePlan(Long userId, String birthday) {
        int age = (int) ChronoUnit.YEARS.between(LocalDate.parse(birthday), LocalDate.now(ZoneId.systemDefault()));
        if (age < 40) {
            throw new ContractException("年龄不符合退休条件");
        }
        Long payNum = payMonthNumRepo.getOrDefault(userId, 0l);
        if (payNum < LIMIT_PAY_NUM) {
            throw new ContractException("未缴满15年");
        }

        int index = age % 40 >= ISSUE_AGE_MAP.length ? ISSUE_AGE_MAP.length - 1 : age % 40;
        int issueMonthNum = ISSUE_AGE_MAP[index];

        Balance balance = getBalance(userId);
        IssuePlan issuePlan = new IssuePlan(age, issueMonthNum, balance.getFreezeAmount().divide(BigDecimal.valueOf(issueMonthNum), 4, BigDecimal.ROUND_HALF_EVEN));
        issuePlanRepo.put(userId, issuePlan);

        LOG("CREATE_ISSUE_PLAN_EVENT",
                userId,
                issuePlan.getAge(),
                issueMonthNum,
                issuePlan.getMonthAmount()
        );
    }

    /**
     * 定期发放养老金(发放给个人)
     */
    public void issue(Long userId, BigDecimal lastYearAvgVal, BigDecimal userIndex) {
        String period = LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMM"));
        if (issueHistoryRepo.containsKey(createIssueHistoryKey(period, userId))) {
            throw new ContractException("本期养老金已发放");
        }
        IssuePlan issuePlan = issuePlanRepo.get(userId);
        if (issuePlan == null)
            throw new ContractException("未创建退休计划");

        Long payMonthNum = payMonthNumRepo.getOrDefault(userId, 0l);
        Balance nationBalance = getBalance(-1l);
        Balance userBalance = getBalance(userId);
        BigDecimal basicVal = lastYearAvgVal.add(userIndex).multiply(BigDecimal.valueOf(payMonthNum)).divide(BigDecimal.valueOf(200));
        BigDecimal userVal = issuePlan.getMonthAmount();

        boolean isSubNationBalance = false;
        if (userBalance.getFreezeAmount().compareTo(userVal) == -1) {
            isSubNationBalance = true;
            if (nationBalance.getFreezeAmount().compareTo(basicVal.add(userVal)) == -1) {
                throw new ContractException("统筹帐户金额不足");
            }
        }
        if (nationBalance.getFreezeAmount().compareTo(basicVal) == -1)
            throw new ContractException("统筹帐户金额不足");

        if (isSubNationBalance) {
            unfreezeNationBalance(userVal);
        } else {
            unfreezeBalance(userId, userVal);
        }
        unfreezeNationBalance(basicVal);


        IssueOrder issueOrder = new IssueOrder(new Date().getTime(), basicVal, userVal);
        issueHistoryRepo.put(createIssueHistoryKey(period, userId), issueOrder);
        LOG("ISSUE_EVENT",
                userId,
                period,
                issueOrder.getTimestamp(),
                issueOrder.getBasicAmount(),
                issueOrder.getUserAmount()
        );
    }

    private void addBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        balance.addBalance(amount);
        balanceRepo.put(userId, balance);
        LOG("DEPOSIT_EVENT", userId, now(), amount);
    }

    private void subBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        balance.addBalance(amount.abs().negate());
        balanceRepo.put(userId, balance);
        LOG("PAY_EVENT", userId, amount, new Date().getTime());
    }

    private void freezeNationBalance(BigDecimal amount) {
        freezeBalance(NATION_USER_ID, amount);
    }

    private void unfreezeNationBalance(BigDecimal amount) {
        unfreezeBalance(NATION_USER_ID, amount);
    }

    private void freezeBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        balance.addFreezeBalance(amount.abs());
        balanceRepo.put(userId, balance);
        LOG("FREEZE_EVENT", userId, amount, new Date().getTime());
    }

    private void unfreezeBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        balance.addFreezeBalance(amount.abs().negate());
        balanceRepo.put(userId, balance);
        LOG("UNFREEZE_EVENT", userId, amount, new Date().getTime());
    }

    private Balance getBalance(Long userId) {
        Balance balance = balanceRepo.get(userId);
        if (balance == null) {
            balance = new Balance();
            balanceRepo.put(userId, balance);
        }
        return balance;
    }

    private String createPayOrderUserKey(String period, Long userId) {
        return period + "-" + userId;
    }

    private String createIssueHistoryKey(String period, Long userId) {
        return period + "-" + userId;
    }

    private long nextPayOrderNextId() {
        return nextKeyId("PAY_ORDER_ID");
    }

    private long nextKeyId(String keyType) {
        Long nextId;
        if (keyGenerateMap.get(keyType) == null) {
            nextId = 0l;
        } else {
            nextId = keyGenerateMap.get(keyType) + 1;
        }
        keyGenerateMap.put(keyType, nextId);
        return nextId;
    }

    private long now() {
        return new Date().getTime();
    }

    private Map castStorage(Object data, Class keyClass, Class valueClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(data);
            JavaType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
            return (Map) objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new ContractException("加载合约数据出错");
        }
    }

    private static class Balance {
        private BigDecimal amount;
        private BigDecimal freezeAmount;

        public Balance() {
            this.amount = BigDecimal.ZERO;
            this.freezeAmount = BigDecimal.ZERO;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public BigDecimal getFreezeAmount() {
            return freezeAmount;
        }

        public void addBalance(BigDecimal amount) {
            this.amount = this.amount.add(amount);
        }

        public void addFreezeBalance(BigDecimal amount) {
            this.freezeAmount = this.freezeAmount.add(amount);
        }
    }

    public static class PayOrder {
        private String period;

        private Long timestamp;

        private BigDecimal salaryBase;

        private Long userId;

        private Long payMonthNum;

        private BigDecimal userAmount;

        private Boolean userStatus;

        private Long entId;

        private BigDecimal entAmount;

        private Boolean entStatus;

        public PayOrder() {
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public BigDecimal getSalaryBase() {
            return salaryBase;
        }

        public void setSalaryBase(BigDecimal salaryBase) {
            this.salaryBase = salaryBase;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public BigDecimal getUserAmount() {
            return userAmount;
        }

        public void setUserAmount(BigDecimal userAmount) {
            this.userAmount = userAmount;
        }

        public Boolean getUserStatus() {
            return userStatus;
        }

        public void setUserStatus(Boolean userStatus) {
            this.userStatus = userStatus;
        }

        public Long getEntId() {
            return entId;
        }

        public void setEntId(Long entId) {
            this.entId = entId;
        }

        public BigDecimal getEntAmount() {
            return entAmount;
        }

        public void setEntAmount(BigDecimal entAmount) {
            this.entAmount = entAmount;
        }

        public Boolean getEntStatus() {
            return entStatus;
        }

        public void setEntStatus(Boolean entStatus) {
            this.entStatus = entStatus;
        }

        public Long getPayMonthNum() {
            return payMonthNum;
        }

        public void setPayMonthNum(Long payMonthNum) {
            this.payMonthNum = payMonthNum;
        }


        @Override
        public String toString() {
            return new StringJoiner(", ", PayOrder.class.getSimpleName() + "[", "]")
                    .add("entAmount=" + entAmount)
                    .add("entId=" + entId)
                    .add("entStatus=" + entStatus)
                    .add("payMonthNum=" + payMonthNum)
                    .add("period='" + period + "'")
                    .add("salaryBase=" + salaryBase)
                    .add("timestamp=" + timestamp)
                    .add("userAmount=" + userAmount)
                    .add("userId=" + userId)
                    .add("userStatus=" + userStatus)
                    .toString();
        }
    }

    public static class IssuePlan {
        private Integer age;
        private Integer issueMonthNum;
        private BigDecimal monthAmount;

        public IssuePlan() {
        }

        public IssuePlan(Integer age, Integer issueMonthNum, BigDecimal monthAmount) {
            this.age = age;
            this.issueMonthNum = issueMonthNum;
            this.monthAmount = monthAmount;
        }

        public Integer getAge() {
            return age;
        }

        public BigDecimal getMonthAmount() {
            return monthAmount;
        }

        public Integer getIssueMonthNum() {
            return issueMonthNum;
        }
    }

    public static class IssueOrder {
        private Long timestamp;
        private BigDecimal basicAmount;
        private BigDecimal userAmount;

        public IssueOrder() {
        }

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


        @Override
        public String toString() {
            return new StringJoiner(", ", IssueOrder.class.getSimpleName() + "[", "]")
                    .add("basicAmount=" + basicAmount)
                    .add("timestamp=" + timestamp)
                    .add("userAmount=" + userAmount)
                    .toString();
        }
    }
}
