# Apache Flink® Troubleshooting Training

## 介绍

这个repo存放了"Apache Flink Troubleshooting"培训相关的资料。  

### 前提

你需要如下工具进行开发：

* git
* JDK 8
* maven
* a Java IDE (Intellij IDEA/Eclipse)

### 培训准备

```bash
git clone git@github.com:flink-china/flink-training-troubleshooting.git
cd flink-training-troubleshooting
mvn clean package
```

### 内容组成

可以在本地址执行 `TroubledStreamingJob` 或者再 Ververica Platform 上运行。

### 本地执行

找到测试文件中的 `TroubledStreamingJobRunner`， 调用`TroubledStreamingJob`的main方法。一旦开始运行后，你可以通过访问 http://localhost:8081 查看Flink UI

### Flink 作业介绍

这个Flink作业从8个 partitions的FakeKafka读取measurement数据. 数据结果是基于一秒的窗口输出，整体的流程如下：

```
+-------------------+     +-----------------------+     +-----------------+     +----------------------+     +--------------------+
|                   |     |                       |     |                 |     |                      |     |                    |
| Fake Kafka Source | --> | Watermarks/Timestamps | --> | Deserialization | --> | Windowed Aggregation | --> | Sink: NormalOutput |
|                   |     |                       |     |                 |     |                      |     |                    |
+-------------------+     +-----------------------+     +-----------------+     +----------------------+     +--------------------+
                                                                                            \
                                                                                             \               +--------------------+
                                                                                              \              |                    |
                                                                                               +-----------> | Sink: LateDataSink |    
                                                                                                             |                    |
                                                                                                             +--------------------+
```

在本地模式, sinks 会输出到 `stdout` (NormalOutput) 以及 `stderr` (LateDataSink)。
而在分布式模式，会使用 `DiscardingSink` 作为相关的sink。

----

*Apache Flink, Flink®, Apache®, the squirrel logo, and the Apache feather logo are either registered trademarks or trademarks of The Apache Software Foundation.*
