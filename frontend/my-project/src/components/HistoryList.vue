<template>
  <div>
    <h1>Lottery History</h1>
    
    <!-- 操作按钮区域 -->
    <div class="actions-bar">
      <button @click="confirmClearHistory" class="clear-btn" :disabled="histories.length === 0">
        清空抽奖记录
      </button>
    </div>
    
    <!-- 消息提示 -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
    
    <table>
      <thead>
        <tr>
          <th>Draw Time</th>
          <th>Prize</th>
          <th>Rarity (stars)</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="history in histories"
          :key="history.id"
          :class="{
            'rarity-purple': history.rarity === 4,
            'rarity-yellow': history.rarity === 5
          }"
        >
          <td>{{ formatTimestamp(history.drawTime) }}</td>
          <td>{{ history.prizeName }}</td>
          <td>
            {{ history.rarity === 5
                ? (history.isLimited ? '限定五星' : '普通五星')
                : (history.rarity + '星')
            }}
          </td>
        </tr>
        <!-- 无数据提示 -->
        <tr v-if="histories.length === 0">
          <td colspan="3" class="empty-message">暂无抽奖记录</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      histories: [],
      message: '',
      messageType: 'info'
    };
  },
  created() {
    this.fetchHistories();
  },
  methods: {
    fetchHistories() {
      axios.get('/api/lottery-history')
        .then(response => {
          this.histories = response.data.sort((a,b) => new Date(b.drawTime) - new Date(a.drawTime));
        })
        .catch(error => {
          console.error('Error fetching histories:', error);
        });
    },
    
    formatTimestamp(timestamp) {
      if (!timestamp) return '未知时间';
      // 使用中国时区显示时间
      return new Date(timestamp).toLocaleString('zh-CN', {
        timeZone: 'Asia/Shanghai',
        hour12: false
      });
    },
    
    confirmClearHistory() {
      if (confirm('确定要删除所有抽奖记录吗？此操作不可恢复！')) {
        this.clearAllHistory();
      }
    },
    
    clearAllHistory() {
      axios.delete('/api/lottery-history/all')
        .then(response => {
          this.histories = [];
          this.showMessage(response.data.message || '已清空所有抽奖记录', 'success');
        })
        .catch(error => {
          console.error('Error clearing history:', error);
          this.showMessage('清空记录失败: ' + (error.response?.data?.message || '未知错误'), 'error');
        });
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

.rarity-purple {
  color: #9c27b0;
  font-weight: bold;
}

.rarity-yellow {
  color: #ffc107;
  font-weight: bold;
  text-shadow: 0px 0px 1px #000;
}

h1 {
  color: #333;
  text-align: center;
  margin-bottom: 20px;
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