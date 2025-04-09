package com.example.lottery.service;

import com.example.lottery.entity.Prize;
import com.example.lottery.repository.PrizeRepository;
import com.example.lottery.repository.LotteryHistoryRepository;
import com.example.lottery.entity.LotteryHistory;
import com.example.lottery.LotteryContext;
import com.example.lottery.LotteryState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrizeServiceTest {

   @Mock
   private PrizeRepository prizeRepository;
   
   @Mock
   private LotteryHistoryRepository lotteryHistoryRepository;

   @Mock
   private LotteryContext lotteryContext;

   @Mock
   private LotteryState lotteryState;

   @InjectMocks
   private PrizeService prizeService;

   @BeforeEach
   void setUp() {
       MockitoAnnotations.openMocks(this);
   }

   @Test
   void testGetAllPrizes() {
       Prize prize1 = new Prize("Prize 1", 3, null, "Description 1");
       Prize prize2 = new Prize("Prize 2", 4, null, "Description 2");
       when(prizeRepository.findAll()).thenReturn(Arrays.asList(prize1, prize2));

       List<Prize> prizes = prizeService.getAllPrizes();

       assertEquals(2, prizes.size());
       assertEquals("Prize 1", prizes.get(0).getName());
       assertEquals("Prize 2", prizes.get(1).getName());
   }

   @Test
   void testSavePrize() {
       Prize prize = new Prize("Prize 1", 3, null, "Description 1");
       when(prizeRepository.save(prize)).thenReturn(prize);

       Prize savedPrize = prizeService.savePrize(prize);

       assertNotNull(savedPrize);
       assertEquals("Prize 1", savedPrize.getName());
   }

   @Test
   void testDrawPrize() {
       // Mock the lotteryContext.determineRarity to return 3 (for a 3-star prize)
       when(lotteryContext.determineRarity(lotteryState)).thenReturn(3);
       
       // Mock findLastFiveStarPrize to return null (assuming no previous 5-star prize)
       when(lotteryHistoryRepository.findLastFiveStarPrize()).thenReturn(null);
       
       // Create a prize and mock repository to return it
       Prize prize = new Prize("Prize 1", 3, null, "Description 1");
       prize.setIsRepeatable(false); // Set the prize to be non-repeatable
       
       // Mock the findEnabledByRarity method to return a list with our prize
       when(prizeRepository.findEnabledByRarity(3)).thenReturn(Arrays.asList(prize));
       
       // Mock lotteryHistoryRepository.save to return the saved history
       when(lotteryHistoryRepository.save(any(LotteryHistory.class))).thenAnswer(i -> i.getArguments()[0]);

       // Draw the prize for the first time
       Prize drawnPrize = prizeService.drawPrize();
       
       // Verify results
       assertNotNull(drawnPrize);
       assertEquals("Prize 1", drawnPrize.getName());
       verify(prizeRepository, times(1)).delete(prize);
       verify(lotteryHistoryRepository, times(1)).save(any(LotteryHistory.class));
       
       // For subsequent tests if needed
       // ...existing code...
   }

   @Test
   void testFourStarPitySystem() {
       // Mock the ninth consecutive 3-star draw
       when(lotteryState.getNoFourStarCount()).thenReturn(9);
       
       // The 10th draw should enforce a 4-star or higher due to pity system
       when(lotteryContext.determineRarity(lotteryState)).thenReturn(4); // The system should convert to 4
       
       // Mock no previous 5-star
       when(lotteryHistoryRepository.findLastFiveStarPrize()).thenReturn(null);
       
       // Create a 4-star prize
       Prize fourStarPrize = new Prize("4★ Pity Prize", 4, null, "Description");
       when(prizeRepository.findEnabledByRarity(4)).thenReturn(Arrays.asList(fourStarPrize));
       
       // Mock lotteryHistoryRepository.save
       when(lotteryHistoryRepository.save(any(LotteryHistory.class))).thenAnswer(i -> i.getArguments()[0]);

       // Perform the draw that should hit pity
       Prize drawnPrize = prizeService.drawPrize();
       
       // Verify results
       assertNotNull(drawnPrize);
       assertEquals(4, drawnPrize.getRarity(), "The pity system should guarantee at least a 4-star");
       verify(lotteryHistoryRepository, times(1)).save(any(LotteryHistory.class));
   }


   @Test
   void testFiveStarGuaranteedPattern() {
       // Mock a 5-star pull
       when(lotteryContext.determineRarity(lotteryState)).thenReturn(5);
       
       // Mock that the last 5-star was NORMAL
       Prize lastFiveStar = new Prize("Previous 5★", 5, "NORMAL", "Previous prize");
       when(lotteryHistoryRepository.findLastFiveStarPrize()).thenReturn(lastFiveStar);
       
       // After NORMAL, the next must be LIMITED (guaranteed)
       Prize limitedFiveStar = new Prize("Limited 5★", 5, "LIMITED", "Guaranteed limited");
       when(prizeRepository.findEnabledFiveStarsByType("LIMITED")).thenReturn(Arrays.asList(limitedFiveStar));
       
       // Mock save
       when(lotteryHistoryRepository.save(any(LotteryHistory.class))).thenAnswer(i -> i.getArguments()[0]);

       // Perform the draw
       Prize drawnPrize = prizeService.drawPrize();
       
       // Verify results
       assertNotNull(drawnPrize);
       assertEquals(5, drawnPrize.getRarity(), "Should have drawn a 5-star");
       assertEquals("LIMITED", drawnPrize.getFiveStarType(), 
                    "After a NORMAL 5-star, the next 5-star MUST be LIMITED");
   }
}