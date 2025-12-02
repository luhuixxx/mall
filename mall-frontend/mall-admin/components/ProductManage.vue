<template>
  <div class="product-manage">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>SPU 列表</span>
              <div class="tools">
                <el-input v-model="spuQuery.name" placeholder="名称" size="small" class="mr8" clearable />
                <el-button size="small" type="primary" @click="fetchSpuList">查询</el-button>
                <el-button size="small" type="success" @click="openCreateSpu">新增 SPU</el-button>
              </div>
            </div>
          </template>
          <el-table :data="spuList" height="420" @row-click="selectSpu" highlight-current-row>
            <el-table-column label="图片" width="100">
              <template #default="{ row }">
                <el-image v-if="row.imageUrl" :src="row.imageUrl" fit="cover" style="width:72px;height:48px" />
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="salesCount" label="销量" width="100" />
            <el-table-column prop="createdTime" label="创建时间" />
            <el-table-column label="操作" width="260">
              <template #default="{ row }">
                <el-button size="small" @click.stop="manageSku(row)">管理 SKU</el-button>
                <el-button size="small" @click.stop="openEditSpu(row)">编辑</el-button>
                <el-button size="small" type="danger" @click.stop="doDeleteSpu(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-footer">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="spuQuery.page"
              :page-size="spuQuery.size"
              :total="spuTotal"
              @current-change="p=>{spuQuery.page=p;fetchSpuList()}"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="panel" shadow="hover">
          <template #header>
            <div class="card-header"><span>SKU 管理</span></div>
          </template>
          <div v-if="currentSpu">
            <div class="mb8">当前 SPU：{{ currentSpu.name }}</div>
            <div class="mb8">
              <el-button size="small" type="success" @click="openCreateSku">新增 SKU</el-button>
            </div>
            <el-table :data="skuList" height="360">
              <el-table-column prop="specification" label="规格" />
              <el-table-column prop="price" label="价格" width="120" />
              <el-table-column prop="stockQuantity" label="库存" width="100" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">{{ row.status === 1 ? '上架' : '下架' }}</template>
              </el-table-column>
              <el-table-column prop="updatedTime" label="更新时间" />
              <el-table-column label="操作" width="260">
                <template #default="{ row }">
                  <el-button size="small" @click="openEditSku(row)">编辑</el-button>
                  <el-button size="small" type="warning" @click="openAdjustStock(row)">调整库存</el-button>
                  <el-button size="small" type="danger" @click="doDeleteSku(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else class="placeholder">请选择左侧 SPU 查看与管理其 SKU</div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="spuDialog.visible" :title="spuDialog.isEdit ? '编辑 SPU' : '新增 SPU'" width="520px">
      <el-form :model="spuForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="spuForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="spuForm.description" />
        </el-form-item>
        <template v-if="!spuDialog.isEdit">
          <el-form-item label="商品图片">
            <el-upload
              ref="spuUploadRef"
              :action="uploadAction"
              :headers="uploadHeaders"
              :data="{ name: spuForm.name, description: spuForm.description }"
              :name="'image'"
              :auto-upload="false"
              :limit="1"
              :file-list="spuFileList"
              :on-change="onSpuFileChange"
              :on-remove="onSpuFileRemove"
              :on-success="onSpuUploadSuccess"
              :on-error="onSpuUploadError"
              :before-upload="beforeSpuUpload"
              accept="image/*"
              list-type="text"
            >
              <el-button type="primary">选择图片</el-button>
            </el-upload>
            <div class="mt12" style="color:#909399">仅支持图片类型（png/jpg 等），最多选择 1 个文件</div>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="spuDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="saveSpu">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="skuDialog.visible" :title="skuDialog.isEdit ? '编辑 SKU' : '新增 SKU'" width="520px">
      <el-form :model="skuForm" label-width="100px">
        <el-form-item label="SPU ID">
          <el-input v-model="skuForm.spuId" disabled />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="skuForm.price" type="number" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input v-model="skuForm.stockQuantity" type="number" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="skuForm.status" placeholder="选择状态" style="min-width:160px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="skuForm.specification" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="saveSku">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialog.visible" title="调整库存" width="420px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="增减量">
          <el-input v-model="stockForm.delta" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="saveAdjustStock">确认</el-button>
      </template>
    </el-dialog>
  </div>
  
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSpu, createSpu, updateSpu, deleteSpu, listSkuBySpu, createSku, updateSku, deleteSku, updateSkuStock } from '../api/product'
import { useAuthStore } from '../store/auth'
import { getBaseUrl } from '../utils/request'

const spuQuery = reactive({ page: 1, size: 10, name: '' })
const spuList = ref([])
const spuTotal = ref(0)
const currentSpu = ref(null)

const spuDialog = reactive({ visible: false, isEdit: false })
const spuForm = reactive({ id: null, name: '', description: '', imageUrl: '' })
const spuUploadRef = ref(null)
const spuFileList = ref([])
const uploadAction = getBaseUrl() + '/api/product/spu'
const uploadHeaders = ref({})

const skuList = ref([])
const skuDialog = reactive({ visible: false, isEdit: false })
const skuForm = reactive({ id: null, spuId: null, price: null, stockQuantity: null, status: 1, specification: '' })

const stockDialog = reactive({ visible: false })
const stockForm = reactive({ id: null, delta: 0 })

function isUnauthorized(err) {
  return !!(err && (err.response?.status === 401 || String(err?.message).includes('401')))
}

async function fetchSpuList() {
  try {
    const data = await listSpu({ page: spuQuery.page, size: spuQuery.size, name: spuQuery.name })
    spuList.value = data.records || data.list || []
    spuTotal.value = data.total || 0
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || 'SPU 列表获取失败')
  }
}

function selectSpu(row) {
  currentSpu.value = row
  fetchSkuList()
}

function manageSku(row) {
  currentSpu.value = row
  fetchSkuList()
}

async function fetchSkuList() {
  if (!currentSpu.value) return
  try {
    skuList.value = await listSkuBySpu(currentSpu.value.id)
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || 'SKU 列表获取失败')
  }
}

function openCreateSpu() {
  spuDialog.isEdit = false
  spuForm.id = null
  spuForm.name = ''
  spuForm.description = ''
  spuForm.imageUrl = ''
  spuDialog.visible = true
  const auth = useAuthStore()
  uploadHeaders.value = {
    Authorization: auth.token ? ('Bearer ' + auth.token) : '',
    'X-Auth-Identity': 'EMPLOYEE',
    'X-Auth-UserId': auth.employee?.id ? String(auth.employee.id) : ''
  }
  spuFileList.value = []
}

function openEditSpu(row) {
  spuDialog.isEdit = true
  spuForm.id = row.id
  spuForm.name = row.name
  spuForm.description = row.description || ''
  spuForm.imageUrl = row.imageUrl || ''
  spuDialog.visible = true
}

async function saveSpu() {
  if (!spuForm.name) { ElMessage.error('请输入名称'); return }
  try {
    if (spuDialog.isEdit) {
      await updateSpu(spuForm.id, { name: spuForm.name, description: spuForm.description, imageUrl: spuForm.imageUrl })
      ElMessage.success('SPU 已更新')
    } else {
      if (!spuUploadRef.value) { ElMessage.error('请先选择图片'); return }
      const hasFile = (spuFileList.value && spuFileList.value.length > 0) || (spuUploadRef.value.uploadFiles && spuUploadRef.value.uploadFiles.length > 0)
      if (!hasFile) { ElMessage.error('请先选择商品图片'); return }
      spuUploadRef.value.submit()
      return
    }
    spuDialog.visible = false
    fetchSpuList()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '保存失败')
  }
}

function beforeSpuUpload(file) {
  const isImage = file.type && file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('仅支持图片文件')
  }
  return isImage
}

function onSpuUploadSuccess(response) {
  try {
    const body = typeof response === 'string' ? JSON.parse(response) : response
    if (body && body.code === 0) {
      ElMessage.success('SPU 已创建')
      spuDialog.visible = false
      fetchSpuList()
    } else {
      ElMessage.error(body?.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error('响应解析失败')
  }
}

function onSpuUploadError(err) {
  const msg = (err && err.message) || '上传失败'
  ElMessage.error(msg)
}

function onSpuFileChange(file, fileList) {
  spuFileList.value = fileList
}

function onSpuFileRemove(file, fileList) {
  spuFileList.value = fileList
}

async function doDeleteSpu(row) {
  try {
    await ElMessageBox.confirm('确认删除该 SPU？', '提示', { type: 'warning' })
    await deleteSpu(row.id)
    ElMessage.success('已删除')
    if (currentSpu.value && currentSpu.value.id === row.id) { currentSpu.value = null; skuList.value = [] }
    fetchSpuList()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '删除失败')
  }
}

function openCreateSku() {
  if (!currentSpu.value) { ElMessage.error('请先选择 SPU'); return }
  skuDialog.isEdit = false
  skuForm.id = null
  skuForm.spuId = currentSpu.value.id
  skuForm.price = null
  skuForm.stockQuantity = null
  skuForm.status = 1
  skuForm.specification = ''
  skuDialog.visible = true
}

function openEditSku(row) {
  skuDialog.isEdit = true
  skuForm.id = row.id
  skuForm.spuId = row.spuId
  skuForm.price = row.price
  skuForm.stockQuantity = row.stockQuantity
  skuForm.status = row.status
  skuForm.specification = row.specification || ''
  skuDialog.visible = true
}

async function saveSku() {
  if (!skuForm.spuId || skuForm.price === null || skuForm.stockQuantity === null) { ElMessage.error('请输入必填项'); return }
  try {
    if (skuDialog.isEdit) {
      await updateSku(skuForm.id, { price: Number(skuForm.price), stockQuantity: Number(skuForm.stockQuantity), status: Number(skuForm.status), specification: skuForm.specification })
      ElMessage.success('SKU 已更新')
    } else {
      await createSku({ spuId: skuForm.spuId, price: Number(skuForm.price), stockQuantity: Number(skuForm.stockQuantity), status: Number(skuForm.status), specification: skuForm.specification })
      ElMessage.success('SKU 已创建')
    }
    skuDialog.visible = false
    fetchSkuList()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '保存失败')
  }
}

function openAdjustStock(row) {
  stockForm.id = row.id
  stockForm.delta = 0
  stockDialog.visible = true
}

async function saveAdjustStock() {
  try {
    await updateSkuStock(stockForm.id, { delta: Number(stockForm.delta) })
    ElMessage.success('库存已调整')
    stockDialog.visible = false
    fetchSkuList()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '调整失败')
  }
}

async function doDeleteSku(row) {
  try {
    await ElMessageBox.confirm('确认删除该 SKU？', '提示', { type: 'warning' })
    await deleteSku(row.id)
    ElMessage.success('已删除')
    fetchSkuList()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '删除失败')
  }
}

onMounted(async () => {
  try {
    await fetchSpuList()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '初始化失败')
  }
})
</script>

<style>
.product-manage { padding: 8px }
.panel { margin-bottom: 16px }
.card-header { display: flex; align-items: center; justify-content: space-between }
.tools { display: flex; align-items: center }
.mr8 { margin-right: 8px }
.mb8 { margin-bottom: 8px }
.table-footer { padding: 8px 0 }
.placeholder { color: #909399 }
</style>
