<template>
  <div class="plan-tree">
    <h1>层次化计划管理</h1>
    
    <!-- Message notification -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
    
    <!-- Add new root plan -->
    <div class="add-plan-form">
      <h2>添加新计划</h2>
      <div class="form-group">
        <label>标题</label>
        <input v-model="newPlan.title" type="text" placeholder="输入计划标题" required />
      </div>
      
      <div class="form-group">
        <label>描述</label>
        <textarea v-model="newPlan.description" placeholder="输入计划描述"></textarea>
      </div>
      
      <div class="form-group">
        <label>预计完成时间</label>
        <input v-model="newPlan.expectedCompletionTime" type="datetime-local" required />
      </div>
      
      <div class="form-group">
        <label>奖励计划点</label>
        <input v-model.number="newPlan.rewardPoints" type="number" min="1" required />
      </div>
      
      <button @click="addPlan" class="add-btn" :disabled="!isFormValid">添加根计划</button>
    </div>
    
    <!-- Plan tree view -->
    <div class="plan-tree-container">
      <h2>计划列表 <span class="hierarchy-hint">(可展开查看子计划)</span></h2>
      <div class="filter-tabs">
        <button 
          :class="['tab-btn', { active: activeTab === 'all' }]" 
          @click="activeTab = 'all'"
        >
          全部
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'active' }]" 
          @click="activeTab = 'active'"
        >
          进行中
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'completed' }]" 
          @click="activeTab = 'completed'"
        >
          已完成
        </button>
      </div>
      
      <!-- Plan items -->
      <ul class="plan-list">
        <li v-if="filteredRootPlans.length === 0" class="empty-message">
          暂无计划
        </li>
        <template v-for="plan in filteredRootPlans" :key="plan.id">
          <plan-item 
            :plan="plan" 
            @complete="completePlan" 
            @delete="deletePlan"
            @add-child="showAddChildForm"
          />
        </template>
      </ul>
    </div>
    
    <!-- Add child plan modal -->
    <div v-if="showChildModal" class="modal-overlay">
      <div class="modal-content">
        <h3>添加子计划到: {{ selectedParent?.title }}</h3>
        
        <div class="form-group">
          <label>标题</label>
          <input v-model="newChildPlan.title" type="text" placeholder="输入计划标题" />
        </div>
        
        <div class="form-group">
          <label>描述</label>
          <textarea v-model="newChildPlan.description" placeholder="输入计划描述"></textarea>
        </div>
        
        <div class="form-group">
          <label>预计完成时间</label>
          <input v-model="newChildPlan.expectedCompletionTime" type="datetime-local" />
        </div>
        
        <div class="form-group">
          <label>奖励计划点</label>
          <input v-model.number="newChildPlan.rewardPoints" type="number" min="1" />
        </div>
        
        <div class="modal-actions">
          <button @click="cancelAddChild" class="cancel-btn">取消</button>
          <button 
            @click="addChildPlan" 
            class="add-btn"
            :disabled="!isChildFormValid"
          >
            添加子计划
          </button>
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
      activeTab: 'all', // all, active, completed
      message: '',
      messageType: 'info',
      newPlan: {
        title: '',
        description: '',
        expectedCompletionTime: '',
        rewardPoints: 100
      },
      showChildModal: false,
      selectedParent: null,
      newChildPlan: {
        title: '',
        description: '',
        expectedCompletionTime: '',
        rewardPoints: 50
      }
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
      if (this.activeTab === 'all') {
        return this.plans;
      } else if (this.activeTab === 'active') {
        return this.plans.filter(plan => !plan.completed);
      } else { // completed
        return this.plans.filter(plan => plan.completed);
      }
    }
  },
  created() {
    this.fetchPlans();
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
      
      axios.post('/api/plans', this.newPlan)
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
      axios.post(`/api/plans/${planId}/complete`)
        .then(() => {
          this.fetchPlans(); // Refresh to get updated structure with children
          this.showMessage('计划已完成，奖励计划点已添加', 'success');
        })
        .catch(error => {
          console.error('Error completing plan:', error);
          this.showMessage('完成计划失败', 'error');
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
        rewardPoints: 100
      };
    },
    
    showMessage(message, type = 'info') {
      this.message = message;
      this.messageType = type;
      
      setTimeout(() => {
        this.message = '';
      }, 3000);
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
</style>
