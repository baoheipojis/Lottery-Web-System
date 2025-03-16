import { createRouter, createWebHistory } from 'vue-router';
import HistoryList from '../components/HistoryList.vue';

const routes = [
    {
        path: '/history',
        name: 'HistoryList',
        component: HistoryList
    },
    {
        path: '/prizes',
        name: 'PrizeList',
        component: () => import('../components/PrizeList.vue')
    }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;