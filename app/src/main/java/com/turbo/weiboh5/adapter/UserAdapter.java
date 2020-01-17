package com.turbo.weiboh5.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.turbo.weiboh5.R;
import com.turbo.weiboh5.bean.DataBean;

import java.util.List;

/**
 * 文件名：UserAdapter
 * 作者：Turbo
 * 时间：2020-01-09 13:42
 * 蚁穴虽小，溃之千里。
 */
public class UserAdapter extends BaseAdapter {

    private Context mContext;
    private List<DataBean> mList;

    public UserAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public DataBean getItem(int position) {
        return mList != null ? mList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return mList != null ? position : 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mList != null) {
            DataBean bean = mList.get(position);
            viewHolder.tv_name.setText(bean.getAccount());
        }
        return convertView;
    }

    public void setAdapterData(List<DataBean> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        public final TextView tv_name;

        public ViewHolder(View root) {
            tv_name = root.findViewById(R.id.tv_name);
        }
    }
}
