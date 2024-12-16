package org.itxuexi.controller;

import jakarta.annotation.Resource;
import org.itxuexi.tasks.SMSTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("a")
public class HelloController {

    @Resource
    private SMSTask smsTask;

    @GetMapping("hello")
    public Object hello() {
        return "Hello world~";
    }

    @GetMapping("smsTask")
    public Object smsTask() throws Exception {
        smsTask.sendSMSInTask("15877709777", "1223");
        return "Send SMS In Task done!";
    }
}
