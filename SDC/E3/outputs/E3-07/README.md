# E3-07 证据索引

本题修复“无效地址更新清空已有默认地址”。目标方法为 `UmsMemberReceiveAddressServiceImpl.update`。

## 核心结论

- 改前：6 项测试中 2 项业务失败，Errors 0，Skipped 0。
- 修复：在任何写入之前确认目标地址属于当前会员且可更新。
- 改后：6 项测试全部通过，Failures、Errors、Skipped 均为 0。
- 覆盖率：目标方法行覆盖 17/17，分支覆盖 6/6，方法内无未覆盖项。
- 事务声明、会员过滤和有效默认地址切换顺序均保留。

## 文件

- [一页说明](E3-07-一页说明.md)
- [可直接粘贴文本](E3-07-可直接粘贴.txt)
- [Word 说明书](E3-07规格先行与AI结对编程实验说明.docx)
- [AI 使用记录](AI使用记录.md)
- [本人录屏讲解稿](E3-07-录屏讲解稿.md)
- [按打开顺序录屏稿](E3-07录屏讲解稿-按打开顺序.txt)
- [改前失败日志](evidence/baseline-test.log)
- [改后通过日志](evidence/fixed-test.log)
- [JaCoCo 运行日志](evidence/instrumented-test.log)
- [覆盖率摘要](evidence/coverage.txt)
- [覆盖率 XML](evidence/coverage.xml)
- [覆盖率 CSV](evidence/coverage.csv)

## 代码

- [测试代码](../../../../mall-portal/src/test/java/com/macro/mall/portal/sdc/E307AddressDefaultUpdateTest.java)
- [修复后的服务实现](../../../../mall-portal/src/main/java/com/macro/mall/portal/service/impl/UmsMemberReceiveAddressServiceImpl.java)

## 复现命令

```sh
mvn -B -pl mall-portal -am -DskipTests=false \
  -Dtest=E307AddressDefaultUpdateTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

测试使用真实服务与 Mapper 替身，只证明服务行为和持久化边界交互。真实 MySQL 提交/回滚、完整 HTTP 认证链和并发行为未验证。
