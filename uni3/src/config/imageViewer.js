// 图片预览组件配置
export const imageViewerConfig = {
  // 默认是否使用原生预览
  // true: 使用 uni.previewImage 原生预览（推荐单张图片）
  // false: 使用自定义预览组件（推荐多张图片）
  useNativePreview: false, // 默认使用自定义预览，支持缩略图导航
  
  // 自定义预览组件配置
  customPreview: {
    // 是否显示信息栏
    showInfo: true,
    // 是否显示缩略图导航
    showThumbnail: true,
    // 点击图片是否关闭预览
    closeOnClick: true,
    // 缩放范围
    scaleMin: 1,
    scaleMax: 3
  }
}

// 获取预览配置
export const getImageViewerConfig = () => {
  return imageViewerConfig
}

// 设置是否使用原生预览
export const setUseNativePreview = (useNative) => {
  imageViewerConfig.useNativePreview = useNative
}