<template>
  <div>
    <h1>{{ $t('history.title') }}</h1>
    
    <!-- 筛选控件 -->
    <div class="filter-section">
      <div class="filter-group">
        <label>{{ $t('history.filterByRarity') || '按稀有度筛选' }}:</label>
        <select v-model="selectedRarity" @change="applyFilters">
          <option value="">{{ $t('history.allRarities') || '全部稀有度' }}</option>
          <option value="3">{{ $t('prizes.threeStar') || '三星' }}</option>
          <option value="4">{{ $t('prizes.fourStar') || '四星' }}</option>
          <option value="5">{{ $t('prizes.fiveStar') || '五星' }}</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>{{ $t('history.filterByRedeemStatus') || '按兑换状态筛选' }}:</label>
        <select v-model="selectedRedeemStatus" @change="applyFilters">
          <option value="">{{ $t('history.allStatuses') || '全部状态' }}</option>
          <option value="true">{{ $t('history.redeemed') || '已兑换' }}</option>
          <option value="false">{{ $t('history.notRedeemed') || '未兑换' }}</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>{{ $t('history.filterByFiveStarType') || '按五星类型筛选' }}:</label>
        <select v-model="selectedFiveStarType" @change="applyFilters">
          <option value="">{{ $t('history.allFiveStarTypes') || '全部五星类型' }}</option>
          <option value="normal">{{ $t('prizes.fiveStarNormal') || '常驻五星' }}</option>
          <option value="limited">{{ $t('prizes.fiveStarLimited') || '限定五星' }}</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>{{ $t('history.searchPrize') || '搜索奖品名称' }}:</label>
        <input 
          type="text" 
          v-model="searchTerm" 
          @input="applyFilters"
          :placeholder="$t('history.searchPlaceholder') || '输入奖品名称...'"
          class="search-input"
        />
      </div>
      
      <div class="filter-actions">
        <button @click="clearFilters" class="clear-btn">
          {{ $t('history.clearFilters') || '清除筛选' }}
        </button>
        <span class="result-count">
          {{ $t('history.showingResults', { count: filteredHistory.length, total: history.length }) || `显示 ${filteredHistory.length} / ${history.length} 条记录` }}
        </span>
      </div>
    </div>
    
    <div v-if="loading" class="loading">{{ $t('history.loading') }}</div>
    <div v-else-if="filteredHistory.length === 0" class="no-records">
      {{ history.length === 0 ? $t('history.noRecords') : ($t('history.noFilteredRecords') || '没有符合筛选条件的记录') }}
    </div>
    <div v-else>
      <table>
        <thead>
          <tr>
            <th>{{ $t('history.time') }}</th>
            <th>{{ $t('history.prize') }}</th>
            <th>{{ $t('history.rarity') }}</th>
            <th>{{ $t('history.result') }}</th>
            <th>{{ $t('history.redeemStatus') }}</th>
            <th>{{ $t('history.operation') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedHistory" :key="item.id" :class="getRowClass(item)">
            <td>{{ formatDate(item.drawTime) }}</td>
            <td>{{ item.prizeName }}</td>
            <td>
              {{ item.rarity === 5 
                ? (item.fiveStarType?.toUpperCase() === 'LIMITED' ? $t('prizes.fiveStarLimited') : $t('prizes.fiveStarNormal')) 
                : item.rarity + $t('prizes.star') 
              }}
            </td>
            <td>{{ item.result }}</td>
            <td :class="{'redeemed': item.redeemed, 'not-redeemed': !item.redeemed}">
              {{ item.redeemed ? $t('history.redeemed') : $t('history.notRedeemed') }}
            </td>
            <td>
              <button 
                @click="toggleRedeemStatus(item)" 
                class="toggle-btn"
                :class="{'redeem-btn': !item.redeemed, 'un-redeem-btn': item.redeemed}"
              >
                {{ item.redeemed ? $t('history.markAsNotRedeemed') : $t('history.markAsRedeemed') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- Pagination controls -->
      <div class="pagination">
        <button 
          @click="goToPage(currentPage - 1)" 
          :disabled="currentPage === 1"
          class="page-btn"
        >
          {{ $t('pagination.previous') || 'Previous' }}
        </button>
        
        <!-- Page numbers -->
        <div class="page-numbers">
          <template v-for="(item, index) in displayedPages" :key="index">
            <span v-if="item === '...'" class="ellipsis">...</span>
            <button 
              v-else
              @click="goToPage(item)" 
              :class="['page-number', { active: currentPage === item }]"
            >
              {{ item }}
            </button>
          </template>
        </div>
        
        <button 
          @click="goToPage(currentPage + 1)" 
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          {{ $t('pagination.next') || 'Next' }}
        </button>
      </div>
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
      filteredHistory: [],
      loading: true,
      message: '',
      messageType: '',
      currentPage: 1,
      itemsPerPage: 10,
      // 筛选条件
      selectedRarity: '',
      selectedRedeemStatus: '',
      selectedFiveStarType: '',
      searchTerm: ''
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.filteredHistory.length / this.itemsPerPage);
    },
    paginatedHistory() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredHistory.slice(start, end);
    },
    displayedPages() {
      // Generate array of page numbers to display with ellipsis
      let result = [];
      
      if (this.totalPages <= 7) {
        // If 7 or fewer pages, show all
        for (let i = 1; i <= this.totalPages; i++) {
          result.push(i);
        }
      } else {
        // Always include first page
        result.push(1);
        
        // Add ellipsis if needed
        if (this.currentPage > 3) {
          result.push('...');
        }
        
        // Calculate range of pages around current page
        let startPage = Math.max(2, this.currentPage - 1);
        let endPage = Math.min(this.totalPages - 1, this.currentPage + 1);
        
        // Adjust for edge cases
        if (this.currentPage < 4) {
          startPage = 2;
          endPage = Math.min(5, this.totalPages - 1);
        } else if (this.currentPage > this.totalPages - 3) {
          startPage = Math.max(this.totalPages - 4, 2);
          endPage = this.totalPages - 1;
        }
        
        // Add the range of pages
        for (let i = startPage; i <= endPage; i++) {
          result.push(i);
        }
        
        // Add ellipsis if needed
        if (endPage < this.totalPages - 1) {
          result.push('...');
        }
        
        // Add last page if not already included
        if (this.totalPages > 1) {
          result.push(this.totalPages);
        }
      }
      
      return result;
    }
  },
  created() {
    this.fetchHistory();
  },
  methods: {
    fetchHistory() {
      this.loading = true;
      axios.get('/api/lottery/history')
        .then(response => {
          // Store the data as is, without additional sorting
          // The backend should already return it in newest-to-oldest order
          this.history = response.data;
          this.applyFilters(); // 应用筛选
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
      const url = newStatus 
        ? `/api/lottery/history/${item.id}/redeem` 
        : `/api/lottery/history/${item.id}/unredeem`;
      
      axios.put(url)
        .then(() => {
          // 更新本地数据
          item.redeemed = newStatus;
          
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
    },
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    applyFilters() {
      let filtered = [...this.history];
      
      // 按稀有度筛选
      if (this.selectedRarity) {
        filtered = filtered.filter(item => item.rarity === parseInt(this.selectedRarity));
      }
      
      // 按兑换状态筛选
      if (this.selectedRedeemStatus !== '') {
        const isRedeemed = this.selectedRedeemStatus === 'true';
        filtered = filtered.filter(item => item.redeemed === isRedeemed);
      }
      
      // 按五星类型筛选
      if (this.selectedFiveStarType) {
        filtered = filtered.filter(item => {
          return item.rarity === 5 && item.fiveStarType?.toLowerCase() === this.selectedFiveStarType;
        });
      }
      
      // 按奖品名称搜索
      if (this.searchTerm.trim()) {
        const searchLower = this.searchTerm.toLowerCase().trim();
        filtered = filtered.filter(item => 
          item.prizeName.toLowerCase().includes(searchLower)
        );
      }
      
      this.filteredHistory = filtered;
      this.currentPage = 1; // 重置到第一页
    },
    clearFilters() {
      this.selectedRarity = '';
      this.selectedRedeemStatus = '';
      this.selectedFiveStarType = '';
      this.searchTerm = '';
      this.applyFilters();
    }
  }
};
</script>

<style scoped>
h1 {
  text-align: center;
  margin-bottom: 20px;
}

.filter-section {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 10px;
}

.filter-group label {
  min-width: 120px;
  font-weight: bold;
  color: #333;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  font-size: 14px;
  min-width: 150px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 200px;
}

.filter-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

.clear-btn {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.clear-btn:hover {
  background-color: #5a6268;
}

.result-count {
  font-size: 14px;
  color: #666;
  font-weight: bold;
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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 20px 0;
  gap: 15px;
}

.page-numbers {
  display: flex;
  gap: 5px;
}

.page-number {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ddd;
  background-color: #f9f9f9;
  color: #333;
  font-weight: bold;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-number:hover {
  background-color: #e9e9e9;
}

.page-number.active {
  background-color: #4CAF50;
  color: white;
  border-color: #4CAF50;
}

.page-btn {
  padding: 8px 15px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.page-btn:hover:not(:disabled) {
  background-color: #388E3C;
}

.page-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  opacity: 0.7;
}

.page-info {
  font-size: 16px;
  color: #333;
}

.ellipsis {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #666;
}
</style>