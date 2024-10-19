package com.comp5348.store.service;
import com.comp5348.store.dto.GoodsDTO;
import com.comp5348.store.model.Goods;
import com.comp5348.store.repository.GoodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GoodsService {

    private final GoodsRepository goodsRepository;

    @Autowired
    public GoodsService(GoodsRepository goodsRepository) {
        this.goodsRepository = goodsRepository;
    }

    @Transactional
    public GoodsDTO createOrUpdateGoods(Goods goods) {
        try {
            Goods savedGoods = goodsRepository.save(goods);
            return new GoodsDTO(savedGoods);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create or update goods.", e);
        }
    }

    public GoodsDTO getGoodsById(long id) {
        Optional<Goods> goods = goodsRepository.findById(id);
        return goods.map(GoodsDTO::new).orElse(null);
    }

    public void deleteGoodsById(long id) {
        goodsRepository.deleteById(id);
    }
}