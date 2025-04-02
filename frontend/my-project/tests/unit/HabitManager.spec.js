import { shallowMount, mount } from '@vue/test-utils'
import HabitManager from '@/components/HabitManager.vue'
import axios from 'axios'

// Mock axios
jest.mock('axios')

describe('HabitManager.vue', () => {
  let wrapper;
  
  beforeEach(() => {
    // Mock axios responses
    axios.get.mockResolvedValue({ data: [] });
    axios.post.mockResolvedValue({ data: {} });
    
    // Create wrapper
    wrapper = shallowMount(HabitManager);
  });
  
  afterEach(() => {
    jest.clearAllMocks();
  });
  
  it('fetches habits on creation', () => {
    expect(axios.get).toHaveBeenCalledWith('/api/habits');
  });
  
  it('initializes with default values', () => {
    expect(wrapper.vm.habits).toEqual([]);
    expect(wrapper.vm.loading).toBe(true);
    expect(wrapper.vm.newHabit.name).toBe('');
    expect(wrapper.vm.newHabit.baseRewardPoints).toBe(30);
  });
  
  it('creates a new habit', async () => {
    const testHabit = {
      name: 'Test Habit',
      description: 'Test Description',
      baseRewardPoints: 40
    };
    
    axios.post.mockResolvedValue({ data: { ...testHabit, id: 1 } });
    
    // Set form values
    wrapper.vm.newHabit = testHabit;
    
    // Call createHabit method
    await wrapper.vm.createHabit();
    
    // Check axios call
    expect(axios.post).toHaveBeenCalledWith('/api/habits', testHabit);
    
    // Check if the habit was added to the list
    expect(wrapper.vm.habits).toContainEqual({ ...testHabit, id: 1 });
  });
  
  it('toggles habit completion', async () => {
    const testHabit = {
      id: 1,
      name: 'Test Habit',
      completionDates: []
    };
    
    const testDate = new Date(2025, 3, 15); // April 15, 2025
    
    // Toggle completion (mark as completed)
    await wrapper.vm.toggleHabitCompletion(testHabit, testDate);
    
    // Check axios call - date should be formatted as 2025-04-15
    expect(axios.post).toHaveBeenCalledWith('/api/habits/1/complete?date=2025-04-15');
  });
  
  it('formats date correctly', () => {
    const testDate = new Date(2025, 3, 15); // April 15, 2025
    
    // Format date for input
    const result = wrapper.vm.formatDateForInput(testDate);
    
    // Should return YYYY-MM-DD format
    expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });
});
