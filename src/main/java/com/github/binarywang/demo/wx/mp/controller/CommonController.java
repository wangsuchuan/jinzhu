package com.github.binarywang.demo.wx.mp.controller;

import com.github.binarywang.demo.wx.mp.business.BusinessService;
import com.github.binarywang.demo.wx.mp.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author: Godykc
 * @Description:
 * @Date Created at 22:04 2017/12/3
 */
@RestController
@Slf4j
public class CommonController {
    @Autowired
    private BusinessService businessService;

    @GetMapping("/ping")
    public String ping() {
        String ip= null; //获取本机ip
        String hostName= null; //获取本机计算机名称
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress().toString();
            hostName = addr.getHostName().toString();
        } catch (UnknownHostException e) {
        }
        return "ip:"+ip+",hostName:"+hostName+", 服务正常。。。";
    }
}
