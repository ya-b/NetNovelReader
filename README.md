# 小说阅读器

支持 WebUI / TUI 双模式的网络小说阅读器。

## 环境变量（`.env`）

```
UI_TYPE=webui            # tui 或 webui
WEBUI_TOKEN=              # 可选
WEBUI_PORT=56789          # 可选，默认 56789
CHROME_DRIVER=none       # 当前 none / chrome, 选择chrome，会使用chrome浏览器获取网页内容
CHROME_VERSION=147
CHROME_DATA_DIR=D:\chrome-user-data   # chrome 数据目录，可以为空。不要使用相对路径
DB_URI=postgres://user:password@host:5432/dbname?search_path=my_novel&sslmode=disable # mysql://user:pass@/dbname?parseTime=True sqlite:///D:/repo/novel.db
```

## 运行

```bash
go mod tidy
go run ./cmd/reader

# 或构建后运行
go build -o reader.exe ./cmd/reader
./reader.exe
```

## WebUI 监听 `WEBUI_PORT`，默认 `:56789`，访问 <http://localhost:56789/>。


![webui书架](./screenshot/webui书架.png)

![webui阅读](./screenshot/webui阅读.png)

![webui书源](./screenshot/webui书源.png)


## TUI 使用说明

TUI 模式下，章节标题会显示在内容顶部并随内容滚动。

### 快捷键

| 按键 | 功能 |
|------|------|
| `Ctrl+B` `b` | 打开书架 |
| `Ctrl+B` `n` | 打开下一章 |
| `Ctrl+B` `q` | 退出程序 |
| `/` | 打开底部输入栏 |
| `↑` `↓` `PgUp` `PgDn` `Home` `End` | 滚动内容 |
| `Enter` | 执行输入栏命令 |
| `Backspace` | 删除字符（清空后关闭输入栏） |

### 输入栏命令

| 命令 | 功能 |
|------|------|
| `/books` | 打开书架 |
| `/next` | 打开下一章 |
| `/refresh` | 重新请求当前章节 URL |
| `/open <url>` | 打开指定 URL |
| `/exit` `/quit` | 退出程序 |

### 鼠标操作

- **滚轮**：滚动内容
- **左键点击**：点击书架中的书籍行或内容末尾的"下一章"链接可直接打开

![tui书架](./screenshot/tui书架.png)

阅读界面会伪装成日志

![tui阅读](./screenshot/tui阅读.png)
