package com.github.binarywang.demo.wx.mp.business;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Answer implements Serializable {
    private static final long serialVersionUID = 654132684873639392L;
    private List<Integer> options;

    public Answer() {
    }

    public Answer(List<Integer> options) {
        this.options = options;
    }
}
