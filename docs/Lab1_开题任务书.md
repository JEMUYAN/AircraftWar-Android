# 一、项目概述

本项目将此前用 Java Swing 开发的桌面版《飞机大战》（`AircraftWar-base1.0`）重构为 **Android 应用**（Java，minSdk 30，Android Studio），并在后续实验中逐步加入难度选择、音乐音效、本地排行榜和网络功能。

**现有功能基础：**
- 英雄机、四类敌机（普通、精英、超级精英、王牌）和 Boss 机。
- 五种射击方式：直射、散射、扇形、环形、不射击。
- 五种道具：加血、炸弹、火力、超级火力、冰冻。
- 简单、普通、困难三档难度，随时间提升难度。
- 背景音乐与音效。
- 按难度分别存储的排行榜。

**实施意义：**
1. **工程层面**：练习"跨平台迁移"，把平台无关的游戏逻辑与平台相关的渲染、输入、音频、存储分离。这比重写更贴近实际开发。
2. **技术层面**：掌握 Android 的 Activity 生命周期、`SurfaceView` 与子线程游戏循环、Handler 线程通信、`MediaPlayer`/`SoundPool`、本地存储和网络通信。
3. **质量层面**：旧版实现有些粗糙，例如用大量 `new Thread(sleep)` 做计时、`AbstractGame` 职责过重、排行榜解析不健壮、难度用字符串表示。迁移时同步重构，保持游戏规则不变，提升可维护性与可测试性。
4. **体验层面**：触屏操作、手机端音效、联网排行榜与双人对战，比单机桌面版更有可玩性。

# 二、原有功能迁移分析

**迁移对象：** 本人 `AircraftWar-base1.0` 的全部代码（`edu.hitsz` 下 `basic / aircraft / bullet / shoot / prop / dao / application` 七个包，约 45 个类）。该版本已实现完整游戏逻辑，并应用了单例、工厂、策略、观察者、模板方法和 DAO 等设计模式，有 UML 与单元测试，质量可作为迁移基线。

## 1. 可复用模块和依据

| 模块 | 内容 | 复用依据 |
|---|---|---|
| `basic` | `AbstractFlyingObject`（坐标、速度、碰撞判定、消失） | 属性与移动、碰撞算法是纯数学逻辑，不依赖平台。仅需把图片类型从 `BufferedImage` 换成宽高信息或 `Bitmap`。 |
| `aircraft` | 英雄机/各类敌机及其工厂 | 血量、移动规则、掉落规则与平台无关，工厂方法模式可原样保留。 |
| `bullet` | `HeroBullet`、`EnemyBullet` | 子弹的移动、伤害、出界判定是纯逻辑。 |
| `shoot` | 五种 `ShootStrategy` | 只负责生成子弹对象，是纯 Java 策略模式，可直接复制。 |
| `prop` | 道具类、`PropFactory`、观察者（`PropEventPublisher/Observer`） | 道具效果和事件广播机制与平台无关。 |
| `dao` 的接口与实体 | `ScoreDAO`、`Score` | 接口隔离使上层不依赖具体存储，换实现即可；解析逻辑需增强健壮性。 |
| `application` 中的规则部分 | `AbstractGame` 的难度参数、计分、Boss 阈值、碰撞处理流程，以及 Easy/Normal/Hard 三个子类 | 游戏规则已在桌面端验证，直接沿用（模板方法保留）。 |
| 资源文件 | `src/images/*.png|jpg`、`src/videos/*.wav` | 格式 Android 原生支持，放入 `res/drawable`、`res/raw`（文件名需小写）。 |

> 复用原则：复制后改包名，尽量不改逻辑，必要处再做优化（见下）。

**迁移时顺带优化（不改游戏规则）：**
- 将各处 `new Thread + sleep`（冻结、道具效果计时）统一为游戏循环内按 tick 或时间戳倒计时，避免线程泄漏，也便于暂停和退出时停止。
- 拆分 `AbstractGame`，把逻辑、生成、碰撞和渲染分开。
- 难度改用 `enum`，把魔法数字集中为配置。
- 让逻辑层不依赖 `android.*`，便于 JVM 单元测试，并补充测试。

## 2. 需重新开发的模块和依据

| 模块 | 桌面版实现 | Android 方案 | 重新开发的原因 |
|---|---|---|---|
| 程序入口与界面 | `Main`、`JFrame`/`JPanel`、`DifficultySelectFrame`、`RankingFrame`、`PlayerNameInputDialog` | `Activity` + XML 布局；游戏页用 `SurfaceView`；页面跳转用 `Intent` | Swing/AWT 在 Android 上不可用，应用模型是基于组件和生命周期的。 |
| 绘制与游戏循环 | `JPanel.paint` + `java.util.Timer` | `SurfaceView` 实现 `SurfaceHolder.Callback` 和 `Runnable`，子线程中 `lockCanvas` → 绘制 → `unlockCanvasAndPost` | Android 要求主线程不阻塞，高频绘制应放在子线程；`Graphics` 换为 `Canvas`。 |
| 图片加载 | `ImageIO.read` + 相对路径 | `BitmapFactory.decodeResource`，在 `surfaceCreated` 一次性加载并缓存 | Android 没有可用的相对路径文件系统，需走资源 ID。每帧 decode 会 OOM。 |
| 输入 | `HeroController`（鼠标监听） | 触屏事件（`onTouchEvent`/`OnTouchListener`） | 输入设备由鼠标变为触屏；坐标需按实际屏幕尺寸比例换算，不能写死 512×768。 |
| 音频 | `javax.sound.sampled.Clip` | `MediaPlayer`（BGM，Boss 时切换）+ `SoundPool`（音效），封装在独立 `music` 包，并增加音乐开关 | `javax.sound` 不可用；长音乐和短音效适用不同 API。 |
| 排行榜存储与页面 | 相对路径 `scores_*.txt` + Swing 表格 | 应用私有存储（内部文件、`SharedPreferences` 或 Room）+ `ListView`/`RecyclerView` | 桌面相对路径在 Android 不适用；需考虑线程、权限与数据解析容错。 |
| 跨线程通知 | 无（Swing 同线程回调） | `Handler` + `Looper.getMainLooper()` | 游戏结束后跳转页面属于 UI 操作，只能在主线程。绘制线程需发消息通知 Activity。 |
| 生命周期管理 | 无 | 在 `surfaceDestroyed`、`onPause`/`onDestroy` 中停止线程、释放音频 | Android 应用可随时被切到后台或销毁，需防止线程泄漏和内存泄漏。 |

# 三、新增网络功能设计

计划新增两项网络功能，并预留第三项备选。网络请求一律放在子线程，结果经 `Handler` 回主线程；`INTERNET` 权限在 Manifest 中声明；网络异常时保证单机玩法不受影响。

## 功能 1：云端排行榜（得分云同步）

- **功能内容：** 游戏结束后，玩家昵称、难度、得分和时间上传服务器；排行榜页面可切换"本地榜"和"全球榜"，按难度分别展示前 N 名。
- **设计原因：** 本地榜只能自己和自己比，联网后能与其他玩家比较，增加竞争性。同时它是 Lab4"本地存储"的自然延伸。
- **技术思路：**
  - 服务端提供 REST 接口：`POST /scores`（上传）、`GET /scores?difficulty=hard&limit=20`（拉取），数据用 JSON，存 SQLite 或 MySQL。服务端用 Spring Boot 或 Node.js 实现，开发阶段部署在本机或局域网。
  - 客户端用 `HttpURLConnection` 或 OkHttp，在工作线程中发起请求，用 Gson 或 `org.json` 解析。`ScoreDAO` 新增远程实现（`RemoteScoreDAO`），上层只依赖接口，保持 DAO 模式。
  - 离线时先写本地，恢复网络后补传。上传失败不影响游戏结束流程，并给出提示。
  - 简单防作弊：服务端校验分数范围，并限制单位时间内的提交频率。

## 功能 2：双人联机对战（实时得分同步）

- **功能内容：** 两名玩家通过房间号匹配，同时开始同一难度的游戏。游戏界面实时显示对方的得分和血量，一方血量归零时对战结束，按得分判定胜负，并展示结果页。
- **设计原因：** 课程要求包含网络对战，联机能最大限度发挥实时通信技术；也比单机更有趣。
- **技术思路：**
  - 采用"各端独立运行游戏、只同步关键状态"的方案：每个客户端本地模拟自己的飞机和敌机，每 100~200 ms 向服务器发送 `{playerId, score, hp, alive}`，由服务器转发给对手。这样带宽小、对延迟不敏感，也避免了"全量状态同步"的复杂性。
  - 通信协议用 TCP Socket 或 WebSocket（OkHttp），消息为 JSON，类型包括 `JOIN/READY/START/STATE/GAME_OVER`。服务端维护房间并统一下发 `START` 与随机种子，保证双方敌机序列一致、公平。
  - 客户端新增 `NetworkClient` 类（独立线程收发），与游戏循环解耦，经 `Handler`/回调把对方状态交给渲染层；游戏逻辑层不依赖网络。
  - 断线与超时处理：心跳检测，断线判负或允许重连。

## 备选功能 3：账号登录与个人成绩同步

- **内容与原因：** 用户注册、登录后可保存自己的历史成绩，供云端排行榜识别身份，防止冒名。
- **思路：** `POST /login` 返回 token，之后请求带上；密码在服务端加盐哈希；token 存 `SharedPreferences`。时间不足时只做昵称模式，不实现该项。

# 四、预期结果

**阶段成果：**
- Lab2：Android 版飞机大战可编译、安装并运行。背景滚动，触屏控制英雄机，敌机生成，射击、碰撞、计分和道具正常；图片全部走 `res/drawable` + `BitmapFactory`，退出后绘制线程正确停止。
- Lab3：难度选择页并按选择跳转到对应难度；BGM、Boss BGM 和各类音效播放正常，带音乐开关；游戏结束可通过 `Handler` 回主线程跳转。
- Lab4：本地持久化排行榜（按难度分榜），有排行榜页面。
- Lab5：云端排行榜 + 双人联机对战可用。
- Lab6：自选扩展（候选：更多道具/敌机、暂停与存档、皮肤等）。
- Lab7：完整应用、源码仓库、结题报告和演示。

**质量目标：**
- 游戏规则与桌面版一致，优化只体现在代码结构与稳定性上。
- 逻辑层可用 JUnit 单测覆盖碰撞、射击策略、道具效果、计分和排行榜解析。
- 无明显 ANR、内存泄漏和线程泄漏，Logcat 中无红色异常。
- 提交记录清晰，每完成一步提交一次，提交信息写明原因。

**风险与对策：**
- 联机延迟或断线：只同步关键状态，设置超时与断线处理。
- 服务器部署：开发期用本机或局域网，答辩时提前备好演示环境；网络不可用时单机功能不受影响。
- 时间与进度：先保证单机主线（Lab2–4），网络功能按"云榜 → 对战"的顺序做，账号功能为可选。

# 五、小组人员分工及依据

（个人项目，一人完成。若组队，请在此填写成员与分工。）

| 成员 | 分工 | 依据 |
|---|---|---|
| 〔姓名，待填〕 | 迁移、界面、音频、存储、网络客户端与服务端、测试与文档 | 桌面版代码为本人所写，最熟悉游戏逻辑与设计，迁移风险最小。 |

> 课件提示：任务书需以组为单位提交，并在共享文档中登记组队。姓名、学号等个人信息请在提交前自行填写，本文件所在仓库为公开仓库，不要把学号写进去。
