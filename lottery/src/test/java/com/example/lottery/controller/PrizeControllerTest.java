package com.example.lottery.controller;

import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import com.example.lottery.service.PrizeService;
import com.example.lottery.LotteryContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrizeController.class)
public class PrizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrizeService prizeService;

    @MockBean
    private PrizeRepository prizeRepository;

    @Test
    public void testGetAllPrizes() throws Exception {
        // 准备测试数据
        Prize prize1 = new Prize();
        prize1.setId(1L);
        prize1.setName("测试奖品1");
        prize1.setRarity(3);

        Prize prize2 = new Prize();
        prize2.setId(2L);
        prize2.setName("测试奖品2");
        prize2.setRarity(4);

        List<Prize> prizes = Arrays.asList(prize1, prize2);

        // 模拟服务层返回
        when(prizeService.getAllPrizes()).thenReturn(prizes);

        // 执行请求并验证
        mockMvc.perform(get("/api/prizes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("测试奖品1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("测试奖品2")));
    }

    @Test
    public void testDrawPrize_Success() throws Exception {
        // 准备测试数据
        Prize prize = new Prize();
        prize.setId(1L);
        prize.setName("抽中的奖品");
        prize.setRarity(5);
        prize.setFiveStarType("normal");

        // 模拟服务层返回
        when(prizeService.drawPrize()).thenReturn(prize);

        // 执行请求并验证
        mockMvc.perform(get("/api/prizes/draw"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("抽中的奖品")))
                .andExpect(jsonPath("$.rarity", is(5)));
    }

    @Test
    public void testDrawPrize_InsufficientPoints() throws Exception {
        // 模拟抽奖点数不足异常
        when(prizeService.drawPrize()).thenThrow(new LotteryContext.InsufficientPlanPointsException("计划点不足，无法抽奖"));

        // 执行请求并验证
        mockMvc.perform(get("/api/prizes/draw"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("计划点不足，无法抽奖")))
                .andExpect(jsonPath("$.errorType", is("INSUFFICIENT_POINTS")))
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    public void testDrawPrize_NoPrizeAvailable() throws Exception {
        // 模拟奖池为空异常
        when(prizeService.drawPrize()).thenThrow(new LotteryContext.NoPrizeAvailableException("当前奖池中没有可用奖品"));

        // 执行请求并验证
        mockMvc.perform(get("/api/prizes/draw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("当前奖池中没有可用奖品")))
                .andExpect(jsonPath("$.errorType", is("NO_PRIZE_AVAILABLE")));
    }

    @Test
    public void testAddPrize_Success() throws Exception {
        // 准备测试数据
        Prize inputPrize = new Prize();
        inputPrize.setName("新奖品");
        inputPrize.setRarity(4);
        inputPrize.setDescription("测试描述");
        inputPrize.setIsRepeatable(true);

        Prize savedPrize = new Prize();
        savedPrize.setId(1L);
        savedPrize.setName("新奖品");
        savedPrize.setRarity(4);
        savedPrize.setDescription("测试描述");
        savedPrize.setIsRepeatable(true);

        // 模拟服务层返回
        when(prizeService.savePrize(any(Prize.class))).thenReturn(savedPrize);

        // 执行请求并验证
        mockMvc.perform(post("/api/prizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputPrize)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("新奖品")))
                .andExpect(jsonPath("$.rarity", is(4)));
    }

    @Test
    public void testAddPrize_ValidationError() throws Exception {
        // 准备无名称的奖品数据
        Prize invalidPrize = new Prize();
        invalidPrize.setRarity(4);
        
        // 执行请求并验证失败情况
        mockMvc.perform(post("/api/prizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPrize)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("奖品名称不能为空")))
                .andExpect(jsonPath("$.errorType", is("VALIDATION_ERROR")));
    }

    @Test
    public void testDeletePrize_Success() throws Exception {
        // 模拟成功删除
        doNothing().when(prizeService).deletePrize(1L);

        // 执行请求并验证
        mockMvc.perform(delete("/api/prizes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Prize deleted successfully")))
                .andExpect(jsonPath("$.status", is("success")));
        
        // 验证service方法被调用
        verify(prizeService, times(1)).deletePrize(1L);
    }

    @Test
    public void testDeletePrize_NotFound() throws Exception {
        // 模拟找不到奖品的情况
        doThrow(new RuntimeException("Prize not found")).when(prizeService).deletePrize(99L);

        // 执行请求并验证
        mockMvc.perform(delete("/api/prizes/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Prize not found")))
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    public void testUpdatePrize_Success() throws Exception {
        // 准备测试数据
        Prize existingPrize = new Prize();
        existingPrize.setId(1L);
        existingPrize.setName("原奖品");
        existingPrize.setRarity(3);
        
        Prize updatedPrize = new Prize();
        updatedPrize.setName("更新后的奖品");
        updatedPrize.setRarity(4);
        updatedPrize.setDescription("更新后的描述");
        
        Prize resultPrize = new Prize();
        resultPrize.setId(1L);
        resultPrize.setName("更新后的奖品");
        resultPrize.setRarity(4);
        resultPrize.setDescription("更新后的描述");

        // 模拟仓库层操作
        when(prizeRepository.findById(1L)).thenReturn(Optional.of(existingPrize));
        when(prizeRepository.save(any(Prize.class))).thenReturn(resultPrize);

        // 执行请求并验证
        mockMvc.perform(put("/api/prizes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPrize)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("更新后的奖品")))
                .andExpect(jsonPath("$.rarity", is(4)));
    }

    @Test
    public void testTogglePrizeStatus() throws Exception {
        // 准备测试数据
        Prize resultPrize = new Prize();
        resultPrize.setId(1L);
        resultPrize.setName("测试奖品");
        resultPrize.setEnabled(false); // 假设切换后状态为禁用

        // 模拟服务层返回
        when(prizeService.togglePrizeStatus(eq(1L), eq(false))).thenReturn(resultPrize);

        // 执行请求并验证
        mockMvc.perform(put("/api/prizes/1/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("测试奖品")))
                .andExpect(jsonPath("$.enabled", is(false)));
    }
}