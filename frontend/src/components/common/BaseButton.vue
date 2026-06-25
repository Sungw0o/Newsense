<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary' // primary, secondary, outline, danger, ghost
  },
  type: {
    type: String,
    default: 'button'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'md' // sm, md, lg
  }
})

const buttonClasses = computed(() => {
  const base = 'inline-flex items-center justify-center rounded-xl font-bold transition-all duration-300 active:scale-95 outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2'
  
  const sizes = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-5 py-2.5 text-sm',
    lg: 'px-7 py-3.5 text-base'
  }

  const variants = {
    primary: 'bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-100 hover:shadow-lg disabled:bg-primary-300 disabled:shadow-none',
    secondary: 'bg-secondary-500 hover:bg-secondary-600 text-white shadow-md shadow-secondary-100 hover:shadow-lg disabled:bg-secondary-300 disabled:shadow-none',
    outline: 'bg-white border-2 border-slate-200 text-slate-600 hover:border-primary-500 hover:text-primary-600 disabled:text-slate-300 disabled:border-slate-100',
    danger: 'bg-accent-600 hover:bg-accent-700 text-white shadow-md shadow-accent-100 disabled:bg-accent-300',
    ghost: 'bg-transparent text-slate-500 hover:bg-slate-100 hover:text-slate-700 disabled:text-slate-300'
  }

  return `${base} ${sizes[props.size]} ${variants[props.variant]} ${props.disabled || props.loading ? 'cursor-not-allowed opacity-75 active:scale-100' : ''}`
})
</script>

<template>
  <button 
    :type="type" 
    :class="buttonClasses" 
    :disabled="disabled || loading"
  >
    <!-- Spinner during loading -->
    <span 
      v-if="loading" 
      class="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin mr-2"
    ></span>
    
    <slot />
  </button>
</template>
