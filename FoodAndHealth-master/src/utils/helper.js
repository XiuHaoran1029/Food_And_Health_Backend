/**
 * 将 File 对象转换为 Base64 字符串
 * @param {File} file - 要转换的文件对象
 * @returns {Promise<string>} Base64 编码的数据 URL
 */
export const fileToBase64 = (file) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => resolve(reader.result)
  reader.onerror = reject
})

/**
 * 压缩图片到指定大小
 * @param {Blob} imageBlob - 原始图片 Blob
 * @param {Object} options - 压缩选项
 * @param {number} options.maxWidth - 最大宽度（默认 640）
 * @param {number} options.maxHeight - 最大高度（默认 640）
 * @param {number} options.minSize - 最小文件大小（KB，默认 50）
 * @param {number} options.maxSize - 最大文件大小（KB，默认 100）
 * @returns {Promise<{base64: string, blob: Blob, originalSize: number, compressedSize: number, ratio: number}>}
 */
export async function compressImage(imageBlob, options = {}) {
  const {
    maxWidth = 640,
    maxHeight = 640,
    minSize = 50,
    maxSize = 100,
    initialQuality = 0.7,
    qualityStep = 0.05,
    minQuality = 0.1
  } = options

  console.log('[Compress] 开始压缩图片')
  console.log('[Compress] 原始大小:', (imageBlob.size / 1024).toFixed(2), 'KB')

  return new Promise((resolve, reject) => {
    const img = new Image()
    const url = URL.createObjectURL(imageBlob)

    img.onload = async () => {
      try {
        URL.revokeObjectURL(url)

        // 1. 计算目标尺寸（保持宽高比）
        let { width, height } = img
        const aspectRatio = width / height

        if (width > maxWidth || height > maxHeight) {
          if (width > height) {
            width = maxWidth
            height = Math.round(maxWidth / aspectRatio)
          } else {
            height = maxHeight
            width = Math.round(maxHeight * aspectRatio)
          }
        }

        console.log('[Compress] 目标尺寸:', width, 'x', height)

        // 2. 创建 Canvas 并绘制图片
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height

        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)

        // 3. 循环压缩直到文件大小在目标范围内
        let quality = initialQuality
        let compressedBlob = null
        let compressedSize = 0
        let iterations = 0
        const maxIterations = 20

        while (iterations < maxIterations) {
          // 压缩图片
          compressedBlob = await new Promise((res) => {
            canvas.toBlob(
              (blob) => res(blob),
              'image/jpeg',
              quality
            )
          })

          compressedSize = compressedBlob.size / 1024 // KB
          console.log(`[Compress] 迭代 ${iterations + 1}: 质量 ${quality.toFixed(2)}, 大小 ${compressedSize.toFixed(2)} KB`)

          // 检查是否在目标范围内
          if (compressedSize >= minSize && compressedSize <= maxSize) {
            console.log('[Compress] 压缩成功，文件大小在目标范围内')
            break
          }

          // 如果文件太大，降低质量
          if (compressedSize > maxSize) {
            quality -= qualityStep
          }
          // 如果文件太小，增加质量
          else {
            quality += qualityStep
          }

          // 边界检查
          if (quality < minQuality) {
            console.warn('[Compress] 达到最小质量，停止压缩')
            break
          }
          if (quality > 1) {
            console.warn('[Compress] 达到最大质量，停止压缩')
            break
          }

          iterations++
        }

        // 4. 计算压缩比例
        const originalSize = imageBlob.size / 1024
        const ratio = Number(((originalSize - compressedSize) / originalSize * 100).toFixed(2))

        console.log('[Compress] 压缩完成:')
        console.log(`  - 原始大小: ${originalSize.toFixed(2)} KB`)
        console.log(`  - 压缩后大小: ${compressedSize.toFixed(2)} KB`)
        console.log(`  - 压缩比例: ${ratio}%`)

        // Convert blob to base64 string
        const reader = new FileReader()
        reader.readAsDataURL(compressedBlob)
        reader.onload = () => {
          const base64String = reader.result
          resolve({
            base64: base64String,
            blob: compressedBlob,
            originalSize,
            compressedSize,
            ratio
          })
        }
        reader.onerror = (error) => {
          console.error('[Compress] 转换为base64失败:', error)
          reject(error)
        }
      } catch (error) {
        console.error('[Compress] 压缩失败:', error)
        reject(error)
      }
    }

    img.onerror = (error) => {
      console.error('[Compress] 图片加载失败:', error)
      reject(error)
    }

    img.src = url
  })
}
