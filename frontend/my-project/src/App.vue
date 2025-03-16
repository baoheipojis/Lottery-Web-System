<template>
  <div id="app">
    <router-link to="/history">View Lottery History</router-link>
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'App'
};
</script>