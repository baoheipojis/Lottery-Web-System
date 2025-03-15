package com.example.lottery.repository;

import com.example.lottery.entity.Prize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PrizeRepositoryTest {

    @Autowired
    private PrizeRepository prizeRepository;

    @Test
    @DisplayName("测试保存奖品并根据稀有度和五星类型查询")
    void testSaveAndFindByRarityAndFiveStarType() {
        // 创建并保存几种不同的奖品
        Prize prize3Star = new Prize("30-50CNY充入加密货币", 3, null, "三星奖品示例");
        Prize prize4Star = new Prize("200 CNY充入加密货币", 4, null, "四星奖品示例");
        Prize normalFiveStarPrize = new Prize("普通五星奖品示例", 5, "normal", "普通五星奖品描述");
        Prize limitedFiveStarPrize = new Prize("限定五星奖品示例", 5, "limited", "限定五星奖品描述");

        prizeRepository.save(prize3Star);
        prizeRepository.save(prize4Star);
        prizeRepository.save(normalFiveStarPrize);
        prizeRepository.save(limitedFiveStarPrize);

        // 根据稀有度查询
        List<Prize> threeStarPrizes = prizeRepository.findByRarity(3);
        List<Prize> fourStarPrizes = prizeRepository.findByRarity(4);
        List<Prize> normalFiveStarPrizes = prizeRepository.findByRarityAndFiveStarType(5, "normal");
        List<Prize> limitedFiveStarPrizes = prizeRepository.findByRarityAndFiveStarType(5, "limited");

        // 验证查询结果
        assertEquals(1, threeStarPrizes.size(), "3星奖品数量应为1");
        assertEquals(1, fourStarPrizes.size(), "4星奖品数量应为1");
        assertEquals(1, normalFiveStarPrizes.size(), "普通5星奖品数量应为1");
        assertEquals(1, limitedFiveStarPrizes.size(), "限定5星奖品数量应为1");
    }

    @Test
    @DisplayName("测试奖品更新功能")
    void testUpdatePrize() {
        Prize prize = new Prize("原始奖品", 3, null, "原始描述");
        Prize savedPrize = prizeRepository.save(prize);
        assertNotNull(savedPrize.getId(), "保存后奖品ID不能为空");

        // 修改奖品属性
        savedPrize.setName("更新后的奖品");
        savedPrize.setDescription("更新后的描述");
        Prize updatedPrize = prizeRepository.save(savedPrize);

        assertEquals("更新后的奖品", updatedPrize.getName(), "奖品名称应为更新后的名称");
        assertEquals("更新后的描述", updatedPrize.getDescription(), "奖品描述应为更新后的描述");
    }
}
