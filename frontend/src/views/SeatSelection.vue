<template>
  <div class="seat-selection">
    <Row :gutter="20">
      <Col :span="18">
        <Card>
          <h2 slot="title">
            <Icon type="md-seat" /> 座位选择
            <Button type="text" @click="$router.push('/')" style="float: right">
              <Icon type="md-arrow-back" /> 返回列表
            </Button>
          </h2>

          <div class="operation-tip">
            <Alert type="info" show-icon>
              <span>点击绿色座位进行选择，选好后点击右侧「确认锁座」按钮锁定座位，锁定后请在5分钟内完成支付，超时座位将自动释放</span>
            </Alert>
          </div>

          <div class="stage">
            <div class="stage-label">舞 台</div>
          </div>

          <div class="seats-container">
            <div v-for="row in rows" :key="row" class="seat-row">
              <span class="row-label">{{ row }}排</span>
              <div
                v-for="seat in getRowSeats(row)"
                :key="seat.id"
                class="seat"
                :class="getSeatClass(seat)"
                @click="handleSeatClick(seat)"
                :title="getSeatTooltip(seat)"
              >
                {{ seat.column }}
                <span v-if="seat.status === 'LOCKED' && seat.lockUserId" class="lock-user">
                  {{ getLockUserShort(seat) }}
                </span>
              </div>
            </div>
          </div>

          <div class="legend">
            <div class="legend-item">
              <span class="seat available"></span>
              <span>可选</span>
            </div>
            <div class="legend-item">
              <span class="seat selected"></span>
              <span>已选</span>
            </div>
            <div class="legend-item">
              <span class="seat locked"></span>
              <span>已锁</span>
            </div>
            <div class="legend-item">
              <span class="seat sold"></span>
              <span>已售</span>
            </div>
          </div>
        </Card>
      </Col>

      <Col :span="6">
        <Card>
          <h2 slot="title">
            <Icon type="md-list-box" /> 订单信息
          </h2>

          <div class="current-user">
            <Tag color="blue">
              <Icon type="md-person" /> 当前用户: {{ currentUserName }}
            </Tag>
          </div>

          <Divider />

          <div class="recommend-section">
            <h4>
              <Icon type="md-bulb" /> 智能连座推荐
            </h4>
            <div class="recommend-controls">
              <div class="recommend-row">
                <span class="label">我需要</span>
                <InputNumber v-model="recommendCount" :min="1" :max="6" size="default" style="width: 80px" />
                <span class="label">个连座</span>
              </div>
              <div class="recommend-actions">
                <Button type="primary" @click="loadRecommendations" :loading="recommending">
                  <Icon type="md-search" /> 智能推荐
                </Button>
                <Button type="default" @click="loadRecommendations" v-if="recommendations.length > 0">
                  <Icon type="md-refresh" /> 换一批
                </Button>
              </div>
              </div>

          <div v-if="recommendations.length > 0" class="recommendations">
            <div class="recommend-header">
              <span>为您找到 <strong>{{ recommendations.length }}</strong> 组连座</span>
              <span class="recommend-tip">点击即可自动选中</span>
            </div>
            <div
              v-for="(rec, index) in recommendations"
              :key="index"
              class="recommend-card"
              :class="rec.quality"
              @click="selectRecommendation(rec)"
            >
              <div class="recommend-rank">
                <span class="rank-badge" :class="rec.quality">{{ getQualityText(rec.quality) }}
              </div>
              <div class="recommend-content">
                <div class="recommend-title">
                  <span class="row-label">{{ rec.row }}排</span>
                  <span class="seats-info">{{ getSeatsRange(rec.seats) }}
                </div>
                <div class="recommend-meta">
                  <Tag size="small">{{ rec.seats.length }}座</Tag>
                  <Tag color="orange" size="small">{{ rec.totalPrice }}
                  <span v-if="rec.quality === 'gold'" class="quality-tag gold">
                    <Icon type="md-star" /> 最佳观影
                  </span>
                  <span v-else-if="rec.quality === 'silver'" class="quality-tag silver">
                    <Icon type="md-star-half" /> 优选位置
                  </span>
                  <span v-else class="quality-tag bronze">
                    <Icon type="md-star-outline" /> 性价比高
                  </span>
                </div>
                <div class="recommend-seats-visual">
                  <div
                    v-for="seat in rec.seats"
                    :key="seat.id"
                    class="seat-preview"
                    :title="seat.row + '排' + seat.column + '座'"
                  >
                    {{ seat.column }}
                  </div>
                </div>
              </div>
              <div class="recommend-action">
                <Icon type="ios-arrow-forward" />
              </div>
              </div>
          </div>

          <div v-else-if="hasSearched" class="no-recommendation">
            <Icon type="md-sad" size="32" />
            <p>没有找到合适的连座</p>
            <p class="tip">请尝试减少座位数量或手动选择座位</p>
          </div>

          <Divider />

          <div v-if="selectedSeats.length > 0" class="selected-info">
            <h4>
              <Icon type="md-checkmark-circle" color="#1890ff" /> 
              已选座位 ({{ selectedSeats.length }}个)
            </h4>
            <div class="selected-seats">
              <Tag 
                v-for="seat in selectedSeats" 
                :key="seat.id" 
                color="blue"
                closable 
                @on-close="deselectSeat(seat)"
              >
                {{ seat.row }}排{{ seat.column }}座
                <span class="seat-price">¥{{ seat.price }}</span>
              </Tag>
            </div>
            <div class="seat-detail-list">
              <div v-for="seat in selectedSeats" :key="seat.id" class="seat-detail-item">
                <span>{{ seat.row }}排{{ seat.column }}座</span>
                <span class="price">¥{{ seat.price }}</span>
              </div>
            </div>
            <div class="total-price">
              <span>合计:</span>
              <strong>¥{{ totalPrice }}</strong>
            </div>
            <div class="action-buttons">
              <Button type="default" @click="clearSelection" style="flex: 1">
                清空选择
              </Button>
              <Button type="primary" @click="confirmLock" :loading="locking" style="flex: 2">
                <Icon type="md-lock" /> 确认锁座
              </Button>
            </div>
          </div>

          <div v-else-if="!currentOrder" class="empty-selection">
            <div class="empty-icon">
              <Icon type="md-seat" size="48" />
            </div>
            <p>请在左侧座位图中选择座位</p>
            <p class="tip">点击绿色座位即可选择</p>
          </div>

          <div v-if="currentOrder" class="order-info">
            <Divider />
            <div class="order-header">
              <h4>
                <Icon type="md-document" /> 订单详情
              </h4>
              <Tag :color="getOrderStatusColor(currentOrder.status)">
                {{ getOrderStatusText(currentOrder.status) }}
              </Tag>
            </div>
            <div class="order-detail">
              <p><span>订单号:</span> {{ currentOrder.id }}</p>
              <p><span>场次:</span> {{ getShowName() }}</p>
              <p><span>座位:</span> {{ getOrderSeatsText() }}</p>
              <p><span>金额:</span> <strong class="price-red">¥{{ currentOrder.totalAmount }}</strong></p>
              <p><span>下单时间:</span> {{ formatTime(currentOrder.createTime) }}</p>
              <p v-if="currentOrder.status === 'CANCELLED' && currentOrder.expireTime">
                <span>超时时间:</span> {{ formatTime(currentOrder.expireTime) }}
              </p>
            </div>
            
            <div v-if="currentOrder.status === 'PENDING'" class="countdown-section" :class="{ 'warning': countdownSeconds < 60, 'danger': countdownSeconds < 30 }">
              <div class="countdown-title">
                <Icon type="md-time" /> 支付倒计时
              </div>
              <div class="countdown-time">
                {{ countdownText }}
              </div>
              <div class="countdown-bar">
                <div class="countdown-progress" :style="{ width: countdownPercent + '%' }" :class="{ 'progress-warning': countdownSeconds < 60, 'progress-danger': countdownSeconds < 30 }"></div>
              </div>
              <p class="expire-tip" v-if="countdownSeconds > 60">超时未支付，座位将自动释放</p>
              <p class="expire-tip danger" v-else-if="countdownSeconds > 30">⚠️ 即将超时，请尽快完成支付</p>
              <p class="expire-tip danger" v-else>⏰ 即将超时，座位即将释放！</p>
            </div>

            <div v-if="currentOrder.status === 'CANCELLED'" class="order-cancelled">
              <div class="cancelled-icon">
                <Icon type="md-close-circle" size="48" />
              </div>
              <h4>订单已取消</h4>
              <p>支付超时，座位已自动释放</p>
              <p class="cancelled-detail">释放的座位: {{ getOrderSeatsText() }}</p>
              <p class="cancelled-tip">您可以重新选择座位进行购买</p>
            </div>

            <div v-if="currentOrder.status === 'REFUNDED'" class="order-refunded">
              <div class="refunded-icon">
                <Icon type="md-checkmark-circle" size="48" />
              </div>
              <h4>退票成功</h4>
              <p>退款金额: ¥{{ currentOrder.totalAmount }}</p>
              <p class="refunded-detail">座位已恢复可售状态</p>
            </div>

            <ButtonGroup v-if="currentOrder.status === 'PENDING'" style="width: 100%; margin-top: 15px">
              <Button type="primary" @click="payOrder" size="large" style="flex: 1" :loading="paying">
                <Icon type="md-card" /> 立即支付
              </Button>
            </ButtonGroup>
            <Button v-else-if="currentOrder.status === 'PAID'" type="warning" long @click="refundOrder" size="large">
              <Icon type="md-return-left" /> 申请退票
            </Button>
            <Button v-else-if="currentOrder.status === 'CANCELLED' || currentOrder.status === 'REFUNDED'" type="primary" long @click="resetOrder" size="large">
              <Icon type="md-refresh" /> 重新选座
            </Button>
          </div>

          <div v-if="orderHistory.length > 0" class="order-history">
            <Divider />
            <h4>
              <Icon type="md-time" /> 操作记录
            </h4>
            <div v-for="(record, index) in orderHistory" :key="index" class="history-item" :class="record.type">
              <div class="history-icon">
                <Icon :type="record.icon" />
              </div>
              <div class="history-content">
                <p class="history-title">{{ record.title }}</p>
                <p class="history-desc">{{ record.desc }}</p>
                <p class="history-time">{{ record.time }}</p>
              </div>
            </div>
          </div>
        </Card>
      </Col>
    </Row>
  </div>
</template>

<script>
import axios from 'axios'
import { getSeats, lockSeats, payOrder, refundOrder, recommendSeats } from '../api'

export default {
  name: 'SeatSelection',
  data() {
    return {
      showId: this.$route.params.showId,
      seats: [],
      selectedSeats: [],
      currentOrder: null,
      locking: false,
      paying: false,
      recommendCount: 2,
      recommendations: [],
      recommending: false,
      hasSearched: false,
      refreshTimer: null,
      countdownTimer: null,
      countdownSeconds: 0,
      showName: '',
      orderHistory: [],
      orderStatusPollingTimer: null,
      showTimeoutNotification: false,
      timeoutSeats: []
    }
  },
  computed: {
    rows() {
      const rowSet = new Set(this.seats.map(s => s.row))
      return Array.from(rowSet).sort((a, b) => a - b)
    },
    totalPrice() {
      return this.selectedSeats.reduce((sum, seat) => sum + seat.price, 0)
    },
    currentUserName() {
      const userId = localStorage.getItem('currentUserId') || 'user001'
      const userMap = {
        'user001': '张三 (会员)',
        'user002': '李四 (普通用户)',
        'user003': '王五 (普通用户)'
      }
      return userMap[userId] || userId
    },
    countdownText() {
      const mins = Math.floor(this.countdownSeconds / 60)
      const secs = this.countdownSeconds % 60
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },
    countdownPercent() {
      return (this.countdownSeconds / 300) * 100
    }
  },
  methods: {
    getRowSeats(row) {
      return this.seats.filter(s => s.row === row).sort((a, b) => a.column - b.column)
    },
    getSeatClass(seat) {
      if (this.selectedSeats.find(s => s.id === seat.id)) {
        return 'selected'
      }
      return seat.status.toLowerCase()
    },
    getSeatTooltip(seat) {
      if (seat.status === 'AVAILABLE') {
        return `${seat.row}排${seat.column}座 - ¥${seat.price} - 点击选择`
      } else if (seat.status === 'LOCKED') {
        return `${seat.row}排${seat.column}座 - 已被锁定`
      } else if (seat.status === 'SOLD') {
        return `${seat.row}排${seat.column}座 - 已售出`
      }
      return ''
    },
    getLockUserShort(seat) {
      if (seat.lockUserId === 'user001') return '张'
      if (seat.lockUserId === 'user002') return '李'
      if (seat.lockUserId === 'user003') return '王'
      return '?'
    },
    handleSeatClick(seat) {
      if (seat.status === 'SOLD') {
        this.showSeatSoldTip(seat)
        return
      }
      if (seat.status === 'LOCKED') {
        this.showSeatLockedTip(seat)
        return
      }
      if (this.currentOrder && this.currentOrder.status === 'PENDING') {
        this.$Message.warning('您有未支付的订单，请先完成支付或取消订单')
        return
      }
      this.selectSeat(seat)
    },
    showSeatSoldTip(seat) {
      this.$Modal.info({
        title: '座位已售出',
        content: `
          <div style="padding: 15px 0;">
            <div style="display: flex; align-items: center; margin-bottom: 15px;">
              <div style="width: 60px; height: 60px; border-radius: 50%; background: #fff1f0; display: flex; align-items: center; justify-content: center; margin-right: 15px;">
                <Icon type="md-cart" size="28" color="#f5222d" />
              </div>
              <div>
                <p style="font-size: 16px; font-weight: bold; margin: 0;">${seat.row}排${seat.column}座</p>
                <p style="color: #999; margin: 5px 0 0 0;">票价：¥${seat.price}</p>
              </div>
              </div>
            <p style="color: #666; margin: 0;">该座位已被其他用户购买，无法选择。</p>
            <p style="color: #1890ff; margin-top: 10px 0 0 0;">
              <Icon type="md-lightbulb" /> 建议：您可以选择其他座位，或使用「智能选座」功能查找可用连座。
            </p>
          </div>
        `,
        okText: '我知道了'
      })
    },
    showSeatLockedTip(seat) {
      const userMap = {
        'user001': '张三',
        'user002': '李四',
        'user003': '王五'
      }
      const lockerName = userMap[seat.lockUserId] || seat.lockUserId || '其他用户'
      
      let remainTimeText = ''
      if (seat.lockTime) {
        const now = Date.now()
        const lockTime = new Date(seat.lockTime).getTime()
        const remainTime = Math.max(0, 300 - Math.floor((now - lockTime) / 1000))
        if (remainTime > 0) {
          const mins = Math.floor(remainTime / 60)
          const secs = Math.floor(remainTime % 60)
          remainTimeText = `${mins}分${secs}秒`
        }
      }
      
      this.$Modal.warning({
        title: '座位已被锁定',
        content: `
          <div style="padding: 15px 0;">
            <div style="display: flex; align-items: center; margin-bottom: 15px;">
              <div style="width: 60px; height: 60px; border-radius: 50%; background: #fff7e6; display: flex; align-items: center; justify-content: center; margin-right: 15px;">
                <Icon type="md-lock" size="28" color="#faad14" />
              </div>
              <div>
                <p style="font-size: 16px; font-weight: bold; margin: 0;">${seat.row}排${seat.column}座</p>
                <p style="color: #999; margin: 5px 0 0 0;">票价：¥${seat.price}</p>
              </div>
            </div>
            <div style="background: #fffbe6; padding: 12px; border-radius: 6px; margin-bottom: 10px;">
              <p style="margin: 0 0 5px 0;">
                <Icon type="md-person" color="#faad14" /> <strong>锁定用户：</strong>${lockerName}
              </p>
              ${remainTimeText ? `<p style="margin: 0;"><Icon type="md-time" color="#faad14" /> <strong>预计释放：</strong>${remainTimeText}后</p>` : ''}
            </div>
            <p style="color: #666; margin: 0;">该座位正在被其他用户支付中，暂时无法选择。</p>
            <p style="color: #1890ff; margin-top: 10px;">
              <Icon type="md-lightbulb" /> 建议：您可以选择其他座位，或稍等片刻后刷新页面查看。
            </p>
          </div>
        `,
        okText: '我知道了'
      })
    },
    selectSeat(seat) {
      const index = this.selectedSeats.findIndex(s => s.id === seat.id)
      if (index > -1) {
        this.selectedSeats.splice(index, 1)
      } else {
        this.selectedSeats.push(seat)
        this.$Message.success(`已选择 ${seat.row}排${seat.column}座`)
      }
    },
    deselectSeat(seat) {
      const index = this.selectedSeats.findIndex(s => s.id === seat.id)
      if (index > -1) {
        this.selectedSeats.splice(index, 1)
      }
    },
    clearSelection() {
      this.selectedSeats = []
      this.$Message.info('已清空选择')
    },
    async confirmLock() {
      if (this.selectedSeats.length === 0) {
        this.$Message.warning('请先选择座位')
        return
      }

      this.$Modal.confirm({
        title: '确认锁座',
        content: `您已选择 ${this.selectedSeats.length} 个座位，总计 ¥${this.totalPrice}。锁定后请在5分钟内完成支付，超时座位将自动释放。`,
        onOk: async () => {
          await this.doLock()
        }
      })
    },
    async doLock() {
      this.locking = true
      try {
        const userId = localStorage.getItem('currentUserId') || 'user001'
        const res = await lockSeats({
          showId: this.showId,
          userId: userId,
          seatIds: this.selectedSeats.map(s => s.id)
        })
        if (res.data.success) {
          this.$Message.success('锁座成功! 请尽快完成支付')
          this.currentOrder = res.data.data
          this.selectedSeats = []
          this.recommendations = []
          
          this.addOrderHistory({
            type: 'locked',
            icon: 'md-lock',
            title: '锁座成功',
            desc: `已锁定 ${this.getOrderSeatsText()}，请在5分钟内完成支付`,
            time: this.getCurrentTime()
          })
          
          this.startCountdown()
          this.loadSeats()
        } else {
          this.handleLockError(res.data.message)
        }
      } catch (e) {
        this.$Message.error('操作失败')
      } finally {
        this.locking = false
      }
    },
    handleLockError(errorMessage) {
      if (errorMessage.includes('已被锁定')) {
        const seatMatch = errorMessage.match(/座位\s+(\d+排\d+座)/)
        const seatInfo = seatMatch ? seatMatch[1] : ''
        
        const userMatch = errorMessage.match(/用户【([^】]+)】/)
        const userInfo = userMatch ? userMatch[1] : ''
        
        const timeMatch = errorMessage.match(/预计\s+(\d+)\s+秒后释放/)
        const remainTime = timeMatch ? timeMatch[1] : ''
        
        let content = errorMessage
        if (seatInfo && userInfo) {
          content = `
            <div style="padding: 10px 0;">
              <p style="margin-bottom: 8px;"><strong>冲突座位：</strong>${seatInfo}</p>
              <p style="margin-bottom: 8px;"><strong>锁定用户：</strong>${userInfo}</p>
              ${remainTime ? `<p style="margin-bottom: 8px;"><strong>预计释放：</strong>${remainTime} 秒后</p>` : ''}
              <p style="color: #666; margin-top: 10px;">
                <Icon type="md-information-circle" /> 您可以选择其他座位，或等待该座位释放后再尝试
              </p>
            </div>
          `
        }
        
        this.$Modal.error({
          title: '座位锁定失败',
          content: content,
          okText: '我知道了'
        })
        
        this.addOrderHistory({
          type: 'cancelled',
          icon: 'md-close-circle',
          title: '锁座失败',
          desc: errorMessage,
          time: this.getCurrentTime()
        })
      } else if (errorMessage.includes('已售出')) {
        this.$Modal.warning({
          title: '座位已售出',
          content: `
            <div style="padding: 10px 0;">
              <p>${errorMessage}</p>
              <p style="color: #666; margin-top: 10px;">
                <Icon type="md-information-circle" /> 该座位已被其他用户购买，请选择其他座位
              </p>
            </div>
          `,
          okText: '我知道了'
        })
        
        this.addOrderHistory({
          type: 'cancelled',
          icon: 'md-close-circle',
          title: '锁座失败',
          desc: errorMessage,
          time: this.getCurrentTime()
        })
      } else if (errorMessage.includes('限制') || errorMessage.includes('黑名单')) {
        this.$Modal.warning({
          title: '操作受限',
          content: `
            <div style="padding: 10px 0;">
              <p>${errorMessage}</p>
              <p style="color: #faad14; margin-top: 10px;">
                <Icon type="md-warning" /> 频繁锁座会影响您的购票体验，请合理操作
              </p>
            </div>
          `,
          okText: '我知道了'
        })
      } else {
        this.$Message.error(errorMessage)
      }
      
      this.loadSeats()
    },
    startCountdown() {
      this.countdownSeconds = 300
      this.clearCountdown()
      this.startOrderStatusPolling()
      this.countdownTimer = setInterval(() => {
        this.countdownSeconds--
        if (this.countdownSeconds <= 0) {
          this.clearCountdown()
          this.stopOrderStatusPolling()
          this.handleOrderTimeout()
        }
      }, 1000)
    },
    clearCountdown() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
        this.countdownTimer = null
      }
    },
    startOrderStatusPolling() {
      this.stopOrderStatusPolling()
      this.orderStatusPollingTimer = setInterval(() => {
        this.checkOrderStatus()
      }, 5000)
    },
    stopOrderStatusPolling() {
      if (this.orderStatusPollingTimer) {
        clearInterval(this.orderStatusPollingTimer)
        this.orderStatusPollingTimer = null
      }
    },
    async checkOrderStatus() {
      if (!this.currentOrder || this.currentOrder.status !== 'PENDING') return
      try {
        const res = await axios.get(`/api/ticket/shows`)
        if (res.data && res.data.data) {
          this.loadSeats()
        }
      } catch (e) {
        console.error('检查订单状态失败', e)
      }
    },
    handleOrderTimeout() {
      const cancelledOrder = { ...this.currentOrder, status: 'CANCELLED' }
      const seatsText = this.getOrderSeatsText()
      
      this.currentOrder = cancelledOrder
      
      this.addOrderHistory({
        type: 'cancelled',
        icon: 'md-close-circle',
        title: '订单已超时',
        desc: `订单 ${cancelledOrder.id} 支付超时，座位已自动释放`,
        time: this.getCurrentTime()
      })

      this.$Notification.warning({
        title: '订单已超时',
        desc: `您的订单已超时，以下座位已被释放：\n${seatsText}`,
        duration: 8
      })

      this.timeoutSeats = this.currentOrder.seatIds || []
      this.showTimeoutNotification = true
      
      this.loadSeats()
      
      setTimeout(() => {
        this.showTimeoutNotification = false
      }, 10000)
    },
    async payOrder() {
      this.$Modal.confirm({
        title: '确认支付',
        content: `确定支付 ¥${this.currentOrder.totalAmount} 吗?`,
        onOk: async () => {
          this.paying = true
          try {
            const userId = localStorage.getItem('currentUserId') || 'user001'
            const res = await payOrder(this.currentOrder.id, userId)
            if (res.data.success) {
              this.$Message.success('支付成功!')
              this.currentOrder = res.data.data
              this.clearCountdown()
              this.stopOrderStatusPolling()
              
              this.addOrderHistory({
                type: 'paid',
                icon: 'md-checkmark-circle',
                title: '支付成功',
                desc: `订单 ${this.currentOrder.id} 支付成功，金额 ¥${this.currentOrder.totalAmount}`,
                time: this.getCurrentTime()
              })
              
              this.loadSeats()
            } else {
              this.$Message.error(res.data.message)
              if (res.data.message.includes('超时')) {
                this.clearCountdown()
                this.stopOrderStatusPolling()
                this.handleOrderTimeout()
              }
            }
          } catch (e) {
            this.$Message.error('支付失败')
          } finally {
            this.paying = false
          }
        }
      })
    },
    async refundOrder() {
      this.$Modal.confirm({
        title: '退票确认',
        content: `确定要申请退票吗? 将退款 ¥${this.currentOrder.totalAmount}`,
        onOk: async () => {
          try {
            const userId = localStorage.getItem('currentUserId') || 'user001'
            const res = await refundOrder(this.currentOrder.id, userId)
            if (res.data.success) {
              this.$Message.success('退票成功!')
              
              this.addOrderHistory({
                type: 'refunded',
                icon: 'md-return-left',
                title: '退票成功',
                desc: `订单 ${this.currentOrder.id} 退票成功，退款金额 ¥${this.currentOrder.totalAmount}`,
                time: this.getCurrentTime()
              })
              
              this.currentOrder = { ...this.currentOrder, status: 'REFUNDED' }
              this.loadSeats()
            } else {
              this.$Message.error(res.data.message)
            }
          } catch (e) {
            this.$Message.error('退票失败')
          }
        }
      })
    },
    resetOrder() {
      this.currentOrder = null
      this.clearCountdown()
    },
    async loadRecommendations() {
      this.recommending = true
      this.hasSearched = true
      try {
        const res = await recommendSeats({
          showId: this.showId,
          seatCount: this.recommendCount
        })
        if (res.data.success) {
          this.recommendations = res.data.data || []
          if (this.recommendations.length === 0) {
            this.$Message.warning('没有找到合适的连座，请减少座位数量或手动选择')
          } else {
            this.$Message.success(`为您找到 ${this.recommendations.length} 组优质连座`)
          }
        }
      } catch (e) {
        this.$Message.error('获取推荐失败')
      } finally {
        this.recommending = false
      }
    },
    selectRecommendation(rec) {
      const seats = rec.seats || rec
      this.selectedSeats = [...seats]
      this.$Message.success(`已为您选择 ${seats.length} 个连座`)
    },
    getQualityText(quality) {
      const texts = {
        gold: '黄金位置',
        silver: '优选位置',
        bronze: '经济之选'
      }
      return texts[quality] || '推荐'
    },
    getSeatsRange(seats) {
      if (!seats || seats.length === 0) return ''
      if (seats.length === 1) return seats[0].column + '座'
      return seats[0].column + '-' + seats[seats.length - 1].column + '座'
    },
    getOrderStatusText(status) {
      const texts = {
        PENDING: '待支付',
        PAID: '已支付',
        CANCELLED: '已取消',
        REFUNDED: '已退款'
      }
      return texts[status] || status
    },
    getOrderStatusColor(status) {
      const colors = {
        PENDING: 'orange',
        PAID: 'green',
        CANCELLED: 'default',
        REFUNDED: 'cyan'
      }
      return colors[status] || 'default'
    },
    getShowName() {
      return this.showName || this.showId
    },
    getOrderSeatsText() {
      if (!this.currentOrder || !this.currentOrder.seatIds) return ''
      return this.currentOrder.seatIds.map(id => {
        const seat = this.seats.find(s => s.id === id)
        return seat ? `${seat.row}排${seat.column}座` : id
      }).join('、')
    },
    formatTime(time) {
      if (!time) return ''
      return time.replace('T', ' ').slice(0, 19)
    },
    getCurrentTime() {
      const now = new Date()
      return now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    addOrderHistory(record) {
      this.orderHistory.unshift(record)
      if (this.orderHistory.length > 10) {
        this.orderHistory.pop()
      }
    },
    async loadSeats() {
      try {
        const res = await getSeats(this.showId)
        if (res.data.success) {
          this.seats = res.data.data
          if (this.seats.length > 0 && !this.showName) {
            const show = await axios.get(`/api/ticket/shows`).catch(() => null)
            if (show && show.data && show.data.data) {
              const showInfo = show.data.data.find(s => s.id === this.showId)
              if (showInfo) this.showName = showInfo.name
            }
          }
        }
      } catch (e) {
        console.error(e)
      }
    }
  },
  mounted() {
    this.loadSeats()
    this.refreshTimer = setInterval(() => {
      this.loadSeats()
    }, 5000)
  },
  beforeDestroy() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
    this.clearCountdown()
    this.stopOrderStatusPolling()
  }
}
</script>

<style scoped>
.seat-selection {
  max-width: 1500px;
  margin: 0 auto;
}

.operation-tip {
  margin-bottom: 20px;
}

.stage {
  text-align: center;
  margin-bottom: 30px;
}

.stage-label {
  display: inline-block;
  background: linear-gradient(90deg, #666, #999, #666);
  color: #fff;
  padding: 10px 100px;
  border-radius: 0 0 50px 50px;
  font-size: 16px;
  letter-spacing: 10px;
}

.seats-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.seat-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.row-label {
  width: 40px;
  text-align: right;
  font-size: 12px;
  color: #666;
}

.seat {
  width: 34px;
  height: 34px;
  border-radius: 6px 6px 10px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  line-height: 1;
}

.seat .lock-user {
  font-size: 9px;
  opacity: 0.8;
  margin-top: 1px;
}

.seat.available {
  background: #52c41a;
  color: #fff;
}

.seat.available:hover {
  background: #73d13d;
  transform: scale(1.15);
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.4);
}

.seat.selected {
  background: #1890ff;
  color: #fff;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.4);
}

.seat.locked {
  background: #faad14;
  color: #fff;
  cursor: not-allowed;
}

.seat.sold {
  background: #ccc;
  color: #999;
  cursor: not-allowed;
}

.legend {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 30px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-item .seat {
  cursor: default;
  width: 24px;
  height: 24px;
}

.current-user {
  margin-bottom: 10px;
}

.recommend-section h4 {
  margin-bottom: 10px;
}

.recommend-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommendations h4 {
  margin-top: 15px;
  margin-bottom: 10px;
}

.recommend-item {
  padding: 10px 12px;
  background: #e6f7ff;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid #1890ff;
}

.recommend-item:hover {
  background: #bae7ff;
  transform: translateX(3px);
}

.recommend-title {
  font-weight: bold;
  margin-bottom: 4px;
  font-size: 13px;
}

.recommend-detail {
  font-size: 12px;
  color: #666;
}

.selected-info h4 {
  margin-bottom: 15px;
  color: #1890ff;
}

.selected-seats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px 0;
}

.seat-price {
  margin-left: 4px;
  color: #f5222d;
  font-weight: bold;
}

.seat-detail-list {
  margin: 15px 0;
  padding: 10px;
  background: #fafafa;
  border-radius: 4px;
}

.seat-detail-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 13px;
}

.seat-detail-item .price {
  color: #f5222d;
  font-weight: bold;
}

.total-price {
  font-size: 16px;
  margin: 15px 0;
  text-align: right;
  padding-top: 10px;
  border-top: 1px dashed #e8e8e8;
}

.total-price strong {
  color: #f5222d;
  font-size: 28px;
  margin-left: 10px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.empty-selection {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-icon {
  margin-bottom: 15px;
  color: #d9d9d9;
}

.empty-selection p {
  margin: 5px 0;
}

.empty-selection .tip {
  font-size: 12px;
  color: #bfbfbf;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.order-header h4 {
  margin: 0;
}

.order-detail {
  background: #fafafa;
  padding: 12px;
  border-radius: 4px;
}

.order-detail p {
  margin: 6px 0;
  font-size: 13px;
  display: flex;
  justify-content: space-between;
}

.order-detail p span:first-child {
  color: #999;
}

.price-red {
  color: #f5222d;
  font-size: 16px;
}

.countdown-section {
  margin-top: 15px;
  padding: 15px;
  background: #fff7e6;
  border-radius: 6px;
  text-align: center;
}

.countdown-title {
  color: #d46b08;
  margin-bottom: 10px;
  font-size: 13px;
}

.countdown-time {
  font-size: 36px;
  font-weight: bold;
  color: #d46b08;
  font-family: 'Courier New', monospace;
}

.countdown-bar {
  width: 100%;
  height: 6px;
  background: #ffe7ba;
  border-radius: 3px;
  margin-top: 10px;
  overflow: hidden;
}

.countdown-progress {
  height: 100%;
  background: linear-gradient(90deg, #faad14, #f5222d);
  transition: width 1s linear;
}

.expire-tip {
  color: #f5222d;
  font-size: 12px !important;
  margin-top: 10px !important;
  text-align: center;
}

.expire-tip.danger {
  font-weight: bold;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.countdown-section.warning {
  background: #fff7e6;
  animation: pulse-warning 2s infinite;
}

.countdown-section.danger {
  background: #fff1f0;
  animation: pulse-danger 1s infinite;
}

@keyframes pulse-warning {
  0%, 100% { box-shadow: 0 0 0 0 rgba(250, 173, 20, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(250, 173, 20, 0); }
}

@keyframes pulse-danger {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245, 34, 45, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(245, 34, 45, 0); }
}

.recommend-section {
  background: #fafafa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.recommend-section h4 {
  margin: 0 0 12px 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.recommend-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recommend-row .label {
  color: #666;
  font-size: 14px;
}

.recommend-actions {
  display: flex;
  gap: 8px;
}

.recommendations {
  margin-top: 15px;
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 4px;
}

.recommend-header strong {
  color: #1890ff;
  margin: 0 4px;
}

.recommend-tip {
  color: #999;
  font-size: 12px;
}

.recommend-card {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 10px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.recommend-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #d9d9d9;
  transition: all 0.3s;
}

.recommend-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
  transform: translateX(2px);
}

.recommend-card:hover::before {
  width: 6px;
}

.recommend-card.gold::before {
  background: linear-gradient(180deg, #faad14, #f5222d);
}

.recommend-card.silver::before {
  background: linear-gradient(180deg, #1890ff, #722ed1);
}

.recommend-card.bronze::before {
  background: linear-gradient(180deg, #52c41a, #13c2c2);
}

.recommend-card.gold:hover {
  border-color: #faad14;
  box-shadow: 0 2px 12px rgba(250, 173, 20, 0.2);
}

.recommend-card.silver:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 12px rgba(24, 144, 255, 0.2);
}

.recommend-card.bronze:hover {
  border-color: #52c41a;
  box-shadow: 0 2px 12px rgba(82, 196, 26, 0.2);
}

.recommend-rank {
  margin-right: 12px;
}

.rank-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
  color: #fff;
  background: #999;
  white-space: nowrap;
}

.rank-badge.gold {
  background: linear-gradient(90deg, #faad14, #f5222d);
}

.rank-badge.silver {
  background: linear-gradient(90deg, #1890ff, #722ed1);
}

.rank-badge.bronze {
  background: linear-gradient(90deg, #52c41a, #13c2c2);
}

.recommend-content {
  flex: 1;
  min-width: 0;
}

.recommend-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.row-label {
  font-weight: bold;
  color: #333;
  font-size: 15px;
}

.seats-info {
  color: #666;
  font-size: 14px;
}

.recommend-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.quality-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  margin-left: 4px;
}

.quality-tag.gold {
  background: #fff7e6;
  color: #faad14;
}

.quality-tag.silver {
  background: #e6f7ff;
  color: #1890ff;
}

.quality-tag.bronze {
  background: #f6ffed;
  color: #52c41a;
}

.recommend-seats-visual {
  display: flex;
  gap: 4px;
  margin-top: 6px;
}

.seat-preview {
  width: 24px;
  height: 24px;
  background: #52c41a;
  color: #fff;
  border-radius: 4px 4px 6px 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: bold;
}

.recommend-action {
  color: #bfbfbf;
  margin-left: 8px;
  transition: all 0.3s;
}

.recommend-card:hover .recommend-action {
  color: #1890ff;
  transform: translateX(4px);
}

.no-recommendation {
  text-align: center;
  padding: 30px 20px;
  color: #999;
  background: #fafafa;
  border-radius: 8px;
  margin-top: 15px;
}

.no-recommendation p {
  margin: 8px 0 0 0;
}

.no-recommendation .tip {
  font-size: 12px;
  color: #bfbfbf;
}

.progress-warning {
  background: linear-gradient(90deg, #faad14, #fa8c16) !important;
}

.progress-danger {
  background: linear-gradient(90deg, #f5222d, #cf1322) !important;
}

.order-cancelled, .order-refunded {
  text-align: center;
  padding: 20px;
  margin: 15px 0;
  border-radius: 8px;
}

.order-cancelled {
  background: #fff1f0;
  border: 1px solid #ffa39e;
}

.order-refunded {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.cancelled-icon {
  color: #f5222d;
  margin-bottom: 10px;
}

.refunded-icon {
  color: #52c41a;
  margin-bottom: 10px;
}

.order-cancelled h4, .order-refunded h4 {
  margin: 10px 0;
  color: #333;
}

.cancelled-detail, .refunded-detail {
  color: #666;
  font-size: 13px;
  margin: 8px 0;
}

.cancelled-tip {
  color: #1890ff;
  font-size: 12px;
  margin-top: 10px;
}

.order-history {
  margin-top: 15px;
}

.history-item {
  display: flex;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 6px;
  background: #fafafa;
  border-left: 3px solid #d9d9d9;
}

.history-item.locked {
  border-left-color: #1890ff;
  background: #e6f7ff;
}

.history-item.paid {
  border-left-color: #52c41a;
  background: #f6ffed;
}

.history-item.cancelled {
  border-left-color: #f5222d;
  background: #fff1f0;
}

.history-item.refunded {
  border-left-color: #faad14;
  background: #fffbe6;
}

.history-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  font-size: 16px;
}

.history-item.locked .history-icon {
  color: #1890ff;
}

.history-item.paid .history-icon {
  color: #52c41a;
}

.history-item.cancelled .history-icon {
  color: #f5222d;
}

.history-item.refunded .history-icon {
  color: #faad14;
}

.history-content {
  flex: 1;
  min-width: 0;
}

.history-title {
  font-weight: bold;
  color: #333;
  margin: 0 0 4px 0;
  font-size: 13px;
}

.history-desc {
  color: #666;
  font-size: 12px;
  margin: 0 0 4px 0;
  word-break: break-all;
}

.history-time {
  color: #999;
  font-size: 11px;
  margin: 0;
}

h4 {
  margin-bottom: 10px;
  color: #333;
}
</style>
