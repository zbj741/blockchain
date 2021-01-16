package com.buaa.blockchain.entity;

import lombok.Data;

@Data
public class BlockNumInfo {
    private String txlength_range;
    private Long block_num;
    private String sign_node;
}
