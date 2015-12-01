package com.iscoderweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iscoderweather.app.model.City;
import com.iscoderweather.app.model.County;
import com.iscoderweather.app.model.IsCoderWeatherDB;
import com.iscoderweather.app.model.Province;
import com.iscoderweather.app.util.HttpCallbackListener;
import com.iscoderweather.app.util.HttpUtil;
import com.iscoderweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL＿CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private IsCoderWeatherDB iscoderWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	
	/**
	 *  市列表
	 */
	private List<City> cityList;
	
	/**
	 * 县列表
	 */
	private List<County> countyList;
	
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	
	/**
	 * 选中的城市
	 */
	private City selectedCity;
	
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(com.iscoderweather.app.R.layout.choose_area);
		listView = (ListView)findViewById(com.iscoderweather.app.R.id.list_view);
		titleText = (TextView)findViewById(com.iscoderweather.app.R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		iscoderWeatherDB = IsCoderWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL＿CITY) {
					selectedCity = cityList.get(index);
					queryCountites();
				}
			}
			
		});
		queryProvinces();//加载省级数据
	}
	
	/**
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到再到服务器查询。
	 */
	private void queryProvinces() {
		provinceList = iscoderWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();//刷新ListView上的adeper中的数据。通常在数据改变后刷新。
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFormServer(null, "province");
		}
	}
	
	/**
	 * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
	 */
	protected void queryCities() {
		//Log.d("cxd", "选中的省份：" + selectedProvince.getId());
		cityList = iscoderWeatherDB.loadCities(selectedProvince.getId());
		Log.d("cxd-loadCities()", "cityList大小： "+cityList.size());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL＿CITY;
		} else {
			queryFormServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	 * 查询选中城市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
	 */
	protected void queryCountites() {
		countyList = iscoderWeatherDB.loadCounty(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFormServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据。
	 */
	private void queryFormServer(final String code, final String type) {
		String address;
		if(!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				//Log.d("cxd", response);
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handlerProvincesResponse(iscoderWeatherDB, response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesRespopnse(iscoderWeatherDB, response, selectedProvince.getId());
					Log.d("cxd-saveCity", "结果： " + result);
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(iscoderWeatherDB, response, selectedCity.getId());
				}
				if (result) {
					//通过runOnUiThread()方法回到主线程处理逻辑。
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//Log.d("cxd-result", "对话框关闭");
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCountites();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				//通过OnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}});
			}
			
		});
		
	}
	
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 捕获Back按键，根据当前的界别来判断，此时应该返回市列表、省列表、还是直接退出
	 */
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL＿CITY) {
			queryProvinces();
		} else {
			finish();
		}
	}
}
