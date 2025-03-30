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
            <td>
              <div v-if="editMode[plan.id]?.title">
                <input v-model="editData[plan.id].title" class="edit-input" />
              </div>
              <div v-else class="cell-with-edit">
                {{ plan.title }}
                <button @click="startEdit(plan, 'title')" class="edit-btn">✎</button>
              </div>
            </td>
            <td>
              <div v-if="editMode[plan.id]?.description">
                <textarea v-model="editData[plan.id].description" class="edit-textarea"></textarea>
              </div>
              <div v-else class="cell-with-edit">
                {{ plan.description }}
                <button @click="startEdit(plan, 'description')" class="edit-btn">✎</button>
              </div>
            </td>
            <td>
              <div v-if="editMode[plan.id]?.expectedCompletionTime">
                <input 
                  v-model="editData[plan.id].expectedCompletionTime" 
                  type="datetime-local" 
                  class="edit-input" 
                />
              </div>
              <div v-else class="cell-with-edit">
                {{ formatDateTime(plan.expectedCompletionTime) }}
                <button @click="startEdit(plan, 'expectedCompletionTime')" class="edit-btn">✎</button>
              </div>
            </td>
            <td>
              <div v-if="editMode[plan.id]?.rewardPoints">
                <input 
                  v-model.number="editData[plan.id].rewardPoints" 
                  type="number" 
                  min="1"
                  class="edit-input" 
                />
              </div>
              <div v-else class="cell-with-edit">
                {{ plan.rewardPoints }}
                <button @click="startEdit(plan, 'rewardPoints')" class="edit-btn">✎</button>
              </div>
            </td>
            <td>
              <div v-if="isEditing(plan.id)">
                <button @click="saveChanges(plan.id)" class="save-btn">保存</button>
                <button @click="cancelEdit(plan.id)" class="cancel-btn">取消</button>
              </div>
              <div v-else>
                <button @click="completePlan(plan.id)" class="complete-btn">完成</button>
                <button @click="deletePlan(plan.id)" class="delete-btn">删除</button>
              </div>
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
      messageType: 'info',
      editMode: {}, // 跟踪每个计划的编辑状态
      editData: {}  // 存储编辑中的数据
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
    },
    
    // 开始编辑
    startEdit(plan, field) {
      // 确保 editData 中有这个计划的副本
      if (!this.editData[plan.id]) {
        // 对于时间字段，需要特殊处理格式
        let expectedCompletionTime = plan.expectedCompletionTime;
        if (expectedCompletionTime) {
          // 转换为 yyyy-MM-ddThh:mm 格式以适应 datetime-local 输入
          const date = new Date(expectedCompletionTime);
          expectedCompletionTime = date.toISOString().slice(0, 16);
        }
        
        this.editData[plan.id] = { 
          ...plan,
          expectedCompletionTime
        };
      }
      
      // 确保 editMode 中有这个计划的跟踪状态
      if (!this.editMode[plan.id]) {
        this.editMode[plan.id] = {};
      }
      
      // 设置指定字段为编辑模式
      this.editMode[plan.id][field] = true;
    },
    
    // 取消编辑
    cancelEdit(planId) {
      this.editMode[planId] = {};
    },
    
    // 检查计划是否处于编辑模式
    isEditing(planId) {
      return this.editMode[planId] && Object.values(this.editMode[planId]).some(value => value);
    },
    
    // 保存更改
    saveChanges(planId) {
      const updatedPlan = { ...this.editData[planId] };
      
      axios.put(`/api/plans/${planId}`, updatedPlan)
        .then(response => {
          // 更新本地数据
          const index = this.plans.findIndex(p => p.id === planId);
          if (index !== -1) {
            this.plans[index] = response.data;
          }
          
          // 重置编辑状态
          this.cancelEdit(planId);
          
          this.showMessage('计划更新成功', 'success');
        })
        .catch(error => {
          console.error('Error updating plan:', error);
          this.showMessage('更新计划失败', 'error');
        });
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

/* 编辑相关样式 */
.cell-with-edit {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.edit-btn {
  visibility: hidden;
  background: none;
  border: none;
  color: #2196F3;
  cursor: pointer;
  font-size: 16px;
  padding: 2px 5px;
  margin-left: 5px;
}

tr:hover .edit-btn {
  visibility: visible;
}

.edit-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #2196F3;
  border-radius: 4px;
}

.edit-textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #2196F3;
  border-radius: 4px;
  min-height: 60px;
  resize: vertical;
}

.save-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}

.cancel-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn:hover {
  background-color: #45a049;
}

.cancel-btn:hover {
  background-color: #d32f2f;
}
</style>
