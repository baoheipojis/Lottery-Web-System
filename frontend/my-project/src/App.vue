<template>
  <div id="app">
    <nav class="navbar">
      <router-link to="/history" class="nav-link">View Lottery History</router-link>
      <router-link to="/prizes" class="nav-link">View All Prizes</router-link>
    </nav>
    <div class="content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App'
};
</script>

<style>
body {
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
  background-color: #f5f5f5;
}

.navbar {
  background-color: #333;
  padding: 15px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: bold;
  background-color: #555;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: #777;
}

.router-link-active {
  background-color: #4CAF50;
}

.content {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}
</style>