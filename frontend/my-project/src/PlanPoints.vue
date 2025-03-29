<template>
  <div>
    <h2>Current Plan Points: {{ currentPlanPoints }}</h2>
    <h3>Plan Points Records</h3>
    <ul>
      <li v-for="record in planPointsRecords" :key="record.id">
        Changed by {{ record.amountChange }} at {{ record.timestamp }}
      </li>
    </ul>
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

ul {
  list-style-type: none;
  padding: 0;
}

li {
  background-color: #fff;
  margin: 10px 0;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style>