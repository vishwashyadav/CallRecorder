package com.callrecorder

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import com.callrecorder.bean.UploadResponseBean
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel


class ImageUploadHandler private constructor() {


    companion object {
        //        private var sInstance: ImageUploadHandler? = null
        val provider: WebServiceProvider? = WebServiceProvider.retrofitForMedia.create(WebServiceProvider::class.java)


        fun uploadImageToServer(requestCode: Int, imagePath: String, imageName: String,destpath: String
                                , context: Context, listener: UploadListener) {
try {


    var isImageType: Boolean = false
    Logger.e("file path", imagePath)
    var new_file = File(imagePath)

    var destFile = copyFileOrDirectory(imagePath, destpath);

    if (destFile!!.exists()) {

        //Logger.e("file Size:", "" + (destFile.length() / 1024))

        val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), destFile)
        val nameReq = RequestBody.create(MediaType.parse("text/plain"), imageName)

        val body = MultipartBody.Part.createFormData("file","test",reqFile)

        var provider1: WebServiceProvider? = WebServiceProvider.retrofitForMedia.create(WebServiceProvider::class.java)

        provider1?.uploadImage(body, nameReq)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : SingleObserver<UploadResponseBean> {
                    override fun onSuccess(t: UploadResponseBean) {
////                            Utils.hideDialog()

                        if (t.status) {

                            listener.onUpload(t.fileUrl, requestCode);
                            if(!TextUtils.isEmpty(t.fileUrl))
                            {
                                //new_file.delete();
                                destFile.delete();
                            }
                        } else {
                            listener.onFailed("Something went wrong, please try again!")
                        }

                    }

                    override fun onSubscribe(d: Disposable) {
                        var s: String;

                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
////                            Utils.hideDialog()
                        listener.onFailed("Something went wrong, please try again!")
                    }

                })
    } else {

        Logger.e("File Does not exists", imagePath)
    }
}
catch (ex:java.lang.Exception)
{
    var s = ex.message;
}
        }

        fun copyFileOrDirectory(srcDir: String, dstDir: String):File? {
            var dst:File?=null
            try {
                val src = File(srcDir)
                dst = File(dstDir, src.name)

                if (src.isDirectory) {

                    val files = src.list()
                    val filesLength = files!!.size
                    for (i in 0 until filesLength) {
                        val src1 = File(src, files[i]).path
                        val dst1 = dst.path
                        copyFileOrDirectory(src1, dst1)

                    }
                } else {
                    copyFile(src, dst)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return dst
        }

        @Throws(IOException::class)
        fun copyFile(sourceFile: File, destFile: File) {
            if (!destFile.parentFile.exists())
                destFile.parentFile.mkdirs()

            if (!destFile.exists()) {
                destFile.createNewFile()
            }

            var source: FileChannel? = null
            var destination: FileChannel? = null

            try {
                source = FileInputStream(sourceFile).channel
                destination = FileOutputStream(destFile).channel
                destination!!.transferFrom(source, 0, source!!.size())
            } finally {
                source?.close()
                destination?.close()
            }
        }


    }


    interface UploadListener {
        fun onUpload(url: String?, requestCode: Int)

        fun onFailed(error: String?)
    }


}
