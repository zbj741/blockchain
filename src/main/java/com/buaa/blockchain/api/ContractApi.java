package com.buaa.blockchain.api;

import com.buaa.blockchain.entity.Contract;
import com.buaa.blockchain.entity.ContractAccount;
import com.buaa.blockchain.utils.Page;

import java.util.List;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/15
 * @since JDK1.8
 */
public interface ContractApi {
    ContractAccount findContractById(int id);

    int findContractNum();

    Page<Contract> findPageList(Long page_index, Long page_size);

    List<String> findContractNameList();
}
