<template>
  <div class="user-manage">
    <el-card class="panel" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="tools">
            <el-input v-model="query.username" placeholder="用户名" size="small" class="mr8" clearable />
            <el-input v-model="query.email" placeholder="邮箱" size="small" class="mr8" clearable />
            <el-button size="small" type="primary" @click="fetchUsers">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="users" height="480">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="name" label="姓名" width="180" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="余额" width="140">
          <template #default="{ row }">{{ formatBalance(row.balance) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="360">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="openBalance(row)">设置余额</el-button>
            <el-button size="small" type="danger" @click="doResetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="query.page"
          :page-size="query.size"
          :total="total"
          @current-change="p=>{query.page=p;fetchUsers()}"
        />
      </div>
    </el-card>

    <el-dialog v-model="editDialog.visible" title="编辑用户" width="520px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="balanceDialog.visible" title="设置余额" width="420px">
      <el-form :model="balanceForm" label-width="100px">
        <el-form-item label="余额">
          <el-input v-model="balanceForm.balance" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="balanceDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="saveBalance">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUser, resetUserPassword, updateUserBalance } from '../api/user'

const query = reactive({ page: 1, size: 10, username: '', email: '' })
const users = ref([])
const total = ref(0)

const editDialog = reactive({ visible: false })
const editForm = reactive({ id: null, name: '', email: '' })

const balanceDialog = reactive({ visible: false })
const balanceForm = reactive({ id: null, balance: null })

function isUnauthorized(err) {
  return !!(err && (err.response?.status === 401 || String(err?.message).includes('401')))
}

function formatBalance(v) {
  if (v === null || v === undefined) return '-'
  return Number(v).toFixed(2)
}

async function fetchUsers() {
  try {
    const data = await listUsers({ page: query.page, size: query.size, username: query.username, email: query.email })
    users.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '用户列表获取失败')
  }
}

function openEdit(row) {
  editForm.id = row.id
  editForm.name = row.name || ''
  editForm.email = row.email || ''
  editDialog.visible = true
}

async function saveEdit() {
  if (!editForm.id) return
  try {
    await updateUser(editForm.id, { name: editForm.name, email: editForm.email })
    ElMessage.success('已更新')
    editDialog.visible = false
    fetchUsers()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '更新失败')
  }
}

function openBalance(row) {
  balanceForm.id = row.id
  balanceForm.balance = row.balance != null ? Number(row.balance) : 0
  balanceDialog.visible = true
}

async function saveBalance() {
  if (!balanceForm.id) return
  try {
    await updateUserBalance(balanceForm.id, { balance: Number(balanceForm.balance) })
    ElMessage.success('余额已设置')
    balanceDialog.visible = false
    fetchUsers()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '设置失败')
  }
}

async function doResetPwd(row) {
  try {
    await ElMessageBox.confirm('确认将该用户密码重置为默认值？', '提示', { type: 'warning' })
    await resetUserPassword(row.id)
    ElMessage.success('密码已重置')
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '重置失败')
  }
}

onMounted(async () => {
  try {
    await fetchUsers()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '初始化失败')
  }
})
</script>

<style>
.user-manage { padding: 8px }
.panel { margin-bottom: 16px }
.card-header { display: flex; align-items: center; justify-content: space-between }
.tools { display: flex; align-items: center }
.mr8 { margin-right: 8px }
.table-footer { padding: 8px 0 }
</style>
