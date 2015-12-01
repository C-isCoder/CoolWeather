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
	public static final int LEVEL��CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private IsCoderWeatherDB iscoderWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	/**
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	
	/**
	 *  ���б�
	 */
	private List<City> cityList;
	
	/**
	 * ���б�
	 */
	private List<County> countyList;
	
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	
	/**
	 * ��ǰѡ�еļ���
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
				} else if (currentLevel == LEVEL��CITY) {
					selectedCity = cityList.get(index);
					queryCountites();
				}
			}
			
		});
		queryProvinces();//����ʡ������
	}
	
	/**
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ���ٵ���������ѯ��
	 */
	private void queryProvinces() {
		provinceList = iscoderWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();//ˢ��ListView�ϵ�adeper�е����ݡ�ͨ�������ݸı��ˢ�¡�
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFormServer(null, "province");
		}
	}
	
	/**
	 * ��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
	 */
	protected void queryCities() {
		//Log.d("cxd", "ѡ�е�ʡ�ݣ�" + selectedProvince.getId());
		cityList = iscoderWeatherDB.loadCities(selectedProvince.getId());
		Log.d("cxd-loadCities()", "cityList��С�� "+cityList.size());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL��CITY;
		} else {
			queryFormServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	 * ��ѯѡ�г��������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ�������ݡ�
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
					Log.d("cxd-saveCity", "����� " + result);
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(iscoderWeatherDB, response, selectedCity.getId());
				}
				if (result) {
					//ͨ��runOnUiThread()�����ص����̴߳����߼���
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							//Log.d("cxd-result", "�Ի���ر�");
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
				//ͨ��OnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}});
			}
			
		});
		
	}
	
	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/**
	 * ����Back���������ݵ�ǰ�Ľ�����жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳�
	 */
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL��CITY) {
			queryProvinces();
		} else {
			finish();
		}
	}
}
