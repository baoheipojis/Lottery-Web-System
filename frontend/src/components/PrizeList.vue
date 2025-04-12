<template>
  <div>
    <div class="header-section">
      <h1>{{ $t('prizes.title') }}</h1>
      
      <!-- Lottery explanation button -->
      <div class="lottery-info-container">
        <button 
          class="info-button" 
          @click="toggleExplanationModal" 
          :title="$t('prizes.explanation.title')"
        >
          <i class="info-icon">ℹ</i>
        </button>
      </div>
    </div>
    
    <!-- Draw Card Button -->
    <div class="draw-section">
      <button @click="drawPrize" class="draw-btn" :disabled="isDrawing">
        {{ isDrawing ? $t('prizes.drawing') : $t('prizes.drawCard') }}
      </button>
      <button @click="drawTenPrizes" class="draw-btn ten-draw" :disabled="isDrawing">
        {{ isDrawing ? $t('prizes.drawing') : $t('prizes.tenDraw') }}
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
        {{ $t('prizes.showAddRow') }}
      </label>
    </div>
    
    <!-- Prize Table -->
    <table>
      <thead>
        <tr>
          <th>{{ $t('prizes.prizeName') }}</th>
          <th>{{ $t('prizes.rarity') }}</th>
          <th>{{ $t('prizes.description') }}</th>
          <th>{{ $t('prizes.repeatable') }}</th>
          <th>{{ $t('prizes.status') }}</th>
          <th>{{ $t('prizes.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <!-- New Prize Input Row - conditionally shown -->
        <tr v-if="showAddRow" class="new-prize-row">
          <td>
            <input 
              v-model="newPrize.name" 
              type="text" 
              :placeholder="$t('prizes.inputName')" 
              required
            />
          </td>
          <td>
            <select 
              v-model="newPrize.rarity" 
              required
            >
              <option value="" disabled selected>{{ $t('prizes.selectRarity') }}</option>
              <option value="3">3{{ $t('prizes.star') }}</option>
              <option value="4">4{{ $t('prizes.star') }}</option>
              <option value="5">5{{ $t('prizes.star') }}</option>
            </select>
            
            <select 
              v-if="newPrize.rarity == 5"
              v-model="newPrize.fiveStarType"
              class="five-star-type"
            >
              <option value="normal">{{ $t('prizes.fiveStarNormal') }}</option>
              <option value="limited">{{ $t('prizes.fiveStarLimited') }}</option>
            </select>
          </td>
          <td>
            <input 
              v-model="newPrize.description"
              type="text"
              :placeholder="$t('prizes.prizeDescription')"
            />
          </td>
          <td>
            <input 
              v-model="newPrize.isRepeatable" 
              type="checkbox"
              class="checkbox-center"
            />
          </td>
          <td class="center-content">
            <input 
              v-model="newPrize.enabled" 
              type="checkbox"
              class="checkbox-center"
              checked
            />
          </td>
          <td>
            <button 
              @click="addPrize" 
              class="add-btn"
              :disabled="!isFormValid"
            >{{ $t('prizes.addPrize') }}</button>
          </td>
        </tr>
        
        <!-- Existing Prizes -->
        <tr 
          v-for="prize in prizes" 
          :key="prize.id"
          :class="{
            'rarity-purple': prize.rarity === 4,
            'rarity-yellow': prize.rarity === 5,
            'disabled-prize': !prize.enabled
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
                <option value="3">3{{ $t('prizes.star') }}</option>
                <option value="4">4{{ $t('prizes.star') }}</option>
                <option value="5">5{{ $t('prizes.star') }}</option>
              </select>
              <select 
                v-if="editData[prize.id].rarity == 5"
                v-model="editData[prize.id].fiveStarType"
                class="five-star-type-edit"
              >
                <option value="normal">{{ $t('prizes.fiveStarNormal') }}</option>
                <option value="limited">{{ $t('prizes.fiveStarLimited') }}</option>
              </select>
              <button @click="saveField(prize.id, 'rarity')" class="save-btn">✓</button>
              <button @click="cancelEdit(prize.id, 'rarity')" class="cancel-btn">✗</button>
            </div>
            <div v-else class="cell-with-edit">
              {{ prize.rarity === 5
                  ? (prize.fiveStarType?.toUpperCase() === 'LIMITED' ? $t('prizes.fiveStarLimited') : $t('prizes.fiveStarNormal'))
                  : (prize.rarity + $t('prizes.star'))
              }}
              <button @click="startEdit(prize, 'rarity')" class="edit-btn">✎</button>
            </div>
          </td>
          
          <td>
            <div v-if="editMode[prize.id]?.description">
              <textarea v-model="editData[prize.id].description" rows="4" class="edit-input"></textarea>
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
              {{ isRepeatable(prize) ? $t('prizes.repeatableYes') : $t('prizes.repeatableNo') }}
              <button @click="startEdit(prize, 'isRepeatable')" class="edit-btn">✎</button>
            </div>
          </td>
          
          <td class="center-content">
            <div v-if="editMode[prize.id]?.enabled">
              <input 
                v-model="editData[prize.id].enabled" 
                type="checkbox"
                class="checkbox-center"
              />
              <button @click="saveField(prize.id, 'enabled')" class="save-btn">✓</button>
              <button @click="cancelEdit(prize.id, 'enabled')" class="cancel-btn">✗</button>
            </div>
            <div v-else class="cell-with-edit">
              <span :class="{'enabled': prize.enabled, 'disabled': !prize.enabled}">
                {{ prize.enabled ? $t('prizes.enabled') : $t('prizes.disabled') }}
              </span>
              <button @click="startEdit(prize, 'enabled')" class="edit-btn">✎</button>
            </div>
          </td>
          
          <td>
            <button 
              @click="togglePrizeStatus(prize)"
              class="toggle-status-btn"
              :class="{'enable-btn': !prize.enabled, 'disable-btn': prize.enabled}"
            >
              {{ prize.enabled ? $t('prizes.disable') : $t('prizes.enable') }}
            </button>
            <button 
              @click="confirmDelete(prize)"
              class="delete-btn"
            >{{ $t('prizes.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
    
    <!-- Floating add button when input row is hidden -->
    <div v-if="!showAddRow" class="floating-add-btn-container">
      <button 
        @click="toggleAddRow" 
        class="floating-add-btn"
        :title="$t('prizes.showAddRow')"
      >
        {{ $t('prizes.addPrize') }}
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
          <h2>{{ $t('prizes.congratulations') }}</h2>
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
                  ? (drawnPrize.fiveStarType?.toUpperCase() === 'LIMITED' ? $t('prizes.fiveStarLimited') : $t('prizes.fiveStarNormal'))
                  : (drawnPrize?.rarity + $t('prizes.star'))
              }}
            </div>
            <div class="prize-name">{{ drawnPrize?.name }}</div>
            <div class="prize-desc">{{ drawnPrize?.description }}</div>
            <div class="prize-repeatable">
              {{ isRepeatable(drawnPrize) ? $t('prizes.repeatableYes') : $t('prizes.repeatableNo') }}
            </div>
          </div>
          
          <div v-if="isMultiDraw" class="multi-draw-container">
            <div class="prize-card" v-if="currentDrawPrize">
              <div class="prize-rarity">
                {{ currentDrawPrize?.rarity === 5
                    ? (currentDrawPrize.fiveStarType?.toUpperCase() === 'LIMITED' ? $t('prizes.fiveStarLimited') : $t('prizes.fiveStarNormal'))
                    : (currentDrawPrize?.rarity + $t('prizes.star'))
                }}
              </div>
              <div class="prize-name">{{ currentDrawPrize?.name }}</div>
              <div class="prize-desc">{{ currentDrawPrize?.description }}</div>
              <div class="prize-repeatable">
                {{ isRepeatable(currentDrawPrize) ? $t('prizes.repeatableYes') : $t('prizes.repeatableNo') }}
              </div>
            </div>
            
            <div class="prize-summary">
              <div class="summary-title">{{ $t('prizes.tenDrawSummary') }}</div>
              <div class="rarity-counts">
                <span class="rarity-count rarity-3">3{{ $t('prizes.star') }}: {{ countRarity(3) }}</span>
                <span class="rarity-count rarity-4">4{{ $t('prizes.star') }}: {{ countRarity(4) }}</span>
                <span class="rarity-count rarity-5">5{{ $t('prizes.star') }}: {{ countRarity(5) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeDrawModal" class="modal-btn">{{ $t('prizes.confirm') }}</button>
          <button @click="isMultiDraw ? drawTenPrizes() : drawAgain()" class="modal-btn draw-again-btn">
            {{ isMultiDraw ? $t('prizes.tenDrawAgain') : $t('prizes.drawAgain') }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- Lottery explanation modal -->
    <div v-if="showExplanation" class="explanation-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ $t('prizes.explanation.title') }}</h2>
          <div class="modal-actions">
            <button 
              @click="isSimpleExplanation = true" 
              class="toggle-btn"
              :class="{ active: isSimpleExplanation }"
            >
              {{ $t('prizes.explanation.simple') }}
            </button>
            <button 
              @click="isSimpleExplanation = false" 
              class="toggle-btn"
              :class="{ active: !isSimpleExplanation }"
            >
              {{ $t('prizes.explanation.complete') }}
            </button>
            <button class="close-btn" @click="showExplanation = false">×</button>
          </div>
        </div>
        
        <!-- Simple explanation -->
        <div v-if="isSimpleExplanation" class="explanation-content">
          <ol>
            <li>{{ $t('prizes.explanation.simple1') }}</li>
            <li>{{ $t('prizes.explanation.simple2') }}</li>
            <li>{{ $t('prizes.explanation.simple3') }}</li>
            <li>{{ $t('prizes.explanation.simple4') }}</li>
            <li>{{ $t('prizes.explanation.simple5') }}</li>
          </ol>
        </div>
        
        <!-- Complete explanation -->
        <div v-else class="explanation-content">
          <ol start="0">
            <li>{{ $t('prizes.explanation.complete1') }}</li>
            <li>{{ $t('prizes.explanation.complete2') }}</li>
            <li>{{ $t('prizes.explanation.complete3') }}</li>
            <li>{{ $t('prizes.explanation.complete4') }}</li>
            <li>{{ $t('prizes.explanation.complete5') }}</li>
            <li>{{ $t('prizes.explanation.complete6') }}</li>
          </ol>
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
        isRepeatable: false,
        enabled: true // 默认启用
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
      editData: {},
      // Lottery explanation properties
      showExplanation: false,
      isSimpleExplanation: true // true for simple, false for complete
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
            isRepeatable: false,
            enabled: true
          };
          
          // Show success message
          this.submitMessage = this.$t('prizes.addSuccess');
          this.submitStatus = 'success';
          
          // Clear message after 3 seconds
          setTimeout(() => {
            this.submitMessage = '';
          }, 3000);
        })
        .catch(error => {
          console.error('Error adding prize:', error);
          let errorMessage = this.$t('prizes.addError');
          
          if (error.response && error.response.data) {
            if (error.response.data.errorType === 'VALIDATION_ERROR') {
              errorMessage = error.response.data.message || this.$t('prizes.validationError');
            } else {
              errorMessage = error.response.data.message || this.$t('prizes.unknownError');
            }
          }
          
          this.submitMessage = errorMessage;
          this.submitStatus = 'error';
        });
    },
    confirmDelete(prize) {
      if (confirm(`${this.$t('prizes.confirmDelete')} "${prize.name}" ${this.$t('prizes.confirmDelete')}?`)) {
        this.deletePrize(prize.id);
      }
    },
    
    deletePrize(id) {
      axios.delete(`/api/prizes/${id}`)
        .then(response => {
          // Remove from local array
          this.prizes = this.prizes.filter(prize => prize.id !== id);
          
          // Show success message (use the message from backend if available)
          this.submitMessage = response.data?.message || this.$t('prizes.deleteSuccess');
          this.submitStatus = 'success';
          
          // Clear message after 3 seconds
          setTimeout(() => {
            this.submitMessage = '';
          }, 3000);
        })
        .catch(error => {
          console.error('Error deleting prize:', error);
          
          // Extract more detailed error message if available
          let errorMessage = this.$t('prizes.deleteError');
          
          if (error.response) {
            // The server responded with a status code outside of 2xx range
            if (error.response.data && error.response.data.message) {
              errorMessage += ': ' + error.response.data.message;
            } else if (error.response.status === 404) {
              errorMessage += ': ' + this.$t('prizes.notFound');
            } else {
              errorMessage += ': ' + this.$t('prizes.serverError') + ' (' + error.response.status + ')';
            }
          } else if (error.request) {
            // The request was made but no response was received
            errorMessage += ': ' + this.$t('prizes.noResponse');
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
          let errorMessage = this.$t('prizes.drawError');
          
          if (error.response && error.response.data) {
            // 处理特定类型的错误
            if (error.response.data.errorType === 'INSUFFICIENT_POINTS') {
              errorMessage = error.response.data.message || this.$t('prizes.insufficientPoints');
            } else if (error.response.data.errorType === 'NO_PRIZE_AVAILABLE') {
              errorMessage = error.response.data.message || this.$t('prizes.noPrizeAvailable');
            } else {
              errorMessage = error.response.data.message || this.$t('prizes.drawProcessError');
            }
          }
          
          this.submitMessage = errorMessage;
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
          try {
            const response = await axios.get('/api/prizes/draw');
            this.drawnPrizes.push(response.data);
          } catch (error) {
            console.error(`Error on draw ${i+1}:`, error);
            // 处理具体的错误类型
            let errorMessage = this.$t('prizes.drawError');
            
            if (error.response && error.response.data) {
              if (error.response.data.errorType === 'INSUFFICIENT_POINTS') {
                errorMessage = `${this.$t('prizes.drawError')} ${i+1}: ${error.response.data.message || this.$t('prizes.insufficientPoints')}`;
              } else if (error.response.data.errorType === 'NO_PRIZE_AVAILABLE') {
                errorMessage = `${this.$t('prizes.drawError')} ${i+1}: ${error.response.data.message || this.$t('prizes.noPrizeAvailable')}`;
              } else {
                errorMessage = `${this.$t('prizes.drawError')} ${i+1}: ${error.response.data.message || this.$t('prizes.drawProcessError')}`;
              }
            }
            
            // 显示错误，但继续尝试其他抽卡
            this.submitMessage = errorMessage;
            this.submitStatus = 'error';
            break; // 中断十连抽
          }
        }
        
        if (this.drawnPrizes.length > 0) {
          this.showDrawModal = true;
        }
      } catch (error) {
        console.error('Error drawing 10 prizes:', error);
        this.submitMessage = this.$t('prizes.tenDrawError') + ': ' + (error.response?.data?.message || this.$t('prizes.unknownError'));
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
          throw new Error(this.$t('prizes.updateFailed'));
        }
        // Handle success
        prize.name = prize.newName;
        this.submitMessage = this.$t('prizes.updateSuccess');
        this.submitStatus = 'success';
        setTimeout(() => {
          this.submitMessage = '';
        }, 3000);
      } catch (error) {
        console.error('Error updating prize:', error);
        this.submitMessage = this.$t('prizes.updateError') + ': ' + error.message;
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
          throw new Error(this.$t('prizes.updateFailed'));
        }
        
        const updatedPrize = await response.json();
        
        // Update the prize in the local array
        const index = this.prizes.findIndex(p => p.id === prizeId);
        if (index !== -1) {
          this.prizes[index] = updatedPrize;
        }
        
        // Turn off edit mode for this field
        this.cancelEdit(prizeId, field);
        
        this.submitMessage = this.$t('prizes.updateSuccess');
        this.submitStatus = 'success';
        setTimeout(() => {
          this.submitMessage = '';
        }, 3000);
      } catch (error) {
        console.error('Error updating prize:', error);
        this.submitMessage = this.$t('prizes.updateError') + ': ' + error.message;
        this.submitStatus = 'error';
        setTimeout(() => {
          this.submitMessage = '';
        }, 5000);
      }
    },
    togglePrizeStatus(prize) {
      const newStatus = !prize.enabled;
      const statusText = newStatus ? this.$t('prizes.enable') : this.$t('prizes.disable');
      
      axios.put(`/api/prizes/${prize.id}/toggle`, { enabled: newStatus })
        .then(response => {
          // 更新本地奖品数据
          const index = this.prizes.findIndex(p => p.id === prize.id);
          if (index !== -1) {
            this.prizes[index] = response.data;
          }
          
          this.submitMessage = `${this.$t('prizes.prize')} "${prize.name}" ${statusText}`;
          this.submitStatus = 'success';
          
          // 3秒后清除消息
          setTimeout(() => {
            this.submitMessage = '';
          }, 3000);
        })
        .catch(error => {
          console.error(`切换奖品状态失败:`, error);
          this.submitMessage = `${statusText} ${this.$t('prizes.prize')} ${this.$t('prizes.failed')}: ${error.response?.data?.message || this.$t('prizes.unknownError')}`;
          this.submitStatus = 'error';
          
          setTimeout(() => {
            this.submitMessage = '';
          }, 5000);
        });
    },
    toggleExplanationModal() {
      this.showExplanation = !this.showExplanation;
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
  transition: background-color 0.2s;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.toggle-status-btn {
  margin-right: 5px;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.enable-btn {
  background-color: #4CAF50;
  color: white;
}

.enable-btn:hover {
  background-color: #388E3C;
}

.disable-btn {
  background-color: #FFC107;
  color: #333;
}

.disable-btn:hover {
  background-color: #FFA000;
}

.disabled-prize {
  opacity: 0.6;
}

.enabled {
  color: #4CAF50;
  font-weight: bold;
}

.disabled {
  color: #F44336;
  font-weight: bold;

}

.draw-section {
  display: flex;
  justify-content: center;
  margin: 20px 0;
  gap: 15px;
}

.draw-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 12px 30px;
  font-size: 18px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.draw-btn:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
}

.draw-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.draw-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.ten-draw {
  background-color: #9C27B0;
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

/* Header section with info button */
.header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

/* Info button styling */
.lottery-info-container {
  position: relative;
}

.info-button {
  background-color: #2196F3;
  color: white;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s;
}

.info-button:hover {
  background-color: #1976D2;
}

.info-icon {
  font-style: normal;
  font-weight: bold;
}

/* Modal styling */
.explanation-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.explanation-modal .modal-content {
  background-color: white;
  border-radius: 8px;
  padding: 0;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.modal-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle-btn {
  padding: 6px 12px;
  background-color: #e0e0e0;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.toggle-btn.active {
  background-color: #2196F3;
  color: white;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #757575;
  margin-left: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: #e0e0e0;
  color: #333;
}

.explanation-content {
  padding: 20px;
  line-height: 1.6;
}

.explanation-content ol {
  margin: 0;
  padding-left: 20px;
}

.explanation-content li {
  margin-bottom: 10px;
}

.explanation-content li:last-child {
  margin-bottom: 0;
}

/* Row toggle styling */
.row-toggle {
  text-align: right;
  margin-bottom: 10px;
  font-size: 14px;
}

.row-toggle label {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.row-toggle input[type="checkbox"] {
  margin-right: 5px;
}

/* Mobile responsiveness improvements */
@media (max-width: 768px) {
  .habits-list {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    flex-direction: column;
  }
  
  .header-section {
    flex-direction: column;
    gap: 10px;
  }
  
  .modal-content {
    width: 95%;
    max-width: none;
  }
}
</style>
