# MQ-Toy

A tiny version of [RocketMQ](https://rocketmq.apache.org/), inspired
from [houbb/mq](https://github.com/houbb/mq).

## Enhancement

Highlights several enhancements compared with [houbb/mq](https://github.com/houbb/mq) here:

- fewer external dependencies, realize more built-in utilities
- for robustness, json parser/wrapper change from [fastjson](https://github.com/alibaba/fastjson)
  to [Jackson](https://github.com/FasterXML/jackson)
- generate [Snowflake ID](https://en.wikipedia.org/wiki/Snowflake_ID), instead of original UUID
- default message push relies on long polling, while original push only used for urgent message
- performance improvement and more reasonable implementation of details
- cleaner code, naming and structure, with more notes

Also fixed some bugs.

## 相关学习参考

### RocketMQ

- [RocketMQ 部分核心概念](https://www.cnblogs.com/qdhxhz/p/11094624.html)
- [Topic、Tag 和 GroupName 概念介绍](https://blog.csdn.net/agonie201218/article/details/118561943)
- [消息“伪推送”模式解析](https://mp.weixin.qq.com/s/opqRf8UjI9rRW_4befWrbA)（消费者长轮询，相当于掌握是否接受推送的主动权）

### Netty网络编程

- [Netty 入门知识点整理](https://zhuanlan.zhihu.com/p/94584675)
- [Netty 核心原理剖析与 RPC 实践](https://learn.lianglianglee.com/%E4%B8%93%E6%A0%8F/Netty%20%E6%A0%B8%E5%BF%83%E5%8E%9F%E7%90%86%E5%89%96%E6%9E%90%E4%B8%8E%20RPC%20%E5%AE%9E%E8%B7%B5-%E5%AE%8C/)
- [ChannelPipeline 相关概念（包括 ChannelHandler 及 ChannelHandlerContext）](https://www.cnblogs.com/qdhxhz/p/10234908.html)
- [启动类 ServerBootstrap 和 Bootstrap](https://www.jianshu.com/p/c912dfe0dceb)
- [bossGroup(parentGroup) 和 workerGroup(childGroup) 的作用及关系](https://blog.csdn.net/weixin_44976692/article/details/118414027)
- [数据发送方法 WriteAndFlush() 底层实现](https://www.cnblogs.com/ZhuChangwu/p/11228433.html)
- [channelHandlerContext.close() 与 channel.close()](https://emacsist.github.io/2018/04/27/%E7%BF%BB%E8%AF%91netty4%E4%B8%AD-ctx.close-%E4%B8%8E-ctx.channel.close-%E7%9A%84%E5%8C%BA%E5%88%AB/)
- [eventLoopGroup.shutdownGracefully()](https://blog.csdn.net/m0_45406092/article/details/104634198)

### Java编程最佳实践

- [Objects.requireNonNull() 方法：Fail-fast 设计思想](https://blog.csdn.net/qq_42671519/article/details/121530411)
- Jackson 要求反序列化的类提供无参构造函数
- [Lambda 表达式](https://objcoding.com/2019/03/04/lambda/)
- [方法引用](https://www.cnblogs.com/xiaoxi/p/7099667.html)
- [线程池原理](https://mp.weixin.qq.com/s/lVem8mGANea8aUYF3XCO7Q)
- [JDK 9+ 字符串拼接探究](https://github.com/yesh0/stringbuilder-test)
  （注：循环字符串拼接仍以提前创建 StringBuilder 复用为佳，因为循环 "+" 拼接字符串会重复创建 StringBuilder）
- [优雅关闭](https://blog.csdn.net/m0_45406092/article/details/104578672)
- [参考命名规范](https://pdai.tech/md/develop/ut/dev-qt-code-style-2.html)