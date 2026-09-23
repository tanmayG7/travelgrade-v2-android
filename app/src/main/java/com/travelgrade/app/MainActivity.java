package com.travelgrade.app;

import android.app.*;
import android.os.*;
import android.provider.MediaStore;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    enum Preset { TRAVEL_NATURAL, ALPINE_BLUE, MOUNTAIN_DRAMA, WARM_LANDSCAPE }
    ImageView image; Bitmap original, graded; SeekBar intensity; TextView status, presetLabel; boolean showingAfter=true;
    ArrayList<Uri> selected = new ArrayList<>(); Preset preset=Preset.TRAVEL_NATURAL;
    int dp(float n){ return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
    TextView tv(String s,float size){ TextView t=new TextView(this); t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(size); return t; }
    Button btn(String s){ Button b=new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setAllCaps(false); return b; }
    @Override public void onCreate(Bundle b){ super.onCreate(b); build(); }

    void build(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(14),dp(12),dp(14),dp(8)); root.setBackgroundColor(Color.rgb(14,18,22));
        TextView title=tv("Travel Grade",24); title.setTypeface(null,1); root.addView(title,new LinearLayout.LayoutParams(-1,dp(36)));
        TextView sub=tv("Natural travel presets • local processing • batch export",12); sub.setTextColor(Color.rgb(165,176,188)); root.addView(sub,new LinearLayout.LayoutParams(-1,dp(27)));

        FrameLayout frame=new FrameLayout(this); frame.setBackgroundColor(Color.BLACK); image=new ImageView(this); image.setScaleType(ImageView.ScaleType.FIT_CENTER); frame.addView(image,new FrameLayout.LayoutParams(-1,-1));
        TextView badge=tv("BEFORE / AFTER",10); badge.setPadding(dp(8),dp(4),dp(8),dp(4)); badge.setBackgroundColor(Color.argb(170,0,0,0)); FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.END); bp.setMargins(0,dp(8),dp(8),0); frame.addView(badge,bp);
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,0,1); fp.setMargins(0,dp(6),0,dp(6)); root.addView(frame,fp);

        LinearLayout controls=new LinearLayout(this); controls.setOrientation(LinearLayout.VERTICAL); controls.setBackgroundColor(Color.rgb(23,29,35)); controls.setPadding(dp(10),dp(7),dp(10),dp(5));
        presetLabel=tv("Preset  •  Travel Natural",14); presetLabel.setTypeface(null,1); controls.addView(presetLabel,new LinearLayout.LayoutParams(-1,dp(25)));
        HorizontalScrollView hsv=new HorizontalScrollView(this); LinearLayout chips=new LinearLayout(this); chips.setOrientation(LinearLayout.HORIZONTAL);
        String[] names={"Travel Natural","Alpine Blue","Mountain Drama","Warm Landscape"};
        for(int i=0;i<names.length;i++){ final int k=i; Button c=btn(names[i]); c.setTextSize(12); c.setPadding(dp(8),0,dp(8),0); chips.addView(c,new LinearLayout.LayoutParams(dp(128),dp(40))); c.setOnClickListener(v->{preset=Preset.values()[k]; presetLabel.setText("Preset  •  "+names[k]); refreshGrade();}); }
        hsv.addView(chips); controls.addView(hsv,new LinearLayout.LayoutParams(-1,dp(43)));
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); TextView il=tv("Intensity",12); row.addView(il,new LinearLayout.LayoutParams(dp(58),-2)); intensity=new SeekBar(this); intensity.setMax(150); intensity.setProgress(100); row.addView(intensity,new LinearLayout.LayoutParams(0,dp(38),1)); controls.addView(row);
        intensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar s,int p,boolean f){ refreshGrade(); } public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){} });
        root.addView(controls,new LinearLayout.LayoutParams(-1,dp(125)));

        LinearLayout buttons=new LinearLayout(this); buttons.setGravity(Gravity.CENTER); Button pick=btn("Select photos"), compare=btn("Before / After"), save=btn("Export all"); buttons.addView(pick,new LinearLayout.LayoutParams(0,dp(50),1)); buttons.addView(compare,new LinearLayout.LayoutParams(0,dp(50),1)); buttons.addView(save,new LinearLayout.LayoutParams(0,dp(50),1)); root.addView(buttons);
        status=tv("Select photos to begin",11); status.setTextColor(Color.rgb(165,176,188)); root.addView(status,new LinearLayout.LayoutParams(-1,dp(27)));
        pick.setOnClickListener(v->pickImages()); compare.setOnClickListener(v->{ if(original!=null){showingAfter=!showingAfter; image.setImageBitmap(showingAfter?graded:original);} }); save.setOnClickListener(v->exportAll());
        setContentView(root);
    }

    void pickImages(){
        Intent i;
        if(Build.VERSION.SDK_INT>=33){ i=new Intent(MediaStore.ACTION_PICK_IMAGES); i.setType("image/*"); i.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX,50); i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true); }
        else { i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("image/*"); i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true); i.addCategory(Intent.CATEGORY_OPENABLE); }
        startActivityForResult(i,41);
    }
    @Override protected void onActivityResult(int r,int c,Intent d){ super.onActivityResult(r,c,d); if(r!=41||c!=RESULT_OK||d==null)return; selected.clear(); if(d.getClipData()!=null){for(int x=0;x<d.getClipData().getItemCount();x++)selected.add(d.getClipData().getItemAt(x).getUri());} else if(d.getData()!=null) selected.add(d.getData()); if(!selected.isEmpty()) load(selected.get(0)); status.setText(selected.size()+" photo"+(selected.size()==1?"":"s")+" selected • previewing first"); }
    void load(Uri u){ try{ BitmapFactory.Options o=new BitmapFactory.Options(); o.inPreferredConfig=Bitmap.Config.ARGB_8888; InputStream in=getContentResolver().openInputStream(u); original=BitmapFactory.decodeStream(in,null,o); in.close(); if(original==null)throw new Exception(); showingAfter=true; refreshGrade(); }catch(Exception e){Toast.makeText(this,"Could not read photo",Toast.LENGTH_SHORT).show();} }
    void refreshGrade(){ if(original==null)return; graded=grade(original,intensity.getProgress()/100f,preset); image.setImageBitmap(showingAfter?graded:original); }

    Bitmap grade(Bitmap src,float amount,Preset p){
        int w=src.getWidth(),h=src.getHeight(),max=2600; float scale=Math.min(1f,max/(float)Math.max(w,h)); int W=Math.max(1,(int)(w*scale)),H=Math.max(1,(int)(h*scale)); Bitmap in=src; if(W!=w||H!=h)in=Bitmap.createScaledBitmap(src,W,H,true);
        Bitmap out=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888); int[] px=new int[W*H]; in.getPixels(px,0,W,0,0,W,H); float a=Math.max(.15f,Math.min(1.5f,amount));
        for(int y=0;y<H;y++) for(int x=0;x<W;x++){
            int idx=y*W+x,c=px[idx]; float r=Color.red(c)/255f,g=Color.green(c)/255f,b=Color.blue(c)/255f; float lum=.2126f*r+.7152f*g+.0722f*b;
            float contrast=.07f, vib=.12f, warmth=0f, blue=0f, darkTop=.055f, vignette=.035f;
            if(p==Preset.ALPINE_BLUE){ contrast=.10f; vib=.16f; blue=.035f; darkTop=.08f; vignette=.045f; }
            if(p==Preset.MOUNTAIN_DRAMA){ contrast=.15f; vib=.13f; blue=.018f; darkTop=.07f; vignette=.06f; }
            if(p==Preset.WARM_LANDSCAPE){ contrast=.08f; vib=.15f; warmth=.028f; darkTop=.05f; vignette=.04f; }
            // neutralize cool grey cast, with restrained warmth in highlights
            float cool=Math.max(0,b-r)*0.035f*a; r+=cool*.75f; g+=cool*.15f; b-=cool*.22f; r+=warmth*a*(0.8f+0.2f*lum); g+=warmth*a*0.18f; b-=warmth*a*0.45f;
            // blue sky bias, without globally pushing shadows blue
            if(b>r*1.02f && b>g*.99f){ b+=blue*a; }
            // vibrance: muted colors get more lift than already-saturated colors
            float mx=Math.max(r,Math.max(g,b)),mn=Math.min(r,Math.min(g,b)),sat=mx-mn; float lift=(1f-sat)*vib*a; r+=(r-lum)*lift; g+=(g-lum)*lift; b+=(b-lum)*lift;
            float con=contrast*a; r=(r-.5f)*(1+con)+.5f; g=(g-.5f)*(1+con)+.5f; b=(b-.5f)*(1+con)+.5f;
            float ny=y/(float)Math.max(1,H-1); boolean sky=(b>r*1.025f && b>g*.98f && ny<.65f); if(sky){float top=1f-ny/.65f; float d=darkTop*top*top*a; r*=1-d;g*=1-d;b*=1-d;}
            float dx=(x-W/2f)/(W/2f),dy=(y-H/2f)/(H/2f); float edge=Math.max(0,(dx*dx+dy*dy)-.68f)/.32f; float vig=vignette*Math.min(1,edge)*a; r*=1-vig;g*=1-vig;b*=1-vig;
            // restrained highlight recovery: compress very bright pixels slightly
            if(lum>.84f){ float q=(lum-.84f)/.16f*.055f*a; r-=q;g-=q;b-=q; }
            px[idx]=Color.rgb(clamp(r*255),clamp(g*255),clamp(b*255));
        }
        out.setPixels(px,0,W,0,0,W,H); if(in!=src)in.recycle(); return out;
    }
    int clamp(float v){return (int)Math.max(0,Math.min(255,v));}

    void exportAll(){
        if(selected.isEmpty()){Toast.makeText(this,"Select photos first",Toast.LENGTH_SHORT).show();return;}
        status.setText("Exporting "+selected.size()+" photos…");
        new Thread(()->{
            int done=0; for(Uri u:selected){ try{ BitmapFactory.Options o=new BitmapFactory.Options(); o.inPreferredConfig=Bitmap.Config.ARGB_8888; InputStream in=getContentResolver().openInputStream(u); Bitmap src=BitmapFactory.decodeStream(in,null,o); in.close(); if(src==null)continue; Bitmap out=grade(src,intensity.getProgress()/100f,preset); saveBitmap(out); if(out!=src)out.recycle(); src.recycle(); done++; }catch(Exception ignored){} }
            final int d=done; runOnUiThread(()->{status.setText("Done • "+d+" photo"+(d==1?"":"s")+" saved to Pictures/Travel Grade"); Toast.makeText(this,"Export complete",Toast.LENGTH_SHORT).show();});
        }).start();
    }
    void saveBitmap(Bitmap b)throws Exception{
        String name="TravelGrade_"+System.currentTimeMillis()+".jpg"; ContentValues cv=new ContentValues(); cv.put(MediaStore.Images.Media.DISPLAY_NAME,name); cv.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg"); if(Build.VERSION.SDK_INT>=29)cv.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Travel Grade"); Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv); if(u==null)throw new IOException(); OutputStream os=getContentResolver().openOutputStream(u); b.compress(Bitmap.CompressFormat.JPEG,95,os); os.close();
    }
}
