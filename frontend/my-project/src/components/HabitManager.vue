<template>
  <div class="habit-manager">
    <h1>习惯管理</h1>
    
    <!-- 创建新习惯表单 -->
    <div class="form-container">
      <h2>新建习惯</h2>
      <form @submit.prevent="createHabit" class="habit-form">
        <div class="form-group">
          <label for="habitName">习惯名称</label>
          <input 
            id="habitName" 
            v-model="newHabit.name" 
            type="text" 
            placeholder="例如：跑步、阅读、冥想..." 
            required
          />
        </div>
        
        <div class="form-group">
          <label for="habitDescription">描述（可选）</label>
          <textarea 
            id="habitDescription"
            v-model="newHabit.description"
            placeholder="描述这个习惯的详情..."
          ></textarea>
        </div>
        
        <div class="form-group">
          <label for="baseReward">基础奖励点数</label>
          <input 
            id="baseReward" 
            v-model.number="newHabit.baseRewardPoints" 
            type="number" 
            min="1" 
            required
          />
        </div>
        
        <div class="form-row">
          <div class="form-group half">
            <label for="threshold1">连续天数阈值1</label>
            <input 
              id="threshold1" 
              v-model.number="newHabit.consecutiveDaysThreshold1" 
              type="number" 
              min="2"
              placeholder="例如：3"
            />
          </div>
          
          <div class="form-group half">
            <label for="bonus1">额外奖励1</label>
            <input 
              id="bonus1" 
              v-model.number="newHabit.bonusPoints1" 
              type="number" 
              min="1"
              placeholder="例如：10"
            />
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group half">
            <label for="threshold2">连续天数阈值2</label>
            <input 
              id="threshold2" 
              v-model.number="newHabit.consecutiveDaysThreshold2" 
              type="number" 
              min="2"
              placeholder="例如：7"
            />
          </div>
          
          <div class="form-group half">
            <label for="bonus2">额外奖励2</label>
            <input 
              id="bonus2" 
              v-model.number="newHabit.bonusPoints2" 
              type="number" 
              min="1"
              placeholder="例如：30"
            />
          </div>
        </div>
        
        <!-- 惩罚设置部分 -->
        <div class="penalty-section">
          <div class="form-group">
            <div class="checkbox-group">
              <input 
                id="enablePenalty" 
                v-model="newHabit.enablePenalty" 
                type="checkbox"
              />
              <label for="enablePenalty">启用未完成惩罚</label>
            </div>
          </div>
          
          <div class="form-group" v-if="newHabit.enablePenalty">
            <label for="penaltyPoints">惩罚点数（未完成时扣除）</label>
            <input 
              id="penaltyPoints" 
              v-model.number="newHabit.penaltyPoints" 
              type="number" 
              min="1"
              placeholder="例如：10"
              required
            />
          </div>
        </div>
        
        <div class="form-actions">
          <button type="submit" class="btn btn-primary">创建习惯</button>
          <button type="button" class="btn btn-secondary" @click="resetForm">重置</button>
        </div>
      </form>
    </div>
    
    <!-- 习惯列表 -->
    <div class="habits-container">
      <h2>我的习惯</h2>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="habits.length === 0" class="no-habits">
        还没有创建任何习惯。开始创建你的第一个习惯吧！
      </div>
      <div v-else class="habits-list">
        <div v-for="habit in habits" :key="habit.id" class="habit-card">
          <div class="habit-header">
            <h3>{{ habit.name }}</h3>
            <div class="habit-actions">
              <button @click="editHabit(habit)" class="action-btn edit-btn">编辑</button>
              <button @click="deleteHabit(habit.id)" class="action-btn delete-btn">删除</button>
            </div>
          </div>
          
          <div class="habit-description">{{ habit.description || '无描述' }}</div>
          
          <div class="habit-rewards">
            <div class="reward-item">
              <span class="reward-label">基础奖励:</span>
              <span class="reward-value">{{ habit.baseRewardPoints }} 点</span>
            </div>
            
            <div v-if="habit.consecutiveDaysThreshold1" class="reward-item">
              <span class="reward-label">连续 {{ habit.consecutiveDaysThreshold1 }} 天:</span>
              <span class="reward-value">+{{ habit.bonusPoints1 }} 点</span>
            </div>
            
            <div v-if="habit.consecutiveDaysThreshold2" class="reward-item">
              <span class="reward-label">连续 {{ habit.consecutiveDaysThreshold2 }} 天:</span>
              <span class="reward-value">+{{ habit.bonusPoints2 }} 点</span>
            </div>
            
            <div v-if="habit.enablePenalty" class="reward-item penalty">
              <span class="reward-label">未完成惩罚:</span>
              <span class="penalty-value">-{{ habit.penaltyPoints }} 点</span>
            </div>
          </div>
          
          <div class="habit-calendar">
            <div class="calendar-header">
              <button @click="changeMonth(habit.id, -1)" class="calendar-nav-btn">&lt;</button>
              <span>{{ currentMonths[habit.id] ? formatYearMonth(currentMonths[habit.id]) : '' }}</span>
              <button @click="changeMonth(habit.id, 1)" class="calendar-nav-btn">&gt;</button>
            </div>
            
            <div class="calendar-grid">
              <div v-for="day in ['日', '一', '二', '三', '四', '五', '六']" 
                  :key="day" 
                  class="calendar-day-header">
                {{ day }}
              </div>
              
              <div v-for="date in getCalendarDates(habit.id)" 
                  :key="date.toISOString()" 
                  class="calendar-day" 
                  :class="{
                    'current-month': isSameMonth(date, currentMonths[habit.id]),
                    'completed': isDateCompleted(habit, date),
                    'future': isFutureDate(date),
                    'today': isToday(date)
                  }"
                  @click="toggleHabitCompletion(habit, date)">
                {{ date.getDate() }}
              </div>
            </div>
            
            <div class="calendar-actions" v-if="habit.enablePenalty">
              <button 
                @click="applyPenaltyForDate(habit)" 
                class="penalty-btn"
                title="手动执行未完成惩罚"
              >
                手动执行惩罚
              </button>
            </div>
          </div>
          
          <div class="streak-info">
            <div class="current-streak">当前连续: <span>{{ getCurrentStreak(habit) }} 天</span></div>
            <div class="longest-streak">最长连续: <span>{{ getLongestStreak(habit) }} 天</span></div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑习惯对话框 -->
    <div v-if="showEditDialog" class="edit-dialog-overlay">
      <div class="edit-dialog">
        <h2>编辑习惯</h2>
        <form @submit.prevent="updateHabit" class="habit-form">
          <div class="form-group">
            <label for="editName">习惯名称</label>
            <input 
              id="editName" 
              v-model="editingHabit.name" 
              type="text" 
              required
            />
          </div>
          
          <div class="form-group">
            <label for="editDescription">描述（可选）</label>
            <textarea 
              id="editDescription"
              v-model="editingHabit.description"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label for="editBaseReward">基础奖励点数</label>
            <input 
              id="editBaseReward" 
              v-model.number="editingHabit.baseRewardPoints" 
              type="number" 
              min="1" 
              required
            />
          </div>
          
          <div class="form-row">
            <div class="form-group half">
              <label for="editThreshold1">连续天数阈值1</label>
              <input 
                id="editThreshold1" 
                v-model.number="editingHabit.consecutiveDaysThreshold1" 
                type="number" 
                min="2"
              />
            </div>
            
            <div class="form-group half">
              <label for="editBonus1">额外奖励1</label>
              <input 
                id="editBonus1" 
                v-model.number="editingHabit.bonusPoints1" 
                type="number" 
                min="1"
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group half">
              <label for="editThreshold2">连续天数阈值2</label>
              <input 
                id="editThreshold2" 
                v-model.number="editingHabit.consecutiveDaysThreshold2" 
                type="number" 
                min="2"
              />
            </div>
            
            <div class="form-group half">
              <label for="editBonus2">额外奖励2</label>
              <input 
                id="editBonus2" 
                v-model.number="editingHabit.bonusPoints2" 
                type="number" 
                min="1"
              />
            </div>
          </div>
          
          <!-- 惩罚设置部分 -->
          <div class="penalty-section">
            <div class="form-group">
              <div class="checkbox-group">
                <input 
                  id="editEnablePenalty" 
                  v-model="editingHabit.enablePenalty" 
                  type="checkbox"
                />
                <label for="editEnablePenalty">启用未完成惩罚</label>
              </div>
            </div>
            
            <div class="form-group" v-if="editingHabit.enablePenalty">
              <label for="editPenaltyPoints">惩罚点数（未完成时扣除）</label>
              <input 
                id="editPenaltyPoints" 
                v-model.number="editingHabit.penaltyPoints" 
                type="number" 
                min="1"
                required
              />
            </div>
          </div>
          
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-secondary" @click="closeEditDialog">取消</button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- 手动执行惩罚对话框 -->
    <div v-if="showPenaltyDialog" class="edit-dialog-overlay">
      <div class="edit-dialog">
        <h2>手动执行惩罚</h2>
        <form @submit.prevent="confirmApplyPenalty" class="habit-form">
          <div class="form-group">
            <label for="penaltyDate">选择日期</label>
            <input 
              id="penaltyDate" 
              v-model="penaltyDate" 
              type="date"
              :max="today"
              required
            />
          </div>
          
          <p class="penalty-warning">
            注意：将为习惯【{{ penaltyHabit?.name }}】在选定日期执行惩罚，扣除 {{ penaltyHabit?.penaltyPoints }} 计划点。
            <br>
            此操作不可撤销，请确认该日期确实未完成习惯。
          </p>
          
          <div class="form-actions">
            <button type="submit" class="btn btn-danger">确认执行惩罚</button>
            <button type="button" class="btn btn-secondary" @click="closePenaltyDialog">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'HabitManager',
  data() {
    return {
      habits: [],
      loading: true,
      newHabit: {
        name: '',
        description: '',
        baseRewardPoints: 30,
        consecutiveDaysThreshold1: 3,
        bonusPoints1: 10,
        consecutiveDaysThreshold2: 7,
        bonusPoints2: 30,
        enablePenalty: false,
        penaltyPoints: 10
      },
      currentMonths: {},
      showEditDialog: false,
      editingHabit: null,
      // 新增惩罚相关数据
      showPenaltyDialog: false,
      penaltyHabit: null,
      penaltyDate: null,
      today: new Date().toISOString().split('T')[0]
    };
  },
  created() {
    this.fetchHabits();
  },
  methods: {
    async fetchHabits() {
      this.loading = true;
      try {
        const response = await axios.get('/api/habits');
        this.habits = response.data;
        
        // 初始化每个习惯的当前月份为当前日期
        const monthsObj = {};
        this.habits.forEach(habit => {
          monthsObj[habit.id] = new Date();
        });
        this.currentMonths = monthsObj; // 使用新对象更新
      } catch (error) {
        console.error('加载习惯失败', error);
      } finally {
        this.loading = false;
      }
    },
    
    async createHabit() {
      try {
        const response = await axios.post('/api/habits', this.newHabit);
        this.habits.push(response.data);
        
        // 创建新对象来更新
        this.currentMonths = { 
          ...this.currentMonths, 
          [response.data.id]: new Date() 
        };
        
        this.resetForm();
      } catch (error) {
        console.error('创建习惯失败', error);
      }
    },
    
    resetForm() {
      this.newHabit = {
        name: '',
        description: '',
        baseRewardPoints: 30,
        consecutiveDaysThreshold1: 3,
        bonusPoints1: 10,
        consecutiveDaysThreshold2: 7,
        bonusPoints2: 30,
        enablePenalty: false,
        penaltyPoints: 10
      };
    },
    
    async deleteHabit(id) {
      if (!confirm('确定要删除这个习惯吗？所有相关记录都将被永久删除。')) {
        return;
      }
      
      try {
        await axios.delete(`/api/habits/${id}`);
        this.habits = this.habits.filter(h => h.id !== id);
      } catch (error) {
        console.error('删除习惯失败', error);
      }
    },
    
    editHabit(habit) {
      this.editingHabit = { ...habit };
      this.showEditDialog = true;
    },
    
    closeEditDialog() {
      this.showEditDialog = false;
      this.editingHabit = null;
    },
    
    async updateHabit() {
      try {
        const response = await axios.put(`/api/habits/${this.editingHabit.id}`, this.editingHabit);
        const index = this.habits.findIndex(h => h.id === response.data.id);
        if (index !== -1) {
          this.habits.splice(index, 1, response.data);
        }
        this.closeEditDialog();
      } catch (error) {
        console.error('更新习惯失败', error);
      }
    },
    
    // 日历相关方法
    formatYearMonth(date) {
      // 添加空值检查
      if (!date) return '';
      return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' });
    },
    
    changeMonth(habitId, delta) {
      // 添加空值检查
      if (!this.currentMonths[habitId]) {
        this.currentMonths[habitId] = new Date();
      }
      
      const currentMonth = new Date(this.currentMonths[habitId]);
      currentMonth.setMonth(currentMonth.getMonth() + delta);
      
      // 创建新对象来更新
      const updatedMonths = { ...this.currentMonths };
      updatedMonths[habitId] = currentMonth;
      this.currentMonths = updatedMonths;
    },
    
    getCalendarDates(habitId) {
      // 添加空值检查
      if (!this.currentMonths[habitId]) {
        this.currentMonths = { 
          ...this.currentMonths, 
          [habitId]: new Date() 
        };
      }
      
      const month = this.currentMonths[habitId];
      const year = month.getFullYear();
      const monthIndex = month.getMonth();
      
      // 获取当月第一天
      const firstDay = new Date(year, monthIndex, 1);
      
      // 获取当月最后一天
      const lastDay = new Date(year, monthIndex + 1, 0);
      
      // 获取第一天是星期几 (0 = 周日, 1 = 周一, ...)
      const firstDayOfWeek = firstDay.getDay();
      
      // 上个月需要显示的天数
      const daysFromPrevMonth = firstDayOfWeek;
      
      // 创建日历网格的日期数组 (6周，每周7天)
      const dates = [];
      
      // 添加上个月的日期
      for (let i = daysFromPrevMonth - 1; i >= 0; i--) {
        const prevDate = new Date(year, monthIndex, -i);
        dates.push(prevDate);
      }
      
      // 添加当月的日期
      for (let i = 1; i <= lastDay.getDate(); i++) {
        const currentDate = new Date(year, monthIndex, i);
        dates.push(currentDate);
      }
      
      // 添加下个月的日期，直到填满6*7=42个格子
      const remainingDays = 42 - dates.length;
      for (let i = 1; i <= remainingDays; i++) {
        const nextDate = new Date(year, monthIndex + 1, i);
        dates.push(nextDate);
      }
      
      return dates;
    },
    
    isSameMonth(date, refDate) {
      return date.getMonth() === refDate.getMonth() && 
             date.getFullYear() === refDate.getFullYear();
    },
    
    isDateCompleted(habit, date) {
      const dateString = date.toISOString().split('T')[0]; // 格式化为 YYYY-MM-DD
      return habit.completionDates.some(d => d === dateString);
    },
    
    isFutureDate(date) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return date > today;
    },
    
    isToday(date) {
      const today = new Date();
      return date.getDate() === today.getDate() && 
             date.getMonth() === today.getMonth() && 
             date.getFullYear() === today.getFullYear();
    },
    
    async toggleHabitCompletion(habit, date) {
      // 不允许标记未来日期
      if (this.isFutureDate(date)) {
        return;
      }
      
      const dateString = date.toISOString().split('T')[0]; // 格式化为 YYYY-MM-DD
      
      try {
        if (this.isDateCompleted(habit, date)) {
          // 取消标记完成
          await axios.post(`/api/habits/${habit.id}/uncomplete?date=${dateString}`);
        } else {
          // 标记为完成
          await axios.post(`/api/habits/${habit.id}/complete?date=${dateString}`);
        }
        // 重新加载习惯数据
        this.fetchHabits();
      } catch (error) {
        console.error('更新习惯完成状态失败', error);
      }
    },
    
    // 计算连续天数信息
    getCurrentStreak(habit) {
      if (!habit.completionDates || habit.completionDates.length === 0) {
        return 0;
      }
      
      // 将完成日期转换为 Date 对象数组并按日期排序
      const dates = habit.completionDates
        .map(d => new Date(d))
        .sort((a, b) => b - a); // 降序排序
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      // 检查最近的完成日期是否是今天或昨天
      const mostRecentDate = dates[0];
      mostRecentDate.setHours(0, 0, 0, 0);
      
      const yesterday = new Date(today);
      yesterday.setDate(today.getDate() - 1);
      
      // 如果最近的完成日期不是今天或昨天，没有当前连续记录
      if (!(mostRecentDate.getTime() === today.getTime() || 
            mostRecentDate.getTime() === yesterday.getTime())) {
        return 0;
      }
      
      // 计算当前连续天数
      let streak = 1;
      let currentDate = new Date(mostRecentDate);
      
      for (let i = 1; i < dates.length; i++) {
        const prevDate = new Date(currentDate);
        prevDate.setDate(prevDate.getDate() - 1);
        
        // 查找是否有前一天的记录
        const matchingDate = dates[i];
        matchingDate.setHours(0, 0, 0, 0);
        
        if (matchingDate.getTime() === prevDate.getTime()) {
          streak++;
          currentDate = new Date(matchingDate);
        } else {
          break;
        }
      }
      
      return streak;
    },
    
    getLongestStreak(habit) {
      if (!habit.completionDates || habit.completionDates.length === 0) {
        return 0;
      }
      
      // 将完成日期转换为 Date 对象并排序
      const sortedDates = habit.completionDates
        .map(d => new Date(d))
        .sort((a, b) => a - b); // 升序排序
      
      let longestStreak = 1;
      let currentStreak = 1;
      
      for (let i = 1; i < sortedDates.length; i++) {
        const currentDate = sortedDates[i];
        const prevDate = sortedDates[i - 1];
        
        // 检查日期是否连续
        const diffTime = currentDate.getTime() - prevDate.getTime();
        const diffDays = diffTime / (1000 * 3600 * 24);
        
        if (Math.round(diffDays) === 1) {
          currentStreak++;
        } else if (Math.round(diffDays) > 1) {
          // 重置当前连续计数
          currentStreak = 1;
        }
        
        longestStreak = Math.max(longestStreak, currentStreak);
      }
      
      return longestStreak;
    },
    
    // 打开惩罚对话框
    applyPenaltyForDate(habit) {
      this.penaltyHabit = habit;
      this.penaltyDate = this.today;
      this.showPenaltyDialog = true;
    },
    
    // 关闭惩罚对话框
    closePenaltyDialog() {
      this.showPenaltyDialog = false;
      this.penaltyHabit = null;
      this.penaltyDate = null;
    },
    
    // 确认执行惩罚
    async confirmApplyPenalty() {
      if (!this.penaltyHabit || !this.penaltyDate) {
        return;
      }
      
      try {
        const response = await axios.post(
          `/api/habits/${this.penaltyHabit.id}/apply-penalty?date=${this.penaltyDate}`
        );
        
        if (response.data.status === 'success') {
          alert(`惩罚已成功执行，扣除了 ${this.penaltyHabit.penaltyPoints} 计划点`);
          this.closePenaltyDialog();
        } else {
          alert(`执行惩罚失败: ${response.data.message}`);
        }
      } catch (error) {
        console.error('执行惩罚失败', error);
        alert(`执行惩罚失败: ${error.response?.data?.message || error.message}`);
      }
    }
  }
};
</script>

<style scoped>
.habit-manager {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.habit-manager h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-container {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-container h2 {
  margin-top: 0;
  color: #2196F3;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 10px;
}

.habit-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-group.half {
  flex: 1;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.form-group input,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.2s;
}

.btn-primary {
  background-color: #2196F3;
  color: white;
}

.btn-primary:hover {
  background-color: #1976D2;
}

.btn-secondary {
  background-color: #e0e0e0;
  color: #333;
}

.btn-secondary:hover {
  background-color: #d0d0d0;
}

.btn-danger {
  background-color: #f44336;
  color: white;
}

.btn-danger:hover {
  background-color: #d32f2f;
}

.habits-container {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.habits-container h2 {
  margin-top: 0;
  color: #2196F3;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 10px;
}

.loading, .no-habits {
  text-align: center;
  padding: 20px;
  color: #757575;
}

.habits-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.habit-card {
  background-color: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.habit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.habit-header h3 {
  margin: 0;
  color: #333;
}

.habit-actions {
  display: flex;
  gap: 5px;
}

.action-btn {
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.edit-btn {
  background-color: #2196F3;
  color: white;
}

.edit-btn:hover {
  background-color: #1976D2;
}

.delete-btn {
  background-color: #f44336;
  color: white;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.habit-description {
  color: #757575;
  margin-bottom: 10px;
  font-size: 14px;
}

.habit-rewards {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.reward-item {
  display: flex;
  justify-content: space-between;
  margin: 5px 0;
}

.reward-label {
  color: #555;
}

.reward-value {
  font-weight: bold;
  color: #2196F3;
}

.reward-item.penalty {
  border-top: 1px dashed #e0e0e0;
  padding-top: 5px;
  margin-top: 5px;
}

.penalty-value {
  font-weight: bold;
  color: #f44336;
}

.habit-calendar {
  margin-top: 15px;
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.calendar-nav-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #2196F3;
  cursor: pointer;
  padding: 5px 10px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.calendar-day-header {
  text-align: center;
  font-weight: bold;
  color: #757575;
  padding: 5px;
}

.calendar-day {
  text-align: center;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
}

.calendar-day:not(.current-month) {
  color: #bdbdbd;
  background-color: #f9f9f9;
}

.calendar-day.completed {
  background-color: #81c784;
  color: white;
}

.calendar-day.future {
  background-color: #f5f5f5;
  cursor: not-allowed;
  color: #bdbdbd;
}

.calendar-day.today {
  border: 2px solid #2196F3;
  font-weight: bold;
}

.calendar-actions {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.penalty-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.penalty-btn:hover {
  background-color: #d32f2f;
}

.streak-info {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.current-streak, .longest-streak {
  font-size: 14px;
  color: #555;
}

.current-streak span, .longest-streak span {
  font-weight: bold;
  color: #2196F3;
}

.edit-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.edit-dialog {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.edit-dialog h2 {
  margin-top: 0;
  color: #2196F3;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 10px;
}

.penalty-section {
  border-top: 1px dashed #e0e0e0;
  margin-top: 15px;
  padding-top: 15px;
}

.checkbox-group {
  display: flex;
  align-items: center;
}

.checkbox-group input[type="checkbox"] {
  margin-right: 10px;
  transform: scale(1.2);
}

.penalty-warning {
  background-color: #fff8e1;
  border-left: 4px solid #ffc107;
  padding: 10px;
  margin: 15px 0;
  color: #d32f2f;
  font-size: 14px;
  line-height: 1.5;
}
</style>
