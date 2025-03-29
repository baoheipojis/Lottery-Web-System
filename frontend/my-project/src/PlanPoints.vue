<template>
  <div>
    <h1>Plan Points</h1>
    <div class="summary-card">
      <h2>Current Plan Points: <span class="plan-points-value">{{ currentPlanPoints }}</span></h2>
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
      planPointsRecords: []
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
    }
  },
  async created() {
    try {
      const response = await fetch('/api/plan-points');
      const data = await response.json();
      this.currentPlanPoints = data.currentPlanPoints;
      this.planPointsRecords = data.planPointsRecords;
    } catch (error) {
      console.error('Error fetching plan points data:', error);
    }
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
</style>