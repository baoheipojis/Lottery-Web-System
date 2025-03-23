<template>
  <div>
    <h1>All Prizes</h1>
    
    <!-- Add Prize Form -->
    <div class="add-prize-form">
      <h2>添加新奖品</h2>
      <form @submit.prevent="addPrize">
        <div class="form-group">
          <label for="name">奖品名称:</label>
          <input 
            id="name" 
            v-model="newPrize.name" 
            type="text" 
            required
          />
        </div>
        
        <div class="form-group">
          <label for="rarity">稀有度:</label>
          <select 
            id="rarity" 
            v-model="newPrize.rarity" 
            required
          >
            <option value="3">3星</option>
            <option value="4">4星</option>
            <option value="5">5星</option>
          </select>
        </div>
        
        <div class="form-group" v-if="newPrize.rarity == 5">
          <label for="fiveStarType">五星类型:</label>
          <select 
            id="fiveStarType" 
            v-model="newPrize.fiveStarType"
          >
            <option value="normal">普通</option>
            <option value="limited">限定</option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="description">描述:</label>
          <textarea 
            id="description" 
            v-model="newPrize.description"
            rows="3"
          ></textarea>
        </div>
        
        <div class="form-group checkbox">
          <input 
            id="isRepeatable" 
            v-model="newPrize.isRepeatable" 
            type="checkbox"
          />
          <label for="isRepeatable">可重复获取</label>
        </div>
        
        <button type="submit" class="submit-btn">添加奖品</button>
      </form>
      <div v-if="submitMessage" :class="['message', submitStatus]">
        {{ submitMessage }}
      </div>
    </div>
    
    <!-- Prize Table -->
    <table>
      <thead>
        <tr>
          <th>Prize Name</th>
          <th>Rarity</th>
          <th>是否可重复</th>
        </tr>
      </thead>
      <tbody>
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
          <td :class="{'repeatable': isRepeatable(prize)}">{{ isRepeatable(prize) ? '是' : '否' }}</td>
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
      prizes: [],
      newPrize: {
        name: '',
        rarity: 3,
        fiveStarType: null,
        description: '',
        isRepeatable: false
      },
      submitMessage: '',
      submitStatus: ''
    };
  },
  created() {
    this.fetchPrizes();
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
            rarity: 3,
            fiveStarType: null,
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

.repeatable {
  color: #2196F3;
  font-weight: bold;
}

.add-prize-form {
  max-width: 600px;
  margin: 0 auto 30px auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

h2 {
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
}

input[type="text"], 
select, 
textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.checkbox {
  display: flex;
  align-items: center;
}

.checkbox input {
  margin-right: 10px;
}

.checkbox label {
  margin: 0;
}

.submit-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 12px 20px;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
  margin-top: 10px;
}

.submit-btn:hover {
  background-color: #45a049;
}

.message {
  margin-top: 15px;
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
</style>
