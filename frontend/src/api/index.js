import axios from 'axios'

const request = axios.create({
  baseURL: '/api/ticket',
  timeout: 10000
})

export const getShows = () => {
  return request.get('/shows')
}

export const getSeats = (showId) => {
  return request.get(`/seats/${showId}`)
}

export const lockSeats = (data) => {
  return request.post('/lock', data)
}

export const payOrder = (orderId, userId) => {
  return request.post(`/pay/${orderId}?userId=${userId}`)
}

export const refundOrder = (orderId, userId) => {
  return request.post(`/refund/${orderId}?userId=${userId}`)
}

export const recommendSeats = (data) => {
  return request.post('/recommend', data)
}

export const getUserInfo = (userId) => {
  return request.get(`/user/${userId}`)
}

export default request
