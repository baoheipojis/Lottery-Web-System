<template>
  <div>
    <h1>All Prizes</h1>
    <table>
      <thead>
        <tr>
          <th>Prize Name</th>
          <th>Rarity</th>
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
      prizes: []
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
        })
        .catch(error => {
          console.error('Error fetching prizes:', error);
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
