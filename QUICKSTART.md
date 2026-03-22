# Lexi 快速开始指南

## 项目已完成的内容

你的单词背诵系统已经搭建完成，包含以下功能：

### 🎯 主要功能模块

#### 1. 首页（Home）
- ⭕ 中央圆形"进入单词背诵"按钮
- 📋 背诵计划展示和管理
- ➕ 添加新计划功能
- 📊 进度条显示学习进度

#### 2. 单词本（Wordbook）
- 📚 5个预设单词本（高考、四级、六级、雅思、托福）
- 📈 显示单词总数和已掌握数
- 📊 学习进度可视化
- 🔄 支持选择不同单词本

#### 3. 游戏（Game）
- ✏️ 单词拼写
- 🎯 单词匹配
- 🎧 听音识词
- 🔗 单词接龙
- ⚡ 快速反应
- 📝 单词填空

#### 4. 我的（Drawer Menu）
- 👤 用户登录/注册
- 📊 学习统计信息
- ⚙️ 应用设置
- ℹ️ 关于应用
- 🚪 退出登录

### 🎨 UI/UX 特点

- 现代化的Material 3设计
- 响应式布局
- 流畅的页面切换
- 统一的配色方案（靛蓝、紫色、粉红）
- 直观的导航结构

## 项目结构

```
Lexi/
├── app/
│   └── src/main/java/com/syq/lexi/
│       ├── MainActivity.kt              # 应用入口
│       ├── ui/
│       │   ├── navigation/
│       │   │   └── MainNavigation.kt    # 导航管理
│       │   ├── screens/
│       │   │   ├── HomeScreen.kt        # 首页
│       │   │   ├── WordbookScreen.kt    # 单词本
│       │   │   ├── GameScreen.kt        # 游戏
│       │   │   └── DrawerContent.kt     # 我的页面
│       │   ├── theme/
│       │   │   ├── Color.kt             # 颜色配置
│       │   │   ├── Theme.kt             # 主题配置
│       │   │   └── Type.kt              # 字体配置
│       │   └── viewmodel/
│       │       └── ViewModels.kt        # 状态管理
│       └── data/
│           └── model/
│               └── Models.kt            # 数据模型
├── README.md                            # 项目说明
└── ARCHITECTURE.md                      # 架构文档
```

## 如何运行

### 方式一：Android Studio
1. 打开 Android Studio
2. 选择 "Open" → 选择 `d:\Lexi` 文件夹
3. 等待 Gradle 同步完成
4. 点击 "Run" 或按 Shift + F10
5. 选择模拟器或连接的设备

### 方式二：命令行
```bash
cd d:\Lexi
./gradlew build          # 构建项目
./gradlew installDebug   # 安装到设备
```

## 项目特色

### ✨ 已实现的功能
- ✅ 完整的导航系统
- ✅ 所有主要页面的UI
- ✅ 响应式布局
- ✅ 数据模型定义
- ✅ ViewModel框架
- ✅ 主题系统

### 🔄 需要后续实现的功能
- ⏳ 单词背诵核心逻辑
- ⏳ 游戏游玩逻辑
- ⏳ 数据库集成（Room）
- ⏳ 用户认证系统
- ⏳ 云同步功能
- ⏳ 语音发音功能

## 开发建议

### 第一步：实现单词背诵页面
创建 `StudyScreen.kt`，实现：
- 单词卡片展示
- 中文/英文切换
- 标记掌握/未掌握
- 下一个/上一个导航

### 第二步：集成数据库
使用 Room 数据库：
- 存储单词数据
- 记录学习进度
- 保存用户信息

### 第三步：实现游戏逻辑
为每个游戏创建独立的页面和逻辑

### 第四步：添加用户系统
实现登录、注册、个人资料管理

## 文件说明

| 文件 | 说明 |
|------|------|
| `MainActivity.kt` | 应用主入口，初始化主题和导航 |
| `MainNavigation.kt` | 管理底部导航栏和抽屉菜单 |
| `HomeScreen.kt` | 首页，显示背诵按钮和计划 |
| `WordbookScreen.kt` | 单词本选择页面 |
| `GameScreen.kt` | 游戏选择页面 |
| `DrawerContent.kt` | 左侧菜单（我的页面） |
| `Models.kt` | 数据模型定义 |
| `ViewModels.kt` | 状态管理 |
| `Color.kt` | 颜色配置 |
| `Theme.kt` | 主题配置 |

## 常见问题

**Q: 如何修改应用名称？**
A: 编辑 `app/src/main/res/values/strings.xml`

**Q: 如何修改应用图标？**
A: 替换 `app/src/main/res/mipmap-*/ic_launcher.webp`

**Q: 如何添加新的页面？**
A: 
1. 在 `ui/screens/` 创建新的 Composable 函数
2. 在 `MainNavigation.kt` 中添加新的导航项
3. 在底部导航栏中添加对应的按钮

**Q: 如何修改颜色方案？**
A: 编辑 `ui/theme/Color.kt` 中的颜色值

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **设计系统**: Material 3
- **最低API**: 28 (Android 9.0)
- **目标API**: 35 (Android 15)

## 下一步

1. 在 Android Studio 中打开项目
2. 运行应用查看效果
3. 根据需求修改和扩展功能
4. 参考 `ARCHITECTURE.md` 了解详细的架构设计

祝你开发愉快！🚀
