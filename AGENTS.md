# AGENTS.md — AircraftWar（Java Swing → Android 重构实验）

> 给 coding agent 的项目入口说明。**事实性内容（命令、路径、类名）以仓库实际状态为准；发现与现状不符时，先核实再修正本文件。**
> 更新规则见文末「维护本文件」。未经用户许可，不得修改本文件。

## 1. 项目目标

课程《软件构造实践》（HITSZ，2026 秋）实验项目：把 `Archive/AircraftWar-base1.0`（Java Swing 桌面版飞机大战）重构为 **Android 应用（Java，minSdk 30，建议项目名 `AircraftWar2026`，路径不含中文）**，并随各 Lab 逐步加功能。

**不是照搬，而是"迁移 + 优化"**：Archive 中部分实现粗糙（见 §7），新项目要在迁移时改进，而不是复制缺陷。

| Lab | 主题 | 状态 |
|---|---|---|
| 1 | 任务书（模板 `Lab Material/Lab1/Opening Thesis Proposal Assignment.md`；成稿 `docs/Lab1_开题任务书.md`，规划网络功能：云端排行榜、双人联机对战） | 初稿完成（用户需填姓名/分工后提交） |
| 2 | 迁移：空壳 → 资源 → SurfaceView 游戏循环 → 触屏与逻辑 | 材料已有 |
| 3 | 难度选择页、Intent 跳转、MediaPlayer(BGM)/SoundPool(音效)、Handler、MVC | 材料已有 |
| 4 | 排行榜与本地存储 | 待更新 |
| 5 | 网络对战 | 待更新 |
| 6 | 自选扩展功能 | 待更新 |
| 7 | 结题验收与报告 | 待更新 |

各 Lab 要求以 `Lab Material/slides/实验N.pdf` 为准（读取 PDF 用 `pdf` skill 或 `python3 -c "import pypdf"`；本机无 `pdftotext`）。示例工程在 `Lab Material/Lab2/SurfaceViewDemo.zip`、`Lab3/Actvity_SoundPool.zip`、`Lab3/HandlerDemo(1).rar`（rar 本机无 `unrar`，解压需用户处理或另装）。**新 Lab 材料出现后，先读课件再动手，并按 §10 更新本文件。**

## 2. 仓库布局

```
AircraftWar/                      # 根仓库（git init 于此）
├── AGENTS.md
├── .gitignore                    # 忽略 Archive/、Lab Material/、.DS_Store（改动需用户许可）
├── docs/                         # 自己撰写的文档（会公开）
│   └── Lab1_开题任务书.md        # Lab1 交付物：已完成，姓名/分工待用户填写
├── Archive/                      # 只读参考，见 §6
│   ├── AircraftWar-base1.0/      # Swing 源码（自带独立 .git，remote=JEMUYAN/AircraftWar）
│   └── 2024312380_廖梓承_实验报告.docx   # 个人实验报告，含个人信息
├── Lab Material/                 # 课件与示例（只读）
└── <Android 工程目录>            # 尚未创建；创建后在 §2 补充其路径
```

Android 工程尚未创建。创建时由用户在 Android Studio 新建（Empty Views Activity，Java，minSdk 30），或经用户同意由 agent 手写 Gradle 骨架。

## 3. Archive 源码模块与依赖（`src/edu/hitsz/`）

```
application ──► aircraft ──► basic(AbstractFlyingObject)
   │  │            │  └────► shoot(ShootStrategy)──► bullet ──► basic
   │  │            └───────► prop(PropEventObserver)
   │  └──► prop(PropFactory / PropEventPublisher)
   └─────► dao(ScoreDAO/Score)
```

| 包 | 职责 | 平台相关？ | 迁移策略（Lab2 课件） |
|---|---|---|---|
| `basic` | `AbstractFlyingObject`：坐标、速度、碰撞、`vanish` | 依赖 `ImageManager`(BufferedImage) 取尺寸 | 逻辑复用，图像类型换 `Bitmap` |
| `aircraft` | `AbstractAircraft`→Hero(单例)/Mob/Elite/Superior/Ace/Boss；各 `*EnemyFactory`（工厂方法） | 部分用 `new Thread+sleep` 做冻结 | 逻辑复用；计时改为游戏循环 tick |
| `bullet` | `BaseBullet`→`HeroBullet`/`EnemyBullet` | 同上 | 复用 |
| `shoot` | `ShootStrategy`：Direct/Spread/Fan/Circle/NoShoot（策略模式） | 否 | 直接复用 |
| `prop` | `BaseProp`→Hp/Bomb/Power/SuperPower/Frozen；`PropFactory`；`PropEventPublisher/Observer`（观察者，炸弹/冻结广播） | `PropEffectRunnable` 用线程计时 | 复用结构，计时重做 |
| `dao` | `ScoreDAO`/`ScoreDAOImpl`/`Score`：按难度读写 `scores_*.txt`（CSV：name,score,`yyyy-MM-dd HH:mm`） | 文件路径相对工作目录 | Lab4 改为 App 私有存储（内部文件/SharedPreferences/Room 择一，征得用户同意） |
| `application` | `Main`、`AbstractGame`(JPanel+Timer，模板方法)→Easy/Normal/Hard；`HeroController`(鼠标)；`ImageManager`(ImageIO+相对路径)；`AudioManager`(javax.sound Clip)；`*Frame/Dialog`(Swing UI) | **全部平台相关** | 重写：Activity + SurfaceView + `BitmapFactory`；触屏；MediaPlayer/SoundPool；XML 布局 |

Archive 的 `uml/*.puml` 记录设计模式（Singleton、工厂、策略、观察者、模板方法、DAO），迁移时**保持这些模式关系**，除非 §7 的优化明确要求改动。资源：`src/images/*`（→ `res/drawable`，文件名须小写+数字+下划线）、`src/videos/*.wav`（→ `res/raw`）。

Archive 测试：`test/edu/hitsz/aircraft/HeroAircraftTest.java`（JUnit 5，仅 1 个，用反射改单例字段）。

## 4. 目标 Android 架构约束（来自课件，Lab 推进时可补充）

- 分层：**游戏逻辑层（纯 Java，不 import `android.*`，可 JVM 单测）** / **平台适配层（Canvas 绘制、Bitmap、触摸、音频）** / **UI 层（Activity、XML）**。逻辑实体不得持有 `Bitmap`/`Context`；通过 ID 或渲染层映射取图。
- 入口：Activity + `SurfaceView` 子类（`SurfaceHolder.Callback + Runnable`）。`surfaceCreated` 加载并缓存 Bitmap、启动线程；`surfaceChanged` 取真实宽高；`surfaceDestroyed` 置标志位**并 join 线程**。
- **线程边界**：绘制线程只"算+画"；任何 UI 操作（跳页、Toast、控件）经 `Handler(Looper.getMainLooper())` 或 Activity 公开方法切回主线程（Lab3 游戏结束→排行榜即此场景）。
- 禁止：每帧 `decode` 图片；写死像素坐标（用比例）；静态变量/长生命周期对象持有 Activity（用 `getApplicationContext()`）；退出后线程仍运行。
- 音频：封装独立管理类放 `music` 包——`MyMediaPlayer`（bgm.wav、bgm_boss.wav；Boss BGM 时暂停普通 BGM）、`MySoundPool`（bullet_hit / bomb_explosion / get_supply / game_over）；与 Activity 解耦，生命周期内释放资源；需有音乐开关。
- 页面：难度选择 Activity 通过 Intent extra 传难度；Manifest 中声明所有新 Activity。
- 架构模式：课件推荐 MVC，并指出 Activity 易臃肿——保持 Activity 薄，逻辑下沉。

## 5. 编码规范

沿用 Archive 风格并保持一致：
- Java；包名根 `edu.hitsz`（Android 工程 applicationId 由用户在创建时确定，agent 不擅改）。
- 类名 UpperCamelCase，常量 `UPPER_SNAKE`，中文 Javadoc/注释，公开类与关键方法写 `/** ... */`。
- 抽象类+接口隔离，用工厂/策略/观察者/模板方法/单例，不要用 `instanceof` 链替代多态。
- 资源命名：`a-z0-9_`，字母开头。
- 日志用 `android.util.Log`（`TAG` 常量），不用 `System.out`/`printStackTrace`。
- 提交信息写**原因**（课件要求：每解决一个坑写一句原因；每完成迁移一步提交一次）。AI 辅助需在课程"AI 使用日志"记录——**提醒用户**，agent 不代写假记录。

## 6. 受保护的数据、文件与操作

**绝对不要（除非用户在当前对话明确许可）：**
1. 修改、删除、移动 `Archive/` 下任何文件（含其自带 `.git`、`out/`、`.idea/`、分数文件）；不要在其中执行会改变工作区的命令（`git checkout/reset/clean/commit`、构建输出到该目录）。需要运行旧版时，复制到临时目录。
2. 修改 `Lab Material/` 内容（课件、zip/rar）。解压到 `/tmp` 或工作区外查看。
3. 把 `Archive/2024312380_*实验报告.docx`、`scores_*.txt`（含玩家名/时间）、`Lab Material/slides`（课程版权）、`.idea/`、`local.properties`、签名密钥/keystore、`*.jks`、令牌或凭据**提交到公开 GitHub 仓库**。本仓库为 **public**。
4. `git push --force`、改写已推送历史、删除远程仓库/分支、`rm -rf`、`git clean -fdx`、`git reset --hard`。
5. 擅自创建/修改根 `.gitignore`、更改 git 全局配置、安装/升级 SDK 或 Android Studio、同意 SDK 许可。
6. 擅自修改本文件。

**提交前检查**：`git status` 确认无上述敏感文件；Android 工程需有合适的 `.gitignore`（`build/`、`.gradle/`、`.idea/`、`local.properties`）——创建时征求用户意见。

**Git 注意**：远程为 `https://github.com/JEMUYAN/AircraftWar-Android`（public，分支 `main`）。`Archive/`、`Lab Material/`、`.DS_Store` 已被根 `.gitignore` 忽略，不会入库；`Archive/AircraftWar-base1.0` 另有自己的 `.git`（remote 为 `JEMUYAN/AircraftWar`），与根仓库互不相干，不要动它。提交时仍优先 `git add` 明确路径，并检查 `git status`。
- 公开文档（如 `docs/`）不得写学号等个人信息。
- 本机 SSH 到 GitHub 不通；推送若失败，改用 HTTPS 或经 GitHub MCP，不要擅自改 git 全局配置。

## 7. Archive 已知粗糙点 → 新项目优化清单

迁移时优先改进（逐项实施前简述方案；与用户确认较大改动）：

| # | 问题（证据） | 优化方向 |
|---|---|---|
| 1 | 冻结/道具效果/敌机状态用 `new Thread(sleep)`（`EliteEnemy/AceEnemy/SuperiorEnemy/EnemyBullet/PropEffectRunnable/HeroAircraft`）：线程泄漏、无法随暂停/退出停止、与帧循环不同步、线程安全隐患 | 统一为**基于游戏 tick/时间戳的计时器**（在游戏循环内倒计时），无额外线程 |
| 2 | `AbstractGame` 约 450 行，混合状态、生成、碰撞、渲染、音频、Swing UI（上帝类） | 拆分：World/Engine（逻辑）、Spawner、CollisionSystem、Renderer、GameController；`AbstractGame` 的模板方法（难度参数）保留 |
| 3 | `HeroAircraft` 单例带线程，且单测靠反射重置 | 去全局可变单例或提供 `reset()`；利于测试与重开局 |
| 4 | 逻辑类依赖 `ImageManager`（BufferedImage、`src/images/...` 相对路径） | 逻辑层不依赖图像；尺寸由渲染层资源表提供 |
| 5 | `AudioManager` 绑定 javax.sound、硬编码路径；Boss 用 `hasBoss` 等松散布尔状态 | 换 `music` 包封装；状态用枚举/小状态机 |
| 6 | `ScoreDAOImpl` 相对路径写文件、`split(",")` 无容错（用户名含逗号或坏行即崩）、吞异常 `printStackTrace`、文件名难度字符串分支 | 私有目录存储、健壮解析、难度用 `enum`、错误走 `Log`/回调 |
| 7 | 难度用字符串（"easy"/"medium"...）、魔法数字散落（60/100/40/20…） | `enum Difficulty` + 配置对象/常量 |
| 8 | 测试覆盖极少（仅 `HeroAircraftTest`） | 逻辑层纯 Java，补 JUnit 单测（碰撞、射击策略、道具、计分、DAO 解析） |
| 9 | `Main.WINDOW_WIDTH/HEIGHT` 固定 512×768 | 按 `surfaceChanged` 实际尺寸比例换算 |

保持行为等价的前提：优化不得改变游戏规则（分数、难度曲线、道具效果）——除非用户同意；对比基线为 Archive 行为。

## 8. 环境与工具（2026-11-24 在本机核实）

| 项 | 状态 |
|---|---|
| OS / shell | macOS (darwin 25.6)，zsh |
| JDK | `/opt/homebrew/opt/openjdk@21/bin/java`（21.0.10）；Android Studio 自带 JBR：`/Applications/Android Studio.app/Contents/jbr/Contents/Home` |
| Android Studio | `/Applications/Android Studio.app` 已安装 |
| Android SDK | `~/Library/Android/sdk`：`build-tools/36.0.0`、`platforms/`、`platform-tools/`、`emulator/`、`system-images/`、`licenses/`、`sources/` |
| adb / emulator | **不在 PATH**；用绝对路径：`~/Library/Android/sdk/platform-tools/adb`、`~/Library/Android/sdk/emulator/emulator` |
| AVD | `Medium_Phone_API_37.0` |
| gradle | 无全局安装——**使用工程内 `./gradlew`**（工程创建后） |
| git | `/opt/homebrew/bin/git`；user `sagm` / `hitszllll06@outlook.com` |
| gh CLI | **未安装**；GitHub 通过 MCP（`plugin-github-github`，账号 `JEMUYAN`）操作 |
| GitHub SSH | 本机 `ssh -T git@github.com` 当前 **Permission denied (publickey)**；推送需 HTTPS+凭据或让用户修复 SSH |
| Python | pyenv `python3`，`pypdf` 可用；无 `pdftotext`、`unrar` |

建议会话内环境变量（不写入全局）：
```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"   # 与 AGP 兼容性更稳；也可用 openjdk@21
```

### 常用命令（Android 工程创建后；在其根目录执行）
```bash
./gradlew assembleDebug                  # 构建 debug APK
./gradlew testDebugUnitTest              # JVM 单元测试（逻辑层）
./gradlew lintDebug                      # Lint
./gradlew connectedDebugAndroidTest      # 需要已连接设备/模拟器
emulator -list-avds
emulator -avd Medium_Phone_API_37.0 -no-snapshot &      # 启动模拟器（后台）
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n <applicationId>/<包名>.MainActivity
adb logcat -v time | grep -E "FATAL EXCEPTION|<TAG>"     # 崩溃先找 FATAL EXCEPTION 与 Caused by
adb shell pidof <applicationId>
adb exec-out screencap -p > /tmp/screen.png              # 截图验证 UI
```
> 以上命令在 Android 工程创建并成功构建后才算"已验证"；首次验证后请把实际可用版本回写本节。

### 运行旧版 Java 版（仅作行为基线，勿在 Archive 内生成产物）
Archive 的 IDE 工程是 IntelliJ（`.iml`，JDK11，JUnit 5.14，`lib/commons-lang3-3.8.1.jar`）。资源按**工作目录相对路径**加载（`src/images/...`），需在 Archive 内部目录为工作目录运行。若要运行：复制到 `/tmp/aw-base` 后编译：
```bash
cp -R Archive/AircraftWar-base1.0 /tmp/aw-base && cd /tmp/aw-base
mkdir -p /tmp/aw-out && javac -encoding UTF-8 -cp "lib/*" -d /tmp/aw-out $(find src -name '*.java')
java -cp "/tmp/aw-out:lib/*" edu.hitsz.application.Main     # 需要图形界面
```
（未在本机实测；成功/失败后请更新。）

## 9. 分工：agent 能做 vs 需用户在 Android Studio 操作

**agent 可独立完成：** 读/改 Java、XML、Gradle 脚本、Manifest；命令行构建/单测/lint（`./gradlew`）；用 `adb`/`emulator` 启动模拟器、安装、启动、抓 logcat、截图；git 提交；用 GitHub MCP 操作仓库；阅读课件/文档并整理。

**需要用户（Android Studio GUI 或本机授权）：**
- 首次创建工程（New Project → Empty Views Activity，Java，minSdk 30，项目名/路径）、Gradle Sync、SDK/AGP/Gradle 升级与许可确认、下载 system image。
- 交互式调试：断点调试、Attach debugger、Profiler（内存/CPU）、Layout Inspector、Logcat 过滤与可视化分析。
- 真机调试授权（USB 调试、RSA 指纹确认）；模拟器图形窗口中的手动触控体验（agent 只能用 `adb shell input` 模拟粗略触摸）。
- 可视化布局编辑（XML 可由 agent 写，但效果需用户在 Preview 确认）。
- 签名 keystore 生成与发布构建（Build → Generate Signed Bundle/APK），涉及密钥——由用户保管。
- 提交到课程平台（课件要求提交到 Gitee）、填写 AI 使用日志、结题报告截图。
- 修复 GitHub SSH/令牌凭据。
- 解压 `.rar`（本机无 unrar，除非用户同意安装）。

遇到 agent 无法验证的 UI 行为，明确告知"需在 Android Studio/模拟器中人工确认"，不要声称已验证。

## 10. 维护本文件

- 仅在**用户许可**下更新；其他 coding agent 完成任务后若有以下变化，应**提出修改建议**并等用户确认：新 Lab 要求、Android 工程路径/applicationId/依赖、命令实测结果、新的架构决策、§7 清单项的完成状态。
- 保持精简：不要复制课件/README 大段内容，引用路径即可；每节只记录"agent 做事需要知道的"。
- 更新方式：改对应小节并同步 §1 状态表；标注日期（如 `核实于 YYYY-MM-DD`）；命令必须是实测可用的才可标"已验证"。
- 新增 Lab 时的模板：在 §1 表里更新状态；在对应章节补"本 Lab 引入的约束/模块/命令"；若 Lab 引入敏感数据（令牌、服务器地址、keystore），同步 §6。
- 变更记录（追加在下方）：
  - 2026-11-24：初版（基于 Lab1–3 材料与 Archive 分析）。
  - 2026-11-24：新增 `.gitignore`、`docs/Lab1_开题任务书.md`；创建 GitHub 远程仓库 AircraftWar-Android；更新 §1、§2、§6。
