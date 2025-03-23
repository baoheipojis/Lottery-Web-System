<template>
  <div>
    <h1>All Prizes</h1>
    
    <!-- Display status messages -->
    <div v-if="submitMessage" :class="['message', submitStatus]">
      {{ submitMessage }}
    </div>
    
    <!-- Column visibility toggle -->
    <div class="column-toggle">
      <label>
        <input type="checkbox" v-model="showActionsColumn">
        显示操作列
      </label>
    </div>
    
    <!-- Prize Table -->
    <table>
      <thead>
        <tr>
          <th>Prize Name</th>
          <th>Rarity</th>
          <th>Description</th>
          <th>可重复获取</th>
          <th v-if="showActionsColumn">Actions</th>
        </tr>
      </thead>
      <tbody>
        <!-- New Prize Input Row -->
        <tr class="new-prize-row">
          <td>
            <input 
              v-model="newPrize.name" 
              type="text" 
              placeholder="输入奖品名称" 
              required
            />
          </td>
          <td>
            <select 
              v-model="newPrize.rarity" 
              required
            >
              <option value="" disabled selected>选择稀有度</option>
              <option value="3">3星</option>
              <option value="4">4星</option>
              <option value="5">5星</option>
            </select>
            
            <select 
              v-if="newPrize.rarity == 5"
              v-model="newPrize.fiveStarType"
              class="five-star-type"
            >
              <option value="normal">普通</option>
              <option value="limited">限定</option>
            </select>
          </td>
          <td>
            <input 
              v-model="newPrize.description"
              type="text"
              placeholder="奖品描述"
            />
          </td>
          <td>
            <input 
              v-model="newPrize.isRepeatable" 
              type="checkbox"
              class="checkbox-center"
            />
          </td>
          <td v-if="showActionsColumn">
            <button 
              @click="addPrize" 
              class="add-btn"
              :disabled="!isFormValid"
            >添加</button>
          </td>
        </tr>
        
        <!-- Existing Prizes -->
        <tr 
          v-for="prize in prizes" 
          :key="prize.id"
          :class="{
            'rarity-purple': prize.rarity === 4,
            'rarity-yellow': prize.rarity === 5
          }"
        >
          <td>{{ prize.name }}</td>
          <td>
            {{ prize.rarity === 5
                ? (prize.fiveStarType?.toUpperCase() === 'LIMITED' ? '限定五星' : '普通五星')
                : (prize.rarity + '星')
            }}
          </td>
          <td>{{ prize.description }}</td>
          <td class="center-content" :class="{'repeatable': isRepeatable(prize)}">
            {{ isRepeatable(prize) ? '是' : '否' }}
          </td>
          <td v-if="showActionsColumn">
            <button 
              @click="confirmDelete(prize)"
              class="delete-btn"
            >删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    
    <!-- Add button outside table when action column is hidden -->
    <div v-if="!showActionsColumn" class="floating-add-btn-container">
      <button 
        @click="addPrize" 
        class="floating-add-btn"
        :disabled="!isFormValid"
        title="添加奖品"
      >
        添加奖品
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      prizes: [],
      newPrize: {
        name: '',
        rarity: '',
        fiveStarType: 'normal',
        description: '',
        isRepeatable: false
      },
      submitMessage: '',
      submitStatus: '',
      showActionsColumn: true // Default to showing the Actions column
    };
  },
  created() {
    this.fetchPrizes();
  },
  computed: {
    isFormValid() {
      return this.newPrize.name && this.newPrize.rarity;
    }
  },
  methods: {
    fetchPrizes() {
      axios.get('/api/prizes')
        .then(response => {
          this.prizes = response.data;
          console.log('Prizes data:', this.prizes); // Debug log to check values
        })
        .catch(error => {
          console.error('Error fetching prizes:', error);
        });
    },
    isRepeatable(prize) {
      // Use isRepeatable property instead of is_repeatable
      return prize.isRepeatable === true || 
             prize.isRepeatable === 1 || 
             prize.isRepeatable === '1' || 
             prize.isRepeatable === 'true';
    },
    addPrize() {
      // Convert rarity to number to ensure proper type
      this.newPrize.rarity = parseInt(this.newPrize.rarity);
      
      // Clear five star type if not a 5-star prize
      if (this.newPrize.rarity !== 5) {
        this.newPrize.fiveStarType = null;
      }
      
      axios.post('/api/prizes', this.newPrize)
        .then(response => {
          // Update the prizes list
          this.prizes.push(response.data);
          
          // Reset form
          this.newPrize = {
            name: '',
            rarity: '',
            fiveStarType: 'normal',
            description: '',
            isRepeatable: false
          };
          
          // Show success message
          this.submitMessage = '奖品添加成功！';
          this.submitStatus = 'success';
          
          // Clear message after 3 seconds
          setTimeout(() => {
            this.submitMessage = '';
          }, 3000);
        })
        .catch(error => {
          console.error('Error adding prize:', error);
          this.submitMessage = '添加失败: ' + (error.response?.data?.message || '未知错误');
          this.submitStatus = 'error';
        });
    },
    confirmDelete(prize) {
      if (confirm(`确定要删除奖品 "${prize.name}" 吗？`)) {
        this.deletePrize(prize.id);
      }
    },
    
    deletePrize(id) {
      axios.delete(`/api/prizes/${id}`)
        .then(() => {
          // Remove from local array
          this.prizes = this.prizes.filter(prize => prize.id !== id);
          
          // Show success message
          this.submitMessage = '奖品删除成功！';
          this.submitStatus = 'success';
          
          // Clear message after 3 seconds
          setTimeout(() => {
            this.submitMessage = '';
          }, 3000);
        })
        .catch(error => {
          console.error('Error deleting prize:', error);
          this.submitMessage = '删除失败: ' + (error.response?.data?.message || '未知错误');
          this.submitStatus = 'error';
        });
    }
  },
  watch: {
    'newPrize.rarity'(newVal) {
      if (newVal == 5 && !this.newPrize.fiveStarType) {
        this.newPrize.fiveStarType = 'normal';
      }
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

tbody tr:nth-child(even):not(.new-prize-row) {
  background-color: #f2f2f2;
}

tbody tr:hover:not(.new-prize-row) {
  background-color: #e0e0e0;
}

.new-prize-row {
  background-color: #f9f9f9;
  border-bottom: 2px solid #4CAF50;
}

.new-prize-row input[type="text"],
.new-prize-row select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
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

.repeatable {
  color: #2196F3;
  font-weight: bold;
}

.center-content {
  text-align: center;
}

.checkbox-center {
  display: block;
  margin: 0 auto;
}

.add-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
}

.add-btn:hover {
  background-color: #45a049;
}

.add-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.message {
  margin: 15px auto;
  padding: 10px;
  border-radius: 4px;
  text-align: center;
  max-width: 600px;
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

.five-star-type {
  margin-top: 5px;
  width: 100%;
}

.column-toggle {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
  font-size: 14px;
}

.column-toggle label {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.column-toggle input[type="checkbox"] {
  margin-right: 6px;
}

.floating-add-btn-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 100;
}

.floating-add-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 15px 20px;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.floating-add-btn:hover {
  background-color: #45a049;
  box-shadow: 0 6px 12px rgba(0,0,0,0.3);
}

.floating-add-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.delete-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 6px 12px;
  font-size: 14px;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
}

.delete-btn:hover {
  background-color: #d32f2f;
}
</style>
