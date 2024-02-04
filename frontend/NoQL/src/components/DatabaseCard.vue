<script setup lang="ts">
import type { Database } from '@/api/databaseApi'

const props = defineProps<{
  database?: Database
}>()
</script>

<template>
  <v-card v-if="props.database">
    <v-card-title>
      {{ props.database.name }}
      <v-chip class="ma-2" color="pink" label>
        {{ props.database.engine }}
      </v-chip>
    </v-card-title>

    <v-card-item>
      <span>{{ props.database.host }}</span>
      <span class="font-weight-bold mx-1">:</span>
      <span>{{ props.database.port }}</span>
      <span class="font-weight-bold mx-1">/</span>
      <span>{{ props.database.database }}</span>
    </v-card-item>

    <!--actions-->
    <v-card-actions>
      <v-btn>Query</v-btn>
      <v-tooltip text="Not implemented yet">
        <template v-slot:activator="{ props }">
          <v-btn v-bind="props">Update</v-btn>
        </template>
      </v-tooltip>
    </v-card-actions>
  </v-card>
</template>

<style scoped></style>
