<template>
  <div>
    <h1>计划列表</h1>
    
    <!-- 添加新计划表单 -->
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
      
      <button @click="addPlan" class="add-btn" :disabled="!isFormValid">添加计划</button>
    </div>

    <!-- 消息提示 -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
    
    <!-- 计划列表 -->
    <div class="plan-section">
      <h2>进行中的计划</h2>
      <table>
        <thead>
          <tr>
            <th>标题</th>
            <th>描述</th>
            <th>预计完成时间</th>
            <th>奖励计划点</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="plan in activePlans" :key="plan.id">
            <td>{{ plan.title }}</td>
            <td>{{ plan.description }}</td>
            <td>{{ formatDateTime(plan.expectedCompletionTime) }}</td>
            <td>{{ plan.rewardPoints }}</td>
            <td>
              <button @click="completePlan(plan.id)" class="complete-btn">完成</button>
              <button @click="deletePlan(plan.id)" class="delete-btn">删除</button>
            </td>
          </tr>
          <tr v-if="activePlans.length === 0">
            <td colspan="5" class="empty-message">暂无进行中的计划</td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div class="plan-section">
      <h2>已完成的计划</h2>
      <table>
        <thead>
          <tr>
            <th>标题</th>
            <th>描述</th>
            <th>预计完成时间</th>
            <th>实际完成时间</th>
            <th>奖励计划点</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="plan in completedPlans" :key="plan.id">
            <td>{{ plan.title }}</td>
            <td>{{ plan.description }}</td>
            <td>{{ formatDateTime(plan.expectedCompletionTime) }}</td>
            <td>{{ formatDateTime(plan.actualCompletionTime) }}</td>
            <td>{{ plan.rewardPoints }}</td>
          </tr>
          <tr v-if="completedPlans.length === 0">
            <td colspan="5" class="empty-message">暂无已完成的计划</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      plans: [],
      newPlan: {
        title: '',
        description: '',
        expectedCompletionTime: '',
        rewardPoints: 100
      },
      message: '',
      messageType: 'info'
    };
  },
  computed: {
    activePlans() {
      return this.plans.filter(plan => !plan.completed);
    },
    completedPlans() {
      return this.plans.filter(plan => plan.completed);
    },
    isFormValid() {
      return this.newPlan.title && 
        this.newPlan.expectedCompletionTime && 
        this.newPlan.rewardPoints > 0;
    }
  },
  created() {
    this.fetchPlans();
  },
  methods: {
    fetchPlans() {
      axios.get('/api/plans')
        .then(response => {
          this.plans = response.data;
        })
        .catch(error => {
          console.error('Error fetching plans:', error);
          this.showMessage('获取计划列表失败', 'error');
        });
    },
    
    formatDateTime(dateTime) {
      if (!dateTime) return '未设置';
      const date = new Date(dateTime);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    
    addPlan() {
      if (!this.isFormValid) return;
      
      axios.post('/api/plans', this.newPlan)
        .then(response => {
          this.plans.push(response.data);
          this.resetForm();
          this.showMessage('计划添加成功', 'success');
        })
        .catch(error => {
          console.error('Error adding plan:', error);
          this.showMessage('添加计划失败', 'error');
        });
    },
    
    completePlan(id) {
      axios.post(`/api/plans/${id}/complete`)
        .then(response => {
          const index = this.plans.findIndex(p => p.id === id);
          if (index !== -1) {
            this.plans[index] = response.data;
          }
          this.showMessage('计划已完成，奖励计划点已添加', 'success');
        })
        .catch(error => {
          console.error('Error completing plan:', error);
          this.showMessage('完成计划失败', 'error');
        });
    },
    
    deletePlan(id) {
      if (!confirm('确定要删除此计划吗？')) return;
      
      axios.delete(`/api/plans/${id}`)
        .then(() => {
          this.plans = this.plans.filter(p => p.id !== id);
          this.showMessage('计划已删除', 'success');
        })
        .catch(error => {
          console.error('Error deleting plan:', error);
          this.showMessage('删除计划失败', 'error');
        });
    },
    
    resetForm() {
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
h1, h2 {
  color: #333;
  margin-bottom: 20px;
}

h1 {
  text-align: center;
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

.plan-section {
  margin-bottom: 30px;
}

table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-radius: 5px;
  overflow: hidden;
  margin-top: 10px;
}

th {
  background-color: #4CAF50;
  color: white;
  padding: 12px;
  font-weight: bold;
  text-align: left;
}

td {
  padding: 12px;
  border-bottom: 1px solid #ddd;
}

tbody tr:nth-child(even) {
  background-color: #f2f2f2;
}

tbody tr:hover {
  background-color: #e0e0e0;
}

.complete-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}

.complete-btn:hover {
  background-color: #0b7dda;
}

.delete-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.delete-btn:hover {
  background-color: #d32f2f;
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

.empty-message {
  text-align: center;
  color: #777;
  font-style: italic;
}
</style>
