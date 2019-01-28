package com.github.binarywang.demo.wx.mp.business;

import com.github.binarywang.demo.wx.mp.redis.RedisService;
import com.github.binarywang.demo.wx.mp.utils.JsonUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;

@Service
@Slf4j
public class BusinessService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private ImageService imageService;

    private static long TestTime = 60*30l;

    //根据收到的内容分析组装回复
    public String analyzeTest(WxMpUser userWxInfo, String content, WxMpService weixinService) {
        if(StringUtils.isBlank(content)) return null;
        String statusKey = userWxInfo.getOpenId()+":status";
        Integer status = redisService.get(statusKey);
        if(status == null && !StringUtils.equals("测试",content))
            return null;
        if(StringUtils.equals("测试",content))
            status= StatusEnum.非答题状态.code;
        String result = null;
        String answerKey = userWxInfo.getOpenId()+":answer";
        String answerStr = redisService.get(answerKey);
        Answer answer = null;
        if(StringUtils.isNotBlank(answerStr))
            answer = new Gson().fromJson(answerStr,Answer.class);
        switch(StatusEnum.getByCode(status)){
            case 非答题状态:
                result = "猪年马上到，你脑海里闪过的第一个念头是__？\n" +
                    "\n" +
                    "1、时间这么快？！\n" +
                    "2、狗年过的&*# \n" +
                    "3、猪年是不是不让吃猪肉了？";
                answer = new Answer(new ArrayList<>());
                redisService.put(statusKey,StatusEnum.答第一题.code,TestTime);
                redisService.put(answerKey, JsonUtils.toJson(answer),TestTime);
                break;
            case 答第一题:
                if(StringUtils.equalsAnyIgnoreCase(content,"1","2","3")){
                    result = "如果你可以获得一种超能力，请在三秒内决定__？\n" +
                        "\n" +
                        "1、隐身\n" +
                        "2、飞行\n" +
                        "3、过目不忘";
                    List<Integer> options = answer.getOptions();
                    if(CollectionUtils.isEmpty(options)) options = new ArrayList<>();
                    if(options.size() > 0) options = new ArrayList<>();
                    options.add(Integer.valueOf(content));
                    answer.setOptions(options);
                    redisService.put(statusKey,StatusEnum.答第二题.code,TestTime);
                    redisService.put(answerKey,JsonUtils.toJson(answer),TestTime);
                }else
                    result = "请回复 1、2、3中的一项";
                break;
            case 答第二题:
                if(StringUtils.equalsAnyIgnoreCase(content,"1","2","3")){
                    result = "猪年里，你最希望听到的一句话是__？\n" +
                        "\n" +
                        "1、你值得拥有\n" +
                        "2、你中大奖了\n" +
                        "3、我养你啊！";
                    List<Integer> options = answer.getOptions();
                    options.add(Integer.valueOf(content));
                    answer.setOptions(options);
                    redisService.put(statusKey,StatusEnum.答第三题.code,TestTime);
                    redisService.put(answerKey,JsonUtils.toJson(answer),TestTime);
                }else
                    result = "请回复 1、2、3中的一项";
                break;
            case 答第三题:
                if(StringUtils.equalsAnyIgnoreCase(content,"1","2","3")){
                    result = "对于猪来说，新年最大的愿望是__？\n" +
                        "\n" +
                        "1、当上主角我最闪亮\n" +
                        "2、给我一杯忘情水\n" +
                        "3、我还想再活五百年";
                    List<Integer> options = answer.getOptions();
                    options.add(Integer.valueOf(content));
                    answer.setOptions(options);
                    redisService.put(statusKey,StatusEnum.答第四题.code,TestTime);
                    redisService.put(answerKey,JsonUtils.toJson(answer),TestTime);
                }else
                    result = "请回复 1、2、3中的一项";
                break;
            case 答第四题:
                if(StringUtils.equalsAnyIgnoreCase(content,"1","2","3")){
                    result = "正在分析中…(ง •̀_•́)ง，客官稍等片刻，自动输出结果~";
                    List<Integer> options = answer.getOptions();
                    options.add(Integer.valueOf(content));
                    answer.setOptions(options);
                    imageService.generateImageAsync(userWxInfo,weixinService,options);
                    redisService.put(statusKey,StatusEnum.图片绘制.code,TestTime);
                    redisService.put(answerKey,JsonUtils.toJson(answer),TestTime);
                }else
                    result = "请回复 1、2、3中的一项";
                break;
            default: break;
        }
        return result;
    }

    public void clearUserTest(WxMpUser userWxInfo){
        String statusKey = userWxInfo.getOpenId()+":status";
        redisService.delete(statusKey);
        String answerKey = userWxInfo.getOpenId()+":answer";
        redisService.delete(answerKey);
    }
}
