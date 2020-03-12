package com.fatapp.textcut;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;


public class MainActivity extends AppCompatActivity {
    final int REQUEST_CODE_CHOOSE_FILE = 0;

    private boolean loading = false;
    private String filePath;

    private Dialog mDialog;

    private int choseNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissins();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/plain");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_FILE);
            }
        });


        findViewById(R.id.transform).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //显示按下时的背景图片
                    v.setBackgroundResource(R.drawable.button_roud_shape_click);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //显示抬起时的背景图片
                    v.setBackgroundResource(R.drawable.button_round_shape);
                }
                return false;
            }
        });

        findViewById(R.id.transform).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        //定义一个setting记录APP是几次启动
        SharedPreferences setting = getSharedPreferences("com.fatapp.textcut", 0);
        boolean user_first = setting.getBoolean("FIRST", true);
        if (user_first) {// 第一次则跳转到欢迎页面
            setting.edit().putBoolean("FIRST", false).apply();
        }
    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (loading) {
            if (keyCode == KeyEvent.KEYCODE_BACK)
                Toast.makeText(this, R.string.cannotBack, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, R.string.exitWarn, Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                System.exit(0);
            }
            return true;
        }
    }//屏蔽返回键

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    String path;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        return;
                    }

                    Uri uri;
                    uri = data.getData();

                    if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                        path = uri.getPath();
                    }
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            path = uri.getPath();
                            Log.println(Log.ERROR, "Path ", path);
                        } else {
                            path = GetFile.getPath(this, uri);
                        }
                    } else {//4.4以下下系统调用方法
                        path = GetFile.getRealPathFromURI(getBaseContext(), uri);
                    }


                    filePath = path;

                    File text = new File(filePath);
                    if (text.isFile() | text.exists()) {
                        try {
                            int n = GetText.getFileLineNumber(GetText.convertCodeAndGetText(text.getAbsolutePath()));
                            Toast.makeText(this, getString(R.string.totalLine, Integer.toString(n)), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    final File txt = new File(filePath);
                    if (txt.isFile() | txt.exists()) {
                        GetText.setDone(false);
                        MainActivity.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        loading = true;
                        findViewById(R.id.loadingView).setVisibility(View.VISIBLE);
                        new Thread(new Runnable() {
                            public void run() {
                                //在这里可以进行UI操作
                                do {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            float percent = (float) GetText.getOnLine() / GetText.getLineNumber() * 100;
                                            TextView loadingText = findViewById(R.id.loadingText);
                                            loadingText.setText(getString(R.string.transCoding, Integer.toString((int) percent), Integer.toString(GetText.getOnLine()), Integer.toString(GetText.getLineNumber())));
                                        }
                                    });
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } while (!GetText.getDone());
                            }
                        }).start();
                        new Thread(new Runnable() {
                            public void run() {
                                GetText.getText(GetText.convertCodeAndGetText(txt.getAbsolutePath()));
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        TextView o = findViewById(R.id.originalTextView);
                                        o.setText(GetText.getText());
                                        findViewById(R.id.loadingView).setVisibility(View.GONE);
                                        MainActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        loading = false;
                                    }
                                });
                            }
                        }).start();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void getPermissins() {
        requestPermissins(new PermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied(String[] deniedPermissions) {
                Toast.makeText(MainActivity.this, R.string.permissionDenied, Toast.LENGTH_SHORT).show();
                getPermissins();
            }
        });
    }

    private void requestPermissins(PermissionUtils.OnPermissionListener mOnPermissionListener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mOnPermissionListener.onPermissionGranted();
            return;
        }
        String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
        PermissionUtils.requestPermissions(this, 0
                , permissions, mOnPermissionListener);
    }

    /**
     * 显示弹出框
     */
    private void showDialog() {
        if (mDialog == null) {
            initDialog();
        }
        mDialog.show();
    }

    /**
     * 初始化分享弹出框
     */
    private void initDialog() {
        mDialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        mDialog.setCanceledOnTouchOutside(true); //手指触碰到外界取消
        mDialog.setCancelable(true);             //可取消 为true
        Window window = mDialog.getWindow();      // 得到dialog的窗体
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.ActionSheetDialogAnimation);

        View view = View.inflate(this, R.layout.dialog_layout, null); //获取布局视图
        final NumberPickerView picker = view.findViewById(R.id.picker);
        ArrayList<String> displayValuesList = new ArrayList<>();
        for (int i = 2; i <= 100; i++) {
            displayValuesList.add(Integer.toString(i));
        }
        String[] displayValues = new String[displayValuesList.size()];
        displayValues = displayValuesList.toArray(displayValues);
        picker.setDisplayedValues(displayValues);
        picker.setMinValue(2);
        picker.setMaxValue(100);
        picker.setValue(2);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                choseNumber = picker.getValue();

                if (GetText.getText() == null) {
                    Snackbar.make(v, R.string.sourceTextEmpty, Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (GetText.getText().length() - 1 < choseNumber) {
                    Snackbar.make(v, R.string.cutNumberTooBig, Snackbar.LENGTH_LONG).show();
                    return;
                }
                ArrayList<String> output = Cut.cutText(GetText.getText(), choseNumber);

                File file = new File(filePath);
                String fileName = getFileNameNoEx(file.getName());
                for (int n = 0; n <= output.size() - 1; n++) {
                    int m = n + 1;
                    writeTxtToFile(output.get(n), file.getParent() + "/" + fileName + "/", fileName + "_" + m + ".txt");
                }

                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Snackbar.make(findViewById(R.id.contentView), R.string.save + ": " + file.getParent() + "/" + fileName + "/", Snackbar.LENGTH_LONG).show();
//                Toast.makeText(getBaseContext(), "已将文本切割成" + choseNumber + "份", Toast.LENGTH_SHORT).show();
//                Toast.makeText(getBaseContext(), "保存：" + file.getParent() + "/" + fileName + "/", Toast.LENGTH_SHORT).show();
            }
        });

        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     * */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

//生成文件

    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

//生成文件夹

    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

}

