package com.dlei.app.sharding.provider.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author donglei
 * @version 2023-01-10
 */
@Slf4j
public class NetworkUtils {

    public static String getLocalHost() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn(e.getMessage());
        }
        return "0.0.0.0";
    }
}
