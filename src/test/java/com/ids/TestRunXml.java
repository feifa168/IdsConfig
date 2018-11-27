package com.ids;

import com.ids.param.ParamConfig;
import org.junit.Test;

public class TestRunXml {
    @Test
    public void testRunXml123() {
        if (ParamConfig.parse("run.xml")) {
            System.out.println("parse run.xml ok");
        } else {
            System.out.println("param run.xml fail");
        }
    }
}
