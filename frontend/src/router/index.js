import Vue from 'vue'
import VueRouter from 'vue-router'
import ShowList from '../views/ShowList.vue'
import SeatSelection from '../views/SeatSelection.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'ShowList',
    component: ShowList
  },
  {
    path: '/seats/:showId',
    name: 'SeatSelection',
    component: SeatSelection
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
