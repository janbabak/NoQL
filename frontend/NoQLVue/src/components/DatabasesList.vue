<script setup lang="ts">
import databaseApi, { type Database } from '@/api/databaseApi'
import { onMounted, type Ref, ref } from 'vue'
import DatabaseCard from '@/components/DatabaseCard.vue'

const databases: Ref<Database[]> = ref([])
const databasesLoading: Ref<boolean> = ref(false)

onMounted(() => {
  loadDatabases()
})

// Load list of databases form API
async function loadDatabases() {
  databasesLoading.value = true
  try {
    const response = await databaseApi.getAll()
    databases.value = response.data
  } catch (error: any) {
    console.log(error.message) // TODO: handles
  } finally {
    databasesLoading.value = false
  }
}
</script>

<template>
  <div class="d-flex flex-column container">
    <h1 class="mb-4">Databases</h1>

    <!--databases-->
    <ul v-if="!databasesLoading" class="d-flex flex-column ga-4">
      <DatabaseCard v-for="database in databases" :key="database.id" :database="database" />
    </ul>
    <!--loading-->
    <div v-else class="d-flex flex-column ga-4">
      <v-skeleton-loader v-for="i in 3" :key="i" type="paragraph" elevation="2" />
    </div>
  </div>
</template>

<style scoped></style>
