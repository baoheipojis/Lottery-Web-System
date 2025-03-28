<template>
  <div>
    <h1>All Prizes</h1>
    
    <!-- Draw Card Button -->
    <div class="draw-section">
      <button @click="drawPrize" class="draw-btn" :disabled="isDrawing">
        {{ isDrawing ? '抽取中...' : '抽 卡' }}
      </button>
      <button @click="drawTenPrizes" class="draw-btn ten-draw" :disabled="isDrawing">
        {{ isDrawing ? '抽取中...' : '十连抽' }}
      </button>
    </div>
    
    <!-- Display status messages -->
    <div v-if="submitMessage" :class="['message', submitStatus]">
      {{ submitMessage }}
    </div>
    
    <!-- Input row visibility toggle -->
    <div class="row-toggle">
      <label>
        <input type="checkbox" v-model="showAddRow">
        显示添加行
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
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <!-- New Prize Input Row - conditionally shown -->
        <tr v-if="showAddRow" class="new-prize-row">
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
          <td>
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
          <td>
            <div v-if="editMode[prize.id]?.name">
              <input v-model="editData[prize.id].name" class="edit-input" />
              <button @click="saveField(prize.id, 'name')" class="save-btn">✓</button>
              <button @click="cancelEdit(prize.id, 'name')" class="cancel-btn">✗</button>
            </div>
            <div v-else class="cell-with-edit">
              {{ prize.name }}
              <button @click="startEdit(prize, 'name')" class="edit-btn">✎</button>
            </div>
          </td>
          
          <td>
            <div v-if="editMode[prize.id]?.rarity">
              <select v-model="editData[prize.id].rarity" class="edit-input">
                <option value="3">3星</option>
                <option value="4">4星</option>
                <option value="5">5星</option>
              </select>
              <select 
                v-if="editData[prize.id].rarity == 5"
                v-model="editData[prize.id].fiveStarType"
                class="five-star-type-edit"
              >
                <option value="normal">普通</option>
                <option value="limited">限定</option>
              </select>
              <button @click="saveField(prize.id, 'rarity')" class="save-btn">✓</button>
              <button @click="cancelEdit(prize.id, 'rarity')" class="cancel-btn">✗</button>
            </div>
            <div v-else class="cell-with-edit">
              {{ prize.rarity === 5
                  ? (prize.fiveStarType?.toUpperCase() === 'LIMITED' ? '限定五星' : '普通五星')
                  : (prize.rarity + '星')
              }}
              <button @click="startEdit(prize, 'rarity')" class="edit-btn">✎</button>
            </div>
          </td>
          
          <td>
            <div v-if="editMode[prize.id]?.description">
              <input v-model="editData[prize.id].description" class="edit-input" />
              <button @click="saveField(prize.id, 'description')" class="save-btn">✓</button>
              <button @click="cancelEdit(prize.id, 'description')" class="cancel-btn">✗</button>
            </div>
            <div v-else class="cell-with-edit">
              {{ prize.description }}
              <button @click="startEdit(prize, 'description')" class="edit-btn">✎</button>
            </div>
          </td>
          
          <td class="center-content" :class="{'repeatable': isRepeatable(prize)}">
            <div v-if="editMode[prize.id]?.isRepeatable">
              <input 
                v-model="editData[prize.id].isRepeatable" 
                type="checkbox"
                class="checkbox-center"
              />
              <button @click="saveField(prize.id, 'isRepeatable')" class="save-btn">✓</button>
              <button @click="cancelEdit(prize.id, 'isRepeatable')" class="cancel-btn">✗</button>
            </div>
            <div v-else class="cell-with-edit">
              {{ isRepeatable(prize) ? '是' : '否' }}
              <button @click="startEdit(prize, 'isRepeatable')" class="edit-btn">✎</button>
            </div>
          </td>
          
          <td>
            <button 
              @click="confirmDelete(prize)"
              class="delete-btn"
            >删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    
    <!-- Floating add button when input row is hidden -->
    <div v-if="!showAddRow" class="floating-add-btn-container">
      <button 
        @click="toggleAddRow" 
        class="floating-add-btn"
        title="显示添加行"
      >
        添加奖品
      </button>
    </div>
    
    <!-- Draw Result Modal -->
    <div v-if="showDrawModal" class="modal-backdrop" @click="closeDrawModal">
      <div class="modal-content" :class="{
        'rarity-3': drawnPrize && drawnPrize.rarity === 3,
        'rarity-4': drawnPrize && drawnPrize.rarity === 4,
        'rarity-5': drawnPrize && drawnPrize.rarity === 5,
        'multi-draw': isMultiDraw
      }" @click.stop>
        <button class="close-modal" @click="closeDrawModal">&times;</button>
        
        <div class="modal-header">
          <h2>恭喜！</h2>
          <div v-if="isMultiDraw" class="draw-navigation">
            <span>{{ currentDrawIndex + 1 }} / {{ drawnPrizes.length }}</span>
            <div class="nav-buttons">
              <button @click="prevPrize" :disabled="currentDrawIndex === 0">&lt;</button>
              <button @click="nextPrize" :disabled="currentDrawIndex >= drawnPrizes.length - 1">&gt;</button>
            </div>
          </div>
        </div>
        
        <div class="modal-body">
          <div class="prize-card" v-if="!isMultiDraw">
            <div class="prize-rarity">
              {{ drawnPrize?.rarity === 5
                  ? (drawnPrize.fiveStarType?.toUpperCase() === 'LIMITED' ? '限定五星' : '普通五星')
                  : (drawnPrize?.rarity + '星')
              }}
            </div>
            <div class="prize-name">{{ drawnPrize?.name }}</div>
            <div class="prize-desc">{{ drawnPrize?.description }}</div>
            <div class="prize-repeatable">
              {{ isRepeatable(drawnPrize) ? '可重复获取' : '不可重复获取' }}
            </div>
          </div>
          
          <div v-if="isMultiDraw" class="multi-draw-container">
            <div class="prize-card" v-if="currentDrawPrize">
              <div class="prize-rarity">
                {{ currentDrawPrize?.rarity === 5
                    ? (currentDrawPrize.fiveStarType?.toUpperCase() === 'LIMITED' ? '限定五星' : '普通五星')
                    : (currentDrawPrize?.rarity + '星')
                }}
              </div>
              <div class="prize-name">{{ currentDrawPrize?.name }}</div>
              <div class="prize-desc">{{ currentDrawPrize?.description }}</div>
              <div class="prize-repeatable">
                {{ isRepeatable(currentDrawPrize) ? '可重复获取' : '不可重复获取' }}
              </div>
            </div>
            
            <div class="prize-summary">
              <div class="summary-title">十连抽结果统计：</div>
              <div class="rarity-counts">
                <span class="rarity-count rarity-3">3星: {{ countRarity(3) }}</span>
                <span class="rarity-count rarity-4">4星: {{ countRarity(4) }}</span>
                <span class="rarity-count rarity-5">5星: {{ countRarity(5) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeDrawModal" class="modal-btn">确定</button>
          <button @click="isMultiDraw ? drawTenPrizes() : drawAgain()" class="modal-btn draw-again-btn">
            {{ isMultiDraw ? '再十连抽' : '再抽一次' }}
          </button>
        </div>
      </div>
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
      showAddRow: true,
      isDrawing: false,
      drawnPrize: null,
      showDrawModal: false,
      // New properties for 10-draw
      isMultiDraw: false,
      drawnPrizes: [],
      currentDrawIndex: 0,
      drawCount: 0,
      editMode: {},
      editData: {}
    };
  },
  created() {
    this.fetchPrizes();
  },
  computed: {
    isFormValid() {
      return this.newPrize.name && this.newPrize.rarity;
    },
    currentDrawPrize() {
      if (this.drawnPrizes.length > 0 && this.currentDrawIndex < this.drawnPrizes.length) {
        return this.drawnPrizes[this.currentDrawIndex];
      }
      return null;
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
        .then(response => {
          // Remove from local array
          this.prizes = this.prizes.filter(prize => prize.id !== id);
          
          // Show success message (use the message from backend if available)
          this.submitMessage = response.data?.message || '奖品删除成功！';
          this.submitStatus = 'success';
          
          // Clear message after 3 seconds
          setTimeout(() => {
            this.submitMessage = '';
          }, 3000);
        })
        .catch(error => {
          console.error('Error deleting prize:', error);
          
          // Extract more detailed error message if available
          let errorMessage = '删除失败';
          
          if (error.response) {
            // The server responded with a status code outside of 2xx range
            if (error.response.data && error.response.data.message) {
              errorMessage += ': ' + error.response.data.message;
            } else if (error.response.status === 404) {
              errorMessage += ': 奖品不存在或已被删除';
            } else {
              errorMessage += ': 服务器响应错误 (状态码: ' + error.response.status + ')';
            }
          } else if (error.request) {
            // The request was made but no response was received
            errorMessage += ': 未收到服务器响应，请检查网络连接';
          } else {
            // Something happened in setting up the request
            errorMessage += ': ' + error.message;
          }
          
          this.submitMessage = errorMessage;
          this.submitStatus = 'error';
          
          // Clear error message after 5 seconds
          setTimeout(() => {
            this.submitMessage = '';
          }, 5000);
        });
    },
    toggleAddRow() {
      this.showAddRow = true;
      // Focus on the first input field after a short delay to allow rendering
      setTimeout(() => {
        const firstInput = document.querySelector('.new-prize-row input[type="text"]');
        if (firstInput) firstInput.focus();
      }, 100);
    },
    drawPrize() {
      this.isDrawing = true;
      this.isMultiDraw = false;
      
      axios.get('/api/prizes/draw')
        .then(response => {
          this.drawnPrize = response.data;
          this.showDrawModal = true;
        })
        .catch(error => {
          console.error('Error drawing prize:', error);
          this.submitMessage = '抽卡失败: ' + (error.response?.data?.message || '未知错误');
          this.submitStatus = 'error';
        })
        .finally(() => {
          this.isDrawing = false;
        });
    },
    
    async drawTenPrizes() {
      this.isDrawing = true;
      this.isMultiDraw = true;
      this.drawnPrizes = [];
      this.currentDrawIndex = 0;
      this.drawCount = 10;
      
      try {
        for (let i = 0; i < 10; i++) {
          const response = await axios.get('/api/prizes/draw');
          this.drawnPrizes.push(response.data);
        }
        this.showDrawModal = true;
      } catch (error) {
        console.error('Error drawing 10 prizes:', error);
        this.submitMessage = '十连抽失败: ' + (error.response?.data?.message || '未知错误');
        this.submitStatus = 'error';
      } finally {
        this.isDrawing = false;
      }
    },
    
    closeDrawModal() {
      this.showDrawModal = false;
    },
    
    drawAgain() {
      this.closeDrawModal();
      // Short delay before drawing again for better UX
      setTimeout(() => {
        this.drawPrize();
      }, 300);
    },
    
    nextPrize() {
      if (this.currentDrawIndex < this.drawnPrizes.length - 1) {
        this.currentDrawIndex++;
      }
    },
    
    prevPrize() {
      if (this.currentDrawIndex > 0) {
        this.currentDrawIndex--;
      }
    },
    
    countRarity(rarity) {
      return this.drawnPrizes.filter(prize => prize.rarity === rarity).length;
    },
    
    async updatePrize(prize) {
      try {
        const body = { ...prize, name: prize.newName };
        const response = await fetch(`/api/prizes/${prize.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        });
        if (!response.ok) {
          throw new Error('Update failed');
        }
        // Handle success
        prize.name = prize.newName;
        this.submitMessage = '奖品更新成功！';
        this.submitStatus = 'success';
        setTimeout(() => {
          this.submitMessage = '';
        }, 3000);
      } catch (error) {
        console.error('Error updating prize:', error);
        this.submitMessage = '更新失败: ' + error.message;
        this.submitStatus = 'error';
        setTimeout(() => {
          this.submitMessage = '';
        }, 5000);
      }
    },
    startEdit(prize, field) {
      // Initialize edit data structure for this prize if it doesn't exist
      if (!this.editData[prize.id]) {
        this.editData[prize.id] = { ...prize };
      }
      
      // Initialize edit mode structure for this prize if it doesn't exist
      if (!this.editMode[prize.id]) {
        this.editMode[prize.id] = {};
      }
      
      // Set the field to edit mode (Vue 3 direct assignment)
      this.editMode[prize.id][field] = true;
    },
    
    cancelEdit(prizeId, field) {
      if (this.editMode[prizeId]) {
        this.editMode[prizeId][field] = false;
      }
    },
    
    async saveField(prizeId, field) {
      try {
        const prize = this.prizes.find(p => p.id === prizeId);
        if (!prize || !this.editData[prizeId]) return;
        
        // Create payload with just the updated field
        const payload = { ...prize };
        
        if (field === 'rarity') {
          // Handle special case for rarity since it might affect fiveStarType
          payload.rarity = parseInt(this.editData[prizeId].rarity);
          if (payload.rarity === 5) {
            payload.fiveStarType = this.editData[prizeId].fiveStarType;
          } else {
            payload.fiveStarType = null;
          }
        } else {
          // For other fields, just copy the edited value
          payload[field] = this.editData[prizeId][field];
        }
        
        const response = await fetch(`/api/prizes/${prizeId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        
        if (!response.ok) {
          throw new Error('Update failed');
        }
        
        const updatedPrize = await response.json();
        
        // Update the prize in the local array
        const index = this.prizes.findIndex(p => p.id === prizeId);
        if (index !== -1) {
          this.prizes[index] = updatedPrize;
        }
        
        // Turn off edit mode for this field
        this.cancelEdit(prizeId, field);
        
        this.submitMessage = '奖品更新成功！';
        this.submitStatus = 'success';
        setTimeout(() => {
          this.submitMessage = '';
        }, 3000);
      } catch (error) {
        console.error('Error updating prize:', error);
        this.submitMessage = '更新失败: ' + error.message;
        this.submitStatus = 'error';
        setTimeout(() => {
          this.submitMessage = '';
        }, 5000);
      }
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

.row-toggle {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
  font-size: 14px;
}

.row-toggle label {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.row-toggle input[type="checkbox"] {
  margin-right: 6px;
}

.draw-section {
  display: flex;
  justify-content: center;
  margin: 30px 0;
  gap: 15px;
}

.draw-btn {
  background: linear-gradient(135deg, #ff9800, #ff5722);
  color: white;
  border: none;
  padding: 15px 60px;
  font-size: 20px;
  font-weight: bold;
  border-radius: 50px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(255, 87, 34, 0.4);
  transition: all 0.3s ease;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.draw-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(255, 87, 34, 0.5);
}

.draw-btn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 5px rgba(255, 87, 34, 0.5);
}

.draw-btn:disabled {
  background: linear-gradient(135deg, #cccccc, #999999);
  cursor: not-allowed;
  transform: translateY(0);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.ten-draw {
  background: linear-gradient(135deg, #9c27b0, #673ab7);
}

.ten-draw:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 15px rgba(156, 39, 176, 0.5);
}

.ten-draw:active {
  transform: translateY(1px);
  box-shadow: 0 2px 5px rgba(156, 39, 176, 0.5);
}

/* Modal styles */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
  position: relative;
  animation: modalAppear 0.3s ease-out;
  border: 4px solid #e0e0e0;
}

.modal-content.rarity-3 {
  border-color: #2196f3;
  box-shadow: 0 0 20px rgba(33, 150, 243, 0.5);
}

.modal-content.rarity-4 {
  border-color: #9c27b0;
  box-shadow: 0 0 20px rgba(156, 39, 176, 0.5);
}

.modal-content.rarity-5 {
  border-color: #ffc107;
  box-shadow: 0 0 30px rgba(255, 193, 7, 0.8);
}

.modal-content.multi-draw {
  max-width: 600px;
}

.close-modal {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-header {
  padding: 15px 20px;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.modal-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.draw-again-btn {
  background-color: #ff5722;
  color: white;
}

/* Prize card styles */
.prize-card {
  text-align: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.prize-rarity {
  font-size: 14px;
  margin-bottom: 5px;
  font-weight: bold;
}

.rarity-3 .prize-rarity {
  color: #2196f3;
}

.rarity-4 .prize-rarity {
  color: #9c27b0;
}

.rarity-5 .prize-rarity {
  color: #ffc107;
  text-shadow: 0 0 2px rgba(0,0,0,0.3);
}

.prize-name {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.prize-desc {
  font-size: 16px;
  color: #666;
  margin-bottom: 15px;
}

.prize-repeatable {
  font-size: 14px;
  color: #999;
}

@keyframes modalAppear {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.draw-navigation {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 10px;
  font-size: 16px;
}

.nav-buttons {
  margin-top: 5px;
}

.nav-buttons button {
  background-color: #4CAF50;
  color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin: 0 5px;
  cursor: pointer;
  font-weight: bold;
  font-size: 16px;
}

.nav-buttons button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.multi-draw-container {
  display: flex;
  flex-direction: column;
}

.prize-summary {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f0f0;
  border-radius: 6px;
  text-align: center;
}

.summary-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.rarity-counts {
  display: flex;
  justify-content: space-around;
}

.rarity-count {
  font-weight: bold;
  padding: 5px 10px;
  border-radius: 15px;
}

.rarity-count.rarity-3 {
  color: #2196f3;
}

.rarity-count.rarity-4 {
  color: #9c27b0;
}

.rarity-count.rarity-5 {
  color: #ffc107;
  text-shadow: 0 0 2px rgba(0,0,0,0.2);
}

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
  width: 80%;
  padding: 5px;
  border: 1px solid #2196F3;
  border-radius: 3px;
}

.five-star-type-edit {
  margin-top: 5px;
  width: 80%;
}

.save-btn, .cancel-btn {
  border: none;
  margin-left: 5px;
  cursor: pointer;
  padding: 2px 5px;
}

.save-btn {
  color: green;
}

.cancel-btn {
  color: red;
}
</style>
