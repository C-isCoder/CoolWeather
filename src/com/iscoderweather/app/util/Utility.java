package com.iscoderweather.app.util;

import android.text.TextUtils;
import android.util.Log;

import com.iscoderweather.app.model.City;
import com.iscoderweather.app.model.County;
import com.iscoderweather.app.model.IsCoderWeatherDB;
import com.iscoderweather.app.model.Province;

public class Utility {
	/**
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handlerProvincesResponse (IsCoderWeatherDB iscoderWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {//检查返回的字符串response是否为空，长度是否为0
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					iscoderWeatherDB.saveProvince(province);//将解析出来的数据保存到Province表中。
				}
				return true;
			}
		}	
		return false;
		
	}
	
	/**
	 * 解析和处理服务器返回的市级数据
	 */
	public static boolean handleCitiesRespopnse(IsCoderWeatherDB iscoderWeatherDB, String response, int provinceId){
		if(!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			//Log.d("cxd-allCities" + "所有城市：" + allCities.toString(), response);
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
	 * 解析和处理服务器返回的县级数据
	 */
	public static boolean handleCountiesResponse (IsCoderWeatherDB iscoderWeatherDB, String response, int cityId) {
		if(!TextUtils.isEmpty(response)) {
			String [] allCounties = response.split(",");//按","对数据进行分割
			if (allCounties != null && allCounties.length > 0) {
				for(String c : allCounties){
					String[] array = c.split("\\|");//按照“|”对数据分割
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
