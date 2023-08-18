# MQ-Toy

A reproduction of [houbb/mq](https://github.com/houbb/mq), at the same time a tiny version
of [RocketMQ](https://rocketmq.apache.org/).

## Enhancement

Highlights several enhancements compared with [houbb/mq](https://github.com/houbb/mq) here:

- fewer external dependencies, realize more built-in utilities
- for robustness, json parser/wrapper change from [fastjson](https://github.com/alibaba/fastjson)
  to [Jackson](https://github.com/FasterXML/jackson)
- generate [Snowflake ID](https://en.wikipedia.org/wiki/Snowflake_ID), instead of original UUID
- performance improvement and more reasonable implementation of some details
- cleaner code, with more notes

Also fixed some bugs.

## 相关学习参考

### RocketMQ

- [RocketMQ部分核心概念](https://www.cnblogs.com/qdhxhz/p/11094624.html)
- [Topic、Tag和GroupName概念介绍](https://blog.csdn.net/agonie201218/article/details/118561943)
- [消费者消息拉取模式解析](https://cloud.tencent.com/developer/article/1696821)

### Netty网络编程

- [Netty入门知识点整理](https://zhuanlan.zhihu.com/p/94584675)
- [Netty 核心原理剖析与 RPC 实践](https://learn.lianglianglee.com/%E4%B8%93%E6%A0%8F/Netty%20%E6%A0%B8%E5%BF%83%E5%8E%9F%E7%90%86%E5%89%96%E6%9E%90%E4%B8%8E%20RPC%20%E5%AE%9E%E8%B7%B5-%E5%AE%8C/)
- [ChannelPipeline相关概念（包括ChannelHandler及ChannelHandlerContext）](https://www.cnblogs.com/qdhxhz/p/10234908.html)
- [启动类ServerBootstrap和Bootstrap](https://www.jianshu.com/p/c912dfe0dceb)
- [bossGroup(parentGroup)和workerGroup(childGroup)的作用及关系](https://blog.csdn.net/weixin_44976692/article/details/118414027)
- [数据发送方法WriteAndFlush()底层实现](https://www.cnblogs.com/ZhuChangwu/p/11228433.html)

### Java编程最佳实践

- [Objects.requireNonNull()方法的意义](https://blog.csdn.net/qq_42671519/article/details/121530411)
- [Lambda表达式](https://objcoding.com/2019/03/04/lambda/)
- Jackson要求反序列化的类提供无参构造函数