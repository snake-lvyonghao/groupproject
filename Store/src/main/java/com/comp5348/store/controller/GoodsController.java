package com.comp5348.store.controller;

import com.comp5348.store.dto.GoodsDTO;
import com.comp5348.store.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService goodsService;

    @Autowired
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping
    public List<GoodsDTO> getAllGoods() {
        return goodsService.getAllGoods(); // 返回 DTO 列表
    }
}
