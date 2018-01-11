package space.exploration.mars.rover.service;

import space.exploration.communications.protocol.service.DanRDRData;

public class DANCalibrationService {
    private int sol = 0;

    private DANServiceUtil danServiceUtil = null;

    public DANCalibrationService(int sol){
        danServiceUtil = new DANServiceUtil(sol);
    }


    public DanRDRData.DANDerivedData getDanPayload(){
        


        return null;
    }

}
