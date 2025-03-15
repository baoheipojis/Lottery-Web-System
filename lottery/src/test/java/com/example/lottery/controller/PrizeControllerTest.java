package com.example.lottery.controller;

import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrizeRepository prizeRepository;

    @Test
    void testAddPrize() throws Exception {
        // 准备测试数据
        String prizeJson = """
            {
                "name": "测试奖品",
                "rarity": 4,
                "fiveStarType": "normal",
                "description": "测试描述",
                "isRepeatable": true
            }
        """;

        // 调用 POST 请求并验证结果
        mockMvc.perform(post("/api/prizes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prizeJson))
                .andExpect(status().isOk()) // 检查返回状态码 200
                .andExpect(jsonPath("$.name").value("测试奖品")) // 验证返回的 JSON 数据
                .andExpect(jsonPath("$.rarity").value(4))
                .andExpect(jsonPath("$.fiveStarType").value("normal"))
                .andExpect(jsonPath("$.description").value("测试描述"))
                .andExpect(jsonPath("$.isRepeatable").value(true));
    }
}
