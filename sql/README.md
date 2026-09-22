# StudyHub 数据库脚本

在 MySQL 8 客户端中执行脚本，不需要 Docker。

## 全新数据库

1. 执行 `sql/init.sql`：创建 `studyhub` 数据库和表。
2. 执行一次 `sql/seed.sql`：插入开发演示用户、笔记与标签。演示用户 `test03`（管理员）和 `user_b`（普通用户）的初始密码均为 `password`，仅用于本地演示。

如果使用 MySQL 命令行，可以先进入项目根目录并登录 MySQL，然后在 MySQL 提示符执行：

```sql
SOURCE sql/init.sql;
SOURCE sql/seed.sql;
```

也可以用 MySQL Workbench 等客户端分别打开文件执行。`seed.sql` 中的笔记没有去重约束，重复运行会插入重复演示笔记。

## 已有旧数据库

先备份数据库，再检查表结构是否需要 `sql/migrate_v2.sql` 中的变更；需要时只执行迁移脚本，不要把 `init.sql` / `seed.sql` 当作升级脚本反复执行。迁移不会替你创建演示账号；如需管理员演示账号，请先核对现有用户与数据，再决定是否执行种子脚本。
