```vue
<template>
  <div>
    <h1>Lottery History</h1>
    <ul>
      <li v-for="history in histories" :key="history.id">
        {{ history.drawTime }} - {{ history.prizeName }} ({{ history.rarity }} stars)
      </li>
    </ul>
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
          this.histories = response.data;
        })
        .catch(error => {
          console.error('Error fetching histories:', error);
        });
    }
  }
};
</script>