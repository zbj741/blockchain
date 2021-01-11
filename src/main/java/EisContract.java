import com.buaa.blockchain.contract.component.ContractException;
import com.buaa.blockchain.contract.component.IContract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/6
 * @since JDK1.8
 */
public class EisContract extends IContract {
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
    private static final String PAY_ORDER_MAP_ENT_TYPE = "PAY_ORDER_MAP_ENT_TYPE";
    private static final String ISSUE_PLAN_TYPE = "ISSUE_PLAN_TYPE";
    private static final String ISSUE_ORDER_TYPE = "ISSUE_ORDER_TYPE";
    private static final String NEXT_ID_TYPE = "NEXT_ID_TYPE";

    private Map<Long, Balance> balanceRepo;
    private Map<Long, PayOrder> payOrderRepo;   // 1:<缴存明细>; 2:缴存明细; 3:缴存明细
    private Map<String, Long> payOrderUserRepo; // 示例: 202101-1:1; 202002:1:77. e.g <缴存期数-用户编码>:<缴存明细ID>
    private Map<String, Long> payOrderEntRepo;  // 示例: 202101-1:1; 202002:1:77. e.g <缴存期数-企业编码>:<缴存明细ID>
    private Map<Long, IssuePlan> issuePlanRepo; // 示例: <UserId>:<退休计划>
    private Map<String, IssueOrder> issueHistoryRepo;  // 示例: 202101-1:<发放明细> e.g <发放期数-用户编码>:<发放明细>
    private Map<String, Long> keyGenerateMap;

    public EisContract(Map storage) {
        balanceRepo = (Map<Long, Balance>) storage.get(BALANCE_TYPE);
        if (storage.get(BALANCE_TYPE) == null) {
            balanceRepo = new HashMap<>();
            storage.put(BALANCE_TYPE, balanceRepo);
        }
        payOrderRepo = (Map<Long, PayOrder>) storage.get(PAY_ORDER_TYPE);
        if (storage.get(PAY_ORDER_TYPE) == null) {
            payOrderRepo = new HashMap<>();
            storage.put(PAY_ORDER_TYPE, payOrderRepo);
        }
        payOrderUserRepo = (Map<String, Long>) storage.get(PAY_ORDER_MAP_USER_TYPE);
        if (storage.get(PAY_ORDER_MAP_USER_TYPE) == null) {
            payOrderUserRepo = new HashMap<>();
            storage.put(PAY_ORDER_MAP_USER_TYPE, payOrderUserRepo);
        }
        payOrderEntRepo = (Map<String, Long>) storage.get(PAY_ORDER_MAP_ENT_TYPE);
        if (storage.get(PAY_ORDER_MAP_ENT_TYPE) == null) {
            payOrderEntRepo = new HashMap<>();
            storage.put(PAY_ORDER_MAP_ENT_TYPE, payOrderEntRepo);
        }
        issuePlanRepo = (Map<Long, IssuePlan>) storage.get(ISSUE_PLAN_TYPE);
        if (storage.get(ISSUE_PLAN_TYPE) == null) {
            issuePlanRepo = new HashMap<>();
            storage.put(ISSUE_PLAN_TYPE, issuePlanRepo);
        }
        issueHistoryRepo = (Map<String, IssueOrder>) storage.get(ISSUE_ORDER_TYPE);
        if (storage.get(ISSUE_ORDER_TYPE) == null) {
            issueHistoryRepo = new HashMap<>();
            storage.put(ISSUE_ORDER_TYPE, issueHistoryRepo);
        }
        keyGenerateMap = (Map<String, Long>) storage.get(NEXT_ID_TYPE);
        if (storage.get(NEXT_ID_TYPE) == null) {
            keyGenerateMap = new HashMap<>();
            storage.put(NEXT_ID_TYPE, keyGenerateMap);
        }
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

        LOG("DEPOSIT_EVENT", userId, now(), amount);
    }

    /**
     * 生成每月待缴明细
     */
    public void createPayOrder(Long userId, Long entId, BigDecimal salaryBase) {
        if (salaryBase == null || salaryBase.compareTo(BigDecimal.ONE) <= 0)
            throw new ContractException("缴费基数应大于0");

        String period = LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMM"));
        if (payOrderUserRepo.containsKey(createPayOrderUserKey(period, userId))
                || payOrderEntRepo.containsKey(createPayOrderEntKey(period, entId))) {
            throw new ContractException("本期待缴明细已生成");
        }

        final long payOrderId = nextPayOrderNextId();
        PayOrder payOrder = new PayOrder();
        payOrder.setPeriod(period);
        payOrder.setSalaryBase(salaryBase);
        payOrder.setTimestamp(new Date().getTime());
        payOrder.setEntId(entId);
        payOrder.setEntAmount(salaryBase.multiply(BigDecimal.valueOf(0.14)));
        payOrder.setEntStatus(false);
        payOrder.setUserId(userId);
        payOrder.setUserAmount(salaryBase.multiply(BigDecimal.valueOf(0.08)));
        payOrder.setUserStatus(false);
        payOrderRepo.put(payOrderId, payOrder);
        payOrderUserRepo.put(createPayOrderUserKey(period, userId), payOrderId);
        payOrderEntRepo.put(createPayOrderEntKey(period, entId), payOrderId);

        LOG("CREATE_PAY_ORDER_EVENT",
                payOrderId,
                period,
                payOrder.getSalaryBase(),
                payOrder.getTimestamp(),
                payOrder.getEntId(),
                payOrder.getEntAmount(),
                payOrder.getUserId(),
                payOrder.getUserAmount()
        );
    }

    /**
     * 养老金缴存(个人)
     */
    public void payOrderByUser(Long userId, String period) {
        Long payOrderId = payOrderUserRepo.get(createPayOrderUserKey(period, userId));
        if (payOrderId == null) {
            throw new ContractException("当期缴存暂未生成");
        }

        PayOrder payOrder = payOrderRepo.get(payOrderId);
        payOrder.setUserStatus(true);

        subBalance(userId, payOrder.getUserAmount());
        LOG("PAY_EVENT", userId, payOrder.getUserAmount(), new Date().getTime());
        freezeBalance(userId, payOrder.getUserAmount());
        LOG("FREEZE_EVENT", userId, payOrder.getUserAmount(), new Date().getTime());

        payOrderRepo.put(payOrderId, payOrder);
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
    public void payOrderByEnt(Long entId, Long userId, String period) {
        Long payOrderId = payOrderEntRepo.get(createPayOrderEntKey(period, entId));
        if (payOrderId == null) {
            throw new ContractException("当期缴存暂未生成");
        }

        PayOrder payOrder = payOrderRepo.get(payOrderId);
        if (!payOrder.getEntId().equals(entId)) {
            throw new ContractException("非本公司员工");
        }

        subBalance(entId, payOrder.getEntAmount());
        LOG("PAY_EVENT", entId, payOrder.getEntAmount(), new Date().getTime());
        freezeNationBalance(payOrder.getEntAmount());
        LOG("FREEZE_EVENT", entId, payOrder.getEntAmount(), new Date().getTime());


        payOrder.setEntStatus(true);
        payOrderRepo.put(payOrderId, payOrder);

        LOG("PAY_ORDER_ENT_EVENT",
                payOrderId,
                entId,
                payOrder.getEntAmount(),
                new Date().getTime()
        );
    }

    public void createIssuePlan(Long userId, String beginWork) {
        int workYears = (int) ChronoUnit.YEARS.between(LocalDate.parse(beginWork), LocalDate.now(ZoneId.systemDefault()));
        if (workYears < 40) {
            throw new ContractException("年龄不符合退休条件");
        }
        int index = workYears % 40 >= ISSUE_AGE_MAP.length ? ISSUE_AGE_MAP.length - 1 : workYears % 40;
        int issueMonthNum = ISSUE_AGE_MAP[index];

        Balance balance = getBalance(userId);
        IssuePlan issuePlan = new IssuePlan(workYears, issueMonthNum, balance.getFreezeAmount().divide(BigDecimal.valueOf(issueMonthNum), 4, BigDecimal.ROUND_HALF_EVEN));
        issuePlanRepo.put(userId, issuePlan);

        LOG("CREATE_ISSUE_PLAN_EVENT",
                userId,
                issuePlan.getWorkYears(),
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

        Balance nationBalance = getBalance(-1l);
        Balance userBalance = getBalance(userId);
        BigDecimal basicVal = lastYearAvgVal.add(userIndex).multiply(BigDecimal.valueOf(issuePlan.getWorkYears())).divide(BigDecimal.valueOf(200));
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
            LOG("UNFREEZE_EVENT", -1l, userVal, new Date().getTime());
        } else {
            unfreezeBalance(userId, userVal);
            LOG("UNFREEZE_EVENT", userId, userVal, new Date().getTime());
        }
        unfreezeNationBalance(basicVal);
        LOG("UNFREEZE_EVENT", -1l, basicVal, new Date().getTime());


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
    }

    private void subBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        balance.addBalance(amount.abs().negate());
        balanceRepo.put(userId, balance);
    }

    private void freezeNationBalance(BigDecimal amount) {
        freezeBalance(-1l, amount);
    }

    private void unfreezeNationBalance(BigDecimal amount) {
        unfreezeBalance(-1l, amount);
    }

    private void freezeBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        balance.addFreezeBalance(amount.abs());
        balanceRepo.put(userId, balance);
    }

    private void unfreezeBalance(Long userId, BigDecimal amount) {
        Balance balance = getBalance(userId);
        balance.addFreezeBalance(amount.abs().negate());
        balanceRepo.put(userId, balance);
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

    private String createPayOrderEntKey(String period, Long entId) {
        return period + "-" + entId;
    }

    private String createIssueHistoryKey(String period, Long userId) {
        return period + "-" + userId;
    }

    private long nextPayOrderNextId() {
        return nextKeyId("PAY_ORDER_ID");
    }

    private long nextKeyId(String keyType) {
        Long nextId = keyGenerateMap.get(keyType) ;
        if(nextId == null){
           nextId = 0l;
           keyGenerateMap.put(keyType, nextId);
        }else{
           keyGenerateMap.put(keyType, nextId++);
        }
        return nextId;
    }

    private long now() {
        return new Date().getTime();
    }

    private class Balance {
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

    private class PayOrder {
        private String period;

        private Long timestamp;

        private BigDecimal salaryBase;

        private Long userId;

        private BigDecimal userAmount;

        private Boolean userStatus;

        private Long entId;

        private BigDecimal entAmount;

        private Boolean entStatus;

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

    }

    private class IssuePlan {
        private Integer workYears;
        private Integer issueMonthNum;
        private BigDecimal monthAmount;

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
