<template>
  <div>
    <h1>Lottery History</h1>
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
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      histories: []
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
</style>