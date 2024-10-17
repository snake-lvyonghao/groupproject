package com.comp5348.store.service;
import com.comp5348.store.model.Goods;
import com.comp5348.store.repository.GoodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoodsService {

    private final GoodsRepository goodsRepository;

    @Autowired
    public GoodsService(GoodsRepository goodsRepository) {
        this.goodsRepository = goodsRepository;
    }

    @Transactional
    public Goods createOrUpdateGoods(Goods goods) {
        try {
            Goods savedGoods = goodsRepository.save(goods);
            return savedGoods;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create or update goods.", e);
        }
    }

    public Goods getGoodsById(long id) {
        return goodsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goods not found with id: " + id));
    }

    public void deleteGoodsById(long id) {
        goodsRepository.deleteById(id);
    }
}