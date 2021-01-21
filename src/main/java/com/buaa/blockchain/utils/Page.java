package com.buaa.blockchain.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/16
 * @since JDK1.8
 */
@Getter
@Setter
public class Page<T> {
    private Long pageIndex;
    private Long pageSize;
    private Long totalPage;
    private Long totalSize;
    private List<T> data;
}
