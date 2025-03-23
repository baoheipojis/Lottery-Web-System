// src/main/java/com/example/lottery/service/PrizeService.java
package com.example.lottery.service;

import com.example.lottery.entity.Prize;
import java.util.List;

public interface PrizeService {
    
    /**
     * 获取所有奖品
     * @return 奖品列表
     */
    List<Prize> getAllPrizes();
    
    /**
     * 抽奖方法
     * @return 抽中的奖品
     */
    Prize drawPrize();
    
    /**
     * 保存新奖品
     * @param prize 奖品信息
     * @return 保存后的奖品（包含ID）
     */
    Prize savePrize(Prize prize);
    
    /**
     * 删除奖品
     * @param id 奖品ID
     */
    void deletePrize(Long id);
}