package com.net168.testapp.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;

import com.sohu.qf.fuconfig.BaseFuBean;
import com.sohu.qf.fuconfig.FilterBean;
import com.sohu.qf.fuconfig.FuConfigManager;
import com.sohu.qf.fuconfig.IFuConfigCallback;
import com.sohu.qf.fuconfig.authpack;

import java.util.List;


/**
 * 这个jar引入需要
 * 拷贝 libs工程下的 qffu-1.0.0.jar   QFHttp-1.0.4.aar 两个文件
 *
 * 然后在gradle的dependencies配置
 * compile 'com.google.code.gson:gson:2.8.0'
 * compile 'com.squareup.okhttp3:okhttp:3.4.1'
 * compile(name: 'QFHttp-1.0.4', ext: 'aar')
 * compile(name: 'QfForFu-1.0.2', ext: 'aar')
 *
 * 将 v3.mp3   face_beautification.mp3  到主工程assets/Fraceunity目录下，   这两个文件不要用本工程的，怕不配套，要跟随fu的sdk版本，他们会提供
 * 以后切回千帆这两个文件会放在千帆的assets/Fraceunity目录下，方便随时更换升级
 * 权限需要 本地文件权限 和 网络权限
 *
 * 现在暂时支持贴纸列表的数据和各个贴纸的配置文件
 *
 */
public class MainActivity extends Activity {

    private FuConfigManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        /**
         * 先获取一个配置管理器，单例
         */
        mManager = FuConfigManager.getInstance(getApplicationContext());
        /**
         * 方便你们测试指定的文件存放路径，正式包不要指定这个，不设置会用默认的qf的路径
         */
        mManager.setFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test");
        /**
         * 获取贴纸配置列表接口
         * BaseFuBean这个是贴纸的bean
         * BaseFuBean -> name  贴纸保存的文件名
         * BaseFuBean -> staticUrl   贴纸的显示图片icon
         * BaseFuBean -> dynamicUrl  贴纸文件的下载路径
         * BaseFuBean -> loacl;
         * IFuConfigCallback   调用回调，这个请求是会取本地文件或者网络图片的缓存，所以需要一定时间，所以做成回调使用
         * List<BaseFuBean> result 贴纸列表数据
         * onFailed   服务失败或者网络错误等情况回调
         */
        mManager.getEffectList(new IFuConfigCallback<List<BaseFuBean>>() {
            @Override
            public void onSuccess(List<BaseFuBean> result) {
                downFile(result);
            }

            @Override
            public void onFailed() {

            }
        });

        /**
         * 手势贴纸，千帆自用
         */
        mManager.getGestureList(new IFuConfigCallback<List<BaseFuBean>>() {
            @Override
            public void onSuccess(List<BaseFuBean> result) {
                downFile(result);
            }

            @Override
            public void onFailed() {

            }
        });

        //集成了鉴权文件
        authpack a;

        /**
         * 获取v3.mp3的配置文件二进制数据(Fu基础配置)
         */
        byte[] d1 = mManager.getBaseFuConfig();


        /**
         * 获取滤镜列表接口  FilterBean
         * FilterBean -> key  传给fu使用的key，类似于 faceunity.fuItemSetParam(mFacebeautyItem, "filter_name", !!key!!);
         * FilterBean -> name 列表显示的name，用于ui显示
         * FilterBean -> iconRes 列表显示的本地图片资源，用于ui显示
         */
        List<FilterBean> list = mManager.getFilterList();

        /**
         * 获取face_beautification.mp3的配置文件二进制数据（美白、大眼、瘦脸、磨皮）
         */
        byte[] d2 = mManager.getFaceBeautifyFuConfig();



    }

    /**
     * 这个是下载每个单独贴纸的配置数据文件
     * 你们那边应该做成列表点击后开始down
     * 然后监听回调IFuConfigCallback<String>
     * 会返回一个本地文件地址，然后拿这个文件的数据去设置到fu sdk里面
     * 这是一个异步的过程
     * 这里是模拟全部下载
     * 调用完会在mManager.setFilePath有down的文件
     * 有效期内再次调用会直接去用file缓存
     */
    private void downFile(List<BaseFuBean> data) {
        for (int i = 0; i < data.size(); i++) {
            BaseFuBean b = data.get(i);
            //去获取单个贴纸的配置文件，返回本地文件的路径
            //获取方式，根据不同情况会去网络或者本地获取，对于外部的影响只是回调时间长短的问题
            //如果想获取本地地址的话，可以直接取BaseFuBean.loacl  但是不一定可靠，存在用户手动删除该文件的风险
            //这个方法检测本地文件，如果可用直接返回BaseFuBean.loacl 如果不可用会去网络拉取后返回
            mManager.getFile(b, new IFuConfigCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.i("xxxx", "success download , path = " + result);
                }

                @Override
                public void onFailed() {
                    Log.i("xxxx", "failed");
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消掉配置管理器未完成任务的IFuConfigCallback监听的UI回调
        mManager.cancelAll();
    }
}
