package com.campustrade.platform.goods.assembler;

import com.campustrade.platform.category.assembler.CategoryAssembler;
import com.campustrade.platform.category.dto.response.CategoryResponseDTO;
import com.campustrade.platform.goods.dataobject.GoodsDO;
import com.campustrade.platform.goods.dataobject.GoodsImageDO;
import com.campustrade.platform.goods.dto.response.GoodsResponseDTO;
import com.campustrade.platform.user.assembler.UserProfileAssembler;
import org.springframework.stereotype.Component;

@Component
public class GoodsAssembler {

    private final UserProfileAssembler userProfileAssembler;
    private final CategoryAssembler categoryAssembler;

    public GoodsAssembler(UserProfileAssembler userProfileAssembler, CategoryAssembler categoryAssembler) {
        this.userProfileAssembler = userProfileAssembler;
        this.categoryAssembler = categoryAssembler;
    }

    public GoodsResponseDTO toResponse(GoodsDO goods) {
        CategoryResponseDTO categoryResponse = goods.getCategory() == null ? null : categoryAssembler.toResponse(goods.getCategory());
        return new GoodsResponseDTO(
                goods.getId(),
                goods.getTitle(),
                goods.getDescription(),
                goods.getPrice(),
                goods.getConditionLevel(),
                goods.getCampusLocation(),
                goods.getStatus(),
                categoryResponse,
                userProfileAssembler.toResponse(goods.getSeller()),
                goods.getImages().stream()
                        .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                        .map(GoodsImageDO::getImageUrl)
                        .toList(),
                goods.getCreatedAt(),
                goods.getUpdatedAt()
        );
    }
}

