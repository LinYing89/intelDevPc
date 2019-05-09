package com.bairock.intelDevPc.view;

import javafx.scene.control.TextField;

/**
 * 自定义输入控件, 只允许输入数字
 * @author 44489
 * @version 2019年5月1日上午11:43:55
 */
public class NumberTextField extends TextField {

    private String reg = "[a-zA-Z.]";
    
    @Override
    public void replaceText(int start, int end, String text) {
        if (!text.matches(reg)) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String replacement) {
        if (!replacement.matches(reg)) {
            super.replaceSelection(replacement);
        }
    }
}
