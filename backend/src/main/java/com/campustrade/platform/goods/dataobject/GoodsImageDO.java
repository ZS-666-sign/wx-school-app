package com.campustrade.platform.goods.dataobject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsImageDO {

    private Long id;
    private Long goodsId;
    private String imageUrl;
    private Integer sortOrder = 0;
}
