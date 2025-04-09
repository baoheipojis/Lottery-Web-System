// package com.example.lottery.controller;

// import com.example.lottery.LotteryContext;
// import com.example.lottery.entity.Prize;
// import com.example.lottery.repository.PrizeRepository;
// import com.example.lottery.service.PrizeService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import java.util.Arrays;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// class PrizeControllerUnitTest {

//     @Mock
//     private PrizeService prizeService;

//     @Mock
//     private PrizeRepository prizeRepository;

//     @InjectMocks
//     private PrizeController prizeController;

//     private Prize testPrize;
//     private Prize fiveStarPrize;
//     private Prize fourStarPrize;
//     private Prize threeStarPrize;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
        
//         // Create test data
//         testPrize = new Prize();
//         testPrize.setId(1L);
//         testPrize.setName("Test Prize");
//         testPrize.setRarity(4);
//         testPrize.setDescription("Description for test prize");
//         testPrize.setIsRepeatable(true);
//         testPrize.setEnabled(true);
        
//         // Setup prizes of different rarities for probability testing
//         fiveStarPrize = new Prize();
//         fiveStarPrize.setId(2L);
//         fiveStarPrize.setName("5★ Prize");
//         fiveStarPrize.setRarity(5);
//         fiveStarPrize.setFiveStarType("normal");
//         fiveStarPrize.setIsRepeatable(true);
//         fiveStarPrize.setEnabled(true);
        
//         fourStarPrize = new Prize();
//         fourStarPrize.setId(3L);
//         fourStarPrize.setName("4★ Prize");
//         fourStarPrize.setRarity(4);
//         fourStarPrize.setIsRepeatable(true);
//         fourStarPrize.setEnabled(true);
        
//         threeStarPrize = new Prize();
//         threeStarPrize.setId(4L);
//         threeStarPrize.setName("3★ Prize");
//         threeStarPrize.setRarity(3);
//         threeStarPrize.setIsRepeatable(true);
//         threeStarPrize.setEnabled(true);
//     }

//     @Test
//     void getAllPrizes_ShouldReturnListOfPrizes() {

//     @Test
//     void drawPrize_InsufficientPoints_ShouldReturnErrorResponse() {
//         // Arrange
//         when(prizeService.drawPrize()).thenThrow(new LotteryContext.InsufficientPlanPointsException("Not enough points"));

//         // Act
//         ResponseEntity<?> response = prizeController.drawPrize();

//         // Assert
//         assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//         Map<String, String> responseBody = (Map<String, String>) response.getBody();
//         assertEquals("Not enough points", responseBody.get("message"));
//         assertEquals("INSUFFICIENT_POINTS", responseBody.get("errorType"));
//         verify(prizeService, times(1)).drawPrize();
//     }

//     @Test
//     void addPrize_ValidPrize_ShouldReturnSavedPrize() {
//         // Arrange
//         when(prizeService.savePrize(any(Prize.class))).thenReturn(testPrize);

//         // Act
//         ResponseEntity<?> response = prizeController.addPrize(testPrize);

//         // Assert
//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         assertEquals(testPrize, response.getBody());
//         verify(prizeService, times(1)).savePrize(any(Prize.class));
//     }

//     @Test
//     void deletePrize_ExistingId_ShouldReturnSuccessMessage() {
//         // Arrange
//         doNothing().when(prizeService).deletePrize(anyLong());
        
//         // Act
//         ResponseEntity<?> response = prizeController.deletePrize(1L);
        
//         // Assert
//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         Map<String, String> responseBody = (Map<String, String>) response.getBody();
//         assertEquals("Prize deleted successfully", responseBody.get("message"));
//         assertEquals("success", responseBody.get("status"));
//         verify(prizeService, times(1)).deletePrize(1L);
//     }
// }
// }