import { createRouter, createWebHistory } from 'vue-router';
import HistoryList from '../components/HistoryList.vue';
import PlanPoints from '../PlanPoints.vue';

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
    },
    {
        path: '/plan-points',
        name: 'PlanPoints',
        component: PlanPoints
    },
    {
        path: '/plans',
        name: 'PlanList',
        component: () => import('../components/PlanList.vue')
    }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;