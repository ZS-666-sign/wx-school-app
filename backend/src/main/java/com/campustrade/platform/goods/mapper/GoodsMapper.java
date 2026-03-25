package com.campustrade.platform.goods.mapper;

import com.campustrade.platform.goods.dataobject.GoodsDO;
import com.campustrade.platform.goods.dataobject.GoodsImageDO;
import com.campustrade.platform.goods.enums.GoodsStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper {

    int insert(GoodsDO goods);

    int update(GoodsDO goods);

    int updateStatus(@Param("id") Long id, @Param("status") GoodsStatusEnum status);

    int deleteById(@Param("id") Long id);

    GoodsDO findById(@Param("id") Long id);

    List<GoodsDO> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("status") GoodsStatusEnum status,
                         @Param("limit") int limit,
                         @Param("offset") int offset);

    long countSearch(@Param("keyword") String keyword,
                     @Param("categoryId") Long categoryId,
                     @Param("status") GoodsStatusEnum status);

    List<GoodsDO> findBySellerId(@Param("sellerId") Long sellerId,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    long countBySellerId(@Param("sellerId") Long sellerId);

    List<GoodsImageDO> findImagesByGoodsId(@Param("goodsId") Long goodsId);

    List<GoodsImageDO> findImagesByGoodsIds(@Param("goodsIds") List<Long> goodsIds);

    int deleteImagesByGoodsId(@Param("goodsId") Long goodsId);

    int batchInsertImages(@Param("goodsId") Long goodsId, @Param("images") List<GoodsImageDO> images);
}
