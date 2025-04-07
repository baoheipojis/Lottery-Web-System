<template>
  <div>
    <div class="plan-item" :class="{ 'completed': plan.completed, 'repeatable': plan.repeatable }">
      <div class="plan-checkbox-container">
        <input 
          type="checkbox" 
          :checked="plan.completed" 
          @click.prevent="handleCheckboxClick"
          title="点击可切换完成状态"
        />
      </div>
      
      <div class="plan-details" @click="toggleExpanded">
        <div class="plan-header">
          <div class="plan-title">
            <!-- 编辑模式标题 -->
            <div v-if="isEditing && editField === 'title'" class="edit-field">
              <input v-model="editedPlan.title" class="edit-input" @click.stop />
              <div class="edit-controls">
                <button @click.stop="saveEdit" class="save-btn">✓</button>
                <button @click.stop="cancelEdit" class="cancel-btn">✗</button>
              </div>
            </div>
            <div v-else class="title-container">
              {{ plan.title }}
              <span v-if="hasChildren" class="children-indicator" :class="{ 'expanded': isExpanded }">
                {{ isExpanded ? '▼' : '▶' }}
              </span>
              <span v-if="plan.repeatable" class="repeat-badge" :title="repeatDescription">⟳</span>
              <button v-if="!plan.completed" @click.stop="startEdit('title')" class="edit-btn">✎</button>
            </div>
          </div>
          <div class="plan-due-date">
            <!-- 编辑模式日期 -->
            <div v-if="isEditing && editField === 'date'" class="edit-field">
              <input 
                v-model="editedPlan.expectedCompletionTime" 
                type="datetime-local" 
                class="edit-input" 
                @click.stop
              />
              <div class="edit-controls">
                <button @click.stop="saveEdit" class="save-btn">✓</button>
                <button @click.stop="cancelEdit" class="cancel-btn">✗</button>
              </div>
            </div>
            <div v-else class="date-container">
              {{ formatDateTime(plan.expectedCompletionTime) }}
              <button v-if="!plan.completed" @click.stop="startEdit('date')" class="edit-btn">✎</button>
            </div>
          </div>
        </div>
        
        <div v-if="isExpanded" class="plan-info">
          <!-- 编辑模式描述 -->
          <div v-if="isEditing && editField === 'description'" class="edit-field">
            <textarea v-model="editedPlan.description" class="edit-textarea" @click.stop></textarea>
            <div class="edit-controls">
              <button @click.stop="saveEdit" class="save-btn">✓</button>
              <button @click.stop="cancelEdit" class="cancel-btn">✗</button>
            </div>
          </div>
          <div v-else class="description-container">
            <div class="plan-description">
              {{ plan.description || '无描述' }}
              <button v-if="!plan.completed" @click.stop="startEdit('description')" class="edit-btn">✎</button>
            </div>
          </div>
          
          <!-- 编辑模式计划点 -->
          <div v-if="isEditing && editField === 'points'" class="edit-field">
            <input 
              v-model.number="editedPlan.rewardPoints" 
              type="number" 
              min="1" 
              class="edit-input" 
              @click.stop
            />
            <div class="edit-controls">
              <button @click.stop="saveEdit" class="save-btn">✓</button>
              <button @click.stop="cancelEdit" class="cancel-btn">✗</button>
            </div>
          </div>
          <div v-else class="points-container">
            <div class="plan-rewards">
              奖励: {{ plan.rewardPoints }} 计划点
              <button v-if="!plan.completed" @click.stop="startEdit('points')" class="edit-btn">✎</button>
            </div>
          </div>
          
          <!-- 其他不可编辑信息 -->
          <div v-if="plan.completed" class="completion-time">
            完成于: {{ formatDateTime(plan.actualCompletionTime) }}
          </div>
          <div v-if="hasChildren" class="children-count">
            子计划数量: {{ plan.children.length }}
          </div>
          <div v-if="plan.repeatable" class="plan-repeat-info">
            重复: {{ repeatDescription }}
          </div>
        </div>
      </div>
      
      <div class="plan-actions">
        <button 
          v-if="!plan.completed" 
          @click="$emit('add-child', plan)" 
          class="action-btn add-child-btn"
          title="添加子计划"
        >
          +
        </button>
        <button 
          @click="$emit('delete', plan.id)" 
          class="action-btn delete-btn"
          title="删除计划"
        >
          ×
        </button>
      </div>
    </div>
    
    <ul v-if="hasChildren && isExpanded" class="child-list">
      <li v-for="child in plan.children" :key="child.id">
        <plan-item 
          :plan="child" 
          @complete="$emit('complete', $event)"
          @uncomplete="$emit('uncomplete', $event)"
          @delete="$emit('delete', $event)"
          @add-child="$emit('add-child', $event)"
          @update="$emit('update', $event)"
        />
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'PlanItem',
  props: {
    plan: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      isExpanded: false,
      isEditing: false,
      editField: null,
      editedPlan: {}
    };
  },
  computed: {
    hasChildren() {
      return this.plan.children && this.plan.children.length > 0;
    },
    repeatDescription() {
      if (!this.plan.repeatable) return '';
      
      let desc = '';
      switch (this.plan.repeatType) {
        case 'DAILY':
          desc = `每${this.plan.repeatInterval || 1}天`;
          break;
        case 'WEEKLY':
          desc = `每${this.plan.repeatInterval || 1}周`;
          break;
        case 'MONTHLY':
          desc = `每${this.plan.repeatInterval || 1}月`;
          break;
        default:
          desc = '重复';
      }
      
      if (this.plan.repeatEndDate) {
        desc += ` (截止至${this.formatDateTime(this.plan.repeatEndDate)})`;
      }
      
      return desc;
    }
  },
  methods: {
    handleCheckboxClick() {
      if (this.plan.completed) {
        // 如果已完成，显示确认对话框
        if (confirm('确定要将此计划标记为未完成吗？之前获得的计划点将被扣除。')) {
          // 用户确认后，发送取消完成的事件
          this.$emit('uncomplete', this.plan.id);
        }
        // 如果用户取消，不做任何操作，复选框保持选中状态
      } else {
        // 如果未完成，直接发送完成的事件
        this.$emit('complete', this.plan.id);
      }
    },
    toggleExpanded() {
      if (!this.isEditing) {
        this.isExpanded = !this.isExpanded;
      }
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
    
    // 编辑相关方法
    startEdit(field) {
      // 创建计划副本用于编辑
      this.editedPlan = { ...this.plan };
      
      // 格式化日期时间以适应 input 类型，修复时区问题
      if (field === 'date' && this.editedPlan.expectedCompletionTime) {
        const date = new Date(this.editedPlan.expectedCompletionTime);
        
        // 使用本地时间格式化，避免时区转换问题
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        this.editedPlan.expectedCompletionTime = `${year}-${month}-${day}T${hours}:${minutes}`;
      }
      
      this.editField = field;
      this.isEditing = true;
    },
    
    saveEdit() {
      // 创建要更新的数据对象
      const updatedData = { id: this.plan.id };
      
      switch (this.editField) {
        case 'title':
          updatedData.title = this.editedPlan.title;
          break;
        case 'description':
          updatedData.description = this.editedPlan.description;
          break;
        case 'date':
          updatedData.expectedCompletionTime = this.editedPlan.expectedCompletionTime;
          break;
        case 'points':
          updatedData.rewardPoints = this.editedPlan.rewardPoints;
          break;
      }
      
      // 发送更新事件到父组件
      this.$emit('update', updatedData);
      
      // 重置编辑状态
      this.isEditing = false;
      this.editField = null;
    },
    
    cancelEdit() {
      this.isEditing = false;
      this.editField = null;
    }
  }
};
</script>

<style scoped>
.plan-item {
  display: flex;
  align-items: flex-start;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 6px;
  background-color: #f9f9f9;
  border-left: 3px solid #4CAF50;
  transition: all 0.2s;
}

.plan-item:hover {
  background-color: #f0f0f0;
}

.plan-item.completed {
  border-left-color: #9e9e9e;
  background-color: #f0f0f0;
  color: #757575;
  text-decoration: line-through;
}

.plan-item.repeatable {
  border-left-color: #2196F3;
}

.plan-checkbox-container {
  padding: 10px;
}

.plan-checkbox-container input[type="checkbox"] {
  transform: scale(1.3);
  cursor: pointer;
}

.plan-details {
  flex: 1;
  cursor: pointer;
  padding: 5px 0;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-title {
  font-weight: bold;
  font-size: 16px;
}

.plan-due-date {
  font-size: 14px;
  color: #757575;
}

.plan-info {
  margin-top: 8px;
  font-size: 14px;
}

.plan-description {
  margin-bottom: 5px;
  color: #555;
}

.plan-rewards {
  color: #2196F3;
  font-weight: bold;
}

.completion-time {
  color: #4CAF50;
  margin-top: 5px;
}

.plan-repeat-info {
  margin-top: 5px;
  color: #2196F3;
  font-size: 14px;
}

.plan-actions {
  display: flex;
  gap: 5px;
}

.action-btn {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.2s;
}

.add-child-btn {
  background-color: #2196F3;
  color: white;
}

.add-child-btn:hover {
  background-color: #1976D2;
}

.delete-btn {
  background-color: #f44336;
  color: white;
}

.delete-btn:hover {
  background-color: #D32F2F;
}

.child-list {
  list-style: none;
  padding-left: 30px;
  margin: 0;
  border-left: 1px dashed #ccc;
  margin-left: 15px;
}

.children-indicator {
  display: inline-block;
  margin-left: 5px;
  font-size: 12px;
  color: #4CAF50;
  transition: transform 0.2s;
}

.children-indicator.expanded {
  transform: rotate(0deg);
}

.repeat-badge {
  display: inline-block;
  margin-left: 5px;
  color: #2196F3;
  font-size: 16px;
  cursor: help;
}

.children-count {
  margin-top: 5px;
  color: #4CAF50;
  font-size: 12px;
}

/* 编辑相关样式 */
.edit-btn {
  visibility: hidden;
  background: none;
  border: none;
  color: #2196F3;
  cursor: pointer;
  font-size: 14px;
  padding: 2px 5px;
  margin-left: 5px;
}

.plan-title:hover .edit-btn,
.plan-description:hover .edit-btn,
.plan-due-date:hover .edit-btn,
.plan-rewards:hover .edit-btn {
  visibility: visible;
}

.edit-field {
  display: flex;
  align-items: center;
  margin: 2px 0;
}

.edit-input, .edit-textarea {
  flex: 1;
  padding: 6px;
  border: 1px solid #2196F3;
  border-radius: 4px;
  font-size: 14px;
}

.edit-textarea {
  min-height: 60px;
  resize: vertical;
}

.edit-controls {
  display: flex;
  margin-left: 8px;
}

.save-btn, .cancel-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 0 4px;
}

.save-btn {
  color: #4CAF50;
}

.cancel-btn {
  color: #f44336;
}

.title-container, .date-container, .description-container, .points-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
