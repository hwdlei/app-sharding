package com.dlei.sharding.test;

import com.alibaba.fastjson2.JSON;
import com.dlei.app.sharding.provider.ProviderMetadataHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author donglei
 * @version 2023-01-11
 */
@RestController
public class TestController {

    @Autowired
    private ProviderMetadataHolder metadataHolder;

    @GetMapping(value = "/find", produces = "application/json; charset=utf-8")
    public String find() {
        return JSON.toJSONString(metadataHolder.getMetadata());
    }

}
