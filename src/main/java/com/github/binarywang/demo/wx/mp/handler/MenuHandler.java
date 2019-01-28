package com.github.binarywang.demo.wx.mp.handler;

import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.MenuButtonType;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
@Component
public class MenuHandler extends AbstractHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {
        String msg = String.format("type:%s, event:%s, key:%s",
            wxMessage.getMsgType(), wxMessage.getEvent(),
            wxMessage.getEventKey());
        if (MenuButtonType.VIEW.equals(wxMessage.getEvent())) {
            return null;
        }
        if (StringUtils.equalsIgnoreCase(MenuButtonType.CLICK,(wxMessage.getEvent()))) {
            if(StringUtils.equals("ceshi123",wxMessage.getEventKey())){
                String content = "hi~，2019年马上就要开始了，来测测你的猪年运势。\n" +
                    "\n" +
                    "回复”测试“，开始测试，并赢取我们的新年锦鲤红包~";
                return WxMpXmlOutMessage.TEXT().content(content)
                    .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
                    .build();
            }
        }

        return WxMpXmlOutMessage.TEXT().content(msg)
            .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
            .build();
    }

}
