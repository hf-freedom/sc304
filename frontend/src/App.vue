<template>
  <div id="app">
    <Layout>
      <Header>
        <div class="header-content">
          <h1>售票座位锁定系统</h1>
          <div class="user-select">
            <Select v-model="currentUserId" style="width:200px" @on-change="switchUser">
              <Option v-for="user in users" :value="user.id" :key="user.id">{{ user.name }} ({{ user.type }})</Option>
            </Select>
          </div>
        </div>
      </Header>
      <Content>
        <router-view />
      </Content>
    </Layout>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      currentUserId: 'user001',
      users: [
        { id: 'user001', name: '张三', type: '会员' },
        { id: 'user002', name: '李四', type: '普通用户' },
        { id: 'user003', name: '王五', type: '普通用户' }
      ]
    }
  },
  methods: {
    switchUser() {
      this.$root.$emit('userSwitched', this.currentUserId)
      this.$Message.info(`已切换到用户: ${this.currentUserId}`)
    }
  },
  created() {
    localStorage.setItem('currentUserId', this.currentUserId)
  }
}
</script>

<style>
#app {
  height: 100vh;
}

.ivu-layout {
  height: 100%;
}

.ivu-layout-header {
  background: #2d8cf0;
  color: #fff;
  line-height: 60px;
  padding: 0 50px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h1 {
  color: #fff;
  margin: 0;
  font-size: 20px;
}

.ivu-layout-content {
  background: #f5f7f9;
  padding: 20px;
  overflow-y: auto;
}
</style>
