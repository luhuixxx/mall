<template>
  <div class="employee-manage">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="员工管理" name="employee">
        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>员工列表</span>
              <div class="tools">
                <el-input v-model="empQuery.username" placeholder="用户名" size="small" class="mr8" clearable />
                <el-button size="small" type="primary" @click="fetchEmployees">查询</el-button>
                <el-button size="small" type="success" @click="openCreateEmp">新增员工</el-button>
              </div>
            </div>
          </template>
          <el-table :data="employees" height="420">
			   <el-table-column prop="id" label="用户ID" width="160" />
            <el-table-column prop="username" label="用户名" width="200" />
            <el-table-column prop="createdTime" label="创建时间" />
            <el-table-column label="操作" width="300">
              <template #default="{ row }">
                <el-button size="small" @click="openEditEmp(row)">修改</el-button>
                <el-button size="small" type="warning" @click="openResetPwd(row)">重置密码</el-button>
                <el-button size="small" type="danger" @click="doDeleteEmp(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-footer">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="empQuery.page"
              :page-size="empQuery.size"
              :total="empTotal"
              @current-change="p=>{empQuery.page=p;fetchEmployees()}"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="角色管理" name="role">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-card class="panel" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>角色列表</span>
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
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="员工与角色" name="assign">
        <el-row :gutter="16">
          <el-col :span="12">
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
          </el-col>
          <el-col :span="12">
            <el-card class="panel" shadow="hover">
              <template #header>
                <div class="card-header"><span>员工权限查询</span></div>
              </template>
              <div class="flex-row">
                <el-input v-model="employeePermId" placeholder="员工ID" class="mr8" />
                <el-button @click="fetchEmployeePerms">查询权限</el-button>
              </div>
              <div class="mt12">
                <el-tag v-for="p in employeePerms" :key="p.id" class="mr8 mb8" type="info">{{ p.description || p.pers }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

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

    <el-dialog v-model="empDialog.visible" :title="empDialog.title" width="420px">
      <div class="dialog-body">
        <template v-if="!empDialog.isEdit">
          <el-form :model="empForm" label-width="90px">
            <el-form-item label="用户名">
              <el-input v-model="empForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="empForm.password" placeholder="请输入密码" type="password" show-password />
            </el-form-item>
          </el-form>
        </template>
        <template v-else>
          <el-form :model="empEditForm" label-width="90px">
            <el-form-item label="用户名">
              <el-input v-model="empEditForm.username" />
            </el-form-item>
          </el-form>
        </template>
      </div>
      <template #footer>
        <el-button @click="empDialog.visible=false">取消</el-button>
        <el-button type="primary" v-if="!empDialog.isEdit" :loading="empLoading" @click="createEmployee">保存</el-button>
        <el-button type="primary" v-else @click="saveEditEmp">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="empResetDialog.visible" :title="empResetDialog.title" width="420px">
      <div class="dialog-body">确认为该员工重置密码？该操作将由后端生成新的密码。</div>
      <template #footer>
        <el-button @click="empResetDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="saveResetPwd">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRole, createRole, updateRole, deleteRole, getAllPermissions, getRolePermissions, setRolePermissions, assignRoles, getEmployeePermissions } from '../api/role'
import { registerEmployee, getPublicKey, listEmployees, updateEmployee, deleteEmployee, resetEmployeePassword } from '../api/employee'
import JSEncrypt from 'jsencrypt'
import { useAuthStore } from '../store/auth'

const query = reactive({ page: 1, size: 10, name: '' })
const roles = ref([])
const total = ref(0)
const currentRole = ref(null)

const dialog = reactive({ visible: false, isEdit: false, form: { id: null, name: '', description: '' } })

const permissions = ref([])
const selectedPermissionIds = ref([])
const transferPermissions = computed(() => permissions.value.map(p => ({ key: p.id, label: p.description || p.perms })))

const assignForm = reactive({ employeeId: '', roleIds: [] })
const employeePermId = ref('')
const employeePerms = ref([])

const activeTab = ref('employee')
const empForm = reactive({ username: '', password: '' })
const empLoading = ref(false)
const empDialog = reactive({ visible: false, isEdit: false, title: '新增员工' })
const empEditForm = reactive({ id: null, username: '' })
const empResetDialog = reactive({ visible: false, title: '重置密码' })
const empResetForm = reactive({ id: null })
const employees = ref([])
const empQuery = reactive({ page: 1, size: 10, username: '' })
const empTotal = ref(0)

function isUnauthorized(err) {
  return !!(err && (err.response?.status === 401 || String(err?.message).includes('401')))
}

async function fetchRoles() {
  try {
    const data = await listRole({ page: query.page, size: query.size, name: query.name })
    roles.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '角色列表获取失败')
  }
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
  try {
    if (dialog.isEdit) {
      await updateRole(dialog.form.id, { name: dialog.form.name, description: dialog.form.description })
      ElMessage.success('已更新')
    } else {
      await createRole({ name: dialog.form.name, description: dialog.form.description })
      ElMessage.success('已创建')
    }
    dialog.visible = false
    fetchRoles()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '保存失败')
  }
}

async function doDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该角色？', '提示', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('已删除')
    if (currentRole.value && currentRole.value.id === row.id) currentRole.value = null
    fetchRoles()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '删除失败')
  }
}

async function fetchPermissions() {
  try {
    permissions.value = await getAllPermissions()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '权限列表获取失败')
  }
}

async function loadRolePermissions() {
  if (!currentRole.value) return
  try {
    const list = await getRolePermissions(currentRole.value.id)
    selectedPermissionIds.value = list.map(i => i.id)
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '角色权限获取失败')
  }
}

async function saveRolePermissions() {
  if (!currentRole.value) return
  try {
    await setRolePermissions(currentRole.value.id, { permissionIds: selectedPermissionIds.value })
    ElMessage.success('权限已保存')
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '保存权限失败')
  }
}

async function saveAssignRoles() {
  if (!assignForm.employeeId) { ElMessage.error('请输入员工ID'); return }
  try {
    await assignRoles({ employeeId: Number(assignForm.employeeId), roleIds: assignForm.roleIds })
    ElMessage.success('角色已分配')
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '分配角色失败')
  }
}

async function fetchEmployeePerms() {
  if (!employeePermId.value) { ElMessage.error('请输入员工ID'); return }
  try {
    employeePerms.value = await getEmployeePermissions(Number(employeePermId.value))
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '查询权限失败')
  }
}

onMounted(async () => {
  try {
    await fetchRoles()
    await fetchPermissions()
    await fetchEmployees()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '初始化失败')
  }
})

async function createEmployee() {
  if (!empForm.username || !empForm.password) { ElMessage.error('请输入用户名与密码'); return }
  empLoading.value = true
  try {
    const auth = useAuthStore()
    if (!auth.publicKey) {
      try { auth.publicKey = await getPublicKey() } catch (_) {}
    }
    const enc = new JSEncrypt()
    enc.setPublicKey(auth.publicKey)
    const encrypted = enc.encrypt(empForm.password) || ''
    await registerEmployee({ username: empForm.username.trim(), password: encrypted })
    ElMessage.success('员工已创建')
    empForm.username = ''
    empForm.password = ''
    empDialog.visible = false
    fetchEmployees()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '创建失败')
  } finally {
    empLoading.value = false
  }
}

async function fetchEmployees() {
  try {
    const data = await listEmployees({ page: empQuery.page, size: empQuery.size, username: empQuery.username })
    employees.value = data.records || data.list || []
    empTotal.value = data.total || 0
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '员工列表获取失败')
  }
}

function openCreateEmp() {
  empDialog.isEdit = false
  empDialog.title = '新增员工'
  empForm.username = ''
  empForm.password = ''
  empDialog.visible = true
}

function openEditEmp(row) {
  empDialog.isEdit = true
  empDialog.title = '修改员工'
  empEditForm.id = row.id
  empEditForm.username = row.username
  empDialog.visible = true
}

async function saveEditEmp() {
  if (!empEditForm.username) { ElMessage.error('请输入用户名'); return }
  try {
    await updateEmployee(empEditForm.id, { username: empEditForm.username.trim() })
    ElMessage.success('已更新')
    empDialog.visible = false
    fetchEmployees()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '更新失败')
  }
}

function openResetPwd(row) {
  empResetForm.id = row.id
  empResetDialog.visible = true
}

async function saveResetPwd() {
  try {
    await resetEmployeePassword(empResetForm.id, {})
    ElMessage.success('密码已重置')
    empResetDialog.visible = false
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '重置失败')
  }
}

async function doDeleteEmp(row) {
  try {
    await ElMessageBox.confirm('确认删除该员工？', '提示', { type: 'warning' })
    await deleteEmployee(row.id)
    ElMessage.success('已删除')
    fetchEmployees()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '删除失败')
  }
}
</script>

<style>
.employee-manage { padding: 8px }
.w420 { max-width: 420px }
.panel { margin-bottom: 16px }
.card-header { display: flex; align-items: center; justify-content: space-between }
.tools { display: flex; align-items: center }
.mr8 { margin-right: 8px }
.mb8 { margin-bottom: 8px }
.mt12 { margin-top: 12px }
.placeholder { color: #909399 }
.flex-row { display: flex; align-items: center }
.dialog-body { padding: 8px }
</style>
