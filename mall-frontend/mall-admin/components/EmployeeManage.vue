<template>
  <div class="employee-manage">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>角色管理</span>
              <div class="tools">
                <el-input v-model="query.name" placeholder="角色名" size="small" class="mr8" clearable />
                <el-button size="small" type="primary" @click="fetchRoles">查询</el-button>
                <el-button size="small" @click="openCreate">新增角色</el-button>
              </div>
            </div>
          </template>
          <el-table :data="roles" height="360" @row-click="selectRole" highlight-current-row>
            <el-table-column prop="name" label="角色名" width="160" />
            <el-table-column prop="description" label="描述" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click.stop="openEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click.stop="doDelete(row)">删除</el-button>
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
              @current-change="p=>{query.page=p;fetchRoles()}"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="card-header"><span>角色权限配置</span></div>
          </template>
          <div v-if="currentRole">
            <div class="mb8">当前角色：{{ currentRole.name }}</div>
            <el-transfer
              v-model="selectedPermissionIds"
              :data="transferPermissions"
              filterable
              :titles="['可选权限','已选权限']"
            />
            <div class="mt12">
              <el-button type="primary" @click="saveRolePermissions">保存权限</el-button>
            </div>
          </div>
          <div v-else class="placeholder">
            请选择左侧角色进行权限配置
          </div>
        </el-card>

        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="card-header"><span>为员工分配角色</span></div>
          </template>
          <el-form :inline="true" :model="assignForm">
            <el-form-item label="员工ID">
              <el-input v-model="assignForm.employeeId" placeholder="例如 1001" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="assignForm.roleIds" multiple placeholder="选择角色" style="min-width:240px">
                <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveAssignRoles">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="card-header"><span>员工权限查询</span></div>
          </template>
          <div class="flex-row">
            <el-input v-model="employeePermId" placeholder="员工ID" class="mr8" />
            <el-button @click="fetchEmployeePerms">查询权限</el-button>
          </div>
          <div class="mt12">
            <el-tag v-for="p in employeePerms" :key="p.id" class="mr8 mb8" type="info">{{ p.perms || p.name }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑角色' : '新增角色'" width="420px">
      <el-form :model="dialog.form" label-width="80px">
        <el-form-item label="角色名">
          <el-input v-model="dialog.form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dialog.form.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible=false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRole, createRole, updateRole, deleteRole, getAllPermissions, getRolePermissions, setRolePermissions, assignRoles, getEmployeePermissions } from '../api/role'

const query = reactive({ page: 1, size: 10, name: '' })
const roles = ref([])
const total = ref(0)
const currentRole = ref(null)

const dialog = reactive({ visible: false, isEdit: false, form: { id: null, name: '', description: '' } })

const permissions = ref([])
const selectedPermissionIds = ref([])
const transferPermissions = computed(() => permissions.value.map(p => ({ key: p.id, label: p.perms || p.name })))

const assignForm = reactive({ employeeId: '', roleIds: [] })
const employeePermId = ref('')
const employeePerms = ref([])

async function fetchRoles() {
  const data = await listRole({ page: query.page, size: query.size, name: query.name })
  roles.value = data.records || data.list || []
  total.value = data.total || 0
}

function selectRole(row) {
  currentRole.value = row
  loadRolePermissions()
}

function openCreate() {
  dialog.isEdit = false
  dialog.form = { id: null, name: '', description: '' }
  dialog.visible = true
}

function openEdit(row) {
  dialog.isEdit = true
  dialog.form = { id: row.id, name: row.name, description: row.description }
  dialog.visible = true
}

async function saveRole() {
  if (!dialog.form.name) { ElMessage.error('请输入角色名'); return }
  if (dialog.isEdit) {
    await updateRole(dialog.form.id, { name: dialog.form.name, description: dialog.form.description })
    ElMessage.success('已更新')
  } else {
    await createRole({ name: dialog.form.name, description: dialog.form.description })
    ElMessage.success('已创建')
  }
  dialog.visible = false
  fetchRoles()
}

async function doDelete(row) {
  await ElMessageBox.confirm('确认删除该角色？', '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('已删除')
  if (currentRole.value && currentRole.value.id === row.id) currentRole.value = null
  fetchRoles()
}

async function fetchPermissions() {
  permissions.value = await getAllPermissions()
}

async function loadRolePermissions() {
  if (!currentRole.value) return
  const list = await getRolePermissions(currentRole.value.id)
  selectedPermissionIds.value = list.map(i => i.id)
}

async function saveRolePermissions() {
  if (!currentRole.value) return
  await setRolePermissions(currentRole.value.id, { permissionIds: selectedPermissionIds.value })
  ElMessage.success('权限已保存')
}

async function saveAssignRoles() {
  if (!assignForm.employeeId) { ElMessage.error('请输入员工ID'); return }
  await assignRoles({ employeeId: Number(assignForm.employeeId), roleIds: assignForm.roleIds })
  ElMessage.success('角色已分配')
}

async function fetchEmployeePerms() {
  if (!employeePermId.value) { ElMessage.error('请输入员工ID'); return }
  employeePerms.value = await getEmployeePermissions(Number(employeePermId.value))
}

onMounted(async () => {
  await fetchRoles()
  await fetchPermissions()
})
</script>

<style>
.employee-manage { padding: 8px }
.panel { margin-bottom: 16px }
.card-header { display: flex; align-items: center; justify-content: space-between }
.tools { display: flex; align-items: center }
.mr8 { margin-right: 8px }
.mb8 { margin-bottom: 8px }
.mt12 { margin-top: 12px }
.placeholder { color: #909399 }
.flex-row { display: flex; align-items: center }
</style>
