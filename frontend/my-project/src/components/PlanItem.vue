<template>
  <div>
    <div class="plan-item" :class="{ 'completed': plan.completed, 'repeatable': plan.repeatable }">
      <div class="plan-checkbox-container">
        <input 
          type="checkbox" 
          :checked="plan.completed" 
          @change="toggleComplete"
          :disabled="plan.completed"
        />
      </div>
      
      <div class="plan-details" @click="toggleExpanded">
        <div class="plan-header">
          <div class="plan-title">
            {{ plan.title }}
            <span v-if="hasChildren" class="children-indicator" :class="{ 'expanded': isExpanded }">
              {{ isExpanded ? '▼' : '▶' }}
            </span>
            <span v-if="plan.repeatable" class="repeat-badge" :title="repeatDescription">⟳</span>
          </div>
          <div class="plan-due-date">
            {{ formatDateTime(plan.expectedCompletionTime) }}
          </div>
        </div>
        
        <div v-if="isExpanded" class="plan-info">
          <div class="plan-description">
            {{ plan.description || '无描述' }}
          </div>
          <div class="plan-rewards">
            奖励: {{ plan.rewardPoints }} 计划点
          </div>
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
          @delete="$emit('delete', $event)"
          @add-child="$emit('add-child', $event)"
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
      isExpanded: false
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
    toggleComplete() {
      if (!this.plan.completed) {
        this.$emit('complete', this.plan.id);
      }
    },
    toggleExpanded() {
      this.isExpanded = !this.isExpanded;
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
</style>
