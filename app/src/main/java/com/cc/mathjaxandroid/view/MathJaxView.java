package com.cc.mathjaxandroid.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cc.mathjaxandroid.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MathJaxView extends FrameLayout implements View.OnTouchListener {
    private String inputText = null;
    private String inputTestColor = "#000000";
    private int inputTextSize = 15;
    private WebView mWebView;
    private Handler handler = new Handler();

    /**
     * laTex can only be rendered when WebView is already loaded
     */
    private boolean webViewLoaded = false;


    public interface OnMathJaxRenderListener {
        void onRendered();

    }

    private OnMathJaxRenderListener onMathJaxRenderListener;

    public MathJaxView(Context context) {
        super(context);
        init(context, null);
    }


    public MathJaxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MathJaxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setRenderListener(OnMathJaxRenderListener onMathJaxRenderListener) {
        this.onMathJaxRenderListener = onMathJaxRenderListener;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void init(Context context, AttributeSet attrSet) {
        mWebView = new WebView(context);
        mWebView.setOnTouchListener(this);
        int gravity = Gravity.CENTER;
        boolean verticalScrollbarsEnabled = false;
        boolean horizontalScrollbarsEnabled = false;

        if (attrSet != null) {
            TypedArray attrs = context.obtainStyledAttributes(attrSet, R.styleable.MathJaxView);
            gravity = attrs.getInteger(R.styleable.MathJaxView_android_gravity, Gravity.CENTER);
            verticalScrollbarsEnabled = attrs.getBoolean(R.styleable.MathJaxView_verticalScrollbarsEnabled, false);
            horizontalScrollbarsEnabled = attrs.getBoolean(R.styleable.MathJaxView_horizontalScrollbarsEnabled, false);
            attrs.recycle();
        }

        addView(mWebView, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity)
        );

        // callback when WebView is loading completed
        webViewLoaded = false;
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (onMathJaxRenderListener != null)
                    onMathJaxRenderListener.onRendered();

            }
        });

        mWebView.setVerticalScrollBarEnabled(verticalScrollbarsEnabled);
        mWebView.setHorizontalScrollBarEnabled(horizontalScrollbarsEnabled);
        mWebView.setBackgroundColor(0);
        WebSettings mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setAppCacheEnabled(true);
        mWebSettings.setAllowContentAccess(true);
        mWebSettings.setSupportZoom(true);
        mWebSettings.setAllowUniversalAccessFromFileURLs(true);
        mWebSettings.setAllowFileAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.setMixedContentMode(WebSettings.LOAD_NORMAL);
        }
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setSavePassword(true);
        mWebSettings.setSaveFormData(true);
        mWebSettings.setLoadsImagesAutomatically(true);
    }

    /**
     * called when webView is ready with rendering LaTex
     */
    protected void rendered() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWebView.setVisibility(View.VISIBLE);
                if (onMathJaxRenderListener != null)
                    onMathJaxRenderListener.onRendered();
            }
        }, 100);
    }


    boolean isChoice = false;

    public void setChoice(boolean choice) {
        isChoice = choice;
    }

    /**
     * @param inputText formatted string
     */
    public void setInputText(String inputText) {
        this.inputText = inputText;
        loadText();
    }

    /**
     * @param text  传入公式
     * @param color  文本颜色 #888888
     */
    public void setInputText(String text, String color) {
        this.inputTestColor = color;
        if (TextUtils.isEmpty(inputText)) {
            this.inputText = text;
            loadText();
        }
        loadColor();
    }

    public void setInputTextSize(int textSize) {
        if (textSize <= 0) {
            return;
        }
        this.inputTextSize = textSize;
        loadText();
    }

    public int getInputTextSize() {
        return inputTextSize;
    }

    public void loadText() {
        String laTexString;
        laTexString = inputText;
        loadLocal(getHtmlLocation(laTexString));
    }

    private void loadColor() {
        String javascriptCommand = "javascript:changeLatexTextColor(\"" + inputTestColor + "\")";
        mWebView.loadUrl(javascriptCommand, null);

    }

    /**
     * 加载本地网页
     *
     * @param html
     */
    private void loadLocal(String html) {
        mWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);

    }

    private String getHtmlLocation(String data) {
        return "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta content=\"width=device-width,height=device-height,initial-scale=1.0,maximum-scale=1.0,user-scalable=no\" name=\"viewport\">\n" +
                "   \n" +
                "     <script>MathJax = {\n" +
                "          tex:{\n" +
                "                  inlineMath: [['$', '$'], ['\\\\(', '\\\\)']]\n" +
                "              },\n" +
                "          svg:{\n" +
                "                  fontCache: 'global'\n" +
                "              }\n" +
                "          };\n" +
                "      </script>\n" +
                "   \n" +
                "      <script type=\"text/javascript\" id=\"MathJax-script\" async  src=\"file:///android_asset/mathjax/tex-chtml.js\"></script>\n" +
                "\n" + "  <script type=\"text/javascript\">\n" +
                "\t           function changeLatexTextColor(color) {\n" +
                "\t                var element = document.getElementById('math')\n" +
                "\t                element.style.color =color\n" +
                "\t            }\n" +
                "\t  </script>" + "   <style type=\"text/css\">\n" +
                "\tmjx-container  {\n" +
                "\t  outline: 0;}\n" +
                "   </style>" +

                "</head>\n" +
                "<body>\n" +
                "\n" +
                "\n" +
                "\n" +
                "<div id='math' style=\"text-align: left; width: 95% !important; height: auto;font-size: "+getInputTextSize()+"px; color:" + inputTestColor +
                ";\">\n" +
                data +
                "\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n";

    }

    private boolean intercept = false;

    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (intercept) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(ev);
    }

    private float mDownPosX;
    private float mDownPosY;
    private float mUpPosX;
    private float mUpPosY;

    private float MOVE_THRESHOLD_DP = 20;
    private OnClickListener clickListener;

    private final int CLICK_ON_WEBVIEW = 1;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CLICK_ON_WEBVIEW:
                    if (clickListener != null) {
                        clickListener.onClick(MathJaxView.this);
                    }

                    break;
            }
            return false;
        }
    });

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.mDownPosX = event.getX();
                this.mDownPosY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                this.mUpPosX = event.getX();
                this.mUpPosY = event.getY();
                if ((Math.abs(mUpPosX - this.mDownPosX) < MOVE_THRESHOLD_DP) && (Math.abs(mUpPosY - this.mDownPosY) < MOVE_THRESHOLD_DP)) {
                    if (!mHandler.hasMessages(CLICK_ON_WEBVIEW)) {
                        mHandler.sendEmptyMessage(CLICK_ON_WEBVIEW);
                        Log.e("测试", "mathwebonTouch");
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }
}
