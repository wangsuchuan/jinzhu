package com.github.binarywang.demo.wx.mp.business;

import com.github.binarywang.demo.wx.mp.image.SvgRenderWrapper;
import com.github.binarywang.demo.wx.mp.redis.RedisService;
import com.github.binarywang.demo.wx.mp.utils.JsonUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class ImageService {
    @Autowired
    private RedisService redisService;

    private static long TestTime = 60*30l;

    @Async
    public void generateImageAsync(WxMpUser userWxInfo, WxMpService wxMpService,List<Integer> options){
        String mediaId = generateImage(userWxInfo,options,wxMpService);
        if(StringUtils.isBlank(mediaId)){
            log.error("------generateImageAsync fail: mediaId is blank");
            return;
        }
        sendResult(userWxInfo.getOpenId(),mediaId,wxMpService);
    }

    private void sendResult(String openId, String mediaId, WxMpService wxMpService) {
        String typeKey = openId+":maxType";
        String type = redisService.get(typeKey);
        if(StringUtils.isBlank(type))
            log.error("type is blank");
        String content = "恭喜你在“"+type+"”上得分最高，" +
            "已被分入“"+type+"”队，" +
            "人数最多队伍的小锦鲤们将可参与2019除夕抢红包盛宴！" +
            "分享你的测试结果到朋友圈，" +
            "让更多朋友参与，" +
            "壮大你的队伍！\n" +
            "\n" +
            "把转发测试结果到朋友圈的截图" +
            "（对所有人可见）" +
            "发送至公众号即可报名抢红包活动，" +
            "每日我们都会通过公众号及时更新活动动态，" +
            "敬请关注！";
        try {
            WxMpKefuMessage message1 = WxMpKefuMessage
                .TEXT()
                .toUser(openId)
                .content(content)
                .build();
            wxMpService.getKefuService().sendKefuMessage(message1);
        } catch (WxErrorException e) {
            log.error("WxErrorException:{}",JsonUtils.toJson(e.getError()));
        }
        try {
            WxMpKefuMessage message2 = WxMpKefuMessage
                .IMAGE()
                .toUser(openId)
                .mediaId(mediaId)
                .build();
            wxMpService.getKefuService().sendKefuMessage(message2);
        } catch (WxErrorException e) {
            log.error("WxErrorException:{}",JsonUtils.toJson(e.getError()));
        }
        String statusKey = openId+":status";
        redisService.delete(statusKey);
        String answerKey = openId+":answer";
        redisService.delete(answerKey);
        redisService.delete(typeKey);
    }

    //测试完毕，生成对应的结果图，上传后返回mediaId
    private String generateImage(WxMpUser userWxInfo, List<Integer> options, WxMpService wxMpService) {
        if(userWxInfo == null){
            log.error("userWxInfo is null");
            return null;
        }
        if(CollectionUtils.isEmpty(options)){
            log.error("options isEmpty");
            return null;
        }
        if(options.size() != 4){
            log.error("options.size() != 4");
            return null;
        }
        int C=0,S=0,T=0,J=0;

        int selection1 = options.get(0);
        if(selection1 == 1) C+=1;
        else if(selection1 == 2) S+=1;
        else if(selection1 == 3) J+=2;

        int selection2 = options.get(1);
        if(selection2 == 1) J+=3;
        else if(selection2 == 2) S+=3;
        else if(selection2 == 3) T+=2;

        int selection3 = options.get(2);
        if(selection3 == 1) S+=2;
        else if(selection3 == 2) C+=3;
        else if(selection3 == 3) T+=3;

        int selection4 = options.get(3);
        if(selection4 == 1) C+=2;
        else if(selection4 == 2) T+=1;
        else if(selection4 == 3) J+=1;

        Map<String,Object> params = new HashMap<>();

        getContent(C,"C", params);
        getContent(S,"S", params);
        getContent(T,"T", params);
        getContent(J,"J", params);
        int cValue = getRandomScore(C);
        Map<String,Object> scoreC = new HashMap<>();
        scoreC.put("svgContent",String.valueOf(cValue));
        scoreC.put("x",cValue<100?593:570);
        params.put("scoreC",scoreC);
        int sValue = getRandomScore(S);
        Map<String,Object> scoreS = new HashMap<>();
        scoreS.put("svgContent",String.valueOf(sValue));
        scoreS.put("x",sValue<100?194:171);
        params.put("scoreS",scoreS);
        int tValue = getRandomScore(T);
        Map<String,Object> scoreT = new HashMap<>();
        scoreT.put("svgContent",String.valueOf(tValue));
        scoreT.put("x",tValue<100?593:570);
        params.put("scoreT",scoreT);
        int jValue = getRandomScore(J);
        Map<String,Object> scoreJ = new HashMap<>();
        scoreJ.put("svgContent",String.valueOf(jValue));
        scoreJ.put("x",jValue<100?194:171);
        params.put("scoreJ",scoreJ);

        inputMaxType(userWxInfo.getOpenId(),cValue,jValue,sValue,tValue);

        params.put("avatar",userWxInfo.getHeadImgUrl());

        String userName = userWxInfo.getNickname();
        if(StringUtils.isBlank(userName)) userName = "匿名用户";
        int a = userName.length()*23 + 20;
        int x = (750-a)/2;
        //昵称矩形框
        Map<String,Object> rect = new HashMap<>();
        rect.put("x",x);
        rect.put("width",a);
        params.put("rect",rect);
        //昵称文本
        Map<String,Object> nickName = new HashMap<>();
        nickName.put("svgContent",userName);
        nickName.put("x",x+10);
        params.put("nickName",nickName);
        //左圆
        Map<String,Object> circleL = new HashMap<>();
        circleL.put("cx",x);
        params.put("circleL",circleL);
        //右圆
        Map<String,Object> circleR = new HashMap<>();
        circleR.put("cx",x+a);
        params.put("circleR",circleR);
//        File file = generateImage(params);
//        if(file == null){
//            log.error("file is null");
//            return null;
//        }
//        if(!file.exists()){
//            log.error("file is not exists");
//            return null;
//        }
//        WxMediaUploadResult res = null;
//        try {
//            res = wxMpService.getMaterialService().mediaUpload(WxConsts.MediaFileType.IMAGE, file);
//        } catch (WxErrorException e) {
//            log.error("-------------generateImage upload file fail：{}", JsonUtils.toJson(e.getError()));
//            return null;
//        }
        BufferedImage imageBuff = generateImageBuff(params);
        if(imageBuff == null){
            log.error("imageBuff is null");
            return null;
        }
        WxMediaUploadResult res = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(imageBuff, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            res = wxMpService.getMaterialService().mediaUpload(WxConsts.MediaFileType.IMAGE, "png", is);
        } catch (Exception e) {
            log.error("-------------generateImage upload file fail：{}", JsonUtils.toJson(e.getMessage()));
            return null;
        }
        return res.getMediaId();
    }

    private void inputMaxType(String openid, int c, int j, int s, int t) {
        String key = openid+":maxType";
        if(c>=j && c>=s && c>=t)
            redisService.put(key,"翘财富",TestTime);
        else if(j>=c && j>=s && j>=t)
            redisService.put(key,"健身心",TestTime);
        else if(s>=c && s>=j && s>=t)
            redisService.put(key,"顶事业",TestTime);
        else
            redisService.put(key,"招桃花",TestTime);
    }

    private void getContent(int value, String type, Map<String,Object> params) {
        switch (type){
            case "C":
                if(value >= 4){
                    params.put("contentC1","从此敢说不差钱");
                    params.put("contentC2","壕情万丈");
                    params.put("contentC3","堆金积玉");
                }else if(value >= 2){
                    params.put("contentC1","不再为钱而烦恼");
                    params.put("contentC2","衣食无忧");
                    params.put("contentC3","自得其乐");
                }else{
                    params.put("contentC1","赚得多花得也多");
                    params.put("contentC2","行善积德");
                    params.put("contentC3","普度众生");
                }
                break;
            case "S":
                if(value >= 4){
                    params.put("contentS1","顺心如意成大腿");
                    params.put("contentS2","平步青云");
                    params.put("contentS3","一步登天");
                }else if(value >= 2){
                    params.put("contentS1","舞台由我来发挥");
                    params.put("contentS2","前程锦秀");
                    params.put("contentS3","马到成功");
                }else{
                    params.put("contentS1","看起来小有所成");
                    params.put("contentS2","百尺竿头");
                    params.put("contentS3","更进一步");
                }
                break;
            case "T":
                if(value >= 4){
                    params.put("contentT1","高歌猛进婚姻里");
                    params.put("contentT2","早生贵子");
                    params.put("contentT3","白头偕老");
                }else if(value >= 2){
                    params.put("contentT1","孤单终结在狗年");
                    params.put("contentT2","双宿双飞");
                    params.put("contentT3","比翼连枝");
                }else{
                    params.put("contentT1","着实凭实力单身");
                    params.put("contentT2","落花无意");
                    params.put("contentT3","流水无情");
                }
                break;
            case "J":
                if(value >= 4){
                    params.put("contentJ1","一身腱子肉真好");
                    params.put("contentJ2","健步如飞");
                    params.put("contentJ3","富态安康");
                }else if(value >= 2){
                    params.put("contentJ1","精神抖擞很舒服");
                    params.put("contentJ2","生龙活虎");
                    params.put("contentJ3","红光满面");
                }else{
                    params.put("contentJ1","身心俱佳最实在");
                    params.put("contentJ2","心宽体胖");
                    params.put("contentJ3","乐不思蜀");
                }
                break;
            default:break;
        }
    }

    private int getRandomScore(int value) {
        int randomScore = new Random().nextInt(30);
        if(value >= 4) return randomScore+130;
        else if(value >= 2) return randomScore+100;
        else return randomScore+70;
    }

    @Value("${templatePath}")
    private String templatePath;

    private String loadContext() {
        File file = new File(templatePath);
        if(!file.exists()){
            log.error("检查模版文件");
            return null;
        }
        try {
            String s = "";
            InputStreamReader in = new InputStreamReader(new FileInputStream(file),"UTF-8");
            BufferedReader br = new BufferedReader(in);
            StringBuffer content = new StringBuffer();
            while ((s=br.readLine())!=null){
                content = content.append(s);
            }
            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return null;
    }

    @Value("${filePath}")
    private String fileDir;

    public File generateImage(Map<String,Object> params){
        String svgContext = loadContext();
        try {
            BufferedImage bf = SvgRenderWrapper.convertToPngAsImg(svgContext,params);
            if(bf == null) return null;
            String filePath = fileDir + System.currentTimeMillis() + ".png";
            File file = new File(filePath);
            ImageIO.write(bf, "png", file);
            return file;
        } catch (Exception e) {
            log.error("-------生成图片异常");
            e.printStackTrace();
        }
        return null;
    }

    public BufferedImage generateImageBuff(Map<String,Object> params){
        String svgContext = loadContext();
        try {
            BufferedImage bf = SvgRenderWrapper.convertToPngAsImg(svgContext,params);
            return bf;
        } catch (Exception e) {
            log.error("-------生成图片异常");
            e.printStackTrace();
        }
        return null;
    }
}
