package com.buaa.blockchain.core;

import com.buaa.blockchain.entity.business.CompanyInfo;
import com.buaa.blockchain.entity.mapper.CompanyInfoMapper;
import com.buaa.blockchain.entity.mapper.PersonInfoMapper;

/**
 * 业务逻辑操作接口
 *
 * state接口将会继承此接口，完成一些业务逻辑上的处理
 * 由于state是会暴露给智能合约的，所以自定义智能合约也可以调用BusinessOps中的功能
 *
 *
 * @author hitty
 * */
public interface BusinessOps {
    int insertCompanyInfo(CompanyInfo companyInfo);
}
