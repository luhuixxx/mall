package com.luxiao.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxiao.mallmodel.product.Spu;
import com.luxiao.mallproduct.mapper.SpuMapper;
import com.luxiao.mallproduct.service.SpuService;
import org.springframework.stereotype.Service;

@Service
public class SpuServiceImpl extends ServiceImpl<SpuMapper, Spu> implements SpuService {
    @Override
    public Page<Spu> pageSpu(int page, int size, String name) {
        LambdaQueryWrapper<Spu> qw = new LambdaQueryWrapper<>();
        qw.like(name != null && !name.isBlank(), Spu::getName, name);
        return this.page(Page.of(page, size), qw);
    }
}

