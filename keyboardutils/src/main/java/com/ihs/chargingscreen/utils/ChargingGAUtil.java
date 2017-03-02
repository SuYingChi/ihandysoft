package com.ihs.chargingscreen.utils;

/**
 * Created by Arthur on 17/3/2.
 */

public class ChargingGAUtil {

    private static ChargingGAUtil instance = null;

    private ChargingGAUtil() {


    }

    public static synchronized ChargingGAUtil getInstance() {
        if (instance == null) {
            instance = new ChargingGAUtil();
        }
        return instance;
    }


    public void chargingEnable(){

    }

}
