<template>
  <div class="plan-management">
    <h1>{{ $t('plans.title') }}</h1>
    
    <div class="plan-tree">
      <!-- Message notification -->
      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
      
      <!-- Add new root plan -->
      <div class="add-plan-form">
        <h2>{{ $t('plans.addNewPlan') }}</h2>
        <div class="form-group">
          <label>{{ $t('plans.planTitle') }}</label>
          <input v-model="newPlan.title" type="text" :placeholder="$t('plans.inputTitle')" required />
        </div>
        
        <div class="form-group">
          <label>{{ $t('plans.description') }}</label>
          <textarea v-model="newPlan.description" :placeholder="$t('plans.inputDescription')"></textarea>
        </div>
        
        <div class="form-group">
          <label>{{ $t('plans.expectedCompletion') }}</label>
          <input v-model="newPlan.expectedCompletionTime" type="datetime-local" required />
          <div class="time-buttons">
            <button type="button" @click="setTimeTodayEnd" class="time-btn">{{ $t('plans.todayEnd') }}</button>
            <button type="button" @click="setTimeTomorrowEnd" class="time-btn">{{ $t('plans.tomorrowEnd') }}</button>
            <button type="button" @click="setTimeWeekendEnd" class="time-btn">{{ $t('plans.weekendEnd') }}</button>
            <button type="button" @click="setTimeMonthEnd" class="time-btn">{{ $t('plans.monthEnd') }}</button>
          </div>
        </div>
        
        <div class="form-group">
          <label>{{ $t('plans.rewardPoints') }}</label>
          <input v-model.number="newPlan.rewardPoints" type="number" min="1" required />
        </div>
        
        <div class="form-group">
          <label>{{ $t('plans.repeatSettings') }}</label>
          <div class="repeat-options">
            <div class="checkbox-group">
              <input type="checkbox" id="repeatable" v-model="newPlan.repeatable" />
              <label for="repeatable">{{ $t('plans.repeatSettings') }}</label>
            </div>
            
            <div v-if="newPlan.repeatable" class="repeat-settings">
              <div class="form-group">
                <label>{{ $t('plans.repeatType') }}</label>
                <select v-model="newPlan.repeatType" required>
                  <option value="DAILY">{{ $t('plans.daily') }}</option>
                  <option value="WEEKLY">{{ $t('plans.weekly') }}</option>
                  <option value="MONTHLY">{{ $t('plans.monthly') }}</option>
                </select>
              </div>
              
              <div class="form-group">
                <label>{{ $t('plans.repeatInterval') }}</label>
                <input 
                  v-model.number="newPlan.repeatInterval" 
                  type="number" 
                  min="1" 
                  :placeholder="$t('plans.intervalUnit')"
                />
                <span class="interval-unit">
                  {{ repeatIntervalUnit }}
                </span>
              </div>
              
              <div class="form-group">
                <label>{{ $t('plans.endDate') }}</label>
                <input 
                  v-model="newPlan.repeatEndDate" 
                  type="datetime-local" 
                  :placeholder="$t('plans.optional')"
                />
              </div>
            </div>
          </div>
        </div>
        
        <button @click="addPlan" class="add-btn" :disabled="!isFormValid">{{ $t('plans.addRootPlan') }}</button>
      </div>
      
      <!-- Plan tree view -->
      <div class="plan-tree-container">
        <!-- Date filter section -->
        <div class="date-filter">
          <div class="filter-label">{{ $t('plans.filterByDate') }}：</div>
          <div class="date-buttons">
            <button @click="filterByDate('today')" :class="['date-btn', { active: dateFilter === 'today' }]">{{ $t('plans.today') }}</button>
            <button @click="filterByDate('tomorrow')" :class="['date-btn', { active: dateFilter === 'tomorrow' }]">{{ $t('plans.tomorrow') }}</button>
            <button @click="filterByDate('week')" :class="['date-btn', { active: dateFilter === 'week' }]">{{ $t('plans.thisWeek') }}</button>
            <button @click="filterByDate('next-week')" :class="['date-btn', { active: dateFilter === 'next-week' }]">{{ $t('plans.nextWeek') }}</button>
            <button @click="filterByDate('month')" :class="['date-btn', { active: dateFilter === 'month' }]">{{ $t('plans.thisMonth') }}</button>
            <button @click="filterByDate('next-month')" :class="['date-btn', { active: dateFilter === 'next-month' }]">{{ $t('plans.nextMonth') }}</button>
            <button @click="filterByDate('all')" :class="['date-btn', { active: dateFilter === 'all' }]">{{ $t('plans.all') }}</button>
          </div>
          <div class="custom-date">
            <label>{{ $t('plans.customDate') }}：</label>
            <input type="date" v-model="customDate" @change="filterByDate('custom')" />
          </div>
        </div>
        
        <h2>{{ $t('plans.plansList') }} <span class="hierarchy-hint">{{ $t('plans.hierarchyHint') }}</span></h2>
        
        <!-- Put the reordered filter tabs here -->
        <div class="filter-tabs">
          <button 
            :class="['tab-btn', { active: activeTab === 'ongoing' }]" 
            @click="activeTab = 'ongoing'"
          >
            {{ $t('plans.ongoing') }}
          </button>
          <button 
            :class="['tab-btn', { active: activeTab === 'all' }]" 
            @click="activeTab = 'all'"
          >
            {{ $t('plans.all') }}
          </button>
          <button 
            :class="['tab-btn', { active: activeTab === 'completed' }]" 
            @click="activeTab = 'completed'"
          >
            {{ $t('plans.completed') }}
          </button>
        </div>
        
        <!-- Plan items -->
        <ul class="plan-list">
          <li v-if="filteredRootPlans.length === 0" class="empty-message">
            {{ $t('plans.noPlan') }}
          </li>
          <template v-for="plan in filteredRootPlans" :key="plan.id">
            <plan-item 
              :plan="plan" 
              @complete="completePlan" 
              @uncomplete="uncompletePlan"
              @delete="deletePlan"
              @add-child="showAddChildForm"
              @update="updatePlan"
            />
          </template>
        </ul>
      </div>
      
      <!-- Add child plan modal -->
      <div v-if="showChildModal" class="modal-overlay">
        <div class="modal-content">
          <h3>{{ $t('plans.toChildPlan') }}: {{ selectedParent?.title }}</h3>
          
          <div class="form-group">
            <label>{{ $t('plans.planTitle') }}</label>
            <input v-model="newChildPlan.title" type="text" :placeholder="$t('plans.inputTitle')" />
          </div>
          
          <div class="form-group">
            <label>{{ $t('plans.description') }}</label>
            <textarea v-model="newChildPlan.description" :placeholder="$t('plans.inputDescription')"></textarea>
          </div>
          
          <div class="form-group">
            <label>{{ $t('plans.expectedCompletion') }}</label>
            <input v-model="newChildPlan.expectedCompletionTime" type="datetime-local" />
            <div class="time-buttons">
              <button type="button" @click="setChildTimeTodayEnd" class="time-btn">{{ $t('plans.todayEnd') }}</button>
              <button type="button" @click="setChildTimeTomorrowEnd" class="time-btn">{{ $t('plans.tomorrowEnd') }}</button>
              <button type="button" @click="setChildTimeWeekendEnd" class="time-btn">{{ $t('plans.weekendEnd') }}</button>
              <button type="button" @click="setChildTimeMonthEnd" class="time-btn">{{ $t('plans.monthEnd') }}</button>
            </div>
          </div>
          
          <div class="form-group">
            <label>{{ $t('plans.rewardPoints') }}</label>
            <input v-model.number="newChildPlan.rewardPoints" type="number" min="1" />
          </div>
          
          <div class="modal-actions">
            <button @click="cancelAddChild" class="cancel-btn">{{ $t('plans.cancel') }}</button>
            <button 
              @click="addChildPlan" 
              class="add-btn"
              :disabled="!isChildFormValid"
            >
              {{ $t('plans.addChildPlan') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import PlanItem from './PlanItem.vue';

export default {
  components: {
    PlanItem
  },
  data() {
    return {
      plans: [], // Initialize as an empty array
      activeTab: 'ongoing', // 修改默认标签为"ongoing"（进行中计划）
      message: '',
      messageType: 'info',
      newPlan: {
        title: '',
        description: '',
        expectedCompletionTime: '',
        rewardPoints: 100,
        repeatable: false,
        repeatType: 'DAILY',
        repeatInterval: 1,
        repeatEndDate: ''
      },
      showChildModal: false,
      selectedParent: null,
      newChildPlan: {
        title: '',
        description: '',
        expectedCompletionTime: '',
        rewardPoints: 50
      },
      dateFilter: 'all',
      customDate: this.formatDateForInput(new Date())
    };
  },
  computed: {
    isFormValid() {
      return this.newPlan.title && 
        this.newPlan.expectedCompletionTime && 
        this.newPlan.rewardPoints > 0;
    },
    isChildFormValid() {
      return this.newChildPlan.title && 
        this.newChildPlan.expectedCompletionTime && 
        this.newChildPlan.rewardPoints > 0;
    },
    filteredRootPlans() {
      return this.filterPlansRecursively(this.plans);
    },
    repeatIntervalUnit() {
      const repeatTypeMap = {
        'DAILY': this.$t('plans.days'),
        'WEEKLY': this.$t('plans.weeks'),
        'MONTHLY': this.$t('plans.months')
      };
      return repeatTypeMap[this.newPlan.repeatType] || '';
    },
    filteredPlans() {
      if (this.activeTab === 'ongoing') {
        return this.plans.filter(plan => !plan.completed);
      } else if (this.activeTab === 'completed') {
        return this.plans.filter(plan => plan.completed);
      } else {
        return this.plans;
      }
    }
  },
  created() {
    this.fetchPlans();
    this.setDefaultEndTimeToday(); // 设置默认完成时间为今天23:59
  },
  methods: {
    fetchPlans() {
      axios.get('/api/plans/root')
        .then(response => {
          // Ensure plans is always an array
          this.plans = Array.isArray(response.data) ? response.data : [];
          console.log('Fetched hierarchical plans:', this.plans); // For debugging
        })
        .catch(error => {
          console.error('Error fetching plans:', error);
          this.showMessage('获取计划列表失败', 'error');
          // Reset to empty array in case of error
          this.plans = [];
        });
    },
    
    addPlan() {
      if (!this.isFormValid) return;
      
      // 确保重复相关字段正确传递
      const planData = { ...this.newPlan };
      
      // 如果不是重复计划，将相关字段设为null
      if (!planData.repeatable) {
        planData.repeatType = null;
        planData.repeatInterval = null;
        planData.repeatEndDate = null;
      }
      
      console.log("Sending plan data:", planData);
      
      axios.post('/api/plans', planData)
        .then(response => {
          this.plans.push(response.data);
          this.resetNewPlanForm();
          this.showMessage('计划添加成功', 'success');
        })
        .catch(error => {
          console.error('Error adding plan:', error);
          this.showMessage('添加计划失败', 'error');
        });
    },
    
    completePlan(planId) {
      // 添加日志以便调试
      console.log("Completing plan:", planId);
      
      axios.post(`/api/plans/${planId}/complete`)
        .then(() => {
          this.fetchPlans(); // 刷新以获取更新的结构和新的重复计划
          this.showMessage('计划已完成，奖励计划点已添加', 'success');
        })
        .catch(error => {
          console.error('Error completing plan:', error);
          this.showMessage('完成计划失败', 'error');
        });
    },
    
    uncompletePlan(planId) {
      axios.post(`/api/plans/${planId}/uncomplete`)
        .then(() => {
          console.log("成功取消完成计划:", planId);
          this.fetchPlans(); // 刷新以获取更新的结构
          this.showMessage('已撤销计划完成状态', 'info');
        })
        .catch(error => {
          console.error('取消完成计划失败:', error);
          // 显示更详细的错误信息
          const errorMessage = error.response?.data?.message || '未知错误';
          console.log('错误详情:', errorMessage);
          this.showMessage('撤销计划完成状态失败: ' + errorMessage, 'error');
          // 显示一个更明显的错误提示
          alert('无法将计划标记为未完成: ' + errorMessage);
        });
    },
    
    deletePlan(planId) {
      if (!confirm('确定要删除此计划吗？这将同时删除所有子计划！')) return;
      
      axios.delete(`/api/plans/${planId}`)
        .then(() => {
          this.fetchPlans(); // Refresh to get updated structure
          this.showMessage('计划已删除', 'success');
        })
        .catch(error => {
          console.error('Error deleting plan:', error);
          this.showMessage('删除计划失败', 'error');
        });
    },
    
    showAddChildForm(parentPlan) {
      this.selectedParent = parentPlan;
      this.showChildModal = true;
      this.newChildPlan = {
        title: '',
        description: '',
        expectedCompletionTime: '',
        rewardPoints: 50
      };
      // 默认设置子计划的完成时间为今天23:59
      this.setChildTimeTodayEnd();
    },
    
    addChildPlan() {
      if (!this.isChildFormValid || !this.selectedParent) return;
      
      axios.post(`/api/plans/${this.selectedParent.id}/children`, this.newChildPlan)
        .then(() => {
          this.cancelAddChild();
          this.fetchPlans(); // Refresh to get updated structure
          this.showMessage('子计划添加成功', 'success');
        })
        .catch(error => {
          console.error('Error adding child plan:', error);
          this.showMessage('添加子计划失败', 'error');
        });
    },
    
    cancelAddChild() {
      this.showChildModal = false;
      this.selectedParent = null;
      this.newChildPlan = {
        title: '',
        description: '',
        expectedCompletionTime: '',
        rewardPoints: 50
      };
    },
    
    resetNewPlanForm() {
      this.newPlan = {
        title: '',
        description: '',
        expectedCompletionTime: '',
        rewardPoints: 100,
        repeatable: false,
        repeatType: 'DAILY',
        repeatInterval: 1,
        repeatEndDate: ''
      };
      this.setDefaultEndTimeToday(); // 重置为默认时间
    },
    
    showMessage(message, type = 'info') {
      this.message = message;
      this.messageType = type;
      
      setTimeout(() => {
        this.message = '';
      }, 3000);
    },
    
    updatePlan(planData) {
      // 使用 API 更新计划
      axios.put(`/api/plans/${planData.id}`, planData)
        .then(response => {
          // 找到并更新本地计划数据
          this.updatePlanInTree(this.plans, response.data);
          this.showMessage('计划更新成功', 'success');
        })
        .catch(error => {
          console.error('Error updating plan:', error);
          this.showMessage('更新计划失败: ' + (error.response?.data?.message || '未知错误'), 'error');
        });
    },
    
    // 递归更新树中的计划节点
    updatePlanInTree(plans, updatedPlan) {
      for (let i = 0; i < plans.length; i++) {
        if (plans[i].id === updatedPlan.id) {
          // 保留 children 引用，因为后端返回的数据可能没有包含完整的子计划
          const children = plans[i].children;
          plans[i] = { ...updatedPlan, children: children || updatedPlan.children || [] };
          return true;
        }
        
        // 递归检查子计划
        if (plans[i].children && plans[i].children.length > 0) {
          if (this.updatePlanInTree(plans[i].children, updatedPlan)) {
            return true;
          }
        }
      }
      return false;
    },
    
    // 设置默认完成时间为当天23:59
    setDefaultEndTimeToday() {
      const today = new Date();
      today.setHours(23, 59, 0, 0);
      this.newPlan.expectedCompletionTime = this.formatDateTimeForInput(today);
    },
    
    // 设置完成时间为今天23:59
    setTimeTodayEnd() {
      const today = new Date();
      today.setHours(23, 59, 0, 0);
      this.newPlan.expectedCompletionTime = this.formatDateTimeForInput(today);
    },
    
    // 设置完成时间为明天23:59
    setTimeTomorrowEnd() {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(23, 59, 0, 0);
      this.newPlan.expectedCompletionTime = this.formatDateTimeForInput(tomorrow);
    },
    
    // 设置完成时间为本周末(周日23:59)
    setTimeWeekendEnd() {
      const today = new Date();
      const dayOfWeek = today.getDay(); // 0 是周日，1 是周一，以此类推
      const daysUntilSunday = dayOfWeek === 0 ? 7 : 7 - dayOfWeek; // 如果今天是周日，那么获取下周日
      
      const sunday = new Date();
      sunday.setDate(today.getDate() + daysUntilSunday);
      sunday.setHours(23, 59, 0, 0);
      
      this.newPlan.expectedCompletionTime = this.formatDateTimeForInput(sunday);
    },
    
    // 设置完成时间为本月末(23:59)
    setTimeMonthEnd() {
      const today = new Date();
      // 获取下个月的第0天，即本月的最后一天
      const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);
      lastDayOfMonth.setHours(23, 59, 0, 0);
      
      this.newPlan.expectedCompletionTime = this.formatDateTimeForInput(lastDayOfMonth);
    },
    
    // 子计划的时间设置
    setChildTimeTodayEnd() {
      const today = new Date();
      today.setHours(23, 59, 0, 0);
      this.newChildPlan.expectedCompletionTime = this.formatDateTimeForInput(today);
    },
    
    setChildTimeTomorrowEnd() {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(23, 59, 0, 0);
      this.newChildPlan.expectedCompletionTime = this.formatDateTimeForInput(tomorrow);
    },
    
    setChildTimeWeekendEnd() {
      const today = new Date();
      const dayOfWeek = today.getDay();
      const daysUntilSunday = dayOfWeek === 0 ? 7 : 7 - dayOfWeek;
      
      const sunday = new Date();
      sunday.setDate(today.getDate() + daysUntilSunday);
      sunday.setHours(23, 59, 0, 0);
      
      this.newChildPlan.expectedCompletionTime = this.formatDateTimeForInput(sunday);
    },
    
    setChildTimeMonthEnd() {
      const today = new Date();
      const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);
      lastDayOfMonth.setHours(23, 59, 0, 0);
      
      this.newChildPlan.expectedCompletionTime = this.formatDateTimeForInput(lastDayOfMonth);
    },
    
    // 修正：格式化日期时间为input元素所需的格式，处理时区问题
    formatDateTimeForInput(date) {
      // 创建本地时间的ISO字符串并截取需要的部分，避免时区转换
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    },
    
    // 格式化日期部分（YYYY-MM-DD）
    formatDateForInput(date) {
      return date.toISOString().split('T')[0];
    },
    
    // 日期过滤函数
    filterByDate(filter) {
      this.dateFilter = filter;
    },
    
    // 检查计划是否在选定的日期范围内
    isPlanInDateRange(plan) {
      if (!plan.expectedCompletionTime) return false;
      
      const planDate = new Date(plan.expectedCompletionTime);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      
      // 计算本周开始和结束时间
      const currentDayOfWeek = today.getDay(); // 0 是周日，1 是周一，...
      const firstDayOfWeek = new Date(today);
      firstDayOfWeek.setDate(today.getDate() - currentDayOfWeek + (currentDayOfWeek === 0 ? -6 : 1)); // 调整为周一开始
      
      const lastDayOfWeek = new Date(firstDayOfWeek);
      lastDayOfWeek.setDate(firstDayOfWeek.getDate() + 6); // 周日结束
      lastDayOfWeek.setHours(23, 59, 59, 999);
      
      // 计算下周的开始和结束时间
      const firstDayOfNextWeek = new Date(lastDayOfWeek);
      firstDayOfNextWeek.setDate(lastDayOfWeek.getDate() + 1);
      firstDayOfNextWeek.setHours(0, 0, 0, 0);
      
      const lastDayOfNextWeek = new Date(firstDayOfNextWeek);
      lastDayOfNextWeek.setDate(firstDayOfNextWeek.getDate() + 6);
      lastDayOfNextWeek.setHours(23, 59, 59, 999);
      
      // 计算本月的开始和结束时间
      const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
      const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);
      lastDayOfMonth.setHours(23, 59, 59, 999);
      
      // 计算下月的开始和结束时间
      const firstDayOfNextMonth = new Date(today.getFullYear(), today.getMonth() + 1, 1);
      const lastDayOfNextMonth = new Date(today.getFullYear(), today.getMonth() + 2, 0);
      lastDayOfNextMonth.setHours(23, 59, 59, 999);
      
      switch (this.dateFilter) {
        case 'today':
          return planDate >= today && planDate < tomorrow;
          
        case 'tomorrow': {
          const dayAfterTomorrow = new Date(tomorrow);
          dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 1);
          return planDate >= tomorrow && planDate < dayAfterTomorrow;
        }
          
        case 'week': {
          return planDate >= firstDayOfWeek && planDate <= lastDayOfWeek;
        }
          
        case 'next-week': {
          return planDate >= firstDayOfNextWeek && planDate <= lastDayOfNextWeek;
        }
          
        case 'month':
          return planDate >= firstDayOfMonth && planDate <= lastDayOfMonth;
          
        case 'next-month':
          return planDate >= firstDayOfNextMonth && planDate <= lastDayOfNextMonth;
          
        case 'custom': {
          const customDateObj = new Date(this.customDate);
          const nextDay = new Date(customDateObj);
          nextDay.setDate(nextDay.getDate() + 1);
          return planDate >= customDateObj && planDate < nextDay;
        }
          
        default:
          return true;
      }
    },
    
    filterPlansRecursively(plans) {
      if (!plans) return [];
      const result = [];

      for (const plan of plans) {
        const filteredChildren = this.filterPlansRecursively(plan.children);
        const passesDate = (this.dateFilter === 'all') || this.isPlanInDateRange(plan);
        const passesTab =
          (this.activeTab === 'all') ||
          (this.activeTab === 'ongoing' && !plan.completed) || // 修改这里，使用'ongoing'替代'active'
          (this.activeTab === 'completed' && plan.completed);

        if ((passesDate && passesTab) || filteredChildren.length > 0) {
          result.push({ ...plan, children: filteredChildren });
        }
      }

      return result;
    }
  }
};
</script>

<style scoped>
.plan-tree {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

h1, h2, h3 {
  color: #333;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
}

.message {
  margin: 15px 0;
  padding: 10px;
  border-radius: 4px;
  text-align: center;
}

.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.info {
  background-color: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.add-plan-form {
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

input[type="text"],
input[type="number"],
input[type="datetime-local"],
textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

textarea {
  height: 80px;
  resize: vertical;
}

.add-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s;
}

.add-btn:hover {
  background-color: #45a049;
}

.add-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.plan-tree-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
}

.filter-tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd;
}

.tab-btn {
  padding: 8px 16px;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 16px;
  color: #555;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #4CAF50;
}

.tab-btn.active {
  color: #4CAF50;
  border-bottom-color: #4CAF50;
  font-weight: bold;
}

.plan-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.empty-message {
  text-align: center;
  color: #777;
  font-style: italic;
  padding: 20px;
}

.modal-overlay {
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

.modal-content {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.hierarchy-hint {
  font-size: 0.8em;
  color: #666;
  font-weight: normal;
}

/* Make child plans connections more visible */
.child-list {
  list-style: none;
  padding-left: 30px;
  margin: 0;
  border-left: 2px dashed #4CAF50;
  margin-left: 15px;
}

.repeat-options {
  margin-top: 10px;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 10px;
}

.repeat-settings {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.interval-unit {
  margin-left: 5px;
  color: #666;
}

.date-filter {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.filter-label {
  font-weight: bold;
  margin-bottom: 8px;
  color: #555;
}

.date-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.date-btn {
  padding: 6px 12px;
  background-color: #e0e0e0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.date-btn:hover {
  background-color: #d0d0d0;
}

.date-btn.active {
  background-color: #4CAF50;
  color: white;
}

.custom-date {
  display: flex;
  align-items: center;
  gap: 10px;
}

.custom-date input {
  padding: 6px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.time-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 5px;
}

.time-btn {
  padding: 5px 10px;
  background-color: #e0e0e0;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.time-btn:hover {
  background-color: #d0d0d0;
}
</style>
