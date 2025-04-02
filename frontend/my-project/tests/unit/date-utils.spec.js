import { formatDateTimeForInput, isDateCompleted, isFutureDate, isToday } from '@/utils/date-utils';

// Create a mock date-utils.js file for testing
// In actual implementation, move these functions to a separate utility file

describe('Date utility functions', () => {
  it('formats date time for input correctly', () => {
    const testDate = new Date(2025, 3, 15, 14, 30); // April 15, 2025, 14:30
    
    const result = formatDateTimeForInput(testDate);
    
    expect(result).toBe('2025-04-15T14:30');
  });
  
  it('checks if date is completed', () => {
    const habit = {
      completionDates: ['2025-04-15', '2025-04-16']
    };
    
    // Test with date that is completed
    const completedDate = new Date(2025, 3, 15); // April 15, 2025
    expect(isDateCompleted(habit, completedDate)).toBe(true);
    
    // Test with date that is not completed
    const notCompletedDate = new Date(2025, 3, 14); // April 14, 2025
    expect(isDateCompleted(habit, notCompletedDate)).toBe(false);
  });
  
  it('checks if date is in the future', () => {
    // Mock current date as April 15, 2025
    const realDateNow = Date.now;
    Date.now = jest.fn(() => new Date(2025, 3, 15).getTime());
    
    // Test with future date
    const futureDate = new Date(2025, 3, 16); // April 16, 2025
    expect(isFutureDate(futureDate)).toBe(true);
    
    // Test with past date
    const pastDate = new Date(2025, 3, 14); // April 14, 2025
    expect(isFutureDate(pastDate)).toBe(false);
    
    // Restore original Date.now
    Date.now = realDateNow;
  });
  
  it('checks if date is today', () => {
    // Mock current date as April 15, 2025
    const realDateNow = Date.now;
    Date.now = jest.fn(() => new Date(2025, 3, 15).getTime());
    
    // Test with today
    const today = new Date(2025, 3, 15); // April 15, 2025
    expect(isToday(today)).toBe(true);
    
    // Test with another date
    const anotherDate = new Date(2025, 3, 14); // April 14, 2025
    expect(isToday(anotherDate)).toBe(false);
    
    // Restore original Date.now
    Date.now = realDateNow;
  });
});
