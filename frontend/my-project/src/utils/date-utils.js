/**
 * Formats a Date object to the format YYYY-MM-DDThh:mm for input fields.
 * 
 * @param {Date} date - Date object to format
 * @returns {string} Formatted date string
 */
export function formatDateTimeForInput(date) {
  if (!date) return '';
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

/**
 * Checks if a given date is marked as completed in a habit.
 * 
 * @param {Object} habit - Habit object
 * @param {Date} date - Date to check
 * @returns {boolean} True if the date is marked as completed
 */
export function isDateCompleted(habit, date) {
  if (!habit || !habit.completionDates || !date) return false;
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const dateString = `${year}-${month}-${day}`;
  
  return habit.completionDates.includes(dateString);
}

/**
 * Checks if a date is in the future.
 * 
 * @param {Date} date - Date to check
 * @returns {boolean} True if the date is in the future
 */
export function isFutureDate(date) {
  if (!date) return false;
  
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date > today;
}

/**
 * Checks if a date is today.
 * 
 * @param {Date} date - Date to check
 * @returns {boolean} True if the date is today
 */
export function isToday(date) {
  if (!date) return false;
  
  const today = new Date();
  return date.getDate() === today.getDate() && 
         date.getMonth() === today.getMonth() && 
         date.getFullYear() === today.getFullYear();
}
