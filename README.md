# Lexi - 智能单词背诵 App

一款基于艾宾浩斯遗忘曲线的 Android 英语单词背诵应用，支持多阶段学习、智能复习调度、云端数据同步。

---

## 功能介绍

### 用户系统
- 注册 / 登录 / 退出
- 注册后自动从系统词库初始化个人词库并上传至个人云端
- 登录时自动从个人云端同步词库及复习数据到本地

### 词库管理
- 内置高考、四级、六级、托福、思维词汇等系统词库
- 支持查看词库详情（总词数、已掌握数、收藏难词数）
- 支持手动同步：对比系统词库与个人词库，补充缺失词库/单词
- 支持删除单词、修改释义

### 三阶段学习模式
每组单词经历三个阶段，层层巩固：
1. **Phase 1 - 词义选择**：看英文选中文（4选1）
2. **Phase 2 - 单词选择**：看中文选英文（4选1）
3. **Phase 3 - 拼写输入**：看中文手动拼写英文

答错的单词会重新加入队列，直到全部答对才能进入下一阶段。

### 智能复习系统
- 基于**艾宾浩斯遗忘曲线**计算复习间隔（1→2→4→7→15→30→60 天）
- 熟悉度算法综合考量：答题正确率（50%）+ 反应速度（30%）+ 拼写表现（20%）
- 熟悉度越高，复习间隔自动拉长（最高 ×2.0）；熟悉度越低，间隔缩短（最低 ×0.5）
- 首页黄色圆形按钮实时展示待复习单词数量
- 复习模式按紧迫程度排序（最早到期的优先）
- 复习完成后显示剩余待复习数量，支持连续复习

### 收藏难词
- 学习过程中可将单词标记为「收藏难词」
- 支持单独进入「难词模式」针对性练习

### 背诵计划
- 为每个词库设置每日背诵目标（词数/天）
- 首页展示计划进度

### 趣味游戏
- 游戏模块（GameScreen）提供多种趣味练习方式

---

## 技术栈

### 客户端（Android）

| 技术 | 说明 |
|------|------|
| Kotlin | 主要开发语言 |
| Jetpack Compose | 声明式 UI 框架 |
| Material 3 | 设计系统 |
| Room | 本地数据库（SQLite 封装） |
| Kotlin Coroutines + Flow | 异步编程 & 响应式数据流 |
| Retrofit 2 | HTTP 网络请求 |
| DataStore | 轻量级本地 KV 存储（Token 等） |
| ViewModel + StateFlow | MVVM 架构状态管理 |

### 服务端（Spring Boot）

| 技术 | 说明 |
|------|------|
| Kotlin + Spring Boot | 后端框架 |
| Spring Security + JWT | 用户认证授权 |
| Spring Data JPA + Hibernate | ORM 数据访问层 |
| MySQL | 关系型数据库 |
| Gradle | 构建工具 |

---

## 代码架构

### 整体架构：MVVM + Repository

```
 UI层 (Compose Screens)
      ↓↑
 ViewModel层 (状态管理 + 业务逻辑)
      ↓↑
 Repository层 (数据来源抽象)
      ↓↑
 数据层 (Room本地DB + Retrofit远程API)
```

### 客户端目录结构

```
app/src/main/java/com/syq/lexi/
├── MainActivity.kt                    # 应用入口
├── LexiApplication.kt                 # Application 类
│
├── data/
│   ├── auth/
│   │   └── AuthPreferences.kt         # DataStore 存储 Token / 用户名
│   ├── database/
│   │   ├── Entities.kt                # Room 实体（WordbookEntity, WordEntity, StudyRecordEntity, StudyPlanEntity）
│   │   ├── Daos.kt                    # Room DAO（WordbookDao, WordDao, StudyRecordDao, StudyPlanDao）
│   │   └── LexiDatabase.kt            # Room 数据库单例
│   ├── network/
│   │   ├── LexiApi.kt                 # Retrofit API 接口定义
│   │   ├── ApiDtos.kt                 # 网络传输对象（WordDto, WordbookDto 等）
│   │   └── RetrofitClient.kt          # Retrofit 单例配置
│   ├── model/
│   │   └── Models.kt                  # 业务数据模型
│   └── repository/
│       ├── WordbookRepository.kt      # 词库 / 单词 / 复习数据本地操作
│       └── SyncRepository.kt          # 云端同步逻辑（初始化 & 手动同步）
│
├── ui/
│   ├── navigation/
│   │   └── MainNavigation.kt          # 导航控制 + ViewModel 生命周期管理
│   ├── screens/
│   │   ├── AuthScreen.kt              # 登录 / 注册界面
│   │   ├── HomeScreen.kt              # 首页（复习按钮 + 背诵计划）
│   │   ├── WordbookScreen.kt          # 词库列表界面
│   │   ├── LearningScreen.kt          # 学习 / 复习主界面（三阶段 + 完成界面）
│   │   ├── StudyScreen.kt             # 单词学习（单词卡片浏览）
│   │   ├── StudyScreenGrouped.kt      # 分组学习界面
│   │   ├── StarredWordsScreen.kt      # 收藏难词界面
│   │   ├── GameScreen.kt              # 游戏模块
│   │   ├── DrawerContent.kt           # 左侧抽屉菜单
│   │   ├── AddWordsDialog.kt          # 添加单词对话框
│   │   ├── AddPlanDialog.kt           # 添加背诵计划对话框
│   │   └── ImportWordbookDialog.kt    # 导入词库对话框
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt           # 用户认证状态管理
│   │   ├── WordbookViewModel.kt       # 词库 / 单词数据 + 远程同步
│   │   ├── LearningViewModel.kt       # 学习会话状态管理 + 复习数据上传
│   │   ├── SyncViewModel.kt           # 云端同步状态管理
│   │   └── StudyPlanViewModel.kt      # 背诵计划管理
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── util/
    └── ReviewAlgorithm.kt             # 艾宾浩斯复习算法 + 熟悉度计算
```

### 服务端目录结构

```
backend/src/main/kotlin/com/lexi/backend/
├── BackendApplication.kt
├── config/
│   ├── SecurityConfig.kt              # Spring Security 配置
│   ├── JwtUtil.kt                     # JWT 生成 / 验证
│   └── JwtAuthFilter.kt               # JWT 请求过滤器
├── controller/
│   ├── AuthController.kt              # 注册 / 登录接口
│   ├── WordbookController.kt          # 个人词库 CRUD + 单词同步
│   ├── SystemController.kt            # 系统词库只读接口
│   └── StudyPlanController.kt         # 背诵计划接口
├── service/
│   ├── AuthService.kt
│   ├── WordbookService.kt
│   ├── SystemDataService.kt
│   └── StudyPlanService.kt
├── entity/                            # JPA 实体
│   ├── User.kt
│   ├── Wordbook.kt / Word.kt          # 个人词库 & 单词
│   ├── SystemWordbook.kt / SystemWord.kt  # 系统词库
│   ├── StudyPlan.kt
│   └── StudyRecord.kt
├── repository/                        # Spring Data JPA Repository
└── dto/
    └── Dtos.kt                        # 请求 / 响应 DTO
```

---

## 数据库设计

### 本地 Room 数据库

**wordbooks 表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int (PK) | 自增主键 |
| name | String | 词库名称 |
| category | String | 分类 |
| description | String | 描述 |
| totalWords | Int | 总词数 |
| createdDate | Long | 创建时间戳 |

**words 表**（外键关联 wordbooks，联合唯一索引 wordbookId+english）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int (PK) | 自增主键 |
| wordbookId | Int (FK) | 所属词库 |
| english | String | 英文单词 |
| chinese | String | 中文释义 |
| pronunciation | String | 音标 |
| partOfSpeech | String | 词性 |
| example | String | 例句 |
| exampleTranslation | String | 例句翻译 |
| isMastered | Boolean | 是否已掌握 |
| isStarred | Boolean | 是否收藏为难词 |
| familiarity | Float | 熟悉度（0~1） |
| reviewCount | Int | 已复习次数 |
| nextReviewDate | Long | 下次复习时间（epoch ms，0=未安排） |

**study_records 表**（每次答题记录）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int (PK) | 自增主键 |
| wordbookId | Int | 所属词库 |
| wordId | Int | 单词 ID |
| studyDate | Long | 答题时间 |
| isCorrect | Boolean | 是否答对 |
| phase | Int | 阶段（0=词义选择，1=单词选择，2=拼写） |
| hesitationMs | Long | 题目出现到作答的时间差（ms） |
| durationMs | Long | 本次学习总用时 |

**study_plans 表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int (PK) | 自增主键 |
| wordbookId | Int | 所属词库 |
| dailyWords | Int | 每日目标词数 |

### 服务端 MySQL 数据库

| 表名 | 说明 |
|------|------|
| `users` | 用户账号（username、password_hash） |
| `wordbooks` | 个人词库（关联 user_id） |
| `words` | 个人单词（含 familiarity、review_count、next_review_date） |
| `system_wordbooks` | 系统词库（只读，管理员维护） |
| `system_words` | 系统单词（只读） |
| `study_plans` | 背诵计划（关联 user_id + wordbook_name） |

---

## API 接口

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |

### 个人词库
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/wordbooks` | 获取个人词库列表 |
| POST | `/api/wordbooks` | 创建词库 |
| DELETE | `/api/wordbooks/{id}` | 删除词库 |
| GET | `/api/wordbooks/{id}/words` | 获取词库单词 |
| POST | `/api/wordbooks/{id}/words/sync` | 批量同步单词 |
| PATCH | `/api/wordbooks/{wbId}/words/{wordId}/mastered` | 更新掌握状态 |
| PATCH | `/api/wordbooks/{wbId}/words/{wordId}/starred` | 更新收藏状态 |
| PATCH | `/api/wordbooks/{wbId}/words/{wordId}/review` | 更新复习数据 |
| DELETE | `/api/wordbooks/{wbId}/words/{wordId}` | 删除单词 |

### 系统词库
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/system/wordbooks` | 获取系统词库列表 |
| GET | `/api/system/wordbooks/{id}/words` | 获取系统词库单词 |

### 背诵计划
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/study-plans` | 获取背诵计划 |
| POST | `/api/study-plans` | 保存背诵计划 |
| DELETE | `/api/study-plans` | 删除背诵计划 |

---

## 复习算法说明

### 熟悉度计算
```
familiarity = 正确率 × 0.5 + 速度因子 × 0.3 + 拼写因子 × 0.2
```
- **正确率**：答对次数 / 总答题次数  
- **速度因子**：平均犹豫时间（≤5s 满分，≥30s 零分）  
- **拼写因子**：Phase 3 拼写阶段的正确率  

### 复习间隔
基础间隔：1 → 2 → 4 → 7 → 15 → 30 → 60 天  
熟悉度调整系数：0.5x（低）~ 2.0x（高）

---

## 快速启动

### 启动服务端
```bash
cd backend
./gradlew bootRun
# 服务运行在 http://localhost:8081
```

### 配置数据库
编辑 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lexi
    username: root
    password: your_password
```

### 启动客户端
1. 用 Android Studio 打开项目根目录
2. 修改 `RetrofitClient.kt` 中的 `BASE_URL` 为服务端地址
3. 编译运行到真机或模拟器（API 28+）

---

## License

MIT License
