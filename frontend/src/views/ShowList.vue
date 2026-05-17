<template>
  <div class="show-list">
    <Card>
      <h2 slot="title">
        <Icon type="md-calendar" /> 演出列表
      </h2>
      <Table :columns="columns" :data="shows">
        <template #status="{ row }">
          <Tag :color="getStatusColor(row.status)">{{ getStatusText(row.status) }}</Tag>
        </template>
        <template #action="{ row }">
          <Button type="primary" size="small" @click="goToSeats(row.id)">
            <Icon type="md-seat" /> 选座购票
          </Button>
        </template>
      </Table>
    </Card>
  </div>
</template>

<script>
import { getShows } from '../api'

export default {
  name: 'ShowList',
  data() {
    return {
      shows: [],
      columns: [
        {
          title: '场次名称',
          key: 'name',
          minWidth: 200
        },
        {
          title: '开始时间',
          key: 'startTime',
          minWidth: 200,
          render: (h, params) => {
            return h('span', params.row.startTime ? params.row.startTime.slice(0, 16) : '-')
          }
        },
        {
          title: '状态',
          slot: 'status',
          minWidth: 120
        },
        {
          title: '座位数',
          minWidth: 100,
          render: (h, params) => {
            return h('span', (params.row.totalRows || 0) * (params.row.totalColumns || 0) + ' 座')
          }
        },
        {
          title: '操作',
          slot: 'action',
          minWidth: 120
        }
      ]
    }
  },
  methods: {
    async loadShows() {
      try {
        const res = await getShows()
        if (res.data.success) {
          this.shows = res.data.data
        }
      } catch (e) {
        this.$Message.error('加载失败')
      }
    },
    goToSeats(showId) {
      this.$router.push(`/seats/${showId}`)
    },
    getStatusColor(status) {
      const colors = {
        MEMBER_PRE_SALE: 'gold',
        ON_SALE: 'green',
        NEAR_START: 'orange',
        STARTED: 'red',
        ENDED: 'default'
      }
      return colors[status] || 'default'
    },
    getStatusText(status) {
      const texts = {
        MEMBER_PRE_SALE: '会员优先购',
        ON_SALE: '售票中',
        NEAR_START: '即将开场',
        STARTED: '已开场',
        ENDED: '已结束'
      }
      return texts[status] || status
    }
  },
  mounted() {
    this.loadShows()
  }
}
</script>

<style scoped>
.show-list {
  max-width: 1000px;
  margin: 0 auto;
}
</style>
