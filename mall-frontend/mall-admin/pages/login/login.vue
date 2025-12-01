<template>
  <view class="login-container">
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="login-form">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" placeholder="请输入密码" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form-item>
    </el-form>
  </view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'

const auth = useAuthStore()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const formRef = ref()
const loading = ref(false)

onMounted(async () => {
  try {
    await auth.fetchPublicKey()
  } catch (e) {}
})

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await auth.login({ username: form.username.trim(), password: form.password })
      ElMessage.success('登录成功')
      uni.reLaunch({ url: '/pages/index/index' })
    } catch (e) {
      ElMessage.error(e.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style>
.login-container { padding: 24px }
.login-form { max-width: 360px; margin: 80px auto }
</style>
