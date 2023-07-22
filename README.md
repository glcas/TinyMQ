# MQ-Toy

A reproduction of [houbb/mq](https://github.com/houbb/mq).

## Enhancement

Highlights several enhancements compared with [houbb/mq](https://github.com/houbb/mq) here:

- fewer external dependencies, realize more built-in utilities
- for robustness, json parser/wrapper change from [fastjson](https://github.com/alibaba/fastjson)
  to [Jackson](https://github.com/FasterXML/jackson)
- generate [Snowflake ID](https://en.wikipedia.org/wiki/Snowflake_ID), instead of original UUID

Also fixed some bugs.