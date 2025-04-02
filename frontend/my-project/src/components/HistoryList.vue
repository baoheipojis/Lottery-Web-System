<template>
  <div>
    <h1>Lottery History</h1>
    
    <div v-if="loading" class="loading">Loading history...</div>
    <div v-else-if="history.length === 0" class="no-records">
      No lottery records found. Try your luck with the lottery!
    </div>
    <div v-else>
      <table>
        <thead>
          <tr>
            <th>Time</th>
            <th>Prize</th>
            <th>Rarity</th>
            <th>Result</th>
            <th>兑现状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in history" :key="item.id" :class="getRowClass(item)">
            <td>{{ formatDate(item.drawTime) }}</td>
            <td>{{ item.prizeName }}</td>
            <td>
              {{ item.rarity === 5 
                ? (item.fiveStarType?.toUpperCase() === 'LIMITED' ? '限定五星' : '普通五星') 
                : item.rarity + '星' 
              }}
            </td>
            <td>{{ item.result }}</td>
            <td :class="{'redeemed': item.redeemed, 'not-redeemed': !item.redeemed}">
              {{ item.redeemed ? '已兑现' : '未兑现' }}
            </td>
            <td>
              <button 
                @click="toggleRedeemStatus(item)" 
                class="toggle-btn"
                :class="{'redeem-btn': !item.redeemed, 'un-redeem-btn': item.redeemed}"
              >
                {{ item.redeemed ? '标记为未兑现' : '标记为已兑现' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Status message -->
    <div v-if="message" :class="['status-message', messageType]">
      {{ message }}
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      history: [],
      loading: true,
      message: '',
      messageType: ''
    };
  },
  created() {
    this.fetchHistory();
  },
  methods: {
    fetchHistory() {
      this.loading = true;
      axios.get('/api/lottery/history')
        .then(response => {
          this.history = response.data;
        })
        .catch(error => {
          console.error('Error fetching history:', error);
          this.showMessage('Failed to load lottery history', 'error');
        })
        .finally(() => {
          this.loading = false;
        });
    },
    formatDate(dateString) {
      if (!dateString) return 'N/A';
      const date = new Date(dateString);
      return date.toLocaleString();
    },
    getRowClass(item) {
      return {
        'rarity-3': item.rarity === 3,
        'rarity-4': item.rarity === 4,
        'rarity-5': item.rarity === 5,
        'five-star-limited': item.rarity === 5 && item.fiveStarType?.toUpperCase() === 'LIMITED',
        'not-redeemed-row': !item.redeemed
      };
    },
    toggleRedeemStatus(item) {
      const newStatus = !item.redeemed;
      axios.put(`/api/lottery/history/${item.id}/redeem`, { redeemed: newStatus })
        .then(response => {
          // Update the local history item
          const index = this.history.findIndex(h => h.id === item.id);
          if (index !== -1) {
            this.history[index] = response.data;
          }
          
          this.showMessage(
            `奖励 "${item.prizeName}" 已被标记为${newStatus ? '已兑现' : '未兑现'}`, 
            'success'
          );
        })
        .catch(error => {
          console.error('Error updating redeem status:', error);
          this.showMessage(
            `无法更新兑现状态: ${error.response?.data?.message || '未知错误'}`, 
            'error'
          );
        });
    },
    showMessage(text, type) {
      this.message = text;
      this.messageType = type;
      
      // Clear message after 3 seconds
      setTimeout(() => {
        this.message = '';
        this.messageType = '';
      }, 3000);
    }
  }
};
</script>

<style scoped>
h1 {
  text-align: center;
  margin-bottom: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #4CAF50;
  color: white;
  font-weight: bold;
}

.rarity-3 {
  background-color: #f9f9f9;
}

.rarity-4 {
  background-color: #e6d0f3;
}

.rarity-5 {
  background-color: #fff3cd;
}

.five-star-limited {
  background-color: #ffe0b2;
}

.not-redeemed-row {
  opacity: 0.7;
}

.loading, .no-records {
  text-align: center;
  padding: 20px;
  font-size: 18px;
  color: #666;
}

.redeemed {
  color: #4CAF50;
  font-weight: bold;
}

.not-redeemed {
  color: #f44336;
  font-weight: bold;
}

.toggle-btn {
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.redeem-btn {
  background-color: #4CAF50;
  color: white;
}

.redeem-btn:hover {
  background-color: #388E3C;
}

.un-redeem-btn {
  background-color: #f44336;
  color: white;
}

.un-redeem-btn:hover {
  background-color: #D32F2F;
}

.status-message {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 10px 15px;
  border-radius: 4px;
  font-weight: bold;
  box-shadow: 0 2px 10px rgba(0,0,0,0.2);
  animation: fadeIn 0.3s, fadeOut 0.3s 2.7s;
  z-index: 1000;
}

.success {
  background-color: #4CAF50;
  color: white;
}

.error {
  background-color: #f44336;
  color: white;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeOut {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(20px); }
}
</style>