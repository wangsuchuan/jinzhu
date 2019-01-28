package com.github.binarywang.demo.wx.mp.handler;

import com.github.binarywang.demo.wx.mp.builder.TextBuilder;
import com.github.binarywang.demo.wx.mp.business.BusinessService;
import com.github.binarywang.demo.wx.mp.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
@Component
@Slf4j
public class MsgHandler extends AbstractHandler {
    @Autowired
    private BusinessService businessService;

    private static final String ImageContent = "除夕夜红包抽奖活动将在2月4日晚8点举行，" +
            "把转发测试结果到朋友圈的截图（对所有人可见）在对话框里发出即视为报名成功。" +
        "我们将在人数最多队伍中挑选出99名小锦鲤，瓜分最高2019元红包！";

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {
        // 获取微信用户基本信息
        WxMpUser userWxInfo = null;
        try {
            userWxInfo = weixinService.getUserService()
                .userInfo(wxMessage.getFromUser(), null);
            if (userWxInfo == null) return null;
        } catch (WxErrorException e) {
            if (e.getError().getErrorCode() == 48001) {
                this.logger.info("该公众号没有获取用户信息权限！");
            }
        }
        if(StringUtils.equalsIgnoreCase(wxMessage.getMsgType(), WxConsts.XmlMsgType.IMAGE)){
            //如果用户发的是图片，返回固定文案
            return new TextBuilder().build(ImageContent, wxMessage, weixinService);
        }
        String result = businessService.analyzeTest(userWxInfo,wxMessage.getContent(),weixinService);
        if(StringUtils.isBlank(result)) return null;
        return new TextBuilder().build(result, wxMessage, weixinService);
    }

}
