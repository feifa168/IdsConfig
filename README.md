## 简介
> IdsConfig工程用于配置ids程序的部署，支持linux，主要实现以下功能。
* 拷贝文件，用于安装程序，主要是把snort程序以及依赖的库拷贝到指定的目录。
* 修改文件
    * 修改rsyslog.conf文件用于指定syslog接收服务器。
    * 修改ld.so.conf用于追加动态库查询路径，因为Linux下不从当前路径查找动态库，所以需要把追加指定的搜索路径。 
* 执行脚本，可扩展，通过创建java进程实现。
    * 启动服务，修改rsyslog.conf文件后要重新启动rsyslog服务。
    * 执行ldconfig，用于重新加载动态库搜索路径。
    * 创建目录，用于存放snort的告警日志。

## 依赖
* dom4j+jaxen，用于解析xml
* commons-io，用于拷贝文件

## 技术要点
* 文件NIO，以下几种方式实现，实现了文件拷贝，目录拷贝直接使用commonio库。
    * 使用FileChannel实现
    * 使用AsynchronousFileChannel拷贝文件，耗时反而不如FileChannel，有时间再优化。
        * 使用Future获取结果
        * 设置回调函数获取结果  
    * 使用commonio库拷贝文件和目录      
* 创建进程，参数cmds有两种传入方法
    * Runtime.getRuntime().exec(cmds)方式
    * new ProcessBuilder(cmds).start()方式