package cn.waynechu.springcloud.test.spi;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhuwei
 * @date 2019/3/26 15:05
 */
@Slf4j
public class Bumblebee implements Robot {

    @Override
    public void sayHello() {
        log.info("Hello, I am Bumblebee.");
    }
}
