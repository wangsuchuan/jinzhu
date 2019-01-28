package com.github.binarywang.demo.wx.mp.business;

import java.io.Serializable;

public enum StatusEnum implements Serializable {
    非答题状态(0),
    答第一题(1),
    答第二题(2),
    答第三题(3),
    答第四题(4),
    图片绘制(5),
    ;
    public final int code;
    StatusEnum(int code) {
        this.code = code;
    }
    public static StatusEnum getByCode(int code) {
        for (StatusEnum c : StatusEnum.values()) {
            if (c.code == code) return c;
        }
        return null;
    }
}
