import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/query/:databaseId',
      name: 'query',
      component: () => import('../views/QueryView.vue'),
      props: {
        update: false,
      }
    }
  ]
})

export default router
