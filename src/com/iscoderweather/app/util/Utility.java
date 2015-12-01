package com.iscoderweather.app.util;

import android.text.TextUtils;
import android.util.Log;

import com.iscoderweather.app.model.City;
import com.iscoderweather.app.model.County;
import com.iscoderweather.app.model.IsCoderWeatherDB;
import com.iscoderweather.app.model.Province;

public class Utility {
	/**
	 * �����ʹ�����������ص�ʡ������
	 */
	public synchronized static boolean handlerProvincesResponse (IsCoderWeatherDB iscoderWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {//��鷵�ص��ַ���response�Ƿ�Ϊ�գ������Ƿ�Ϊ0
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					iscoderWeatherDB.saveProvince(province);//���������������ݱ��浽Province���С�
				}
				return true;
			}
		}	
		return false;
		
	}
	
	/**
	 * �����ʹ�����������ص��м�����
	 */
	public static boolean handleCitiesRespopnse(IsCoderWeatherDB iscoderWeatherDB, String response, int provinceId){
		if(!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			//Log.d("cxd-allCities" + "���г��У�" + allCities.toString(), response);
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					iscoderWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �����ʹ�����������ص��ؼ�����
	 */
	public static boolean handleCountiesResponse (IsCoderWeatherDB iscoderWeatherDB, String response, int cityId) {
		if(!TextUtils.isEmpty(response)) {
			String [] allCounties = response.split(",");//��","�����ݽ��зָ�
			if (allCounties != null && allCounties.length > 0) {
				for(String c : allCounties){
					String[] array = c.split("\\|");//���ա�|�������ݷָ�
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					iscoderWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
