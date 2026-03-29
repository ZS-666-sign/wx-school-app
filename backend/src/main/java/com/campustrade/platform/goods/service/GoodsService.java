package com.campustrade.platform.goods.service;

import com.campustrade.platform.category.dataobject.CategoryDO;
import com.campustrade.platform.category.service.CategoryService;
import com.campustrade.platform.common.AppException;
import com.campustrade.platform.common.PageResponse;
import com.campustrade.platform.goods.assembler.GoodsAssembler;
import com.campustrade.platform.goods.dataobject.GoodsDO;
import com.campustrade.platform.goods.dataobject.GoodsImageDO;
import com.campustrade.platform.goods.dto.request.GoodsSaveRequestDTO;
import com.campustrade.platform.goods.dto.response.GoodsResponseDTO;
import com.campustrade.platform.goods.enums.GoodsStatusEnum;
import com.campustrade.platform.goods.mapper.GoodsMapper;
import com.campustrade.platform.message.mapper.ConversationMapper;
import com.campustrade.platform.user.dataobject.UserDO;
import com.campustrade.platform.user.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ConversationMapper conversationMapper;
    private final GoodsAssembler goodsAssembler;

    public GoodsService(GoodsMapper goodsMapper,
                        CategoryService categoryService,
                        UserService userService,
                        ConversationMapper conversationMapper,
                        GoodsAssembler goodsAssembler) {
        this.goodsMapper = goodsMapper;
        this.categoryService = categoryService;
        this.userService = userService;
        this.conversationMapper = conversationMapper;
        this.goodsAssembler = goodsAssembler;
    }

@Transactional
    @CacheEvict(cacheNames = "goods:list", allEntries = true)
    public GoodsResponseDTO create(Long sellerId, GoodsSaveRequestDTO request) {
        userService.getById(sellerId);

        GoodsDO goods = new GoodsDO();
        goods.setSellerId(sellerId);
        goods.setCategoryId(validateCategoryId(request.categoryId()));
        goods.setTitle(request.title().trim());
        goods.setDescription(request.description().trim());
        goods.setPrice(request.price());
        goods.setConditionLevel(request.conditionLevel().trim());
        goods.setCampusLocation(request.campusLocation().trim());
        goods.setStatus(GoodsStatusEnum.ON_SALE);

        goodsMapper.insert(goods);
        replaceImages(goods.getId(), request.imageUrls());
        return getDetail(goods.getId());
    }

    @Transactional
    @CacheEvict(cacheNames = "goods:list", allEntries = true)
    public GoodsResponseDTO update(Long currentUserId, Long goodsId, GoodsSaveRequestDTO request) {
        GoodsDO existing = getGoodsOrThrow(goodsId);
        validateOwner(currentUserId, existing);

        GoodsDO update = new GoodsDO();
        update.setId(goodsId);
        update.setCategoryId(validateCategoryId(request.categoryId()));
        update.setTitle(request.title().trim());
        update.setDescription(request.description().trim());
        update.setPrice(request.price());
        update.setConditionLevel(request.conditionLevel().trim());
        update.setCampusLocation(request.campusLocation().trim());

        goodsMapper.update(update);
        replaceImages(goodsId, request.imageUrls());
        return getDetail(goodsId);
    }

    @Transactional
    @CacheEvict(cacheNames = "goods:list", allEntries = true)
    public void delete(Long currentUserId, Long goodsId) {
        GoodsDO goods = getGoodsOrThrow(goodsId);
        validateOwner(currentUserId, goods);
        conversationMapper.deleteByGoodsId(goodsId);
        goodsMapper.deleteById(goodsId);
    }

    @Transactional
    @CacheEvict(cacheNames = "goods:list", allEntries = true)
    public GoodsResponseDTO updateStatus(Long currentUserId, Long goodsId, GoodsStatusEnum status) {
        GoodsDO goods = getGoodsOrThrow(goodsId);
        validateOwner(currentUserId, goods);
        goodsMapper.updateStatus(goodsId, status);
        return getDetail(goodsId);
    }

@Transactional(readOnly = true)
    public GoodsResponseDTO getDetail(Long goodsId) {
        GoodsDO goods = getGoodsOrThrow(goodsId);
        goods.setImages(goodsMapper.findImagesByGoodsId(goodsId));
        return goodsAssembler.toResponse(goods);
    }

    @Transactional(readOnly = true)
    public GoodsDO getById(Long goodsId) {
        return getGoodsOrThrow(goodsId);
    }

@Transactional(readOnly = true)
    public PageResponse<GoodsResponseDTO> list(String keyword, Long categoryId, GoodsStatusEnum status, int page, int size) {
        int offset = page * size;
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        List<GoodsDO> goodsList = goodsMapper.search(normalizedKeyword, categoryId, status, size, offset);
        long total = goodsMapper.countSearch(normalizedKeyword, categoryId, status);
        attachImages(goodsList);

        List<GoodsResponseDTO> items = goodsList.stream().map(goodsAssembler::toResponse).toList();
        return PageResponse.of(items, total, page, size);
    }

@Transactional(readOnly = true)
    public PageResponse<GoodsResponseDTO> myGoods(Long sellerId, int page, int size) {
        userService.getById(sellerId);

        int offset = page * size;
        List<GoodsDO> goodsList = goodsMapper.findBySellerId(sellerId, size, offset);
        long total = goodsMapper.countBySellerId(sellerId);
        attachImages(goodsList);

        List<GoodsResponseDTO> items = goodsList.stream().map(goodsAssembler::toResponse).toList();
        return PageResponse.of(items, total, page, size);
    }

    private GoodsDO getGoodsOrThrow(Long goodsId) {
        GoodsDO goods = goodsMapper.findById(goodsId);
        if (goods == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        return goods;
    }

    private Long validateCategoryId(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        CategoryDO category = categoryService.getById(categoryId);
        return category.getId();
    }

    private void validateOwner(Long userId, GoodsDO goods) {
        if (goods.getSeller() == null || !goods.getSeller().getId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "无权操作该商品");
        }
    }

    private void replaceImages(Long goodsId, List<String> imageUrls) {
        goodsMapper.deleteImagesByGoodsId(goodsId);
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        List<GoodsImageDO> images = new ArrayList<>();
        int idx = 0;
        for (String imageUrl : imageUrls) {
            GoodsImageDO image = new GoodsImageDO();
            image.setGoodsId(goodsId);
            image.setImageUrl(imageUrl.trim());
            image.setSortOrder(idx++);
            images.add(image);
        }
        goodsMapper.batchInsertImages(goodsId, images);
    }

    private void attachImages(List<GoodsDO> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }

        List<Long> goodsIds = goodsList.stream().map(GoodsDO::getId).toList();
        List<GoodsImageDO> images = goodsMapper.findImagesByGoodsIds(goodsIds);

        Map<Long, List<GoodsImageDO>> grouped = new HashMap<>();
        for (GoodsImageDO image : images) {
            grouped.computeIfAbsent(image.getGoodsId(), ignored -> new ArrayList<>()).add(image);
        }

        for (GoodsDO goods : goodsList) {
            goods.setImages(grouped.getOrDefault(goods.getId(), new ArrayList<>()));
        }
    }
}
