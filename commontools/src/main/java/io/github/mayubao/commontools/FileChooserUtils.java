package io.github.mayubao.commontools;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

/**
 * Android File Chooser Util
 * Android 文件选择工具类
 *
 * This util class is to help you to pick the File from the system, such as audio, video, img.
 *
 * How to use FileChooserUtil:
 * 1.FileChooserUtil.pickFile(Activity activity, int type)
 *  or FileChooserUtil.pickFile(Fragment fragment, int type)
 *  pick the file from the Android System.
 --------------------------------------------------------------------------------------------------
        ... ...
        FileChooserUtil.pickFile(Activity activity, int type)  or
        FileChooserUtil.pickFile(Fragment fragment, int type)
        ... ...
 --------------------------------------------------------------------------------------------------
 * 2.Get the resouce path from the method
 *
 --------------------------------------------------------------------------------------------------
        ... ...
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            ... ...
            String filePath = FileChooserUtils.getFileResPath(requestCode, resultCode, data, this);
            ... ...
            super.onActivityResult(requestCode, resultCode, data);
        }
        ... ...
--------------------------------------------------------------------------------------------------
 *
 *
 * Created by mayubao on 2016/8/6.
 * Contact me 345269374@qq.com
 */
public class FileChooserUtils {

    public static final int INTENT_REQUEST_CODE_IMAGE   = 0X00000001;     //Request code for image
    public static final int INTENT_REQUEST_CODE_AUDIO   = 0X00000002;     //Request code for audio
    public static final int INTENT_REQUEST_CODE_VIDEO   = 0X00000003;     //Request code for video
    public static final int INTENT_REQUEST_CODE_DEFAULT = 0X00000004;     //Request code for default

    public static final int FILE_TYPE_IMAGE     = 1;    // file type image
    public static final int FILE_TYPE_AUDIO     = 2;    // file type audio
    public static final int FILE_TYPE_VIDEO     = 3;    // file type video
    public static final int FILE_TYPE_DEFAULT   = 4;    // file type ALL

    public static final String IMAGE = "image/*";   // file type image parameter
    public static final String AUDIO = "audio/*";   // file type audio parameter
    public static final String VIDEO = "video/*";   // file type video parameter
    public static final String DEFAULT = "*/*";   // file type ALL parameter

    /**
     * 根据指定类型获取指定类型文件
     * @param activity
     * @param type
     */
    public static void pickFile(Activity activity, int type){
        if(activity == null){
            throw new NullPointerException("activity not be null");
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        String fileTypeParam = DEFAULT;
        int requestCode = INTENT_REQUEST_CODE_DEFAULT;
        switch (type){
            case FILE_TYPE_IMAGE:
                fileTypeParam = IMAGE;
                requestCode = INTENT_REQUEST_CODE_IMAGE;
                break;
            case FILE_TYPE_AUDIO:
                fileTypeParam = AUDIO;
                requestCode = INTENT_REQUEST_CODE_AUDIO;
                break;
            case FILE_TYPE_VIDEO:
                fileTypeParam = VIDEO;
                requestCode = INTENT_REQUEST_CODE_VIDEO;
                break;
            default:
                fileTypeParam = DEFAULT;
                requestCode = INTENT_REQUEST_CODE_DEFAULT;
                break;
        }

        intent.setType(fileTypeParam);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 在Activity onActivityResult(int requestCode, int resultCode, Intent data) 方面里面获取文件的文件路径
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param activity
     * @return
     */
    public static String getFileResPath(int requestCode, int resultCode, Intent data, Activity activity){
        if(activity == null){
            throw new NullPointerException("activity not be null");
        }
        if(resultCode == Activity.RESULT_OK){//正常选择返回
            if(resultCode == Activity.RESULT_OK){//正常选择返回
                if(data != null && data.getData() != null){
                    Uri uri = data.getData();
                    String[] proj = {MediaStore.MediaColumns.DATA};
                    Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
                    try{
                        if(cursor != null){
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                            if ((cursor.getCount() > 0) && cursor.moveToFirst()) {
                                String path = cursor.getString(column_index);
                                return path;
                            }
                        }else{
                            //maybe user choose all file, if user choose all file type,  the path will be null, if the value is null, try get the Uri path
                            return data.getData().getPath();
                        }
                    }catch (Exception e){
                        //cursor.getColumnIndexOrThrow have exception
                    }finally {

                    }
                }
            }
        }
        return null;
    }



    /**
     * 根据指定类型获取指定类型文件
     * @param fragment
     * @param type
     */
    public static void pickFile(Fragment fragment, int type){
        if(fragment == null){
            throw new NullPointerException("fragment not be null");
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        String fileTypeParam = DEFAULT;
        int requestCode = INTENT_REQUEST_CODE_DEFAULT;
        switch (type){
            case FILE_TYPE_IMAGE:
                fileTypeParam = IMAGE;
                requestCode = INTENT_REQUEST_CODE_IMAGE;
                break;
            case FILE_TYPE_AUDIO:
                fileTypeParam = AUDIO;
                requestCode = INTENT_REQUEST_CODE_AUDIO;
                break;
            case FILE_TYPE_VIDEO:
                fileTypeParam = VIDEO;
                requestCode = INTENT_REQUEST_CODE_VIDEO;
                break;
            default:
                fileTypeParam = DEFAULT;
                requestCode = INTENT_REQUEST_CODE_DEFAULT;
                break;
        }

        intent.setType(fileTypeParam);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * Fragment onActivityResult(int requestCode, int resultCode, Intent data) 方面里面获取文件的文件路径
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param fragment
     * @return
     */
    public static String getFileResPath(int requestCode, int resultCode, Intent data, Fragment fragment){
        if(fragment == null){
            throw new NullPointerException("fragment not be null");
        }
        if(resultCode == Activity.RESULT_OK){//正常选择返回
            if(data != null && data.getData() != null){
                Uri uri = data.getData();
                String[] proj = {MediaStore.MediaColumns.DATA};
                Cursor cursor = fragment.getActivity().managedQuery(uri, proj, null, null, null);
                try{
                    if(cursor != null){
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        if ((cursor.getCount() > 0) && cursor.moveToFirst()) {
                            String path = cursor.getString(column_index);
                            return path;
                        }
                    }else{
                        //maybe user choose all file, if user choose all file type,  the path will be null, if the value is null, try get the Uri path
                        return data.getData().getPath();
                    }
                }catch (Exception e){
                    //cursor.getColumnIndexOrThrow have exception
                }finally {

                }
            }
        }
        return null;
    }
}
