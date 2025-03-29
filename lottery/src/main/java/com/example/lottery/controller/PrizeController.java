package com.example.lottery.controller;

import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import com.example.lottery.service.PrizeService;
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
    public Prize drawPrize() {
        return prizeService.drawPrize(); // 调用 PrizeService 中的抽奖逻辑
    }
    
    @PostMapping
    public Prize addPrize(@RequestBody Prize prize) {
        return prizeService.savePrize(prize);
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
}
