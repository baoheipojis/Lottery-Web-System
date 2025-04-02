package com.example.lottery.controller;

import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import com.example.lottery.service.PrizeService;
import com.example.lottery.LotteryContext; // Add this import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prizes")
public class PrizeController {

    private final PrizeService prizeService;
    private final PrizeRepository prizeRepository;

    @Autowired
    public PrizeController(PrizeService prizeService, PrizeRepository prizeRepository) {
        this.prizeService = prizeService;
        this.prizeRepository = prizeRepository;
    }

    // 获取所有奖品
    @GetMapping
    public List<Prize> getAllPrizes() {
        return prizeService.getAllPrizes();
    }

    @GetMapping("/draw")
    public ResponseEntity<?> drawPrize() {
        try {
            Prize prize = prizeService.drawPrize();
            return ResponseEntity.ok(prize);
        } catch (LotteryContext.InsufficientPlanPointsException e) {
            // 计划点不足的错误
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "INSUFFICIENT_POINTS");
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (LotteryContext.NoPrizeAvailableException e) {
            // 奖池为空或没有符合条件的奖品
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "NO_PRIZE_AVAILABLE");
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 其他未预期的错误
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "抽奖过程中发生错误: " + e.getMessage());
            errorResponse.put("errorType", "UNKNOWN_ERROR");
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<?> addPrize(@RequestBody Prize prize) {
        try {
            // 验证必填字段
            if (prize.getName() == null || prize.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("奖品名称不能为空");
            }
            
            if (prize.getRarity() < 3 || prize.getRarity() > 5) {
                throw new IllegalArgumentException("奖品稀有度必须在3-5星之间");
            }
            
            // 如果是5星奖品，验证fiveStarType
            if (prize.getRarity() == 5 && 
                (prize.getFiveStarType() == null || 
                 (!prize.getFiveStarType().equalsIgnoreCase("normal") && 
                  !prize.getFiveStarType().equalsIgnoreCase("limited")))) {
                throw new IllegalArgumentException("5星奖品必须指定类型为normal或limited");
            }
            
            Prize savedPrize = prizeService.savePrize(prize);
            return ResponseEntity.ok(savedPrize);
        } catch (IllegalArgumentException e) {
            // 参数验证错误
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", "VALIDATION_ERROR");
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 其他未预期的错误
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "添加奖品失败: " + e.getMessage());
            errorResponse.put("errorType", "UNKNOWN_ERROR");
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePrize(@PathVariable Long id) {
        try {
            prizeService.deletePrize(id);
            
            // 返回成功消息
            Map<String, String> response = new HashMap<>();
            response.put("message", "Prize deleted successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 返回具体的错误信息
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 捕获其他类型的异常
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public Prize updatePrize(@PathVariable Long id, @RequestBody Prize updated) {
        Prize existing = prizeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prize not found"));
        
        // Update all editable fields
        existing.setName(updated.getName());
        existing.setRarity(updated.getRarity());
        existing.setDescription(updated.getDescription());
        existing.setFiveStarType(updated.getFiveStarType());
        existing.setIsRepeatable(updated.getIsRepeatable());
        
        return prizeRepository.save(existing);
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> togglePrizeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        
        try {
            boolean enabled = payload.getOrDefault("enabled", true);
            Prize updatedPrize = prizeService.togglePrizeStatus(id, enabled);
            return ResponseEntity.ok(updatedPrize);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "status", "error"
            ));
        }
    }
}
