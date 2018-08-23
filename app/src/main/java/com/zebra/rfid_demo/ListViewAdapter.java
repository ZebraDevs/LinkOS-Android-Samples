
package com.zebra.rfid_demo;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewAdapter extends BaseAdapter{

	public ArrayList<HashMap<String, String>> list;
	Activity activity;
	public static final String FIRST_COLUMN="First";
	public static final String SECOND_COLUMN="Second";
	public static final String THIRD_COLUMN="Third";
	public static final String FOURTH_COLUMN="Fourth";
	public static final String FIFTH_COLUMN="Fifth";

	public ListViewAdapter(Activity activity,ArrayList<HashMap<String, String>> list){
		super();
		this.activity=activity;
		this.list=list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	private class ViewHolder{
		TextView txtFirst;
		TextView txtSecond;
		TextView txtThird;
		TextView txtFourth;
		TextView txtFifth;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
	
		ViewHolder holder;
		
		LayoutInflater inflater=activity.getLayoutInflater();
		
		if(convertView == null){
			
			convertView=inflater.inflate(R.layout.colmn_row, null);
			holder=new ViewHolder();
			
			holder.txtFirst=(TextView) convertView.findViewById(R.id.TextFirst);
			holder.txtSecond=(TextView) convertView.findViewById(R.id.TextSecond);
			holder.txtThird=(TextView) convertView.findViewById(R.id.TextThird);
			holder.txtFourth=(TextView) convertView.findViewById(R.id.TextFourth);
			holder.txtFifth=(TextView) convertView.findViewById(R.id.TextFifth);

			convertView.setTag(holder);
		}else{
			
			holder=(ViewHolder) convertView.getTag();
		}
		
		HashMap<String, String> map=list.get(position);
		holder.txtFirst.setText(map.get(FIRST_COLUMN));
		holder.txtSecond.setText(map.get(SECOND_COLUMN));
		holder.txtThird.setText(map.get(THIRD_COLUMN));
		holder.txtFourth.setText(map.get(FOURTH_COLUMN));
		holder.txtFifth.setText(map.get(FIFTH_COLUMN));
		return convertView;
	}

}
