package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.repository.PrizeRepository;
import com.example.model.Prize;

@Service
public class PrizeService {

    @Autowired
    private PrizeRepository prizeRepository;

    public List<Prize> findAll() {
        return prizeRepository.findAll();
    }

    public Prize savePrize(Prize prize) {
        return prizeRepository.save(prize);
    }
}