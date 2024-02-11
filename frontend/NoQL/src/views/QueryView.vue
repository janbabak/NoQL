<script setup lang="ts">
import { computed, onMounted, type Ref, ref } from 'vue'
import { useRoute } from 'vue-router'
import databaseApi, { type Database, type QueryResponse } from '@/api/databaseApi'

const databaseId = useRoute().params.databaseId
const database: Ref<Database | null> = ref(null)
const databaseLoading: Ref<boolean> = ref(false)
const queryLoading: Ref<boolean> = ref(false)
const queryResult: Ref<QueryResponse | null> = ref(null)
const headers = computed(() => {
  return queryResult.value?.result.columnNames.map((columnName, index) => {
    return {
      'title': columnName,
      'key': `${index}`
    }
  })
})

onMounted(() => {
  // loadDatabase()
  // queryDatabase()
  loadSampleData()
  console.log(headers)
})

function loadSampleData() {
  queryResult.value = {
    result: {
      columnNames: ['id', 'name', 'age', 'sex', 'email'],
      rows: [
        [
          '24',
          'Jane Smith',
          '30',
          'F         ',
          'jane.smith@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '25',
          'Jane Doe',
          '28',
          'F         ',
          'jane.doe@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '27',
          'Emily Johnson',
          '40',
          'F         ',
          'emily.johnson@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '29',
          'Sarah Brown',
          '29',
          'F         ',
          'sarah.brown@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '31',
          'Jessica Lee',
          '26',
          'F         ',
          'jessica.lee@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '33',
          'Amanda Martinez',
          '31',
          'F         ',
          'amanda.martinez@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '35',
          'Olivia Garcia',
          '38',
          'F         ',
          'olivia.garcia@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '37',
          'Sophia Lopez',
          '32',
          'F         ',
          'sophia.lopez@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '39',
          'Emma Scott',
          '30',
          'F         ',
          'emma.scott@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '41',
          'Ella Thomas',
          '24',
          'F         ',
          'ella.thomas@example.com',
          '2024-01-28 18:33:43.938089'
        ],
        [
          '43',
          'Grace Miller',
          '34',
          'F         ',
          'grace.miller@example.com',
          '2024-01-28 18:33:43.938089'
        ]
      ]
    },
    query: 'SELECT * FROM public."user"\nWHERE sex = \'F\''
  } as QueryResponse
}

// get query result
async function queryDatabase() {
  queryLoading.value = true
  try {
    const response = await databaseApi.queryNaturalLanguage(
      databaseId,
      'get all users that are male'
    )
    queryResult.value = response.data
  } catch (error: any) {
    console.log(error.message) // TODO: handles
  } finally {
    queryLoading.value = false
  }
}

// get database from API
async function loadDatabase() {
  databaseLoading.value = true
  try {
    const response = await databaseApi.getById(databaseId)
    database.value = response.data
  } catch (error: any) {
    console.log(error.message) // TODO: handles
  } finally {
    databaseLoading.value = false
  }
}
</script>

<template>
  <div class="ma-16">
    <h1>Query</h1>

    <v-data-table :items="queryResult?.result.rows" :headers="headers"/>
  </div>
</template>

<style scoped></style>
