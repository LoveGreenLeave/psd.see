package com.maimiao.psd_see.activity_fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.maimiao.psd_see.IConstants;
import com.maimiao.psd_see.R;
import com.maimiao.psd_see.common.BaseFragment;
import com.maimiao.psd_see.common.LogTrace;
import com.maimiao.psd_see.common.ObjectAdapterList;
import com.maimiao.psd_see.common.PSDReaderEx;
import com.maimiao.psd_see.kernel.DataModel;
import com.maimiao.psd_see.kernel.KernelException;
import com.maimiao.psd_see.kernel.KernelManager;
import com.maimiao.psd_see.listener.IAdapterObjectList;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FragmentHome extends BaseFragment implements View.OnClickListener
        , IAdapterObjectList, FragmentScanSdcard.IFileSelected, AdapterView.OnItemClickListener {
    private final int ITME_TYPE_FILE = 0;
    private final int ITME_TYPE_LAST = 1;

    private View mRootView;
    private boolean mDeleting; //????????????????????????
    private volatile int mFoundNum;
    private DisplayImageOptions mImageOptions;

    public static FragmentHome create(){
        FragmentHome fragment = new FragmentHome();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_home, container, false);

        mRootView.findViewById(R.id.ID_BTN_MENU).setOnClickListener(this);
        mRootView.findViewById(R.id.ID_TXT_ABOUT).setOnClickListener(this);
        mRootView.findViewById(R.id.ID_LAYOUT_MENU).setOnClickListener(this);
        mRootView.findViewById(R.id.ID_TXT_OPEN_PHOTOS).setOnClickListener(this);
        mRootView.findViewById(R.id.ID_TXT_OPEN_SDCARD).setOnClickListener(this);
        mRootView.findViewById(R.id.ID_BTN_REMOVE).setOnClickListener(this);
        mRootView.findViewById(R.id.ID_TXT_FIND_PSD).setOnClickListener(this);
        mRootView.findViewById(R.id.ID_TXT_HELP).setOnClickListener(this);
        mRootView.findViewById(R.id.ID_BTN_SYNC).setOnClickListener(this);

        mRootView.findViewById(R.id.ID_PRGRSS_LOADING).setVisibility(View.GONE);

        mImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.default_image)
                .showImageForEmptyUri(R.mipmap.default_image)
                .showImageOnFail(R.mipmap.default_image).cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY).build();

        //????????????
        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        AdView adView = (AdView) (mRootView.findViewById(R.id.ID_AD_VIEW));
        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("42491D5B367196505D015CC00EEE0263")
                .addTestDevice("2D7EE1868002F161C8E9696732AE8379")
                .addTestDevice("9AA4A76DA7F07F520E249F107053CF29")
                .build();
        // Start loading the ad in the background.
        adView.loadAd(adRequest);

        ListView listView = (ListView)(mRootView.findViewById(R.id.ID_LIST_VIEW));
        ObjectAdapterList adapterList = new ObjectAdapterList(this, listView);
        listView.setAdapter(adapterList);
        listView.setOnItemClickListener(this);

        //BmobUpdateAgent.update(getActivity()); //????????????

        addFileList();

        return mRootView;
    }

    /**
     * ??????????????????
     */
    private void addSimpleFiles(DataModel.FileItemEx fileItem){
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;

            if (IConstants.SIMPLE_PSD.equals(fileItem.filePath)){
                BitmapFactory.decodeResource(getResources(), R.mipmap.simple_psd, opts);
                fileItem.width = opts.outWidth;
                fileItem.height = opts.outHeight;
                fileItem.fileSize = 8007742;
                fileItem.psdColorMode = PSDReaderEx.COLOR_MODE_RGB;
            }
            else {
                BitmapFactory.decodeResource(getResources(), R.mipmap.simple, opts);
                fileItem.width = opts.outWidth;
                fileItem.height = opts.outHeight;
                fileItem.fileSize = opts.outWidth * opts.outHeight * 4;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ??????psd??????
     * @param filePath
     */
    private DataModel.FileItemEx dealPsdFile(String filePath) throws Exception {
        DataModel.FileItemEx fileItem = null;
        //??????????????????,??????????????????
        if (DataModel.FileItemEx.isFileIn(filePath)){
            //buildPSDSmallImage(filePath); //???????????????
            throw new Exception("file has in");
        }

        fileItem = new DataModel.FileItemEx();
        fileItem.filePath = filePath;

        dealPsdFile(fileItem);
        fileItem.save();

        return fileItem;
    }

    /**
     * ??????psd??????????????????
     * @param psdPath
     */
    private void buildPSDSmallImage(String psdPath) throws Exception {
        DataModel.FileItemEx fileItem = DataModel.FileItemEx.getFileByPath(psdPath);
        buildPSDSmallImage(fileItem);
    }

    /**
     * ??????psd??????????????????
     * @param fileItem
     */
    private void buildPSDSmallImage(DataModel.FileItemEx fileItem) throws Exception {
        String bigImgFilePath = KernelManager.getSdcardDir()
                + IConstants.SAVE_PATH + KernelManager.getStringMD5(fileItem.filePath);
        File file = new File(bigImgFilePath);
        if (false == file.exists()) {
            //?????????????????????????????????????????????
            throw new Exception("file not exist");
        }

        String smallImagePath = bigImgFilePath + "_small";
        file = new File(smallImagePath);
        if (file.exists()) {
            //??????????????????????????????
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bigImgFilePath, options);

        if (options.outWidth * options.outHeight <= 200 * 200) {
            //????????????,????????????
            return;
        }

        options.inSampleSize = KernelManager.calculateInSampleSize(options, 200, 200);
        options.inJustDecodeBounds = false;

        Bitmap smallBitmap = BitmapFactory.decodeFile(bigImgFilePath, options);
        if (null == smallBitmap) {
            return;
        }

        LogTrace.log("small image:" + smallImagePath + ", size:" + smallBitmap.getByteCount()
        +", width:" + smallBitmap.getWidth() + ", height:" + smallBitmap.getHeight());
        FileOutputStream writeStream = new FileOutputStream(smallImagePath, false);
        smallBitmap.compress(Bitmap.CompressFormat.PNG, 40, writeStream);
        writeStream.flush();
        writeStream.close();
        smallBitmap.recycle();
        smallBitmap = null;

    }

    /**
     * ??????psd??????
     * @param fileItem
     */
    private void dealPsdFile(DataModel.FileItemEx fileItem) throws IOException, KernelException {
        boolean needCreateImage = false;

        String imgFilePath = KernelManager.getSdcardDir()
                + IConstants.SAVE_PATH + KernelManager.getStringMD5(fileItem.filePath);

        File file = new File(imgFilePath);
        if (false == file.exists() || fileItem.height <= 0 || fileItem.width <= 0
                || fileItem.fileSize <= 0) {
            //?????????????????????????????????????????????
            needCreateImage = true;
        }

        if (needCreateImage){
            /**
             if (IConstants.SIMPLE_PSD.equals(fileItem.filePath)) {
             //??????asset??????simple.psd?????????sdcard???
             psdPath = KernelManager.getSdcardDir() + IConstants.SAVE_PATH + "simple.psd";
             FileOutputStream writeStream = new FileOutputStream(psdPath, false);
             byte[] buffer = new byte[1024];
             int readed = 0;
             InputStream fileStream = getActivity().getAssets().open("simple.psd");
             while ((readed = fileStream.read(buffer)) > 0) {
             writeStream.write(buffer, 0, readed);
             }
             writeStream.close();
             fileStream.close();
             }
             else{
             psdPath = fileItem.filePath;
             }
             **/

            PSDReaderEx psdReader = new PSDReaderEx(); //new PSDReaderEx(fileItem.filePath);
            Bitmap bitmap = psdReader.getImage(fileItem.filePath);

            FileOutputStream writeStream = new FileOutputStream(imgFilePath, false);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, writeStream);
            writeStream.flush();
            writeStream.close();
            bitmap.recycle();
            bitmap = null;

            fileItem.fileSize = psdReader.getFileSize();
            fileItem.psdColorMode = psdReader.getColorMode();
            fileItem.width = psdReader.getWidth();
            fileItem.height = psdReader.getHeight();

            psdReader = null;
        }
    }

    /**
     * ???????????????????????????
     * @param fileItem
     */
    private void dealImageFile(DataModel.FileItemEx fileItem){
        File file = new File(fileItem.filePath);
        if (false == file.exists() || fileItem.fileSize <= 0
                || fileItem.height <= 0 || fileItem.width <= 0){
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileItem.filePath, opts);
            fileItem.height = opts.outHeight;
            fileItem.width = opts.outWidth;
            fileItem.fileSize = file.length();
        }
    }

    private void addFileList() {

        new AsyncTask<Void, DataModel.FileItemEx, Void>() {
            @Override
            protected Void doInBackground(Void ... param) {
                List<DataModel.FileItemEx> list = DataModel.FileItemEx.getFileList();
                if (null == list){
                    list = new ArrayList<DataModel.FileItemEx>();
                }
                DataModel.FileItemEx fileItem = null;

                fileItem = new DataModel.FileItemEx();
                fileItem.filePath = IConstants.SIMPLE_PSD;
                addSimpleFiles(fileItem);
                publishProgress(fileItem);

                fileItem = new DataModel.FileItemEx();
                fileItem.filePath = IConstants.SIMPLE_PNG;
                addSimpleFiles(fileItem);
                publishProgress(fileItem);

                File file = null;
                String filePath = "";
                String imagePath;
                for (DataModel.FileItemEx item : list){
                    try {
                        filePath = item.filePath.toLowerCase();
                        if (filePath.endsWith(".psd")) {
                            //psd??????
                            dealPsdFile(item);
                        } else {
                            //?????????????????????
                            dealImageFile(item);
                        }

                        item.save();
                        publishProgress(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(DataModel.FileItemEx... values) {
                ListView listView = (ListView)(mRootView.findViewById(R.id.ID_LIST_VIEW));
                ObjectAdapterList adapterList = (ObjectAdapterList)(listView.getAdapter());
                adapterList.addItem(values[0]);
                adapterList.notifyDataSetChanged();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //???????????????????????????
                ListView listView = (ListView)(mRootView.findViewById(R.id.ID_LIST_VIEW));
                ObjectAdapterList adapterList = (ObjectAdapterList)(listView.getAdapter());
                adapterList.addItem(new DataModel.ListItem(ITME_TYPE_LAST, 0));
                adapterList.notifyDataSetChanged();
            }
        }.execute();
    }

    /**
     * ??????????????????
     * @param item
     * @param convertView
     */
    private void setFileItem(DataModel.FileItemEx item, View convertView){
        ImageView imageView = (ImageView)(convertView.findViewById(R.id.ID_IMAGE));
        ImageView imgCheck = (ImageView)(convertView.findViewById(R.id.ID_CHECK_SEL));
        imgCheck.setVisibility(View.VISIBLE);

        if (IConstants.SIMPLE_PNG.equals(item.filePath)){
            ImageLoader.getInstance().displayImage(IConstants.SIMPLE_PNG, imageView, mImageOptions);
            imgCheck.setVisibility(View.GONE);
        } else if(IConstants.SIMPLE_PSD.equals(item.filePath)){
            ImageLoader.getInstance().displayImage("assets://simple_psd.jpg", imageView, mImageOptions);
            imgCheck.setVisibility(View.GONE);
        } else if (item.filePath.toLowerCase().endsWith(".psd")) {
            String imgFilePath = KernelManager.getSdcardDir()
                    + IConstants.SAVE_PATH + KernelManager.getStringMD5(item.filePath);
            /**
            String smallImagePath = imgFilePath + "_small";
            file = new File(smallImagePath);
            if (file.exists()) {
                imgFilePath = smallImagePath;
            }
            LogTrace.log("will load file:" + imgFilePath);
            **/
            ImageLoader.getInstance().displayImage("file://" + imgFilePath, imageView, mImageOptions);
        } else{
            ImageLoader.getInstance().displayImage("file://" + item.filePath, imageView, mImageOptions);
        }

        TextView txtView = (TextView)(convertView.findViewById(R.id.ID_TXT_NAME));
        txtView.setText(KernelManager.getFileNameByPath(item.filePath));

        /**
        View viewItem = convertView.findViewById(R.id.ID_LAYOUT_ITEM);
        viewItem.setTag(item);
        viewItem.setOnClickListener(this);
        **/

        StringBuilder fileInfo = new StringBuilder();
        if (item.filePath.toLowerCase().endsWith(".psd")) {
            fileInfo.append(PSDReaderEx.getColorMode(item.psdColorMode)).append(' ');
        }
        fileInfo.append(item.width).append('*').append(item.height)
                .append(' ');
        final int endSpace = fileInfo.length();
        fileInfo.append(KernelManager.getFileSizeString(item.fileSize));

        SpannableString spanString = new SpannableString(fileInfo.toString());
        spanString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.font_red))
                , 0, endSpace
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtView = (TextView)(convertView.findViewById(R.id.ID_TXT_INFO));
        txtView.setText(spanString);

        txtView = (TextView)(convertView.findViewById(R.id.ID_TXT_PATH));
        txtView.setText(item.filePath);

        if (mDeleting){
            //??????????????????
            if (item.isSelected) {
                imgCheck.setImageResource(R.mipmap.check_sel);
            } else {
                imgCheck.setImageResource(R.mipmap.check_unsel);
            }
        } else{
            imgCheck.setVisibility(View.GONE);
        }
    }

    /**
     * ??????sd?????????psd??????
     */
    private void scanSDCardPSDs(){
        mRootView.findViewById(R.id.ID_TXT_SCAN_INFO).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.ID_PRGRSS_LOADING).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.ID_LAYOUT_MENU).setVisibility(View.GONE);
        mRootView.findViewById(R.id.ID_BTN_MENU).setEnabled(false);
        mRootView.findViewById(R.id.ID_BTN_REMOVE).setEnabled(false);
        mFoundNum = 0;

        new AsyncTask<Void, Object, Void>() {
            @Override
            protected Void doInBackground(Void ... param) {
                try {
                    File sdcard = new File(KernelManager.getSdcardDir());
                    if (false == sdcard.exists()) {
                        //sd????????????
                        return null;
                    }
                    String myPath = IConstants.SAVE_PATH.substring(1, IConstants.SAVE_PATH.length() -1);
                    myPath = myPath.toLowerCase();
                    LinkedList<File> listDir = new LinkedList<File>();
                    listDir.add(sdcard);

                    File tempFile = null;
                    File[] files = null;
                    while (false == listDir.isEmpty()){

                        tempFile = listDir.removeFirst();
                        if (tempFile.getAbsolutePath().toLowerCase().contains(myPath)){
                            continue;
                        }
                        files = tempFile.listFiles();
                        if (null == files){
                            continue;
                        }

                        publishProgress(tempFile.getAbsolutePath());

                        for (File item : files){
                            if (item.isDirectory()){
                                listDir.add(item); //???????????????
                            } else {
                                //????????????
                                try {
                                    String filePath = item.getAbsolutePath().toLowerCase();
                                    if (filePath.endsWith(".psd")) {
                                        DataModel.FileItemEx fileItem =
                                                dealPsdFile(item.getAbsolutePath());
                                        if (null == fileItem) {
                                            throw new Exception("file item null");
                                        }
                                        publishProgress(fileItem);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Object... values) {
                ListView listView = (ListView)(mRootView.findViewById(R.id.ID_LIST_VIEW));
                ObjectAdapterList adapterList = (ObjectAdapterList)(listView.getAdapter());
                if (values[0] instanceof DataModel.FileItemEx) {
                    mFoundNum++;
                    adapterList.addItem(adapterList.getCount() - 1, values[0]);
                    adapterList.notifyDataSetChanged();
                } else if (values[0] instanceof String){
                    TextView txtView = (TextView)(mRootView.findViewById(R.id.ID_TXT_SCAN_INFO));
                    txtView.setText((String)values[0]);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //???????????????????????????
                /**
                ListView listView = (ListView)(mRootView.findViewById(R.id.ID_LIST_VIEW));
                ObjectAdapterList adapterList = (ObjectAdapterList)(listView.getAdapter());
                adapterList.notifyDataSetChanged();
                **/

                try {
                    mRootView.findViewById(R.id.ID_TXT_SCAN_INFO).setVisibility(View.GONE);
                    mRootView.findViewById(R.id.ID_PRGRSS_LOADING).setVisibility(View.GONE);
                    mRootView.findViewById(R.id.ID_BTN_MENU).setEnabled(true);
                    mRootView.findViewById(R.id.ID_BTN_REMOVE).setEnabled(true);

                    Toast.makeText(getActivity(), new StringBuilder().append(getString(R.string.found_1))
                                    .append(mFoundNum).append(getString(R.string.found_2)).toString()
                            , Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * ????????????
     * @param dataList
     */
    private void deleteFiles(List<Object> dataList){
        DataModel.FileItemEx fileItem = null;
        File file = null;
        Object objItem = null;

        for (int index = 0; index < dataList.size(); ){
            objItem = dataList.get(index);
            if (objItem instanceof DataModel.FileItemEx){
                fileItem = (DataModel.FileItemEx)objItem;
                if (fileItem.isSelected){
                    file = new File(fileItem.filePath);
                    file.delete();

                    if (fileItem.filePath.toLowerCase().endsWith(".psd")) {
                        file = new File(KernelManager.getSdcardDir()
                                + IConstants.SAVE_PATH + KernelManager.getStringMD5(fileItem.filePath));
                        file.delete();
                    }

                    DataModel.FileItemEx.delete(fileItem);
                    dataList.remove(index);
                } else {
                    index++;
                }
            } else{
                index++;
            }
        }
    }

    private void openSystemAlbum() {
        try {
            mRootView.findViewById(R.id.ID_LAYOUT_MENU).setVisibility(View.GONE);
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            //????????????????????????????????????Action
            if (Build.VERSION.SDK_INT <19) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
            startActivityForResult(intent, IConstants.REQ_OPENABLE);
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.open_album_fail, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * ???????????????????????????
     * @param intent
     */
    public void selectPhoto(Intent intent) throws IOException {
        Uri imageUri = intent.getData();
        InputStream inStream = getActivity().getContentResolver().openInputStream(imageUri);
        byte[] buffer = new byte[2048];
        int readNum = 0;
        long totalLen = 0;
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String filePath = KernelManager.getSdcardDir()
                + IConstants.SAVE_PATH + dateFormat.format(new Date());

        FileOutputStream outputStream = new FileOutputStream(filePath, false);
        while ((readNum = inStream.read(buffer)) > 0){
            outputStream.write(buffer, 0, readNum);
            totalLen += readNum;
        }
        inStream.close();
        outputStream.close();

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);

        DataModel.FileItemEx fileItem = new DataModel.FileItemEx();
        fileItem.filePath = filePath;
        fileItem.fileSize = opts.outHeight * opts.outWidth * 4;
        fileItem.height = opts.outHeight;
        fileItem.width = opts.outWidth;

        fileItem.save();

        ListView listView = (ListView)(mRootView.findViewById(R.id.ID_LIST_VIEW));
        ObjectAdapterList adapterList = (ObjectAdapterList)(listView.getAdapter());
        adapterList.addItem(adapterList.getCount() -1, fileItem);
        adapterList.notifyDataSetChanged();

        //???????????????
        getFragmentManager().beginTransaction().add(R.id.container
                , FragmentPSDDrawView.create(fileItem))
                .addToBackStack(IConstants.FRAGMENT_MAIN_THREAD).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        AdView adView = (AdView) (mRootView.findViewById(R.id.ID_AD_VIEW));
        adView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        AdView adView = (AdView) (mRootView.findViewById(R.id.ID_AD_VIEW));
        adView.pause();
    }

    @Override
    public void onDestroyView() {
        AdView adView = (AdView) (mRootView.findViewById(R.id.ID_AD_VIEW));
        adView.destroy();

        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (R.id.ID_TXT_FIND_PSD == v.getId()){
            //??????psd??????
            scanSDCardPSDs();
        }
        else if(R.id.ID_BTN_MENU == v.getId()){
            int visible = mRootView.findViewById(R.id.ID_LAYOUT_MENU).getVisibility();
            if (View.VISIBLE == visible){
                visible = View.GONE;
            }
            else {
                visible = View.VISIBLE;
            }
            mRootView.findViewById(R.id.ID_LAYOUT_MENU).setVisibility(visible);
        }
        else if (R.id.ID_LAYOUT_MENU == v.getId()){
            v.setVisibility(View.GONE);
        } /**
        else if (R.id.ID_LAYOUT_ITEM == v.getId()){
            DataModel.FileItem fileItem = (DataModel.FileItem)v.getTag();
            getFragmentManager().beginTransaction().add(R.id.container
                    , FragmentPSDDrawView.create(fileItem))
                    .addToBackStack(IConstants.FRAGMENT_MAIN_THREAD).commit();
        } **/
        else if (R.id.ID_TXT_ABOUT == v.getId()){
            getFragmentManager().beginTransaction().add(R.id.container
                    , FragmentAbout.create())
                    .addToBackStack(IConstants.FRAGMENT_MAIN_THREAD).commit();
            mRootView.findViewById(R.id.ID_LAYOUT_MENU).setVisibility(View.GONE);
        }
        else if(R.id.ID_TXT_OPEN_PHOTOS == v.getId()){
            /**
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 0);
            **/
            openSystemAlbum();
        }
        else if (R.id.ID_TXT_OPEN_SDCARD == v.getId()){
            //??????sdcard
            mRootView.findViewById(R.id.ID_LAYOUT_MENU).setVisibility(View.GONE);
            getFragmentManager().beginTransaction().add(R.id.container
                    , FragmentScanSdcard.create(this))
                    .addToBackStack(IConstants.FRAGMENT_MAIN_THREAD).commit();
        } else if(R.id.ID_TXT_HELP == v.getId()){
            //?????????????????????
            mRootView.findViewById(R.id.ID_LAYOUT_MENU).setVisibility(View.GONE);
            getFragmentManager().beginTransaction().add(R.id.container
                    , FragmentHelp.create())
                    .addToBackStack(IConstants.FRAGMENT_MAIN_THREAD).commit();
        }
        else if (R.id.ID_CHECK_SEL == v.getId()){
            //????????????
            CheckBox checkBox = (CheckBox)v;
            DataModel.FileItemEx fileItem = (DataModel.FileItemEx)(checkBox.getTag());
            fileItem.isSelected = checkBox.isChecked();
        } else if (R.id.ID_BTN_REMOVE == v.getId()){
            mDeleting = !mDeleting;
            ListView listView = (ListView)(mRootView.findViewById(R.id.ID_LIST_VIEW));
            ObjectAdapterList adapterList = (ObjectAdapterList)(listView.getAdapter());
            if (mDeleting){
                v.setBackgroundResource(R.drawable.btn_remove_2_selector);
            } else {
                v.setBackgroundResource(R.drawable.btn_remove_1_selector);
                deleteFiles(adapterList.getDataList());
            }
            adapterList.notifyDataSetChanged();
        } else if (R.id.ID_BTN_SYNC == v.getId()){
            //photoshop??????
            getFragmentManager().beginTransaction().add(R.id.container
                    , FragmentConnections.create())
                    .addToBackStack(IConstants.FRAGMENT_MAIN_THREAD).commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogTrace.log("requestCode:" + requestCode + ", resultCode:" + resultCode);
        try {
            if (IConstants.REQ_OPENABLE == requestCode && Activity.RESULT_OK == resultCode) {
                //??????????????????
                selectPhoto(data);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onItemChanged(int position, View convertView, ViewGroup parent
            , ObjectAdapterList adapter) {
        try {
            Object item = adapter.getItem(position);
            if (item instanceof DataModel.ListItem && null == convertView) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.item_last_blank, null, false);
            } else if (item instanceof DataModel.FileItemEx) {
                if (null == convertView) {
                    convertView = LayoutInflater.from(getActivity()).inflate(
                            R.layout.item_file, null, false);
                }
                setFileItem((DataModel.FileItemEx) item, convertView);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public int onAdapterItemViewType(int position, ObjectAdapterList adapter) {
        Object item = adapter.getItem(position);
        if (item instanceof DataModel.FileItemEx){
            return ITME_TYPE_FILE;
        }
        return ITME_TYPE_LAST;
    }

    @Override
    public long onAdapterItemId(int position, ObjectAdapterList adapter) {
        return 0;
    }

    @Override
    public void onSelectedFile(DataModel.FileItemEx fileItem) {
        fileItem.save();

        boolean hasIn = false;
        ListView listView = (ListView)(mRootView.findViewById(R.id.ID_LIST_VIEW));
        ObjectAdapterList adapterList = (ObjectAdapterList)(listView.getAdapter());
        for (Object objItem : adapterList.getDataList()) {
            if (objItem instanceof DataModel.FileItemEx) {
                DataModel.FileItemEx item = (DataModel.FileItemEx)objItem;
                if (item.filePath.equals(fileItem.filePath)) {
                    hasIn = true;
                    break;
                }
            }
        }

        if (false == hasIn) {
            //?????????????????????,??????
            adapterList.addItem(2, fileItem);
            adapterList.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Object objItem = parent.getItemAtPosition(position);
        if (false == objItem instanceof DataModel.FileItemEx) {
            return;
        }
        DataModel.FileItemEx fileItem = (DataModel.FileItemEx)(objItem);
        if (false == mDeleting) {
            getFragmentManager().beginTransaction().add(R.id.container
                    , FragmentPSDDrawView.create(fileItem))
                    .addToBackStack(IConstants.FRAGMENT_MAIN_THREAD).commit();
        } else if (false == fileItem.filePath.startsWith("assets://")){
            fileItem.isSelected = !(fileItem.isSelected);
            ImageView imgCheck = (ImageView) (view.findViewById(R.id.ID_CHECK_SEL));
            if (fileItem.isSelected) {
                imgCheck.setImageResource(R.mipmap.check_sel);
            } else {
                imgCheck.setImageResource(R.mipmap.check_unsel);
            }
        }
    }
}
