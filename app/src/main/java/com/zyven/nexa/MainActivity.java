package com.zyven.nexa;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.webkit.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG=Color.rgb(13,15,24), PANEL=Color.rgb(24,27,41), PURPLE=Color.rgb(142,102,255), TEXT=Color.rgb(241,242,248);
    private final ArrayList<WebView> tabs=new ArrayList<>();
    private int active=0;
    private LinearLayout root, area, bottom;
    private EditText address;
    private TextView status;
    private SharedPreferences prefs;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("nexa",MODE_PRIVATE);
        buildUI(); newTab("https://www.google.com");
    }
    private GradientDrawable shape(int c,int r) { GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setCornerRadius(r); return d; }
    private TextView button(String s,int size) {
        TextView t=new TextView(this); t.setText(s); t.setTextColor(TEXT); t.setTextSize(size);
        t.setGravity(Gravity.CENTER); t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); t.setBackground(shape(PANEL,24)); return t;
    }
    private void buildUI() {
        root=new LinearLayout(this); root.setOrientation(1); root.setPadding(12,10,12,8); root.setBackgroundColor(BG); setContentView(root);
        TextView logo=new TextView(this); logo.setText("NEXA  •  BROWSER"); logo.setTextColor(TEXT); logo.setTextSize(13); logo.setTypeface(null,1); logo.setPadding(5,5,5,10); root.addView(logo);
        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        address=new EditText(this); address.setSingleLine(true); address.setTextColor(TEXT); address.setHintTextColor(Color.LTGRAY); address.setHint("Ara veya adres yaz"); address.setTextSize(14); address.setPadding(14,0,8,0); address.setBackground(shape(PANEL,28)); address.setImeOptions(EditorInfo.IME_ACTION_GO);
        bar.addView(address,new LinearLayout.LayoutParams(0,48,1));
        TextView go=button("Git",14); go.setBackground(shape(PURPLE,24)); LinearLayout.LayoutParams g=new LinearLayout.LayoutParams(58,48); g.setMargins(8,0,0,0); bar.addView(go,g); root.addView(bar);
        go.setOnClickListener(v->navigate()); address.setOnEditorActionListener((v,a,e)->{if(a==EditorInfo.IME_ACTION_GO||a==EditorInfo.IME_ACTION_DONE){navigate();return true;}return false;});
        status=new TextView(this); status.setText("Hazır"); status.setTextColor(Color.GRAY); status.setTextSize(11); status.setPadding(6,7,6,5); root.addView(status);
        area=new LinearLayout(this); area.setBackground(shape(Color.WHITE,18)); LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,0,1); ap.setMargins(0,2,0,10); root.addView(area,ap);
        bottom=new LinearLayout(this); bottom.setGravity(Gravity.CENTER);
        String[] labels={"‹","›","⌂","＋","▢","⋮"};
        for(int i=0;i<labels.length;i++){final int n=i; TextView x=button(labels[i],22); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,48,1); p.setMargins(3,0,3,0); bottom.addView(x,p); x.setOnClickListener(v->action(n));}
        root.addView(bottom);
    }
    private WebView current(){return tabs.isEmpty()?null:tabs.get(active);}
    private void action(int n){
        WebView w=current(); if(w==null)return;
        if(n==0&&w.canGoBack())w.goBack();
        else if(n==1&&w.canGoForward())w.goForward();
        else if(n==2)home();
        else if(n==3)newTab("https://www.google.com");
        else if(n==4)showTabs();
        else menu();
    }
    private void newTab(String url){
        WebView w=new WebView(this); WebSettings s=w.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setLoadsImagesAutomatically(true); s.setMediaPlaybackRequiresUserGesture(true);
        w.setWebViewClient(new WebViewClient(){
            public void onPageStarted(WebView v,String u,android.graphics.Bitmap b){if(v==current()){address.setText(u);status.setText("Yükleniyor…");}}
            public void onPageFinished(WebView v,String u){if(v==current()){address.setText(u);status.setText(v.getTitle()==null?"Sayfa hazır":v.getTitle());} saveHistory(u);}
            public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){String sc=r.getUrl().getScheme(); if("http".equals(sc)||"https".equals(sc))return false; try{startActivity(new Intent(Intent.ACTION_VIEW,r.getUrl()));}catch(Exception ignored){} return true;}
        });
        w.setWebChromeClient(new WebChromeClient(){public void onProgressChanged(WebView v,int p){if(v==current())status.setText("Yükleniyor… "+p+"%");}});
        w.setDownloadListener((u,ua,cd,mt,len)->{try{DownloadManager.Request r=new DownloadManager.Request(Uri.parse(u));r.setTitle(URLUtil.guessFileName(u,cd,mt));r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(u,cd,mt));((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);Toast.makeText(this,"İndirme başladı",0).show();}catch(Exception e){Toast.makeText(this,"İndirme başlatılamadı",0).show();}});
        tabs.add(w); active=tabs.size()-1; showActive(); w.loadUrl(url);
    }
    private void showActive(){area.removeAllViews();WebView w=current();if(w!=null){if(w.getParent()!=null)((android.view.ViewGroup)w.getParent()).removeView(w);area.addView(w,new LinearLayout.LayoutParams(-1,-1));address.setText(w.getUrl()==null?"":w.getUrl());}}
    private void navigate(){String q=address.getText().toString().trim();if(q.isEmpty())return;String u;if(q.matches("(?i)^[a-z][a-z0-9+.-]*://.*"))u=q;else if(q.contains(" ")||!q.contains("."))u="https://www.google.com/search?q="+Uri.encode(q);else u="https://"+q;((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);current().loadUrl(u);}
    private void home(){current().loadDataWithBaseURL("https://nexa.local/", "<meta name='viewport' content='width=device-width,initial-scale=1'><body style='margin:0;background:#0d0f18;color:#f1f2f8;font-family:Arial;display:grid;place-items:center;min-height:100vh'><main style='width:85%;text-align:center'><h1 style='font-size:44px;letter-spacing:5px'>NEXA</h1><p style='color:#9da3b8'>Keşfetmeye başla</p><form action='https://www.google.com/search'><input name='q' placeholder='İnternette ara' style='box-sizing:border-box;width:100%;padding:16px;border:0;border-radius:18px;background:#242941;color:white;font-size:16px'><button style='margin:14px;padding:13px 25px;border:0;border-radius:20px;background:#8e66ff;color:white'>Google’da ara</button></form></main></body>","text/html","UTF-8",null);address.setText("");status.setText("NEXA ana sayfa");}
    private void showTabs(){String[] a=new String[tabs.size()];for(int i=0;i<tabs.size();i++){String t=tabs.get(i).getTitle();a[i]=(i==active?"● ":"")+(t==null||t.isEmpty()?"Yeni sekme":t);}new AlertDialog.Builder(this).setTitle("Sekmeler").setItems(a,(d,n)->{active=n;showActive();}).setNeutralButton("Yeni sekme",(d,n)->newTab("https://www.google.com")).setNegativeButton("Kapat",(d,n)->closeTab()).show();}
    private void closeTab(){if(tabs.size()<=1){current().loadUrl("https://www.google.com");return;}WebView w=tabs.remove(active);w.destroy();active=Math.max(0,active-1);showActive();}
    private void menu(){PopupMenu m=new PopupMenu(this,bottom.getChildAt(5));for(String s:new String[]{"Yer imlerine ekle","Yer imleri","Geçmiş","Sayfayı yenile","İndirmeler","Hakkında"})m.getMenu().add(s);m.setOnMenuItemClickListener(i->{String s=i.getTitle().toString();if(s.equals("Yer imlerine ekle")){WebView w=current();if(w.getUrl()!=null){add("bookmarks",(w.getTitle()==null?w.getUrl():w.getTitle())+"\n"+w.getUrl());Toast.makeText(this,"Kaydedildi",0).show();}}else if(s.equals("Yer imleri"))saved("bookmarks","Yer imleri");else if(s.equals("Geçmiş"))saved("history","Geçmiş");else if(s.equals("Sayfayı yenile"))current().reload();else if(s.equals("İndirmeler")){try{startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));}catch(Exception ignored){}}else new AlertDialog.Builder(this).setTitle("NEXA Browser").setMessage("Android System WebView tabanlı hafif tarayıcı.").setPositiveButton("Tamam",null).show();return true;});m.show();}
    private void add(String k,String v){HashSet<String>s=new HashSet<>(prefs.getStringSet(k,new HashSet<>()));s.remove(v);s.add(v);while(s.size()>100)s.remove(s.iterator().next());prefs.edit().putStringSet(k,s).apply();}
    private void saveHistory(String u){if(u!=null&&!u.startsWith("data:"))add("history",u);}
    private void saved(String k,String title){ArrayList<String>a=new ArrayList<>(prefs.getStringSet(k,new HashSet<>()));Collections.reverse(a);if(a.isEmpty()){Toast.makeText(this,"Henüz kayıt yok",0).show();return;}String[] items=a.toArray(new String[0]);new AlertDialog.Builder(this).setTitle(title).setItems(items,(d,n)->{String v=items[n];current().loadUrl(v.contains("\n")?v.substring(v.lastIndexOf('\n')+1):v);}).setNegativeButton("Kapat",null).show();}
    public void onBackPressed(){WebView w=current();if(w!=null&&w.canGoBack())w.goBack();else super.onBackPressed();}
    protected void onDestroy(){for(WebView w:tabs)w.destroy();tabs.clear();super.onDestroy();}
}