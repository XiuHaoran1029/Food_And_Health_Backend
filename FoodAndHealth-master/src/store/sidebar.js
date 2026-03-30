import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSidebarStore = defineStore('sidebar', () => {
  const isOpen = ref(true)

  function toggle() {
    isOpen.value = !isOpen.value
  }

  function setOpen(value) {
    isOpen.value = value
  }

  return { isOpen, toggle, setOpen }
})
