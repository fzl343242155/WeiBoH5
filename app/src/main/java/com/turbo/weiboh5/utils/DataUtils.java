package com.turbo.weiboh5.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.greendao.gen.DataBeanDao;
import com.turbo.weiboh5.TurboApplication;
import com.turbo.weiboh5.URLs;
import com.turbo.weiboh5.bean.DataBean;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件名：DataUtils
 * 作者：Turbo
 * 时间：2020-01-09 09:37
 * 蚁穴虽小，溃之千里。
 */
public class DataUtils {

    private static DataUtils instance;
    private final DataBeanDao dataBeanDao;

    private DataUtils() {
        dataBeanDao = TurboApplication.getApp().getDaoSession().getDataBeanDao();
    }

    public static DataUtils getInstance() {
        if (null == instance) {
            instance = new DataUtils();
        }
        return instance;
    }

    /**
     * 插入一条数据到数据库
     *
     * @param dataBean
     */
    public void insert(DataBean dataBean) {
        long id = dataBeanDao.insert(dataBean);
        Map<String, Long> map = getData();
        if(map==null){
            map = new HashMap<>();
        }
        map.put(dataBean.getAccount(), id);
        String json = new Gson().toJson(map);
        SharedPreferencesUtils.getInstance(TurboApplication.getApp()).putSP(URLs.USERDATA_KEY, json);
    }

    /**
     * 删除一条数据
     *
     * @param id
     */
    public void delete(Long id) {
        dataBeanDao.deleteByKey(id);
    }

    /**
     * 删除全部数据
     */
    public void deleteAll() {
        dataBeanDao.deleteAll();
    }

    /**
     * 修改其中I一条的数据
     *
     * @param dataBean
     */
    public void update(DataBean dataBean) {
        dataBeanDao.update(dataBean);
    }

    /**
     * 根据ID查询一条信息
     *
     * @param id
     * @return
     */
    public DataBean select(long id) {
        return dataBeanDao.load(id);
    }

    /**
     * 查询所有信息
     *
     * @return
     */
    public List<DataBean> selectAll() {
        return dataBeanDao.loadAll();
    }

    /**
     * 获取当前账号的cookie
     *
     * @return
     */
    public String getCookie() {
        String user = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.CURRENT_USER); //获取当前用户
        DataBean dataBean = DataUtils.getInstance().select(DataUtils.getInstance().getID(user));
        String cookie = dataBean.getCookie();
        return cookie;
    }


    /**
     * 获取当前账号的st
     *
     * @return
     */
    public String getSt() {
        String user = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.CURRENT_USER); //获取当前用户
        DataBean dataBean = DataUtils.getInstance().select(DataUtils.getInstance().getID(user));
        String st = dataBean.getSt();
        return st;
    }

    /**
     * 根据用户名找ID
     *
     * @param user
     * @return
     */
    public long getID(String user) {
        long result = 0l;
        Map<String, Long> map = getData();
        if (!map.isEmpty()) {
            for (String key_str : map.keySet()) {
                if (user.equals(key_str)) {
                    result = map.get(key_str);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获取数据的对应列表
     *
     * @return
     */
    private static Map<String, Long> getData() {
        String json = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.USERDATA_KEY);
        if (TextUtils.isEmpty(json)) {
            return null;
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Long>>() {
            }.getType();
            Map<String, Long> map = gson.fromJson(json, type);
            return map;
        }
    }

}
