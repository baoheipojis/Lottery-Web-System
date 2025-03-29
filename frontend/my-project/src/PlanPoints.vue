<template>
  <div>
    <h2>Current Plan Points: {{ currentPlanPoints }}</h2>
    <h3>Plan Points Records</h3>
    <table>
      <thead>
        <tr>
          <th>Amount Change</th>
          <th>Timestamp</th>
          <th>Balance After Operation</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="record in planPointsRecords" :key="record.id">
          <td>{{ record.amountChange }}</td>
          <td>{{ record.timestamp }}</td>
          <td>{{ record.balanceAfterOperation }}</td>
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

<style>
body {
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
  background-color: #f5f5f5;
}

h2, h3 {
  color: #333;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

th, td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

th {
  background-color: #f4f4f4;
}

tr:nth-child(even) {
  background-color: #f9f9f9;
}
</style>