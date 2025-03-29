//package com.example.lottery.service;
//
//import com.example.lottery.entity.Prize;
//import com.example.lottery.repository.PrizeRepository;
//import com.example.lottery.LotteryContext;
//import com.example.lottery.LotteryState;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//@DataJpaTest
//@Transactional
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ActiveProfiles("test")
//class PrizeServiceTest {
//
//    @Mock
//    private PrizeRepository prizeRepository;
//
//    @Mock
//    private LotteryContext lotteryContext;
//
//    @Mock
//    private LotteryState lotteryState;
//
//    @InjectMocks
//    private PrizeService prizeService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testGetAllPrizes() {
//        Prize prize1 = new Prize("Prize 1", 3, null, "Description 1");
//        Prize prize2 = new Prize("Prize 2", 4, null, "Description 2");
//        when(prizeRepository.findAll()).thenReturn(Arrays.asList(prize1, prize2));
//
//        List<Prize> prizes = prizeService.getAllPrizes();
//
//        assertEquals(2, prizes.size());
//        assertEquals("Prize 1", prizes.get(0).getName());
//        assertEquals("Prize 2", prizes.get(1).getName());
//    }
//
//    @Test
//    void testSavePrize() {
//        Prize prize = new Prize("Prize 1", 3, null, "Description 1");
//        when(prizeRepository.save(prize)).thenReturn(prize);
//
//        Prize savedPrize = prizeService.savePrize(prize);
//
//        assertNotNull(savedPrize);
//        assertEquals("Prize 1", savedPrize.getName());
//    }
//
//    // src/test/java/com/example/lottery/service/PrizeServiceTest.java
//@Test
//void testDrawPrize() {
//    Prize prize = new Prize("Prize 1", 3, null, "Description 1");
//    prize.setIsRepeatable(false); // Set the prize to be non-repeatable
//    when(lotteryContext.executeDraw(lotteryState)).thenReturn(prize);
//
//    // Draw the prize for the first time
//    Prize drawnPrize = prizeService.drawPrize();
//    assertNotNull(drawnPrize);
//    assertEquals("Prize 1", drawnPrize.getName());
//    verify(prizeRepository, times(1)).delete(prize);
//
//    // Attempt to draw the prize again
//    when(lotteryContext.executeDraw(lotteryState)).thenReturn(null); // No prize should be drawn
//    Prize secondDrawnPrize = prizeService.drawPrize();
//    assertNull(secondDrawnPrize);
//}
//
//
//}