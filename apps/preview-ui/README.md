# Preview Generator UI

这是ADOFAI Best 20预览生成器的桌面应用程序。

## 构建

### 构建库模块
```bash
./gradlew :modules:preview-api:build
./gradlew :modules:preview-image:build
./gradlew :modules:preview-collage:build
./gradlew :modules:preview-core:build
```

### 构建所有库
```bash
./gradlew build
```

### 构建Windows可执行程序
```bash
./gradlew :apps:preview-ui:packageMsi
```

构建后的MSI安装程序将位于：`apps/preview-ui/build/compose/binaries/main/msi/`

### 运行应用程序（开发模式）
```bash
./gradlew :apps:preview-ui:run
```

## 使用说明

1. 输入玩家ID（例如：693）
2. （可选）输入背景图片路径
3. 点击"Generate Preview"按钮
4. 等待生成完成
5. 查看预览图片
6. 点击"Save Image"保存图片

## 功能特性

- 从API获取玩家数据
- 生成Best 20预览图
- 显示玩家统计信息
- 保存生成的预览图
