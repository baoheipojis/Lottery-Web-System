<template>
  <div>
    <h1>Plan Points</h1>
    <div class="summary-card">
      <h2>Current Plan Points: <span class="plan-points-value">{{ currentPlanPoints }}</span></h2>
    </div>
    
    <!-- 操作按钮区域 -->
    <div class="actions-bar">
      <button @click="confirmClearRecords" class="clear-btn" :disabled="planPointsRecords.length === 0">
        清空计划点记录
      </button>
    </div>
    
    <!-- 消息提示 -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
    
    <table>
      <thead>
        <tr>
          <th>Amount Change</th>
          <th>Timestamp</th>
          <th>Balance After Operation</th>
          <th>Description</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="record in planPointsRecords" :key="record.id" 
            :class="{'positive-change': record.amountChange > 0, 
                    'negative-change': record.amountChange < 0}">
          <td>{{ record.amountChange }}</td>
          <td>{{ formatTimestamp(record.timestamp) }}</td>
          <td>{{ record.balanceAfterOperation }}</td>
          <td>{{ record.description || '无说明' }}</td>
        </tr>
        <!-- 无数据提示 -->
        <tr v-if="planPointsRecords.length === 0">
          <td colspan="4" class="empty-message">暂无计划点记录</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'PlanPoints',
  data() {
    return {
      currentPlanPoints: 0,
      planPointsRecords: [],
      message: '',
      messageType: 'info'
    };
  },
  methods: {
    formatTimestamp(timestamp) {
      if (!timestamp) return '未知时间';
      // 使用中国时区显示时间
      return new Date(timestamp).toLocaleString('zh-CN', {
        timeZone: 'Asia/Shanghai',
        hour12: false
      });
    },
    
    confirmClearRecords() {
      if (confirm('确定要删除所有计划点记录吗？此操作不可恢复！')) {
        this.clearAllRecords();
      }
    },
    
    clearAllRecords() {
      fetch('/api/plan-points/all', { method: 'DELETE' })
        .then(response => response.json())
        .then(data => {
          this.planPointsRecords = [];
          this.showMessage(data.message || '已清空所有计划点记录', 'success');
        })
        .catch(error => {
          console.error('Error clearing records:', error);
          this.showMessage('清空记录失败: ' + (error.response?.data?.message || '未知错误'), 'error');
        });
    },
    
    showMessage(message, type = 'info') {
      this.message = message;
      this.messageType = type;
      setTimeout(() => {
        this.message = '';
      }, 3000);
    },
    
    async fetchPlanPoints() {
      try {
        const response = await fetch('/api/plan-points');
        const data = await response.json();
        this.currentPlanPoints = data.currentPlanPoints;
        // 修改：对记录进行排序，从最新到最老
        this.planPointsRecords = data.planPointsRecords.sort((a, b) => {
          return new Date(b.timestamp) - new Date(a.timestamp);
        });
      } catch (error) {
        console.error('Error fetching plan points:', error);
      }
    }
  },
  async created() {
    await this.fetchPlanPoints();
  }
};
</script>

<style scoped>
h1 {
  color: #333;
  text-align: center;
  margin-bottom: 20px;
}

.summary-card {
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
  margin: 20px auto;
  text-align: center;
  max-width: 400px;
}

.plan-points-value {
  color: #4CAF50;
  font-weight: bold;
  font-size: 1.2em;
}

table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-radius: 5px;
  overflow: hidden;
  margin-top: 20px;
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

.positive-change {
  color: #2196F3;
  font-weight: bold;
}

.negative-change {
  color: #f44336;
  font-weight: bold;
}

.actions-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 15px;
}

.clear-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.clear-btn:hover {
  background-color: #d32f2f;
}

.clear-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
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
  padding: 20px;
  color: #777;
  font-style: italic;
}
</style>